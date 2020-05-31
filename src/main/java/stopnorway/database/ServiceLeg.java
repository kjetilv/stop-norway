package stopnorway.database;

import stopnorway.entur.LinkSequenceProjection;
import stopnorway.geo.Point;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;
import stopnorway.util.MostlyOnce;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ServiceLeg implements Serializable {

    private final ScheduledStopPoint from;

    private final ScheduledStopPoint to;

    private final ServiceLink link;

    private final Supplier<Box> box;

    public ServiceLeg(ScheduledStopPoint from, ScheduledStopPoint to, ServiceLink link) {

        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.link = Objects.requireNonNull(link, "link");

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

    public Optional<Point> getEndPoint() {
        return this.link.getStartPoint();
    }

    public ServiceLink getLink() {
        return link;
    }

    public Box getBox() {
        return box.get();
    }

    public Collection<Box> scaledBoxes(Scale scale) {
        return link.getProjections().stream()
                .flatMap(linkSequenceProjection -> linkSequenceProjection.getBoxes(scale))
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
}
