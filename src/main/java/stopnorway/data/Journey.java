package stopnorway.data;

import stopnorway.database.AbstractIdentified;
import stopnorway.database.Boxed;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.geo.Box;
import stopnorway.geo.Timespan;
import stopnorway.util.Safe;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Journey extends AbstractIdentified implements Boxed, Named, Comparable<Journey> {

    private final JourneySpecification journeySpecification;

    private final LinkedList<ScheduledStop> scheduledStops;

    private final Box box;

    public Journey(
            Id serviceJourneyId,
            JourneySpecification journeySpecification,
            Collection<ScheduledStop> scheduledStops
    ) {
        super(serviceJourneyId);
        this.journeySpecification = journeySpecification;

        Collection<Id> scheduledStopPointRefs = journeySpecification.getStopPoints().stream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .map(ScheduledStopPoint::getId)
                .collect(Collectors.toList());

        Map<Id, LinkedList<ScheduledStop>> groups = group(
                scheduledStops,
                scheduledStop -> scheduledStop.getStopPoint().getId());

        this.scheduledStops = scheduledStopPointRefs.stream()
                .map(id -> groups.get(id).removeFirst())
                .collect(Collectors.toCollection(LinkedList::new));
        this.box = this.journeySpecification.getBox().orElse(null);
    }

    @Override
    public Optional<Box> getBox() {
        return Optional.ofNullable(box);
    }

    public Optional<LocalTime> getStartTime() {
        if (scheduledStops.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(scheduledStops.getFirst()).map(ScheduledStop::getParsedLocalTime);
    }

    public Optional<LocalTime> getEndTime() {
        if (scheduledStops.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.scheduledStops.getLast()).map(ScheduledStop::getParsedLocalTime);
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

    @Override
    public String getName() {
        return journeySpecification.getName();
    }

    @Override
    public int compareTo(Journey o) {
        return o.getStartTime().isEmpty() ? -1
                : getStartTime().isEmpty() ? 1
                        : getStartTime().flatMap(localTime -> o.getStartTime().map(localTime::compareTo)).orElse(0);
    }

    public Stream<Timespan> getTimespans(Duration duration) {
        return getStartTime()
                .flatMap(startTime -> getEndTime()
                        .map(endTime -> journeySpecification.getServiceLegs().stream()
                                .map(Map.Entry::getValue)
                                .flatMap(serviceLeg -> serviceLeg.getTemporalBoxes(
                                        startTime, endTime, duration))))
                .orElseGet(Stream::empty);
    }

    boolean overlaps(Timespan temporalBox) {
        return getStartTime()
                .flatMap(start -> getEndTime()
                        .map(end -> {
                            LocalTime boxStart = temporalBox.getStart();
                            LocalTime boxEnd = temporalBox.getEnd();
                            return start.isBefore(boxStart) && end.isAfter(boxStart) ||
                                    start.isBefore(boxEnd) && end.isAfter(boxEnd);
                        })
                ).isPresent();
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
