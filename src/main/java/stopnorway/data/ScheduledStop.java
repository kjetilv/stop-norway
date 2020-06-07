package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.AbstractIdentified;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;

import java.time.LocalTime;
import java.util.Objects;

public final class ScheduledStop extends AbstractIdentified implements Named, Comparable<ScheduledStop> {

    private final ScheduledStopPoint stopPoint;

    private final String localTime;

    private final LocalTime parsedLocalTime;

    public ScheduledStop(Id id, ScheduledStopPoint stopPoint, String localTime) {
        super(id);
        this.stopPoint = Objects.requireNonNull(stopPoint, "stopPoint");
        this.localTime = localTime;
        this.parsedLocalTime = localTime == null ? null : LocalTime.parse(localTime);
    }

    public ScheduledStopPoint getStopPoint() {
        return stopPoint;
    }

    public String getLocalTime() {
        return localTime;
    }

    public LocalTime getParsedLocalTime() {
        return parsedLocalTime;
    }

    @Override
    public int compareTo(@NotNull ScheduledStop stop) {
        return parsedLocalTime == null ? 1
                : stop.getLocalTime() == null ? -1
                        : parsedLocalTime.isBefore(stop.getParsedLocalTime()) ? -1
                                : parsedLocalTime.isAfter(stop.getParsedLocalTime()) ? 1
                                        : 0;
    }

    @Override
    public String getName() {
        return stopPoint.getName();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + " @ " + localTime + "]";
    }
}
