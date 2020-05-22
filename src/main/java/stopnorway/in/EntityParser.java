package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class EntityParser<E extends Entity> implements Consumer<XMLEvent>, Supplier<Map<Id, E>> {

    private final Class<E> type;

    private final Collection<Field> fields;

    private final ParseState<E> state;

    private final LinkedList<Stacked> stack = new LinkedList<>();

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Field... fields) {
        this(type, entityMaker, Arrays.asList(fields));
    }

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Collection<Field> fields) {
        this.type = type;
        this.fields = fields;
        this.state = new ParseState<>(entityMaker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type.getSimpleName() + "/" + fields +
                " stack: " + stack.stream().map(Object::toString).collect(Collectors.joining(" => ")) +
                "]";
    }

    @Override
    public void accept(XMLEvent event) {
        stack(event);
        try {
            int eventType = event.getEventType();
            if (eventType == START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                if (isEntity(startElement)) {
                    state.startBuildingEntity(entityId(startElement));
                    return;
                }
                if (state.isBuildingEntity()) {
                    field(startElement).ifPresent(field -> {
                        state.startLookingForField(field);
                        if (state.isLookingForField(FieldType.Ref)) {
                            state.setFieldValue(ref(event));
                        }
                    });
                    return;
                }
            }
            if (eventType == XMLStreamConstants.CHARACTERS) {
                String contents = contents(event);
                if (contents.isBlank()) {
                    return;
                }
                if (state.isLookingForField(FieldType.Content)) {
                    state.setFieldValue(contents);
                }
                return;
            }
            if (eventType == END_ELEMENT) {
                EndElement endElement = event.asEndElement();
                if (isEntity(endElement)) {
                    state.buildEntity();
                    return;
                }
                field(endElement).ifPresent(state::stopLookingForField);
            }
        } catch (Exception e) {
            Location loc = event.getLocation();
            String locString = loc.toString().replaceAll("\n", ", ");
            throw new IllegalStateException(this + ": Failed @ line " + loc.getLineNumber() + " : " + locString, e);
        }
    }

    @Override
    public Map<Id, E> get() {
        return state.get();
    }

    private void stack(XMLEvent event) {
        if (event.getEventType() == START_ELEMENT) {
            stack.addLast(new Stacked(
                    event.asStartElement().getName().getLocalPart(),
                    event.getLocation().getLineNumber()));
        } else if (event.getEventType() == END_ELEMENT) {
            Stacked unstacked = stack.getLast();
            if (unstacked.getName().equals(event.asEndElement().getName().getLocalPart())) {
                stack.removeLast();
            } else {
                throw new IllegalStateException(this + " got unexpected element: " + event);
            }
        }
    }

    private String contents(XMLEvent event) {
        return event.asCharacters().getData();
    }

    private Id ref(XMLEvent event) {
        return ref(event.asStartElement());
    }

    private boolean isEntity(StartElement startElement) {
        return element(startElement).equalsIgnoreCase(type.getSimpleName());
    }

    private Id entityId(StartElement startElement) {
        return id(startElement, "id");
    }

    private boolean isEntity(EndElement endElement) {
        return element(endElement).equalsIgnoreCase(type.getSimpleName());
    }

    private Optional<Field> field(EndElement startElement) {
        return fields.stream()
                .filter(field ->
                        field.endMatch(startElement))
                .findFirst();
    }

    private Id id(StartElement startElement, String attr) {
        String[] idParts = get(startElement, attr).split(":");
        return Id.intern(new Id(
                Operator.valueOf(idParts[0]),
                idParts[1],
                idParts[2],
                get(Integer::parseInt, startElement, "version")));
    }

    private Id ref(StartElement startElement) {
        return id(startElement, "ref");
    }

    private static Optional<Field> field(StartElement startElement) {
        return Arrays.stream(Field.values())
                .filter(field ->
                        field.startMatch(startElement))
                .findFirst();
    }

    private static String element(StartElement startElement) {
        return startElement.getName().getLocalPart();
    }

    private static String element(EndElement endElement) {
        return endElement.getName().getLocalPart();
    }

    private static String get(StartElement startElement, String attr) {
        Attribute attributeByName = startElement.getAttributeByName(QName.valueOf(attr));
        if (attributeByName == null) {
            throw new IllegalArgumentException("No attribute '" + attr + "' in " + startElement);
        }
        return attributeByName.getValue();
    }

    private static <T> T get(Function<String, T> function, StartElement startElement, String attr) {
        return function.apply(startElement.getAttributeByName(QName.valueOf(attr)).getValue());
    }

    public interface EntityMaker<E extends Entity> {

        E entity(Id id, Map<Field, Object> values);
    }

    public final static class Stacked {

        private final String name;

        private final int line;

        Stacked(String name, int line) {

            this.name = name;
            this.line = line;
        }

        @Override
        public String toString() {
            return name + "@" + line;
        }

        public String getName() {
            return name;
        }

        public int getLine() {
            return line;
        }
    }

}
