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
import java.util.Objects;
import java.util.zip.GZIPInputStream;

public final class OperatorSource {

    private static final int BUFF = 16 * 1024;

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory2.newFactory();

    private final Enum<?> source;

    private final File file;

    private final boolean zipped;

    private final long length;

    public OperatorSource(Enum<?> source, File file) {
        this.source = source;
        this.file = Objects.requireNonNull(file, "file");
        this.length = file.length();
        this.zipped = file.getName().endsWith(".gz");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + file + "]";
    }

    public XMLEventReader eventReader() {
        InputStream files = inputStream();
        return readers(files);
    }

    public Enum<?> getEnum() {
        return source;
    }

    long getLength() {
        return length;
    }

    private XMLEventReader readers(InputStream file) {
        try {
            return xmlInputFactory.createXMLEventReader(file, StandardCharsets.UTF_8.name());
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to create factory", e);
        }
    }

    private InputStream inputStream() {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file), 1024 * 1024);
            return zipped
                    ? new GZIPInputStream(in, BUFF)
                    : in;
        } catch (Exception e) {
            throw new IllegalArgumentException(file.getAbsolutePath(), e);
        }
    }
}
