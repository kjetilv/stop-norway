package stopnorway.data;

import stopnorway.database.AbstractBoxed;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.geo.Box;
import stopnorway.util.Safe;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScheduledTrip extends AbstractBoxed implements Named, Comparable<ScheduledTrip> {

    private final TripDefinition tripDefinition;

    private final Collection<ScheduledStop> scheduledStops;

    public ScheduledTrip(Id serviceJourneyId, TripDefinition tripDefinition, Collection<ScheduledStop> scheduledStops) {
        super(serviceJourneyId);
        this.tripDefinition = tripDefinition;

        Collection<Id> scheduledStopPointRefs = tripDefinition.getStopPoints().stream()
                .map(Map.Entry::getValue)
                .map(ScheduledStopPoint::getId)
                .collect(Collectors.toList());

        Map<Id, ScheduledStop> scheduledStopMap = scheduledStops.stream()
                .collect(Collectors.toMap(
                        scheduledStop -> scheduledStop.getStopPoint().getId(),
                        Function.identity()));

        this.scheduledStops = scheduledStopPointRefs.stream()
                .map(scheduledStopMap::get)
                .collect(Collectors.toList());
    }

    public Optional<LocalTime> getStartTime() {
        return this.scheduledStops.stream().findFirst().map(ScheduledStop::getLocalTime);
    }

    public Collection<ScheduledStop> getScheduledStops() {
        return Safe.list(scheduledStops);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                getName() + " @ " + getStartTime().map(LocalTime::toString).orElse("unknown") +
                "]";
    }

    @Override public String getName() {
        return tripDefinition.getName();
    }

    @Override
    public int compareTo(ScheduledTrip o) {
        return o.getStartTime().isEmpty() ? -1
                : getStartTime().isEmpty() ? 1
                        : getStartTime().flatMap(localTime -> o.getStartTime().map(localTime::compareTo)).orElse(0);
    }

    @Override protected Optional<Box> computeBox() {
        return tripDefinition.computeBox();
    }
}
