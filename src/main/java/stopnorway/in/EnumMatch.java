package stopnorway.in;

import javax.xml.namespace.QName;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Map;
import java.util.Optional;

public interface EnumMatch<E extends EnumMatch<E>> {

    String name();

    default <T> T get(Map<? extends EnumMatch<E>, ?> objectMap) {
        return this.<T>getOpt(objectMap)
                .orElseThrow(() ->
                        new IllegalStateException(this + " not found in " + objectMap));
    }

    default <T> T get(Map<? extends EnumMatch<E>, ?> objectMap, T defaultValue) {
        Optional<T> opt = getOpt(objectMap);
        return opt.orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<T> getOpt(Map<? extends EnumMatch<E>, ?> objectMap) {
        return Optional.ofNullable(objectMap).map(m -> (T) m.get(this));
    }

    default boolean startMatch(StartElement startElement) {
        return match(startElement.getName());
    }

    default boolean endMatch(EndElement endElement) {
        return match(endElement.getName());
    }

    private boolean match(QName name) {
        return name.getLocalPart().equalsIgnoreCase(name());
    }

}
