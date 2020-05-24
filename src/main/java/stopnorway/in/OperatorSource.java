package stopnorway.in;

import org.codehaus.stax2.XMLInputFactory2;
import org.jetbrains.annotations.NotNull;
import stopnorway.database.Operator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class OperatorSource {

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory2.newFactory();

    private final Operator operator;

    private final File file;

    public OperatorSource(Operator operator, File file) {
        this.operator = operator;
        this.file = file;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + operator + " @ " + file.getName() + "]";
    }

    public long getSize() {
        return file.length();
    }

    public XMLEventReader eventReader() {
        InputStream file = file();
        return reader(file);
    }

    @NotNull
    static OperatorSource create(Operator operator) {
        return new OperatorSource(operator, file(operator));
    }

    @NotNull
    private static File file(Operator operator) {
        return new File(new File(
                new File(
                        new File(System.getProperty("user.home")),
                        "Documents"),
                "rb_norway-aggregated-netex"),
                sharedData(operator));
    }

    private XMLEventReader reader(InputStream file) {
        try {
            return xmlInputFactory.createXMLEventReader(file, StandardCharsets.UTF_8.name());
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to create factory", e);
        }
    }

    @NotNull
    private InputStream file() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(file.getAbsolutePath(), e);
        }
    }

    private static String sharedData(Operator operator) {
        return String.format("_%s_shared_data.xml", operator.name());
    }
}
