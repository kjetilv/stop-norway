package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class Importer {

    private static final Logger log = LoggerFactory.getLogger(Importer.class);

    public static Path targetPath(Path zipFile) {
        String fileName = zipFile.getFileName().toString();
        String base = fileName.substring(0, fileName.length() - SUFF.length());
        return zipFile.getParent().resolve(Path.of(base));
    }

    public static Path unzipped(Path zipFile, Enum<?>... sources) {
        return unzipped(zipFile, Arrays.asList(sources));
    }

    public static Path unzipped(Path zipFile, Collection<? extends Enum<?>> sources) {
        String filename = zipFile.getFileName().toString();
        if (!zipFile.toFile().isFile()) {
            throw new IllegalArgumentException("Bad zip file: " + zipFile);
        }
        if (!filename.endsWith(SUFF)) {
            throw new IllegalArgumentException("Not a zip file: " + zipFile);
        }
        Path targetPath = targetPath(zipFile);
        File targetDirectory = targetPath.toFile();
        if (targetDirectory.isFile()) {
            throw new IllegalArgumentException("Not a directory: " + targetPath);
        }
        if (!(targetDirectory.isDirectory() || targetDirectory.mkdirs())) {
            throw new IllegalArgumentException("Could not establish directory " + targetPath);
        }
        try (
            FileSystem fileSystem = FileSystems.newFileSystem(zipFile, ClassLoader.getSystemClassLoader())
        ) {
            log.info("{}: Copying to {} ...", zipFile, targetPath);
            fileSystem.getRootDirectories()
                .forEach(path -> {
                    log.info("Copying root {} to {} ...", path, targetPath);
                    copyAll(path, targetPath, sources);
                });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open " + zipFile, e);
        }
        return targetPath;
    }

    private Importer() {

    }

    static final String SUFF = ".zip";

    private static void copyAll(Path path, Path target, Collection<? extends Enum<?>> sources) {
        try {
            AtomicInteger counter = new AtomicInteger();
            AtomicInteger copied = new AtomicInteger();
            try (Stream<Path> walk = Files.walk(path)) {
                walk.filter(file -> file.getFileName() != null)
                    .filter(file -> sources.isEmpty() || sourceMatch(file, sources))
                    .filter(file -> file.getFileName().toString().endsWith(".xml"))
                    .forEach(file -> {
                        int count = counter.incrementAndGet();
                        if (copy(file, target, count)) {
                            copied.incrementAndGet();
                        }
                    });
            }
            log.info("Copied {}/{} files to {}", copied, counter, target);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to walk " + path, e);
        }
    }

    private static boolean sourceMatch(Path file, Collection<? extends Enum<?>> sources) {
        return sources.stream()
            .map(Enum::name).anyMatch(file.getFileName().toString()::contains);
    }

    private static boolean copy(Path source, Path target, int count) {
        try {
            Path targetFile = target.resolve(Path.of(source.getFileName().toString()));
            File file = targetFile.toFile();
            if (!file.exists()) {
                if (shouldLog(count)) {
                    log.info("Copying file #{}, {} => {}", count, file, target);
                }
                Files.copy(source, targetFile);
                return true;
            }
            if (file.length() <= 0) {
                if (shouldLog(count)) {
                    log.info("Copying over file #{}, {} => {}", count, file, target);
                }
                Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to copy " + source + " to " + target, e);
        }
    }

    private static boolean shouldLog(int count) {
        return count < 3 ||
               count < 100 && count % 10 == 0 ||
               count < 1_000 && count % 100 == 0 ||
               count < 10_000 && count % 500 == 0;
    }

}
