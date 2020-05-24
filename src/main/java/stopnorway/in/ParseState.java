package stopnorway.in;

import stopnorway.data.Field;
import stopnorway.data.Sublist;
import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParseState<E extends Entity> {

    private final Map<Id, E> parsedEntities = new LinkedHashMap<>();

    private final EntityMaker<E> entityMaker;

    private Field activeField;

    private Sublist activeSublist;

    private Id activeId;

    private Map<Field, Id> fieldIds;

    private Map<Field, StringBuilder> fieldContents;

    private Map<Sublist, Collection<Collection<?>>> sublists;

    ParseState(EntityMaker<E> entityMaker) {

        this.entityMaker = entityMaker;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + Stream.of(
                Stream.of(parsedEntities.size() + " parsed"),
                activeField == null ? Stream.<String>empty() : Stream.of("activeField: " + activeField),
                activeId == null ? Stream.<String>empty() : Stream.of("activeId: " + activeId),
                fieldIds == null ? Stream.<String>empty() : Stream.of("ids: " + fieldIds),
                fieldContents == null ? Stream.<String>empty() : Stream.of("strings: " + fieldContents)
        ).flatMap(s -> s).collect(Collectors.joining(", ")) + "]";
    }

    public void reset() {
        parsedEntities.clear();
    }

    void completeEntityBuild() {
        if (activeId == null) {
            throw new IllegalStateException(this + " has no active id");
        }
        E entity;
        try {
            EntityData data = buildAndClear();
            entity = entityMaker.entity(data);
            parsedEntities.put(activeId, entity);
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

    <S> void completeList(Sublist sublist, Collection<S> elements) {
        if (this.activeSublist == sublist) {
            if (sublists == null) {
                sublists = new EnumMap<>(Sublist.class);
            }
            sublists.computeIfAbsent(
                    activeSublist,
                    __ -> new ArrayList<>()).add(elements);
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

    boolean isLookingForField(FieldType type) {
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

    Map<Id, E> get() {
        return get(false);
    }

    Map<Id, E> get(boolean clear) {
        if (activeField == null || activeId == null || fieldIds == null || fieldContents == null) {
            if (clear) {
                Map<Id, E> copy = Map.copyOf(parsedEntities);
                parsedEntities.clear();
                return copy;
            }
            return parsedEntities;
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
        if (activeField == null) {
            throw new IllegalStateException(this + " cannot append to unknown field: '" + contents + "''");
        }
        if (fieldContents == null) {
            fieldContents = new EnumMap<>(Field.class);
        }
        fieldContents.compute(
                activeField,
                (__, existing) ->
                        existing == null ? new StringBuilder(contents) : existing.append(contents));
    }

    private EntityData buildAndClear() {
        try {
            return new EntityData(
                    this.activeId,
                    this.fieldIds == null ? Collections.emptyMap() : Map.copyOf(this.fieldIds),
                    this.fieldContents == null
                            ? Collections.emptyMap()
                            : toStrings(this.fieldContents),
                    this.sublists == null
                            ? Collections.emptyMap()
                            : toMap(this.sublists));
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not build", e);
        } finally {
            sublists = null;
            fieldIds = null;
            fieldContents = null;
        }
    }

    private static Map<Sublist, Collection<?>> toMap(Map<Sublist, Collection<Collection<?>>> sublists) {
        return sublists.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, ParseState::unpack));
    }

    private static Collection<?> unpack(Map.Entry<Sublist, Collection<Collection<?>>> e) {
        Collection<Collection<?>> collections = e.getValue();
        if (collections.isEmpty()) {
            return Collections.emptyList();
        }
        if (collections.size() == 1) {
            return List.copyOf(collections.iterator().next());
        }
        return collections.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static Map<Field, String> toStrings(Map<Field, StringBuilder> fieldStrings) {
        return fieldStrings.entrySet().stream()
                .map(e ->
                        new AbstractMap.SimpleEntry<>(
                                e.getKey(),
                                String.valueOf(e.getValue())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }
}
