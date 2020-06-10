package stopnorway.entur;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.geo.Point;
import stopnorway.util.Accept;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ServiceLink extends Entity {

    private final Id fromPoint;

    private final Id toPoint;

    private final String distance;

    private final Collection<LinkSequenceProjection> projections;

    private final Point start;

    private final Point end;

    public ServiceLink(Id id, Id fromPoint, Id toPoint, String distance, LinkSequenceProjection... projections) {
        this(id, fromPoint, toPoint, distance, Arrays.asList(projections));
    }

    public ServiceLink(
            Id id,
            Id fromPoint,
            Id toPoint,
            String distance,
            Collection<LinkSequenceProjection> projections
    ) {
        super(id);
        this.fromPoint = Objects.requireNonNull(fromPoint, "fromPoint");
        this.toPoint = Objects.requireNonNull(toPoint, "toPoint");
        this.distance = distance;
        this.projections = Accept.list(projections);

        this.start = getFirst(LinkSequenceProjection::getStart).orElse(null);
        this.end = getFirst(LinkSequenceProjection::getEnd).orElse(null);
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

    public String getDistance() {
        return distance;
    }

    public Collection<LinkSequenceProjection> getProjections() {
        return projections;
    }

    public Optional<Point> getStartPoint() {
        return Optional.ofNullable(start);
    }

    public Optional<Point> getEndPoint() {
        return Optional.ofNullable(end);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(fromPoint).append("->").append(toPoint);
    }

    @NotNull
    private Optional<Point> getFirst(Function<LinkSequenceProjection, Optional<Point>> getStart) {
        return projections.stream()
                .map(getStart)
                .flatMap(Optional::stream)
                .findFirst();
    }
}
