package stopnorway.in;

import stopnorway.in.EnumMatch;
import stopnorway.in.FieldType;

public enum Field implements EnumMatch {

    Name,

    ShortName,

    TransportMode,

    FromPointRef,

    ToPointRef,

    ProjectedPointRef,

    RoutePointRef,

    Distance,

    DirectionType,

    ScheduledStopPointRef,

    StopPointInJourneyPatternRef,

    JourneyPatternRef,

    DepartureTime,

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
