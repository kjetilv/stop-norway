package stopnorway.in;

import javax.xml.namespace.QName;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Map;
import java.util.Optional;

public enum Field {

    Name,

    FromPointRef,

    ToPointRef,

    Distance;

    private final FieldType fieldType;

    Field() {
        this(null);
    }

    Field(FieldType fieldType) {
        this.fieldType = fieldType != null ? fieldType
                : name().endsWith("Ref") ? FieldType.Ref
                : FieldType.Content;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public <T> T get(Map<Field, ?> objectMap) {
        return this.<T>getOpt(objectMap)
                .orElseThrow(() ->
                        new IllegalStateException(this + " not found in " + objectMap));
    }

    public <T> T get(Map<Field, ?> objectMap, T defaultValue) {
        Optional<T> opt = getOpt(objectMap);
        return opt.orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOpt(Map<Field, ?> objectMap) {
        return Optional.ofNullable(objectMap).map(m -> (T) m.get(this));
    }

    public boolean startMatch(StartElement startElement) {
        return match(startElement.getName());
    }

    public boolean endMatch(EndElement endElement) {
        return match(endElement.getName());
    }

    private boolean match(QName name) {
        return name.getLocalPart().equalsIgnoreCase(name());
    }
}
