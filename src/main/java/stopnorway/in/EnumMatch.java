package stopnorway.in;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public interface EnumMatch {

    String name();

    default boolean startMatch(StartElement startElement) {
        return match(startElement.getName());
    }

    default boolean attributeMatch(Attribute attribute) {
        return match(attribute.getName());
    }

    default boolean endMatch(EndElement endElement) {
        return match(endElement.getName());
    }

    private boolean match(QName name) {
        return name.getLocalPart().equalsIgnoreCase(name());
    }
}
