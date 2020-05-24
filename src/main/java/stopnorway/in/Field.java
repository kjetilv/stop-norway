package stopnorway.in;

public enum Field implements EnumMatch<Field> {

    Name,

    FromPointRef,

    ToPointRef,

    Distance,

    posList;

    private final FieldType fieldType;

    Field() {
        this(null);
    }

    Field(FieldType fieldType) {
        this.fieldType = fieldType != null ? fieldType
                : name().endsWith("Ref") ? FieldType.Ref
                : FieldType.Content;
    }

    public FieldType getFieldType() {
        return fieldType;
    }
}
