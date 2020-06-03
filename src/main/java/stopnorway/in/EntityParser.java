package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.util.Accept;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class EntityParser<E extends Entity> {

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
        this.subParsers = Accept.map(subParsers);
        this.state = state;
    }

    public boolean canDigest(XMLEvent event) {
        return state.isBuildingEntity() ||
                event.getEventType() == START_ELEMENT && isEntity(event.asStartElement());
    }

    public void digest(XMLEvent event) {
        try {
            int eventType = event.getEventType();
            if (state.isBuildingList()) {
                EntityParser<?> entityParser = subParsers.get(state.getActiveSublist());
                if (entityParser != null) {
                    entityParser.digest(event);
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
                    Collection<Entity> subEntities = listParser.get(true);
                    state.absorb(subEntities);
                    state.completeList(matchingSublist, subEntities);
                    listParser.reset();
                }
                return;
            }
            if (eventType == START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                if (isEntity(startElement)) {
                    state.startBuildingEntity(id(startElement, "id"));

                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        Field matchingField = attributeMatch(fields, attribute);
                        if (matchingField != null) {
                            state.setFieldContents(matchingField, attribute.getValue());
                        }
                    }
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

    public <S extends Entity> EntityParser<E> withSublist(Sublist sublist, EntityParser<S> entityParser) {
        LinkedHashMap<Sublist, EntityParser<?>> newSublists = new LinkedHashMap<>(subParsers);
        EntityParser<?> existing = newSublists.put(sublist, entityParser);
        if (existing == null) {
            return new EntityParser<>(
                    type,
                    simpleName,
                    state,
                    fields == null ? null : Arrays.asList(fields),
                    newSublists);
        }
        throw new IllegalStateException("Already contained " + sublist + " -> " + existing);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + simpleName + "/" + Arrays.toString(fields) + "]";
    }

    public Collection<Entity> get() {
        return get(false);
    }

    public Collection<Entity> get(boolean reset) {
        return state.get(reset);
    }

    private void reset() {
        state.reset();
    }

    private boolean isEntity(StartElement startElement) {
        return startElement.getName().getLocalPart().equalsIgnoreCase(simpleName);
    }

    private boolean isEntity(EndElement endElement) {
        return endElement.getName().getLocalPart().equalsIgnoreCase(simpleName);
    }

    private static Id id(StartElement startElement, String attr) {
        String[] idParts = get(startElement, attr).split(":");
        return Id.id(idParts[0], idParts[1], idParts[2]);
    }

    private static <M extends EnumMatch> M startMatch(M[] matches, StartElement startElement) {
        if (matches == null) {
            return null;
        }
        for (M match: matches) {
            if (match.startMatch(startElement)) {
                return match;
            }
        }
        return null;
    }

    private static <M extends EnumMatch> M attributeMatch(M[] matches, Attribute attribute) {
        if (matches == null) {
            return null;
        }
        for (M match: matches) {
            if (match.attributeMatch(attribute)) {
                return match;
            }
        }
        return null;
    }

    private static <M extends EnumMatch> M endMatch(M[] matches, EndElement endElement) {
        if (matches == null) {
            return null;
        }
        for (M match: matches) {
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
}
