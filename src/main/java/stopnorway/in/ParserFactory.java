package stopnorway.in;

import stopnorway.data.Operator;
import stopnorway.database.Entity;
import stopnorway.geo.Points;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ParserFactory {

    private final File documents;

    private final Collection<? extends Enum<?>> operators;

    ParserFactory(Path documents, Enum<?>... operators) {
        this(documents, Arrays.asList(operators));
    }

    public ParserFactory(Path documents, Collection<? extends Enum<?>> operators) {
        this.documents = Objects.requireNonNull(documents, "documents").toFile();
        this.operators = operators == null || operators.isEmpty() ? Set.of(Operator.values()) : Set.copyOf(operators);
    }

    public Parser create(boolean quiet, boolean parallel) {
        return new Parser(
                quiet,
                parallel,
                operators,
                this::operatorSources,
                ParserFactory::allEntityParsers,
                ParserFactory::executorService);
    }

    private static List<EntityParser<? extends Entity>> allEntityParsers() {
        return EntityParsers.all(Points::point);
    }

    private static ExecutorService executorService(int queue) {
        AtomicInteger count = new AtomicInteger();
        int cpus = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cpus,
                cpus * 2,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queue),
                r -> new Thread(r, "xml#" + count.getAndIncrement()),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private Stream<OperatorSource> operatorSources(Enum<?> operator) {
        return files(operator).stream().map(file -> new OperatorSource(operator, file));
    }

    private Collection<File> files(Enum<?> operator) {
        return Stream.concat(
                shared(operator),
                lines(operator)
        ).collect(Collectors.toList());
    }

    private Stream<File> shared(Enum<?> operator) {
        return sharedData(operator)
                .map(name -> new File(documents, name))
                .filter(File::isFile);
    }

    private Stream<File> lines(Enum<?> operator) {
        String[] names = documents
                .list((dir, name) -> name.startsWith(operator.name() + "_"));
        return names == null
                ? Stream.empty()
                : Arrays.stream(names)
                        .map(name -> new File(documents, name));
    }

    private static Stream<String> sharedData(Enum<?> operator) {
        return Stream.of(
                String.format("_%s_shared_data.xml.gz", operator.name()),
                String.format("_%s_shared_data.xml", operator.name()));
    }
}
