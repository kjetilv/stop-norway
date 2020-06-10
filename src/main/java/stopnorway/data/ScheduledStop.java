package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.AbstractIdentified;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.geo.Timespan;

import java.util.Objects;

public final class ScheduledStop extends AbstractIdentified implements Named, Comparable<ScheduledStop> {

    private final ScheduledStopPoint stopPoint;

    private final Timespan timespan;

    public ScheduledStop(Id id, ScheduledStopPoint stopPoint, Timespan timespan) {
        super(id);
        this.stopPoint = Objects.requireNonNull(stopPoint, "stopPoint");
        this.timespan = timespan;
    }

    public ScheduledStopPoint getStopPoint() {
        return stopPoint;
    }

    public Timespan getTimespan() {
        return timespan;
    }

    @Override
    public int compareTo(@NotNull ScheduledStop stop) {
        return timespan.isBefore(stop.getTimespan()) ? -1
                : timespan.isAfter(stop.getTimespan()) ? 1
                : 0;
    }

    @Override
    public String getName() {
        return stopPoint.getName();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + " @ " + timespan + "]";
    }
}
