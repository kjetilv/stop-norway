package stopnorway.in;

import stopnorway.database.Id;

import java.util.Collection;
import java.util.Map;

public final class EntityData {

    private final Id id;

    private final Map<Field, Id> ids;

    private final Map<Field, String> contents;

    private final Map<Attr, String> attributes;

    private final Map<Sublist, Collection<?>> sublists;

    public EntityData(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Attr, String> attributes,
            Map<Sublist, Collection<?>> sublists
    ) {
        this.id = id;
        this.ids = ids;
        this.contents = contents;
        this.attributes = attributes;
        this.sublists = sublists;
    }

    public Id getId() {
        return id;
    }

    public Id getId(Field field) {
        return ids.get(field);
    }

    public String getContent(Field field) {
        return contents.get(field);
    }

    public String getAttribute(Attr attr) {
        return attributes.get(attr);
    }

    public int getIntAttribute(Attr attr) {
        return toInt(attr, attributes.get(attr));
    }

    public int getIntContent(Field field) {
        return toInt(field, getContent(field));
    }

    private int toInt(Enum<?> field, String value) {
        if (value == null) {
            throw new IllegalStateException("No int value: " + field);
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected int value: " + value, e);
        }
    }

    public Collection<?> getSublist(Sublist sublist) {
        return sublists.get(sublist);
    }
}
