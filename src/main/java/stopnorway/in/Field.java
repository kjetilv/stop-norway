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

    RouteRef,

    Distance,

    DirectionType,

    ScheduledStopPointRef,

    ServiceLinkRef,

    StopPointInJourneyPatternRef,

    JourneyPatternRef,

    DepartureTime,

    posList,

    order;

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
