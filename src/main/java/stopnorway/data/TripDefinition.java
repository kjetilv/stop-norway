package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.AbstractBoxed;
import stopnorway.database.Boxed;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLinkInJourneyPattern;
import stopnorway.entur.StopPointInJourneyPattern;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;
import stopnorway.util.Accept;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TripDefinition extends AbstractBoxed implements Serializable, Boxed, Named {

    private final Id journeyPatternId;

    private final String name;

    private final Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints;

    private final Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> serviceLegs;

    public TripDefinition(
            Id journeyPatternId,
            String name,
            List<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints,
            Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> serviceLegs
    ) {
        super(journeyPatternId);
        this.journeyPatternId = journeyPatternId;
        this.name = name;
        this.stopPoints = Accept.list(stopPoints);
        this.serviceLegs = Accept.list(serviceLegs);
    }

    @Override
    public String toString() {
        Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints = this.stopPoints;
        int size = stopPoints.size();
        return getClass().getSimpleName() + "[" + name + " stopPoints:" +
                (size > 6
                        ? str(stopPoints, 3) + " (" + (size - 6) + " more)" + str(stopPoints, -3)
                        : str(stopPoints, 0) +
                                "]");
    }

    public Collection<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> getStopPoints() {
        return stopPoints;
    }

    public Stream<Box> scaledBoxes(Scale scale) {
        return serviceLegs.stream()
                .map(Map.Entry::getValue)
                .flatMap(serviceLeg ->
                                 serviceLeg.scaledBoxes(scale))
                .distinct()
                .sorted();
    }

    public Id getJourneyPatternId() {
        return journeyPatternId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected Optional<Box> computeBox() {
        return serviceLegs.stream()
                .map(Map.Entry::getValue)
                .map(Boxed::getBox)
                .reduce(Box::combined);
    }

    @NotNull
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
