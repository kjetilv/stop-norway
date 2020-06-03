package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser {

    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    private final boolean noisy;

    private final boolean parallel;

    private final Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier;

    public Parser(
            boolean quiet,
            boolean parallel,
            Supplier<Collection<EntityParser<? extends Entity>>> supplier
    ) {
        this.noisy = !quiet;
        this.parallel = parallel;
        this.parsersSupplier = supplier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[parallel:" + parallel + "]";
    }

    public Collection<Entity> entities(Enum<?>... operators) {
        return entities(Arrays.asList(operators));
    }

    public Collection<Entity> entities(Collection<Enum<?>> operators) {
        if (noisy) {
            log.info(
                    "Processing {} operators in {}: {}",
                    operators.size(),
                    parallel ? "parallel" : "sequence",
                    operators.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        Instant start = Instant.now();
        try {
            Stream<OperatorSource> sources = operators.stream().flatMap(OperatorSource::create);
            if (parallel) {
                ExecutorService executorService = executorService();
                try {
                    return sources.map(
                            source -> executorService.submit(
                                    () -> process(source)))
                            .map(this::get)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                } finally {
                    List<Runnable> runnables = executorService.shutdownNow();
                    if (!runnables.isEmpty()) {
                        log.error("{} runnables still active: {}", runnables.size(), runnables);
                    }
                }
            }
            return sources.map(this::process).flatMap(Collection::stream).collect(Collectors.toList());
        } finally {
            if (noisy) {
                Duration time = Duration.between(start, Instant.now());
                log.info("Processed in {}", time);
            }
        }
    }

    private ExecutorService executorService() {
        AtomicInteger count = new AtomicInteger();
        int cpus = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cpus,
                cpus * 2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256),
                r -> new Thread(r, "exec#" + count.getAndIncrement()),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private <T> T get(Future<T> mapFuture) {
        try {
            return mapFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted: " + mapFuture, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed: " + mapFuture, e);
        }
    }

    private <K, V> Map<K, V> reduceMaps(Stream<Map<K, V>> maps) {
        return maps.reduce(reduceMap()).orElseGet(Collections::emptyMap);
    }

    private <K, V> BinaryOperator<Map<K, V>> reduceMap() {
        return (m1, m2) -> {
            m1.putAll(m2);
            return m1;
        };
    }

    private Collection<Entity> process(OperatorSource operatorSource) {
        if (noisy) {
            log.info("Processing {}...", operatorSource);
        }
        Instant startTime = noisy ? Instant.now() : null;
        Collection<EntityParser<? extends Entity>> parsers = parsersSupplier.get();
        try {
            XMLEventReader eventReader = operatorSource.eventReader();
            while (eventReader.hasNext()) {
                XMLEvent event = event(eventReader);
                process(parsers, operatorSource, event);
            }
            return parsers.stream()
                    .map(EntityParser::get)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalStateException(
                    this + " failed to update " + parsers.size() + " parsers for " + operatorSource, e);
        } finally {
            if (noisy) {
                logResults(operatorSource, parsers, Duration.between(startTime, Instant.now()));
            }
        }
    }

    private void logResults(
            OperatorSource operatorSource, Collection<EntityParser<? extends Entity>> parsers, Duration duration
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

    private static XMLEvent event(XMLEventReader reader) {
        try {
            return Objects.requireNonNull(reader.nextEvent(), "reader.nextEvent()");
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to read", e);
        }
    }
}
