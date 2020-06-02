package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Named;
import stopnorway.util.Accept;

import java.util.Collection;
import java.util.function.Consumer;

public class JourneyPattern extends Entity implements Named {

    private final String name;

    private final Id routeRef;

    private final Collection<StopPointInJourneyPattern> pointsInSequence;

    private final Collection<ServiceLinkInJourneyPattern> linksInSequence;

    public JourneyPattern(
            Id id,
            String name,
            Id routeRef,
            Collection<StopPointInJourneyPattern> pointsInSequence,
            Collection<ServiceLinkInJourneyPattern> linksInSequence
    ) {
        super(id);
        this.name = name;
        this.routeRef = routeRef;
        this.pointsInSequence = Accept.list(pointsInSequence);
        this.linksInSequence = Accept.list(linksInSequence);
    }

    @Override
    public String getName() {
        return name;
    }

    public Id getRouteRef() {
        return routeRef;
    }

    public Collection<ServiceLinkInJourneyPattern> getLinksInSequence() {
        return linksInSequence;
    }

    public Collection<StopPointInJourneyPattern> getPointsInSequence() {
        return pointsInSequence;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, pointsInSequence);
        hash(h, name);
        hash(h, routeRef);
    }
}
