package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.util.Accept;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

import static javax.xml.stream.XMLStreamConstants.*;
import static stopnorway.in.Attr.id;
import static stopnorway.in.Attr.ref;

public final class EntityParser<E extends Entity> {

    private static final String URI = "http://www.netex.org.uk/netex";

    private final Class<E> type;

    private final EntityMaker<E> entityMaker;

    private final Field[] fields;

    private final Attr[] attributes;

    private final Sublist[] sublists;

    private final Map<Sublist, EntityParser<?>> subParsers;

    private final ParseState<E> state;

    private final QName name;

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Field... fields) {
        this(type, entityMaker, Arrays.asList(fields));
    }

    public EntityParser(Class<E> type, EntityMaker<E> entityMaker, Collection<Field> fields) {
        this(type, entityMaker, fields, null);
    }

    public EntityParser(
            Class<E> type,
            EntityMaker<E> entityMaker,
            Collection<Field> fields,
            Collection<Attr> attributes
    ) {
        this(
                Objects.requireNonNull(type, "type"),
                new QName(URI, type.getSimpleName()),
                entityMaker,
                null,
                fields,
                attributes,
                null);
    }

    private EntityParser(
            Class<E> type,
            QName name,
            EntityMaker<E> entityMaker,
            ParseState<E> state,
            Collection<Field> fields,
            Collection<Attr> attributes,
            Map<Sublist, EntityParser<?>> subParsers
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.name = Objects.requireNonNull(name, "name");
        this.entityMaker = entityMaker;
        this.fields = fields == null || fields.isEmpty()
                ? null
                : fields.toArray(new Field[0]);
        this.attributes = attributes == null || attributes.isEmpty()
                ? null
                : attributes.toArray(new Attr[0]);
        this.sublists = subParsers == null || subParsers.isEmpty()
                ? null
                : subParsers.keySet().toArray(Sublist[]::new);
        this.subParsers = Accept.map(subParsers);
        this.state = state == null ? new ParseState<>() : state;
    }

    public void digest(XMLEvent event) {
        try {
            int type = event.getEventType();
            if (state.isBuildingList()) {
                delegate(event);
            }
            if (type == END_ELEMENT) {
                processEnd(event.asEndElement());
            } else if (type == START_ELEMENT) {
                processStart(event.asStartElement());
            } else if (type == CHARACTERS) {
                processContents(event);
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
                    name,
                    entityMaker,
                    state,
                    fields == null ? null : Arrays.asList(fields),
                    attributes == null ? null : Arrays.asList(attributes),
                    newSublists);
        }
        throw new IllegalStateException("Already contained " + sublist + " -> " + existing);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "/" + Arrays.toString(fields) + "]";
    }

    Collection<Entity> get() {
        return get(false);
    }

    Collection<Entity> get(boolean reset) {
        return state.get(reset);
    }

    private void delegate(XMLEvent event) {
        EntityParser<?> entityParser = subParsers.get(state.getActiveSublist());
        if (entityParser != null) {
            entityParser.digest(event);
        }
    }

    private void processStart(StartElement startElement) {
        QName elementName = startElement.getName();
        if (isEntity(elementName)) {
            state.startBuildingEntity(Id.parse(getAttribute(startElement, id)));
            if (attributes != null) {
                collectAttributes(startElement);
            }
            return;
        }
        if (state.isBuildingEntity()) {
            Field matchingField = match(this.fields, elementName);
            if (matchingField != null) {
                intializeField(startElement, matchingField);
            }
            Sublist matchingSublist = match(sublists, elementName);
            if (matchingSublist != null) {
                state.startBuildingList(matchingSublist);
            }
        }
    }

    private void processContents(XMLEvent event) {
        if (state.isLookingForField(DataType.Content)) {
            String contents = event.asCharacters().getData();
            state.setContents(contents);
        }
    }

    private void processEnd(EndElement endElement) {
        QName elementName = endElement.getName();
        if (this.name.equals(elementName)) {
            state.completeEntityBuild(entityMaker);
            return;
        }
        Field matchingField = match(fields, elementName);
        if (matchingField != null) {
            state.stopLookingForField(matchingField);
        }
        Sublist sublist = match(sublists, elementName);
        if (state.isBuildingList(sublist)) {
            processSublistEnd(sublist);
        }
    }

    private void processSublistEnd(Sublist sublist) {
        try {
            EntityParser<?> listParser = subParsers.get(sublist);
            Collection<Entity> subEntities = listParser.get(true);
            state.absorb(subEntities);
            state.completeList(sublist, subEntities);
            listParser.reset();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close " + sublist, e);
        }
    }

    private void intializeField(StartElement startElement, Field matchingField) {
        state.startLookingForField(matchingField);
        if (state.isLookingForField(DataType.Ref)) {
            state.setFieldId(Id.parse(getAttribute(startElement, ref)));
        }
    }

    private void collectAttributes(StartElement startElement) {
        for (Attr attr: attributes) {
            Attribute attribute = startElement.getAttributeByName(QName.valueOf(attr.name()));
            if (attribute != null) {
                state.setAttribute(attr, attribute.getValue());
            }
        }
    }

    private void reset() {
        state.reset();
    }

    private boolean isEntity(QName name) {
        return this.name.equals(name);
    }

    private static <M extends EnumMatch> M match(M[] matches, QName name) {
        if (matches == null) {
            return null;
        }
        for (M match: matches) {
            if (match.matches(name)) {
                return match;
            }
        }
        return null;
    }

    private static String getAttribute(StartElement startElement, Attr attr) {
        return startElement.getAttributeByName(attr.qname()).getValue();
    }
}
