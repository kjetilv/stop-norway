package stopnorway.in;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser {

    private static final Logger log = LoggerFactory.getLogger(Parser.class);
    private final boolean logging;
    private final boolean parallel;
    private final Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier;

    public Parser(boolean parallel, Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier) {
        this(false, parallel, parsersSupplier);
    }

    public Parser(
            boolean quiet,
            boolean parallel,
            Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier) {
        this.logging = !quiet;
        this.parallel = parallel;
        this.parsersSupplier = parsersSupplier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[parallel:" + parallel + "]";
    }

    public static HashMap<Id, Entity> collect(Stream<? extends Map.Entry<Id, ? extends Entity>> entryStream) {
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
        if (logging) {
            log.info("Processing {} operators in {}: {}",
                    operators.size(),
                    parallel ? "parallel" : "sequence",
                    operators.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        Stream<OperatorSource> operatorSourceStream = operators.stream().map(OperatorSource::create);
        Stream<Map.Entry<Id, ? extends Entity>> entries =
                (parallel ? operatorSourceStream.parallel() : operatorSourceStream).flatMap(this::process);
        Instant totalStartTime = Instant.now();
        try {
            return collect(entries);
        } finally {
            if (logging) {
                Duration time = Duration.between(totalStartTime, Instant.now());
                long totalSize =
                        operators.stream().map(OperatorSource::create).mapToLong(OperatorSource::getSize).sum();
                log.info("Processed {} operators in {}, total {} bytes", operators.size(), time, totalSize);
            }
        }
    }

    private Stream<? extends Map.Entry<Id, ? extends Entity>> process(OperatorSource operatorSource) {
        if (logging) {
            log.info("Processing {}...", operatorSource);
        }
        Instant startTime = Instant.now();
        Collection<EntityParser<? extends Entity>> parsers = this.parsersSupplier.get();
        try {
            update(parsers, operatorSource);
            return collected(parsers);
        } catch (Exception e) {
            throw new IllegalStateException
                    (this + " failed to update " + parsers.size() + " parsers for " + operatorSource, e);
        } finally {
            if (logging) {
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

    private void update(Collection<EntityParser<? extends Entity>> parsers, OperatorSource operatorSource) {
        XMLEventReader eventReader = operatorSource.eventReader();
        while (eventReader.hasNext()) {
            XMLEvent event = event(eventReader);
            parsers.forEach(parser -> {
                try {
                    parser.accept(event);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            this + " failed to parse for " + operatorSource + " with " + parser, e);
                }
            });
        }
    }

    private static Entity idCheck(Entity e1, Entity e2) {
        if (e1.equals(e2)) {
            return e1;
        }
        throw new IllegalStateException(e1 + " != " + e2);
    }

    private static Stream<? extends Map.Entry<Id, ? extends Entity>> collected(
            Collection<EntityParser<? extends Entity>> parsers
    ) {
        return parsers.stream()
                .map(EntityParser::get)
                .map(Map::entrySet)
                .flatMap(Collection::stream);
    }

    private static XMLEvent event(XMLEventReader reader) {
        try {
            return Objects.requireNonNull(reader.nextEvent(), "reader.nextEvent()");
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to read", e);
        }
    }
}
