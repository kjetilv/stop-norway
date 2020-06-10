package stopnorway.data;

import org.jetbrains.annotations.NotNull;
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
        return departureStop()
                .map(ScheduledStop::getTimespan)
                .map(Timespan::getStart);
    }

    public Optional<LocalTime> getEndTime() {
        return lastStop()
                .map(ScheduledStop::getTimespan)
                .map(Timespan::getEnd);
    }

    public Collection<ScheduledStop> getScheduledStops() {
        return Safe.list(scheduledStops);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                journeySpecification.getLineId().getId() + " " +
                journeySpecification.getLine().getName() + " @" +
                getStartTime().map(LocalTime::toString).orElse("unknown") +
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

    public Stream<Timespan> scaledTimespans(Duration duration) {
        return departureStop()
                .flatMap(depatureStop -> lastStop()
                        .map(lastStop -> depatureStop.getTimespan().combined(lastStop.getTimespan())))
                .stream()
                .flatMap(timespan -> timespan.timespans(duration));
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

    @NotNull
    private Optional<ScheduledStop> departureStop() {
        return scheduledStops.stream().findFirst();
    }

    @NotNull
    private Optional<ScheduledStop> lastStop() {
        return Optional.ofNullable(this.scheduledStops.getLast());
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
