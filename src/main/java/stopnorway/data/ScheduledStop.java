package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.AbstractIdentified;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;

import java.time.LocalTime;

public final class ScheduledStop extends AbstractIdentified implements Named, Comparable<ScheduledStop> {

    private final ScheduledStopPoint stopPoint;

    private final LocalTime localTime;

    public ScheduledStop(Id id, ScheduledStopPoint stopPoint, LocalTime localTime) {
        super(id);
        this.stopPoint = stopPoint;
        this.localTime = localTime;
    }

    public ScheduledStopPoint getStopPoint() {
        return stopPoint;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    @Override
    public int compareTo(@NotNull ScheduledStop point) {
        return localTime == null ? 1
                : point.getLocalTime() == null ? -1
                        : localTime.isBefore(point.getLocalTime()) ? -1
                                : localTime.isAfter(point.getLocalTime()) ? 1
                                        : 0;
    }

    @Override
    public String getName() {
        return stopPoint.getName();
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "[" + getName() + " @ " + localTime + "]";
    }
}
