package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParseState<E extends Entity> {

    private final EntityMaker<E> entityMaker;

    private Field activeField;

    private Sublist activeSublist;

    private Id activeId;

    private Map<Field, Id> fieldIds;

    private Map<Field, String> fieldContents;

    private Map<Attr, String> attributeContents;

    private Map<Sublist, Collection<?>> sublists;

    private Collection<Entity> parsedEntities;

    ParseState(EntityMaker<E> entityMaker) {

        this.entityMaker = entityMaker;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + Stream.of(
                Stream.of(parsedEntities(false).size() + " parsed"),
                activeField == null ? Stream.<String>empty() : Stream.of("activeField: " + activeField),
                activeId == null ? Stream.<String>empty() : Stream.of("activeId: " + activeId),
                fieldIds == null ? Stream.<String>empty() : Stream.of("ids: " + fieldIds),
                fieldContents == null ? Stream.<String>empty() : Stream.of("strings: " + fieldContents)
        ).flatMap(s -> s).collect(Collectors.joining(", ")) + "]";
    }

    public void reset() {
        parsedEntities = null;
    }

    public void absorb(Collection<Entity> idMap) {
        parsedEntities(true).addAll(idMap);
    }

    public int count() {
        return parsedEntities(false).size();
    }

    public void setAttribute(Attr attribute, String value) {
        if (attributeContents == null) {
            attributeContents = new EnumMap<>(Attr.class);
        }
        attributeContents.put(attribute, value);
    }

    public boolean isBuildingList(Sublist sublist) {
        return this.activeSublist != null && this.activeSublist == sublist;
    }

    void completeEntityBuild() {
        if (activeId == null) {
            throw new IllegalStateException(this + " has no active id");
        }
        try {
            EntityData data = extractEntityData();
            E entity = entityMaker.entity(data);
            parsedEntities(true).add(entity);
        } finally {
            activeId = null;
        }
    }

    void startBuildingEntity(Id id) {
        if (this.activeId != null) {
            throw new IllegalStateException(this + " is already building, received: " + id);
        }
        this.activeId = id;
    }

    void startBuildingList(Sublist sublist) {
        activeSublist = sublist;
    }

    @SuppressWarnings("unchecked")
    <S> void completeList(Sublist sublist, Collection<S> elements) {
        if (this.activeSublist == sublist) {
            if (sublists == null) {
                sublists = new EnumMap<>(Sublist.class);
            }
            sublists.compute(activeSublist, (sublist1, objects) -> {
                if (objects == null) {
                    return elements instanceof LinkedList<?> ? elements : new LinkedList<>(elements);
                }
                ((Collection<S>) objects).addAll(elements);
                return objects;
            });
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
        return activeId != null;
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
        if (activeField == null ||
                activeId == null ||
                fieldIds == null ||
                fieldContents == null ||
                attributeContents == null
        ) {
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
        if (fieldIds == null) {
            fieldIds = new EnumMap<>(Field.class);
        }
        if (fieldIds.containsKey(activeField)) {
            throw new IllegalArgumentException(this + " already contains value for " + activeField);
        }
        fieldIds.put(activeField, id);
    }

    void setFieldContents(String contents) {
        if (this.activeField == null) {
            throw new IllegalStateException(this + " cannot append to unknown field: '" + contents + "''");
        }
        setFieldContents(this.activeField, contents);
    }

    void setFieldContents(Field activeField, String contents) {
        if (contents == null || contents.isBlank()) {
            return;
        }
        if (fieldContents == null) {
            fieldContents = new EnumMap<>(Field.class);
        }
        fieldContents.compute(
                activeField,
                (field, s) -> s == null ? contents : s + contents);
    }

    private Collection<Entity> parsedEntities(boolean req) {
        return this.parsedEntities != null ? parsedEntities
                : req ? (this.parsedEntities = new LinkedList<>())
                        : Collections.emptyList();
    }

    private EntityData extractEntityData() {
        try {
            return new EntityData(
                    this.activeId,
                    this.fieldIds == null
                            ? Collections.emptyMap()
                            : this.fieldIds,
                    this.fieldContents == null
                            ? Collections.emptyMap()
                            : this.fieldContents,
                    this.attributeContents == null
                            ? Collections.emptyMap()
                            : attributeContents,
                    this.sublists == null
                            ? Collections.emptyMap()
                            : this.sublists);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not build", e);
        } finally {
            sublists = null;
            fieldIds = null;
            fieldContents = null;
            attributeContents = null;
        }
    }
}
