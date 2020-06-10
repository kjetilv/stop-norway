package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParseState<E extends Entity> {

    private static final int ZERO_OFFSET = 48;

    private Field activeField;

    private Sublist activeSublist;

    private Id id;

    private Map<Field, Id> ids;

    private Map<Field, String> contents;

    private Map<Attr, String> attributes;

    private Map<Sublist, Collection<?>> sublists;

    private Collection<Entity> parsedEntities;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + Stream.of(
                Stream.of(parsedEntities(false).size() + " parsed"),
                activeField == null ? Stream.<String>empty() : Stream.of("activeField: " + activeField),
                id == null ? Stream.<String>empty() : Stream.of("activeId: " + id),
                ids == null ? Stream.<String>empty() : Stream.of("ids: " + ids),
                contents == null ? Stream.<String>empty() : Stream.of("strings: " + contents)
        ).flatMap(s -> s).collect(Collectors.joining(", ")) + "]";
    }

    void reset() {
        parsedEntities = null;
    }

    void absorb(Collection<Entity> idMap) {
        parsedEntities(true).addAll(idMap);
    }

    void setAttribute(Attr attribute, String value) {
        if (attributes == null) {
            attributes = new EnumMap<>(Attr.class);
        }
        attributes.put(attribute, value);
    }

    boolean isBuildingList(Sublist sublist) {
        return this.activeSublist != null && this.activeSublist == sublist;
    }

    Id getId() {
        return Objects.requireNonNull(id, "id");
    }

    Id getId(Field field) {
        return ids == null ? null : ids.get(field);
    }

    String getContent(Field field) {
        return contents == null ? null : contents.get(field);
    }

    int getIntContent(Field field) {
        if (contents == null) {
            return 0;
        }
        String content = contents.get(field);
        if (content == null) {
            return 0;
        }
        return toInt(field, content, 0, false);
    }

    int getOrder() {
        return toInt(Attr.order, getAttribute(Attr.order), 0, true);
    }

    @SuppressWarnings("SameParameterValue")
    String getAttribute(Attr attr) {
        if (attributes == null) {
            throw new IllegalArgumentException(this + ": Not found: " + attr);
        }
        return attributes.get(attr);
    }

    Collection<?> getSublist(Sublist sublist) {
        return sublists == null ? null : sublists.get(sublist);
    }

    void completeEntityBuild(EntityMaker<E> entityMaker) {
        completeWith(entityMaker);
    }

    void startBuildingEntity(Id id) {
        if (this.id != null) {
            throw new IllegalStateException(this + " is already building, received: " + id);
        }
        this.id = id;
    }

    void startBuildingList(Sublist sublist) {
        activeSublist = sublist;
    }

    <S> void completeList(Sublist sublist, Collection<S> elements) {
        if (this.activeSublist == sublist) {
            store(elements);
            this.activeSublist = null;
        } else {
            throw new IllegalStateException(this + " is not bulding " + sublist);
        }
    }

    boolean isBuildingList() {
        return activeSublist != null;
    }

    Sublist getActiveSublist() {
        return activeSublist;
    }

    boolean isBuildingEntity() {
        return id != null;
    }

    boolean isLookingForField(DataType type) {
        return activeField != null && activeField.getFieldType() == type;
    }

    void startLookingForField(Field activeField) {
        this.activeField = activeField;
    }

    void stopLookingForField(Field closingField) {
        if (this.activeField == closingField) {
            this.activeField = null;
        }
    }

    Collection<Entity> get(boolean clear) {
        if (activeField == null || id == null || ids == null || contents == null || attributes == null) {
            Collection<Entity> entities = parsedEntities(false);
            if (clear) {
                reset();
            }
            return entities;
        }
        throw new IllegalStateException(this + " is active");
    }

    void setFieldId(Id id) {
        if (activeField == null) {
            throw new IllegalStateException(this + " cannot set to unknown field: " + id);
        }
        if (ids == null) {
            ids = new EnumMap<>(Field.class);
        }
        if (ids.containsKey(activeField)) {
            throw new IllegalArgumentException(this + " already contains value for " + activeField);
        }
        ids.put(activeField, id);
    }

    void setContents(String contents) {
        if (this.activeField == null) {
            throw new IllegalStateException(this + " cannot append to unknown field: '" + contents + "''");
        }
        setFieldContents(this.activeField, contents);
    }

    void setFieldContents(Field activeField, String contents) {
        if (contents == null || contents.isBlank()) {
            return;
        }
        if (this.contents == null) {
            this.contents = new EnumMap<>(Field.class);
        }
        this.contents.compute(
                activeField,
                (field, s) -> s == null ? contents : s + contents);
    }

    @SuppressWarnings("SameParameterValue")
    private int toInt(EnumMatch en, String value, int defaultValue, boolean required) {
        if (value == null) {
            if (required) {
                throw new IllegalStateException("No int value: " + en);
            }
            return defaultValue;
        }
        return toInt(value.trim());
    }

    private int toInt(String value) {
        if (value.equals("0")) {
            return 0;
        }
        if (value.equals("1")) {
            return 1;
        }
        int length = value.length();
        if (length == 1) {
            return value.charAt(0) - ZERO_OFFSET;
        }
        int v = 0;
        for (int i = 0; i < length; i++) {
            v *= 10;
            v += value.charAt(i) - ZERO_OFFSET;
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    private <S> void store(Collection<S> elements) {
        if (sublists == null) {
            sublists = new EnumMap<>(Sublist.class);
        }
        Collection<?> storedElements = elements instanceof LinkedList<?>
                ? elements
                : new LinkedList<>(elements);
        Collection<S> existing = (Collection<S>) sublists.put(activeSublist, storedElements);
        if (existing != null) {
            existing.addAll(elements);
            sublists.put(activeSublist, existing);
        }
    }

    private void completeWith(EntityMaker<E> entityMaker) {
        if (id == null) {
            throw new IllegalStateException(this + " has no active id");
        }
        try {
            parsedEntities(true).add(entityMaker.entity(this));
        } finally {
            id = null;
            sublists = null;
            ids = null;
            contents = null;
            attributes = null;
        }
    }

    private Collection<Entity> parsedEntities(boolean req) {
        return this.parsedEntities != null ? parsedEntities
                : req ? (this.parsedEntities = new LinkedList<>())
                        : Collections.emptyList();
    }
}
