package stopnorway.database;

import stopnorway.entur.LinkSequenceProjection;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;
import stopnorway.util.MostlyOnce;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ServiceLeg implements Serializable {

    private final ScheduledStopPoint from;

    private final ScheduledStopPoint to;

    private final ServiceLink link;

    private final Integer order;
    private final Supplier<Box> box;

    public ServiceLeg(ScheduledStopPoint from, ScheduledStopPoint to, ServiceLink link) {
        this(from, to, link, null);
    }

    public ServiceLeg(ScheduledStopPoint from, ScheduledStopPoint to, ServiceLink link, Integer order) {

        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.link = Objects.requireNonNull(link, "link");
        this.order = order;

        box = MostlyOnce.get(() ->
                this.link.getProjections().stream()
                        .map(LinkSequenceProjection::getBox)
                        .flatMap(Optional::stream)
                        .reduce(Box::combined)
                        .orElse(null));
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

    public Integer getOrder() {
        return Objects.requireNonNull(order, "order");
    }

    public Optional<Point> getEndPoint() {
        return this.link.getStartPoint();
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

    public Box getBox() {
        return box.get();
    }

    public Collection<Box> scaledBoxes(Scale scale) {
        return link.getProjections().stream()
                .flatMap(linkSequenceProjection ->
                        getBoxes(linkSequenceProjection, scale))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[" + (from.getName() == null ? from : from.getName()) +
                " => " + (to.getName() == null ? to : to.getName()) +
                ": " + link.getDistance() +
                "m]";
    }

    public boolean overlaps(Box box) {
        return getBox().overlaps(box);
    }

    private Stream<Box> getBoxes(LinkSequenceProjection linkSequenceProjection, Scale scale) {
        return linkSequenceProjection.getTrajectory().stream()
                .map(p ->
                        p.scaledBox(scale))
                .distinct();
    }
}
