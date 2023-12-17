package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    ScheduledExecutorService backgroundLogging;

    private final boolean noisy;

    private final boolean parallel;

    private final Collection<? extends Enum<?>> operators;

    private final OperatorSources operatorSources;

    private final Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier;

    private final Function<Integer, ExecutorService> executorServiceProvider;

    Parser(
        boolean quiet,
        boolean parallel,
        Collection<? extends Enum<?>> operators,
        OperatorSources operatorSources,
        Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier,
        Function<Integer, ExecutorService> executorServiceProvider
    ) {
        this.noisy = !quiet;
        this.parallel = parallel;
        this.operators = operators;
        this.operatorSources = operatorSources;
        this.parsersSupplier = parsersSupplier;
        this.executorServiceProvider = executorServiceProvider;
        this.backgroundLogging =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "parse"));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[parallel:" + parallel + "]";
    }

    public Stream<Entity> entities() {
        if (noisy) {
            log.info(
                "Processing {} operators in {}: {}",
                operators.isEmpty()
                    ? "all"
                    : operators.size(),
                parallel
                    ? "parallel"
                    : "sequence",
                operators.isEmpty()
                    ? "<ALL>"
                    : operators.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "))
            );
        }
        Collection<OperatorSource> sources = operators.stream()
            .flatMap(operatorSources::get)
            .collect(Collectors.toList());
        ParseProgress progress = new ParseProgress(sources, operators, Instant.now());
        try {
            if (parallel) {
                return submittedFutures(sources, progress).stream()
                    .map(Parser::awaitFuture)
                    .flatMap(Collection::stream);
            }
            return sources.stream()
                .map(source -> process(source, progress))
                .flatMap(Collection::stream);
        } finally {
            logInBackground(progress);
        }
    }

    @Override
    public void close() {
        backgroundLogging.shutdown();
    }

    private void logInBackground(ParseProgress progress) {
        backgroundLogging.scheduleAtFixedRate(
            () -> {
                if (progress.live()) {
                    progress.summary(Instant.now());
                }
            },
            2, 8, TimeUnit.SECONDS
        );
    }

    private List<Future<Collection<Entity>>> submittedFutures(
        Collection<OperatorSource> sources,
        ParseProgress progress
    ) {
        ExecutorService executorService = executorServiceProvider.apply(sources.size());
        try {
            return sources.stream()
                .map(source ->
                    executorService.submit(() ->
                        process(source, progress)))
                .collect(Collectors.toList());
        } finally {
            executorService.shutdown();
        }
    }

    private Collection<Entity> process(OperatorSource operatorSource, ParseProgress progress) {
        Collection<EntityParser<? extends Entity>> parsers = parsersSupplier.get();
        try {
            XMLEventReader eventReader = operatorSource.eventReader();
            while (eventReader.hasNext()) {
                XMLEvent event = event(eventReader);
                process(parsers, operatorSource, event);
            }
            Collection<Entity> entities = parsers.stream()
                .map(EntityParser::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            return progress.recorded(operatorSource, entities);
        } catch (Exception e) {
            throw new IllegalStateException(
                this + " failed to update " + parsers.size() + " parsers for " + operatorSource, e);
        }
    }

    private void process(
        Collection<EntityParser<? extends Entity>> parsers,
        OperatorSource operatorSource,
        XMLEvent event
    ) {
        for (EntityParser<? extends Entity> parser : parsers) {
            try {
                parser.digest(event);
            } catch (Exception e) {
                throw new IllegalStateException(
                    this + " failed to feed " + event + " for " + operatorSource + " to " + parser, e);
            }
        }
    }

    private static <T> T awaitFuture(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted: " + future, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed: " + future, e);
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
