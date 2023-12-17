package stopnorway.main;

import com.github.kjetilv.flopp.kernel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FlatMain {

    private static final Logger log = LoggerFactory.getLogger(FlatMain.class);

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args[0]);
        Path dir = unzipped(path);

        try (Stream<Path> files = Files.list(dir)) {
            List<List<Map<String, String>>> data =
                files.filter(file ->
                        file.getFileName().toString().endsWith(".txt"))
                    .map(file -> {
                        if (sizeOf(file) > 1_000_000_000) {
                            log.info("Skipping {}", file);
                            return Collections.<Map<String, String>>emptyList();
                        } else {
                            log.info("Reading {}", file);
                            List<Map<String, String>> entries = Collections.synchronizedList(new ArrayList<>());
                            String[] headers = headers(file);
                            try (
                                PartitionedPath partitioned = PartitionedPaths.create(
                                    file,
                                    Shape.decor(1, 0),
                                    Partitioning.create(Runtime.getRuntime().availableProcessors(), 8 * 8192),
                                    new ForkJoinPool(32)
                                )
                            ) {
                                partitioned.forEachPartition((partition,  lineStream) ->
                                    lineStream.forEach(line ->
                                        entries.add(getEntry(line, headers))));
                            }
                            return entries;
                        }
                    })
                    .toList();
            System.out.println(data.size());
        }
    }

    public static Path unzipped(Path zipFile) {
        if (!Files.isRegularFile(zipFile)) {
            throw new IllegalArgumentException("Bad zip file: " + zipFile);
        }
        log.info("Unzipping {}", zipFile);
        Path targetPath = targetPath(zipFile);
        if (Files.isRegularFile(targetPath)) {
            throw new IllegalArgumentException("Not a directory: " + targetPath);
        }
        log.info("Unzipping to {}", targetPath);
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not establish directory " + targetPath, e);
        }
        try (
            FileSystem zip = FileSystems.newFileSystem(zipFile, ClassLoader.getSystemClassLoader())
        ) {
            log.info("{}: Copying to {} ...", zipFile, targetPath);
            zip.getRootDirectories()
                .forEach(rootDirectory -> {
                    try (Stream<Path> paths = pathsIn(rootDirectory)) {
                        paths.forEach(path -> {
                            log.info("  {} ...", path);
                            Path copy = targetPath.resolve(path.getFileName().toString());
                            if (sizeOf(path) != sizeOf(copy)) {
                                copy(path, copy);
                            }
                        });
                    }
                });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open " + zipFile, e);
        }
        return targetPath;
    }

    public static Path targetPath(Path zipFile) {
        String fileName = zipFile.getFileName().toString();
        String base = fileName.substring(0, fileName.length() - ".zip".length());
        return zipFile.getParent().resolve(Path.of(base));
    }

    private FlatMain() {
    }

    private static Map<String, String> getEntry(NpLine line, String[] headers) {
        Map<String, String> entry = new HashMap<>();
        String[] split = line.line().split(",");
        IntStream.range(0, headers.length)
            .forEach(i ->
                entry.put(headers[i], split[i]));
        entry.put("lineNo", String.valueOf(line.lineNo()));
        return entry;
    }

    private static String[] headers(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.findFirst()
                .map(header ->
                    Arrays.stream(header.split(","))
                        .map(String::trim)
                        .filter(name -> !name.isBlank())
                        .toArray(String[]::new))
                .orElseThrow(() ->
                    new IllegalStateException("No header"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse headers: " + path, e);
        }
    }

    private static void copy(Path path, Path copy) {
        try {
            Files.copy(path, copy, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to copy " + path, e);
        }
    }

    private static long sizeOf(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to size " + path, e);
        }
    }

    private static Stream<Path> pathsIn(Path rootDirectory) {
        try {
            return Files.list(rootDirectory);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list " + rootDirectory, e);
        }
    }
}
