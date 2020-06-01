package stopnorway.entur;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.util.Accept;
import stopnorway.util.MostlyOnce;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LinkSequenceProjection extends Entity {

    private final List<Point> trajectory;

    private final Supplier<Optional<Box>> box;

    public LinkSequenceProjection(Id id, Point... points) {
        this(id, Arrays.asList(points));
    }

    public LinkSequenceProjection(Id id, List<Point> trajectory) {
        super(id);
        this.trajectory = Accept.list(trajectory);
        this.box = MostlyOnce.get(() ->
                computeMin().flatMap(min ->
                        computeMax().map(min::box)));
    }

    public Optional<Box> getBox() {
        return box.get();
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        trajectory.forEach(point ->
                hash(h, point.lat(), point.lon()));
    }

    public Collection<Point> getTrajectory() {
        return trajectory;
    }

    public Optional<Point> getStart() {
        return trajectory.stream().findFirst();
    }

    public Optional<Point> getEnd() {
        return trajectory.isEmpty() ? Optional.empty() : Optional.of(trajectory.get(trajectory.size() - 1));
    }

    @NotNull
    private Optional<Point> computeMax() {
        return this.trajectory.stream().max(Comparator.comparing(Point::lat))
                .flatMap(lat ->
                        this.trajectory.stream().max(Comparator.comparing(Point::lon)).map(lat::lon));
    }

    @NotNull
    private Optional<Point> computeMin() {
        return this.trajectory.stream().min(Comparator.comparing(Point::lat))
                .flatMap(lat ->
                        this.trajectory.stream().min(Comparator.comparing(Point::lon)).map(lat::lon));
    }
}
