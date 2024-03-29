package stopnorway.entur;


import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.util.Accept;

import java.util.*;
import java.util.function.Consumer;

public final class LinkSequenceProjection extends Entity {

    private final List<Point> trajectory;

    private final Box box;

    public LinkSequenceProjection(Id id, Point... trajectory) {
        this(id, Arrays.asList(trajectory));
    }

    public LinkSequenceProjection(Id id, Collection<Point> trajectory) {
        super(id);
        this.trajectory = Accept.list(trajectory);
        this.box = computeMin()
                .flatMap(min -> computeMax().map(min::box))
                .orElse(null);
    }

    public Optional<Box> getBox() {
        return Optional.ofNullable(box);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        trajectory.forEach(point -> hash(h, point.lat(), point.lon()));
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


    private Optional<Point> computeMax() {
        return this.trajectory.stream()
                .max(Comparator.comparing(Point::lat))
                .flatMap(lat -> this.trajectory.stream().max(Comparator.comparing(Point::lon)).map(lat::lon));
    }


    private Optional<Point> computeMin() {
        return this.trajectory.stream()
                .min(Comparator.comparing(Point::lat))
                .flatMap(lat -> this.trajectory.stream().min(Comparator.comparing(Point::lon)).map(lat::lon));
    }
}
