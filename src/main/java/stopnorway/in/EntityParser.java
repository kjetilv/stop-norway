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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class EntityParser<E extends Entity> implements Consumer<XMLEvent>, Supplier<Map<Id, E>> {

    private final Class<E> type;

    private final Field[] fields;

    private final Sublist[] sublists;

    private final Map<Sublist, EntityParser<?>> subParsers;

    private final ParseState<E> state;

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Field... fields) {
        this(type, entityMaker, Arrays.asList(fields));
    }

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Collection<Field> fields) {
        this(type, new ParseState<>(entityMaker), fields, null);
    }

    private EntityParser(
            Class<E> type,
            ParseState<E> state,
            Collection<Field> fields,
            Map<Sublist, EntityParser<?>> subParsers
    ) {
        this.type = type;
        this.fields = fields == null || fields.isEmpty()
                ? null
                : fields.toArray(new Field[0]);
        this.sublists = subParsers == null || subParsers.isEmpty()
                ? null
                : subParsers.keySet().toArray(Sublist[]::new);
        this.subParsers = subParsers == null || subParsers.isEmpty()
                ? Collections.emptyMap()
                : Map.copyOf(subParsers);
        this.state = state;
    }

    @Override
    public void accept(XMLEvent event) {
        try {
            int eventType = event.getEventType();
            if (state.isBuildingList()) {
                EntityParser<?> entityParser = subParsers.get(state.getActiveSublist());
                if (entityParser != null) {
                    entityParser.accept(event);
                }
            }
            if (eventType == END_ELEMENT) {
                EndElement endElement = event.asEndElement();
                if (isEntity(endElement)) {
                    state.completeEntityBuild();
                    return;
                }
                Field matchingField = endMatch(this.fields, endElement);
                if (matchingField != null) {
                    state.stopLookingForField(matchingField);
                }
                Sublist matchingSublist = endMatch(this.sublists, endElement);
                if (matchingSublist != null && matchingSublist == state.getActiveSublist()) {
                    EntityParser<?> listParser = subParsers.get(matchingSublist);
                    Map<Id, ?> idMap = listParser.get();
                    state.completeList(matchingSublist, idMap.values());
                }
                return;
            }
            if (eventType == START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                if (isEntity(startElement)) {
                    state.startBuildingEntity(entityId(startElement));
                    return;
                }
                if (state.isBuildingEntity()) {
                    Field matchingField = startMatch(this.fields, startElement);
                    if (matchingField != null) {
                        state.startLookingForField(matchingField);
                        if (state.isLookingForField(FieldType.Ref)) {
                            state.setFieldId(ref(event));
                        }
                    }
                    Sublist matchingSublist = startMatch(sublists, startElement);
                    if (matchingSublist != null) {
                        state.startBuildingList(matchingSublist);
                    }
                }
                return;
            }
            if (eventType == XMLStreamConstants.CHARACTERS) {
                String contents = contents(event);
                if (state.isLookingForField(FieldType.Content)) {
                    state.setFieldContents(contents);
                }
            }
        } catch (Exception e) {
            Location loc = event.getLocation();
            String locString = loc.toString().replaceAll("\n", ", ");
            throw new IllegalStateException(this + ": Failed @ line " + loc.getLineNumber() + " : " + locString, e);
        }
    }

    public <S extends Entity> EntityParser<E> withSublist(Sublist sublist, EntityParser<S> entityParser) {
        LinkedHashMap<Sublist, EntityParser<?>> newSublists = new LinkedHashMap<>(subParsers);
        newSublists.put(sublist, entityParser);
        return new EntityParser<>(
                type,
                state,
                fields == null ? Collections.emptyList() : Arrays.asList(fields),
                newSublists);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type.getSimpleName() + "/" + Arrays.toString(fields) + "]";
    }

    @Override
    public Map<Id, E> get() {
        return state.get();
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

    private Id id(StartElement startElement, String attr) {
        String[] idParts = get(startElement, attr).split(":");
        return Id.id(
                Operator.valueOf(idParts[0]),
                idParts[1],
                idParts[2],
                get(Integer::parseInt, startElement, "version"));
    }

    private Id ref(StartElement startElement) {
        return id(startElement, "ref");
    }

    private static <M extends EnumMatch> M startMatch(M[] matches, StartElement startElement) {
        if (matches == null) {
            return null;
        }
        for (M match : matches) {
            if (match.startMatch(startElement)) {
                return match;
            }
        }
        return null;
    }

    private static <M extends EnumMatch> M endMatch(M[] matches, EndElement endElement) {
        if (matches == null) {
            return null;
        }
        for (M match : matches) {
            if (match.endMatch(endElement)) {
                return match;
            }
        }
        return null;
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

        E entity(Id id, Map<Field, Id> ids, Map<Field, String> contents, Map<Sublist, Collection<?>> lists);
    }
}
