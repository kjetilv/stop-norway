package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseState<E extends Entity> {

    private final Map<Id, E> map = new HashMap<>();

    private final EntityParser.EntityMaker<E> entityMaker;

    private Field activeField;

    private Id activeId;

    private Map<Field, Object> fieldValues;

    public ParseState(EntityParser.EntityMaker<E> entityMaker) {

        this.entityMaker = entityMaker;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + Stream.of(
                Stream.of(map.size() + " parsed"),
                activeField == null ? Stream.<String>empty() : Stream.of("activeField: " + activeField),
                activeId == null ? Stream.<String>empty() : Stream.of("activeId: " + activeId),
                fieldValues == null ? Stream.<String>empty() : Stream.of("fields: " + fieldValues)
        ).flatMap(s -> s).collect(Collectors.joining(", ")) + "]";
    }

    public void buildEntity() {
        if (activeId == null) {
            throw new IllegalStateException(this + " has no active id");
        }
        E entity;
        try {
            Map<Field, Object> values = fieldValues();
            entity = entityMaker.entity(activeId, values);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not build", e);
        }
        map.put(activeId, entity);
        activeId = null;
        fieldValues = null;
    }

    public void startBuildingEntity(Id id) {
        if (this.activeId != null) {
            throw new IllegalStateException(this + " is already building, received: " + id);
        }
        this.activeId = id;
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
        if (closingField == activeField) {
            activeField = null;
        }
    }

    Map<Id, E> get() {
        if (activeField == null || activeId == null || fieldValues == null) {
            return map;
        }
        throw new IllegalStateException(this + " is active");
    }

    void setFieldValue(Object object) {
        if (activeField == null) {
            throw new IllegalStateException(this + " cannot set to unknown field: '" + object + "''");
        }
        fieldValues().put(activeField, object);
    }

    private Map<Field, Object> fieldValues() {
        if (fieldValues == null) {
            fieldValues = new EnumMap<>(Field.class);
        }
        return fieldValues;
    }
}
