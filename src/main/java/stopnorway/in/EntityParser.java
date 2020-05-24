package stopnorway.in;

import stopnorway.data.Field;
import stopnorway.data.Sublist;
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
import java.util.function.Predicate;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class EntityParser<E extends Entity> implements Consumer<XMLEvent>, Predicate<XMLEvent> {

    private final Class<E> type;

    private final Field[] fields;

    private final Sublist[] sublists;

    private final Map<Sublist, EntityParser<?>> subParsers;

    private final ParseState<E> state;

    private final String simpleName;

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Field... fields) {
        this(type, entityMaker, Arrays.asList(fields));
    }

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Collection<Field> fields) {
        this(
                Objects.requireNonNull(type, "type"),
                type.getSimpleName(),
                new ParseState<>(entityMaker),
                fields,
                null);
    }

    private EntityParser(
            Class<E> type,
            String simpleName,
            ParseState<E> state,
            Collection<Field> fields,
            Map<Sublist, EntityParser<?>> subParsers
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.simpleName = simpleName;
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
    public boolean test(XMLEvent event) {
        return state.isBuildingEntity() ||
                event.getEventType() == START_ELEMENT && isEntity(event.asStartElement());
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
                    Map<Id, ?> idMap = listParser.get(true);
                    state.completeList(matchingSublist, idMap.values());
                    listParser.reset();
                }
                return;
            }
            if (eventType == START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                if (isEntity(startElement)) {
                    state.startBuildingEntity(id(startElement, "id"));
                    return;
                }
                if (state.isBuildingEntity()) {
                    Field matchingField = startMatch(this.fields, startElement);
                    if (matchingField != null) {
                        state.startLookingForField(matchingField);
                        if (state.isLookingForField(FieldType.Ref)) {
                            state.setFieldId(id(startElement, "ref"));
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
                if (state.isLookingForField(FieldType.Content)) {
                    String contents = event.asCharacters().getData();
                    state.setFieldContents(contents);
                }
            }
        } catch (Exception e) {
            Location loc = event.getLocation();
            String locString = loc.toString().replaceAll("\n", ", ");
            throw new IllegalStateException(this + ": Failed @ line " + loc.getLineNumber() + " : " + locString, e);
        }
    }

    private void reset() {
        state.reset();
    }

    public <S extends Entity> EntityParser<E> withSublist(Sublist sublist, EntityParser<S> entityParser) {
        LinkedHashMap<Sublist, EntityParser<?>> newSublists = new LinkedHashMap<>(subParsers);
        newSublists.put(sublist, entityParser);
        return new EntityParser<>(
                type,
                simpleName,
                state,
                fields == null ? null : Arrays.asList(fields),
                newSublists);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + simpleName + "/" + Arrays.toString(fields) + "]";
    }

    public Map<Id, E> get() {
        return get(false);
    }

    public Map<Id, E> get(boolean reset) {
        return state.get(reset);
    }

    private boolean isEntity(StartElement startElement) {
        return startElement.getName().getLocalPart().equalsIgnoreCase(simpleName);
    }

    private boolean isEntity(EndElement endElement) {
        return endElement.getName().getLocalPart().equalsIgnoreCase(simpleName);
    }

    private static Id id(StartElement startElement, String attr) {
        String[] idParts = get(startElement, attr).split(":");
        return Id.id(
                Operator.valueOf(idParts[0]),
                idParts[1],
                idParts[2],
                version(Integer::parseInt, startElement, 1));
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

    private static String get(StartElement startElement, String attr) {
        Attribute attributeByName = startElement.getAttributeByName(QName.valueOf(attr));
        if (attributeByName == null) {
            throw new IllegalArgumentException("No attribute '" + attr + "' in " + startElement);
        }
        return attributeByName.getValue();
    }

    private static <T> T version(Function<String, T> function, StartElement startElement, T defaultValue) {
        Attribute attribute = startElement.getAttributeByName(QName.valueOf("version"));
        if (attribute == null) {
            return defaultValue;
        }
        String version = attribute.getValue();
        return version == null ? defaultValue : function.apply(version);
    }
}
