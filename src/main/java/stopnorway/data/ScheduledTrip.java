package stopnorway.data;

import stopnorway.database.AbstractBoxed;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.geo.Box;
import stopnorway.util.Safe;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScheduledTrip extends AbstractBoxed implements Named, Comparable<ScheduledTrip> {

    private final TripDefinition tripDefinition;

    private final Collection<ScheduledStop> scheduledStops;

    public ScheduledTrip(
            Id serviceJourneyId,
            TripDefinition tripDefinition,
            Collection<ScheduledStop> scheduledStops
    ) {
        super(serviceJourneyId);
        this.tripDefinition = tripDefinition;

        Collection<Id> scheduledStopPointRefs = tripDefinition.getStopPoints().stream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .map(ScheduledStopPoint::getId)
                .collect(Collectors.toList());

        Map<Id, LinkedList<ScheduledStop>> groups = group(
                scheduledStops,
                scheduledStop -> scheduledStop.getStopPoint().getId());

        this.scheduledStops = scheduledStopPointRefs.stream()
                .map(id -> groups.get(id).removeFirst())
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
                getName() + " " + getStartTime().map(LocalTime::toString).orElse("unknown") +
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

    private static <K, V extends Comparable<V>> Map<K, LinkedList<V>> group(
            Collection<V> values,
            Function<V, K> key
    ) {
        return values.stream()
                .collect(Collectors.groupingBy(
                        key,
                        HashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toCollection(LinkedList::new),
                                vals -> {
                                    Collections.sort(vals);
                                    return vals;
                                })));
    }
}
