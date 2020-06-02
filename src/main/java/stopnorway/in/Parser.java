package stopnorway.in;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;
import stopnorway.database.Id;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser {

    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    private final boolean noisy;

    private final boolean parallel;

    private final Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier;

    public Parser(boolean parallel, Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier) {
        this(false, parallel, parsersSupplier);
    }

    public Parser(boolean quiet, boolean parallel, Supplier<Collection<EntityParser<? extends Entity>>> supplier) {
        this.noisy = !quiet;
        this.parallel = parallel;
        this.parsersSupplier = supplier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[parallel:" + parallel + "]";
    }

    public static Map<Id, Entity> collect(Stream<? extends Map.Entry<Id, ? extends Entity>> entryStream) {
        return entryStream.collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Parser::idCheck,
                HashMap::new));
    }

    public <E extends Enum<E>> Map<Id, Entity> entities(Enum<?>... operators) {
        return entities(Arrays.asList(operators));
    }

    public Map<Id, Entity> entities(Collection<Enum<?>> operators) {
        if (noisy) {
            log.info(
                    "Processing {} operators in {}: {}",
                    operators.size(),
                    parallel ? "parallel" : "sequence",
                    operators.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        Stream<OperatorSource> sources = operators.stream().map(OperatorSource::create);
        if (parallel) {
            return processParallel(operators, sources);
        }
        return processSequential(operators, sources);
    }

    private Map<Id, Entity> processSequential(Collection<Enum<?>> operators, Stream<OperatorSource> sources) {
        return collect(operators, sources.flatMap(this::process));
    }

    private Map<Id, Entity> processParallel(
            Collection<Enum<?>> operators,
            Stream<OperatorSource> operatorSourceStream
    ) {
        ForkJoinPool fjp = new ForkJoinPool(
                16,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> log.error("Failed: {}", t, e),
                false);
        ForkJoinTask<Map<Id, Entity>> task;
        try {
            task = fjp.submit(
                    () -> processSequential(operators, operatorSourceStream.parallel()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not submit task", e);
        }
        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Parse failed", e);
        }
    }

    private Map<Id, Entity> collect(
            Collection<Enum<?>> operators,
            Stream<? extends Map.Entry<Id, ? extends Entity>> entries
    ) {
        Instant totalStartTime = Instant.now();
        try {
            return collect(entries);
        } finally {
            if (noisy) {
                Duration time = Duration.between(totalStartTime, Instant.now());
                long totalSize =
                        operators.stream().map(OperatorSource::create).mapToLong(OperatorSource::getSize).sum();
                log.info("Processed {} operators in {}, total {} bytes", operators.size(), time, totalSize);
            }
        }
    }

    private Stream<? extends Map.Entry<Id, ? extends Entity>> process(OperatorSource operatorSource) {
        if (noisy) {
            log.info("Processing {}...", operatorSource);
        }
        return parse(operatorSource, this.parsersSupplier.get());
    }

    private Stream<? extends Map.Entry<Id, ? extends Entity>> parse(
            OperatorSource operatorSource,
            Collection<EntityParser<? extends Entity>> parsers
    ) {
        Instant startTime = noisy ? Instant.now() : null;
        try {
            update(operatorSource, parsers);
            return stream(parsers).flatMap(Collection::stream);
        } catch (Exception e) {
            throw new IllegalStateException
                    (this + " failed to update " + parsers.size() + " parsers for " + operatorSource, e);
        } finally {
            if (noisy) {
                logResults(
                        operatorSource,
                        parsers,
                        Duration.between(startTime, Instant.now()));
            }
        }
    }

    private void logResults(
            OperatorSource operatorSource,
            Collection<EntityParser<? extends Entity>> parsers,
            Duration duration
    ) {
        long entities = parsers.stream().mapToLong(parser -> parser.get().size()).sum();
        long nanos = duration.toNanos();
        int byteHz = nanos > 0 ? (int) (1_000_000_000L * operatorSource.getSize() / nanos) : -1;
        int entityHz = nanos > 0 ? (int) (1_000_000_000L * entities / nanos) : -1;

        log.info(
                "Processed {} in {}: {} entities from {} bytes, {} bytes/s, {} entities/s",
                operatorSource,
                duration,
                entities,
                operatorSource.getSize(),
                byteHz,
                entityHz);
    }

    private void update(OperatorSource operatorSource, Collection<EntityParser<? extends Entity>> parsers) {
        operatorSource.eventReaders().forEach(eventReader -> {
            while (eventReader.hasNext()) {
                XMLEvent event = event(eventReader);
                process(parsers, operatorSource, event);
            }
        });
    }

    private void process(
            Collection<EntityParser<? extends Entity>> parsers,
            OperatorSource operatorSource,
            XMLEvent event
    ) {
        for (EntityParser<? extends Entity> parser: parsers) {
            if (parser.canDigest(event)) {
                try {
                    parser.digest(event);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            this + " failed to feed " + event + " for " + operatorSource + " to " + parser, e);
                }
            }
        }
    }

    private static Entity idCheck(Entity e1, Entity e2) {
        if (e1.equals(e2)) {
            return e1;
        }
        throw new IllegalStateException(e1 + " != " + e2);
    }

    @NotNull
    private static Stream<Set<? extends Map.Entry<Id, ? extends Entity>>> stream(
            Collection<EntityParser<?
                    extends Entity>> parsers
    ) {
        return parsers.stream()
                .map(EntityParser::get)
                .map(Map::entrySet);
    }

    private static XMLEvent event(XMLEventReader reader) {
        try {
            return Objects.requireNonNull(reader.nextEvent(), "reader.nextEvent()");
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to read", e);
        }
    }
}
