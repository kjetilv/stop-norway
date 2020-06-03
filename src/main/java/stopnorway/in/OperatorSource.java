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

    private static final int BUFF = 16 * 1024;

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory2.newFactory();

    private final Enum<?> operator;

    private final File file;

    public OperatorSource(Enum<?> operator, File file) {
        this.operator = operator;
        this.file = file;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + operator + ": " + file.getName() + "]";
    }

    public long getSize() {
        return file.length();
    }

    public XMLEventReader eventReader() {
        InputStream files = streams();
        return readers(files);
    }

    static Stream<OperatorSource> create(Enum<?> operator) {
        return files(operator).stream()
                .map(file -> new OperatorSource(operator, file));
    }

    private XMLEventReader readers(InputStream file) {
        try {
            return xmlInputFactory.createXMLEventReader(file, StandardCharsets.UTF_8.name());
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to create factory", e);
        }
    }

    private InputStream streams() {
        try {
            return new GZIPInputStream(
                    new BufferedInputStream(
                            new FileInputStream(file), BUFF), BUFF);
        } catch (Exception e) {
            throw new IllegalArgumentException(file.getAbsolutePath(), e);
        }
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
                Stream.of(new File(
                        documents,
                        sharedData(operator))),
                additional
        ).collect(Collectors.toList());
    }

    private static String sharedData(Enum<?> operator) {
        return String.format("_%s_shared_data.xml.gz", operator.name());
    }
}
