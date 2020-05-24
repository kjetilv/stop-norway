package stopnorway.in;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseState<E extends Entity> {

    private final Map<Id, E> parsedEntities = new LinkedHashMap<>();

    private final EntityParser.EntityMaker<E> entityMaker;

    private Field activeField;

    private Sublist activeSublist;

    private Id activeId;

    private Map<Field, Id> fieldIds;

    private Map<Field, StringBuilder> fieldContents;

    private Map<Sublist, Collection<?>> sublists;

    public ParseState(EntityParser.EntityMaker<E> entityMaker) {

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

    void completeEntityBuild() {
        if (activeId == null) {
            throw new IllegalStateException(this + " has no active id");
        }
        E entity = create();
        parsedEntities.put(activeId, entity);
        activeId = null;
        sublists = null;
        fieldIds = null;
        fieldContents = null;
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
    <S> void stopBuildingList(Sublist sublist, Collection<S> elements) {
        if (this.activeSublist == sublist) {
            if (sublists == null) {
                sublists = new EnumMap<>(Sublist.class);
            }
            Map<Sublist, Collection<?>> sublistCollectionMap = sublists;
            Collection<S> objects = (Collection<S>)
                    sublistCollectionMap.computeIfAbsent(activeSublist, __ -> new ArrayList<>());
            objects.addAll(elements);
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
        if (activeField == null || activeId == null || fieldIds == null || fieldContents == null) {
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

    private E create() {
        try {
            return entityMaker.entity(
                    activeId,
                    fieldIds == null ? Collections.emptyMap() : Map.copyOf(fieldIds),
                    this.fieldContents == null
                            ? Collections.emptyMap()
                            : toStrings(this.fieldContents),
                    sublists == null ? Collections.emptyMap() : Map.copyOf(sublists));
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not build", e);
        }
    }

    @NotNull
    private static Map<Field, String> toStrings(Map<Field, StringBuilder> fieldStrings) {
        return fieldStrings.entrySet().stream()
                .map(e ->
                        new AbstractMap.SimpleEntry<>(e.getKey(), String.valueOf(e.getValue())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }
}
