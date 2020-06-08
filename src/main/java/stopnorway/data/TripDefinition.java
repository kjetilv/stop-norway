package stopnorway.data;

import stopnorway.database.AbstractIdentified;
import stopnorway.database.Boxed;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.*;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;
import stopnorway.util.Accept;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TripDefinition extends AbstractIdentified implements Serializable, Boxed, Named {

    private final String name;

    private final Route route;

    private final Line line;

    private final Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints;

    private final Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> serviceLegs;

    private final Box box;

    public TripDefinition(
            Id journeyPatternId,
            String name,
            Route route,
            Line line,
            Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints,
            Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> serviceLegs
    ) {
        super(journeyPatternId);
        this.name = name;
        this.route = route;
        this.line = line;
        this.stopPoints = Accept.list(stopPoints);
        this.serviceLegs = Accept.list(serviceLegs);
        this.box = this.serviceLegs.stream()
                .map(Map.Entry::getValue)
                .map(Boxed::getBox)
                .flatMap(Optional::stream)
                .reduce(Box::combined)
                .orElse(null);
    }

    public Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> getStopPoints() {
        return stopPoints;
    }

    public Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> getServiceLegs() {
        return serviceLegs;
    }

    public Route getRoute() {
        return route;
    }

    public Line getLine() {
        return line;
    }

    public Id getLineId() {
        return line.getId();
    }

    public Id getJourneyPatternId() {
        return getId();
    }

    public Collection<Point> points() {
        return serviceLegs.stream()
                .map(Map.Entry::getValue)
                .map(ServiceLeg::getServiceLink)
                .map(ServiceLink::getProjections)
                .flatMap(Collection::stream)
                .map(LinkSequenceProjection::getTrajectory)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Stream<Box> scaledBoxes(Scale scale) {
        return serviceLegs.stream()
                .map(Map.Entry::getValue)
                .flatMap(serviceLeg -> serviceLeg.scaledBoxes(scale))
                .distinct()
                .sorted();
    }

    @Override
    public String toString() {
        Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints = this.stopPoints;
        int size = stopPoints.size();
        return getClass().getSimpleName() + "[" +
                (line == null ? "<no line>" : line.getId().getId() + " " + line.getName()) + " " +
                (line == null ? "" : " (" + line.getTransportMode()) + ") " +
                (route == null ? "" : " " + route.getDirectionType() + " ") +
                name + " stopPoints:" + print(stopPoints, size) + "]";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Box> getBox() {
        return Optional.ofNullable(box);
    }

    private String print(Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints, int max) {
        int size = stopPoints.size();
        if (size == 0) {
            return "N/A";
        }
        int half = max / 2;
        if (size > max) {
            return str(stopPoints, half) +
                    " (" + (size - max) + " more)" +
                    str(stopPoints, -half);
        }
        return str(stopPoints, 0);
    }

    private <T extends Named> String str(Collection<? extends Map.Entry<?, T>> entries, int slice) {
        Stream<? extends Map.Entry<?, T>> stream = entries.stream();
        return (slice > 0
                ? stream.limit(slice)
                : stream.skip(entries.size() + slice)
        ).map(Map.Entry::getValue)
                .map(Named::getName)
                .collect(Collectors.joining(" =>"));
    }
}
