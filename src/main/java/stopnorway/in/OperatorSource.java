package stopnorway.in;

import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public final class OperatorSource {

    static final int BUFF = 16 * 1024;
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory2.newFactory();
    private final Enum<?> operator;

    private final Collection<File> files;

    public OperatorSource(Enum<?> operator, Collection<File> files) {
        this.operator = operator;
        this.files = files;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + operator + "]";
    }

    public long getSize() {
        return files.stream().mapToLong(File::length).sum();
    }

    public Stream<XMLEventReader> eventReaders() {
        Stream<InputStream> files = streams();
        return readers(files);
    }

    static OperatorSource create(Enum<?> operator) {
        return new OperatorSource(operator, files(operator));
    }

    private Stream<XMLEventReader> readers(Stream<InputStream> files) {
        return files.map(file -> {
            try {
                return xmlInputFactory.createXMLEventReader(file, StandardCharsets.UTF_8.name());
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Failed to create factory", e);
            }
        });
    }

    private Stream<InputStream> streams() {
        return files.stream().map(file -> {
            try {
                return new GZIPInputStream(
                        new BufferedInputStream(
                                new FileInputStream(file), BUFF), BUFF);
            } catch (Exception e) {
                throw new IllegalArgumentException(file.getAbsolutePath(), e);
            }
        });
    }

    private static Collection<File> files(Enum<?> operator) {
        File documents = new File(
                new File(
                        new File(System.getProperty("user.home")),
                        "Documents"),
                "rb_norway-aggregated-netex");
        String[] fileNames = documents.list((dir, name) -> name.startsWith(operator.name() + "_"));
        Stream<File> additional = fileNames == null
                ? Stream.empty()
                : Arrays.stream(fileNames).map(fileName -> new File(documents, fileName));
        return Stream.concat(
                Stream.of(new File(documents,
                        sharedData(operator))),
                additional
        ).collect(Collectors.toList());
    }

    private static String sharedData(Enum<?> operator) {
        return String.format("_%s_shared_data.xml.gz", operator.name());
    }
}
