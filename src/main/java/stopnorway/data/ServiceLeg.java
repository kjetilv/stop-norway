package stopnorway.data;

import stopnorway.database.*;
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

public final class ServiceLeg extends AbstractIdentified implements Serializable, Ordered, Boxed {

    private final Id serviceLinkId;

    private final ScheduledStopPoint from;

    private final ScheduledStopPoint to;

    private final ServiceLink serviceLink;

    private final Integer order;

    private final Box box;

    public ServiceLeg(Id serviceLinkId, ScheduledStopPoint from, ScheduledStopPoint to, ServiceLink serviceLink) {
        this(serviceLinkId, from, to, serviceLink, null);
    }

    public ServiceLeg(
            Id serviceLinkId,
            ScheduledStopPoint from,
            ScheduledStopPoint to,
            ServiceLink serviceLink,
            Integer order
    ) {
        super(serviceLinkId);
        this.serviceLinkId = serviceLinkId;
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.serviceLink = Objects.requireNonNull(serviceLink, "link");
        this.order = order;
        this.box = this.serviceLink.getProjections().stream()
                .map(LinkSequenceProjection::getBox)
                .flatMap(Optional::stream)
                .reduce(Box::combined)
                .orElse(null);
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
        return this.serviceLink.getStartPoint();
    }

    public Optional<Point> getEndPoint() {
        return this.serviceLink.getEndPoint();
    }

    public int getOrder() {
        return Objects.requireNonNull(order, "order");
    }

    public ServiceLink getServiceLink() {
        return serviceLink;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[" + (from.getName() == null ? from : from.getName()) +
                " => " + (to.getName() == null ? to : to.getName()) +
                ": " + serviceLink.getDistance() +
                "m]";
    }

    @Override
    public Optional<Box> getBox() {
        return Optional.ofNullable(box);
    }

    Stream<Box> scaledBoxes(Scale scale) {
        return serviceLink.getProjections().stream()
                .flatMap(toScaled(scale))
                .distinct();
    }

    private Function<LinkSequenceProjection, Stream<Box>> toScaled(Scale scale) {
        return linkSequenceProjection -> linkSequenceProjection.getTrajectory().stream()
                .map(p -> p.scaledBox(scale))
                .distinct();
    }
}
