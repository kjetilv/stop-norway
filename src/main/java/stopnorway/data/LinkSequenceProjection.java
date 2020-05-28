package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import stopnorway.database.*;
import stopnorway.util.MostlyOnce;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class LinkSequenceProjection extends Entity {

    private final List<Point> trajectory;

    private final Supplier<Optional<Point>> min;

    private final Supplier<Optional<Point>> max;

    private final Supplier<Optional<Box>> box;

    public LinkSequenceProjection(Id id, Point... points) {
        this(id, Arrays.asList(points));
    }

    public LinkSequenceProjection(Id id, List<Point> trajectory) {
        super(id);
        this.trajectory = trajectory == null || trajectory.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(trajectory);
        min = MostlyOnce.get(this::computeMin);
        max = MostlyOnce.get(this::computeMax);
        box = MostlyOnce.get(() ->
                min.get().flatMap(min ->
                        max.get().map(max ->
                                new Box(min, max))));
    }

    public Stream<Box> getBoxes(Scale scale) {
        return trajectory.stream()
                .map(p ->
                        p.scaledBox(scale))
                .distinct();
    }

    public Optional<Box> getBox() {
        return box.get();
    }

    public Optional<Point> getMin() {
        return min.get();
    }

    public Optional<Point> getMax() {
        return max.get();
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

    public long getTrajectoryLength() {
        return trajectory.size();
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
