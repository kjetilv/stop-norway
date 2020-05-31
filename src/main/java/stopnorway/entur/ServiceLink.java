package stopnorway.entur;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.geo.Point;
import stopnorway.util.MostlyOnce;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ServiceLink extends Entity {

    private final Id fromPoint;

    private final Id toPoint;

    private final float distance;

    private final Collection<LinkSequenceProjection> projections;

    private final Supplier<Optional<Point>> start;

    private final Supplier<Optional<Point>> end;

    public ServiceLink(Id id, Id fromPoint, Id toPoint, String distance, LinkSequenceProjection... projections) {
        this(id, fromPoint, toPoint, distance, Arrays.asList(projections));
    }

    public ServiceLink(Id id, Id fromPoint, Id toPoint, String distance, Collection<LinkSequenceProjection> projections) {
        super(id);
        this.fromPoint = Objects.requireNonNull(fromPoint, "fromPoint");
        this.toPoint = Objects.requireNonNull(toPoint, "toPoint");
        this.distance = distance == null ? .0F : Float.parseFloat(distance);
        this.projections = projections == null || projections.isEmpty() ? Collections.emptyList() : projections;

        this.start = MostlyOnce.get(() -> getFirst(LinkSequenceProjection::getStart));
        this.end = MostlyOnce.get(() -> getFirst(LinkSequenceProjection::getEnd));
    }

    public Id getFromPoint() {
        return fromPoint;
    }

    public Id getToPoint() {
        return toPoint;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, fromPoint, toPoint);
    }

    public float getDistance() {
        return distance;
    }

    public Collection<LinkSequenceProjection> getProjections() {
        return projections;
    }

    public Optional<Point> getStartPoint() {
        return start.get();
    }

    public Optional<Point> getEndPoint() {
        return end.get();
    }

    @NotNull
    private Optional<Point> getFirst(Function<LinkSequenceProjection, Optional<Point>> getStart) {
        return projections.stream()
                .map(getStart)
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(fromPoint).append("->").append(toPoint);
    }
}
