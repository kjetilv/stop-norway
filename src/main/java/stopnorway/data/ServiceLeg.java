package stopnorway.data;

import stopnorway.database.AbstractBoxed;
import stopnorway.database.Id;
import stopnorway.database.Ordered;
import stopnorway.entur.LinkSequenceProjection;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ServiceLeg extends AbstractBoxed implements Serializable, Ordered {

    private final Id serviceLinkId;

    private final ScheduledStopPoint from;

    private final ScheduledStopPoint to;

    private final ServiceLink link;

    private final Integer order;

    public ServiceLeg(Id serviceLinkId, ScheduledStopPoint from, ScheduledStopPoint to, ServiceLink link) {
        this(serviceLinkId, from, to, link, null);
    }

    public ServiceLeg(
            Id serviceLinkId,
            ScheduledStopPoint from,
            ScheduledStopPoint to,
            ServiceLink link,
            Integer order
    ) {
        super(serviceLinkId);
        this.serviceLinkId = serviceLinkId;
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.link = Objects.requireNonNull(link, "link");
        this.order = order;
    }

    public Id getServiceLinkId() {
        return serviceLinkId;
    }

    public ScheduledStopPoint getFrom() {
        return from;
    }

    public ScheduledStopPoint getTo() {
        return to;
    }

    public Optional<Point> getStartPoint() {
        return this.link.getStartPoint();
    }

    public Optional<Point> getEndPoint() {
        return this.link.getEndPoint();
    }

    public int getOrder() {
        return Objects.requireNonNull(order, "order");
    }

    public ServiceLink getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ServiceLeg &&
                Objects.equals(from, ((ServiceLeg) o).from) &&
                Objects.equals(to, ((ServiceLeg) o).to) &&
                Objects.equals(link, ((ServiceLeg) o).link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, link);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[" + (from.getName() == null ? from : from.getName()) +
                " => " + (to.getName() == null ? to : to.getName()) +
                ": " + link.getDistance() +
                "m]";
    }

    Stream<Box> scaledBoxes(Scale scale) {
        return link.getProjections().stream()
                .flatMap(toScaled(scale))
                .distinct();
    }

    @Override
    protected Optional<Box> computeBox() {
        return this.link.getProjections().stream()
                .map(LinkSequenceProjection::getBox)
                .flatMap(Optional::stream)
                .reduce(Box::combined);
    }

    private Function<LinkSequenceProjection, Stream<Box>> toScaled(Scale scale) {
        return linkSequenceProjection -> linkSequenceProjection.getTrajectory().stream()
                .map(p -> p.scaledBox(scale))
                .distinct();
    }
}
