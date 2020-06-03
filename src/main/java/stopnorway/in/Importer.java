package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Importer {

    static final String SUFF = ".zip";

    private static final Logger log = LoggerFactory.getLogger(Importer.class);

    private Importer() {

    }

    public static Path unzipped(Path zipFile) {
        String filename = zipFile.getFileName().toString();
        if (!zipFile.toFile().isFile()) {
            throw new IllegalArgumentException("Bad zip file: " + zipFile);
        }
        if (!filename.endsWith(SUFF)) {
            throw new IllegalArgumentException("Not a zip file: " + zipFile);
        }
        String base = filename.substring(0, filename.length() - SUFF.length());
        Path targetPath = zipFile.getParent().resolve(Path.of(base));
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
                        copyAll(path, targetPath);
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open " + zipFile, e);
        }
        return targetPath;
    }

    private static void copyAll(Path path, Path target) {
        try {
            AtomicInteger counter = new AtomicInteger();
            AtomicInteger copied = new AtomicInteger();
            Files.walk(path)
                    .filter(file -> file.getFileName() != null)
                    .filter(file -> file.getFileName().toString().endsWith(".xml"))
                    .forEach(file -> {
                        int count = counter.incrementAndGet();
                        if (copy(file, target, count)) {
                            copied.incrementAndGet();
                        }
                    });
            log.info("Copied {}/{} files to {}", copied, counter, target);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to walk " + path, e);
        }
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
