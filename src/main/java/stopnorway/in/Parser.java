package stopnorway.in;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
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

    public Map<Id, Entity> entities(Operator... operators) {
        return entities(Arrays.asList(operators));
    }

    public Map<Id, Entity> entities(Collection<Operator> operators) {
        if (noisy) {
            log.info("Processing {} operators in {}: {}",
                    operators.size(),
                    parallel ? "parallel" : "sequence",
                    operators.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        return parallel ? processParallel(operators) : processSequential(operators);
    }

    private Map<Id, Entity> processSequential(Collection<Operator> operators) {
        Stream<OperatorSource> operatorSourceStream = operators.stream().map(OperatorSource::create);
        return collect(operators, operatorSourceStream.flatMap(this::process));
    }

    private Map<Id, Entity> processParallel(Collection<Operator> operators) {
        Stream<OperatorSource> operatorSourceStream = operators.stream().map(OperatorSource::create);
        ForkJoinPool fjp = new ForkJoinPool(
                16,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> log.error("Failed: {}", t, e),
                false);
        try {
            return fjp.submit(() ->
                    collect(operators,
                            operatorSourceStream.parallel().flatMap(this::process))).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed", e);
        }
    }

    private Map<Id, Entity> collect(Collection<Operator> operators, Stream<? extends Map.Entry<Id, ? extends Entity>> entries) {
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
            return stream(parsers)
                    .flatMap(Collection::stream);
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

        log.info("Processed {} in {}: {} entities from {} bytes, {} bytes/s, {} entities/s",
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
                for (EntityParser<? extends Entity> parser : parsers) {
                    if (parser.test(event)) {
                        feed(parser, event, operatorSource);
                    }
                }
            }
        });
    }

    private void feed(EntityParser<? extends Entity> parser, XMLEvent event, OperatorSource operatorSource) {
        try {
            parser.accept(event);
        } catch (Exception e) {
            throw new IllegalStateException(
                    this + " failed to parse for " + operatorSource + " with " + parser, e);
        }
    }

    @NotNull
    private Predicate<EntityParser<? extends Entity>> showsInterest(XMLEvent event) {
        return parser -> parser.test(event);
    }

    private static Entity idCheck(Entity e1, Entity e2) {
        if (e1.equals(e2)) {
            return e1;
        }
        throw new IllegalStateException(e1 + " != " + e2);
    }

    @NotNull
    private static Stream<Set<? extends Map.Entry<Id, ? extends Entity>>> stream(Collection<EntityParser<? extends Entity>> parsers) {
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
