package stopnorway.in;

import stopnorway.database.Id;

import java.util.*;

public final class EntityData {

    private final Id id;

    private final Map<Field, Id> ids;

    private final Map<Field, String> contents;

    private final Map<Sublist, Collection<?>> sublists;

    public EntityData(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> sublists
    ) {
        this.id = id;
        this.ids = ids;
        this.contents = contents;
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

    public Collection<?> getSublist(Sublist sublist) {
        return sublists.get(sublist);
    }
}
