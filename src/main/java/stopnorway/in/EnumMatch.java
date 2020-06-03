package stopnorway.in;

import javax.xml.namespace.QName;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public interface EnumMatch {

    String name();

    default boolean startMatch(StartElement startElement) {
        return matches(startElement.getName());
    }

    default boolean endMatch(EndElement endElement) {
        return matches(endElement.getName());
    }

    default boolean matches(QName name) {
        return name.getLocalPart().equalsIgnoreCase(name());
    }
}
