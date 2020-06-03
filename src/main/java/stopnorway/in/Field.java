package stopnorway.in;

public enum Field implements EnumMatch {

    Name,

    ShortName,

    TransportMode,

    FromPointRef,

    ToPointRef,

    ProjectedPointRef,

    RoutePointRef,

    RouteRef,

    Distance,

    DirectionType,

    ScheduledStopPointRef,

    ServiceLinkRef,

    StopPointInJourneyPatternRef,

    JourneyPatternRef,

    LineRef,

    DepartureTime,

    posList;

    private final DataType fieldType;

    Field() {
        this(null);
    }

    Field(DataType fieldType) {
        this.fieldType = fieldType != null ? fieldType
                : name().endsWith("Ref") ? DataType.Ref
                : DataType.Content;
    }

    public DataType getFieldType() {
        return fieldType;
    }
}
