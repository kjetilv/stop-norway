package stopnorway.database;

import stopnorway.util.MostlyOnce;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Box implements Serializable {

    private final Point min;

    private final Point max;

    private final Supplier<Double> area;

    public Box(Point min, Point max) {

        this.min = min(min, max);
        this.max = max(min, max);

        area = MostlyOnce.get(() ->
                this.min.distanceTo(new Point(this.max.lat(), this.min.lon())) *
                        this.min.distanceTo(new Point(this.min.lat(), this.max.lon())));
    }

    public Point max() {
        return max;
    }

    public Point min() {
        return min;
    }

    public double latSpan() {
        return max.lat() - min.lat();
    }

    public double lonSpan() {
        return max.lon() - min.lon();
    }

    public Box scaledTo(Scale scale) {
        return new Box(
                min().downTo(scale),
                max().upTo(scale));
    }

    public Box combined(Box box) {
        return new Box(
                new Point(
                        Math.min(min().lat(), box.min().lat()),
                        Math.min(min().lon(), box.min().lon())),
                new Point(
                        Math.max(max().lat(), box.max().lat()),
                        Math.max(max().lon(), box.max().lon())));
    }

    public double heightMeters() {
        return min().distanceTo(new Point(max.lat(), min.lon()));
    }

    public double widthMeters() {
        return min().distanceTo(new Point(min().lat(), max.lon()));
    }

    public double areaSqMeters() {
        return area.get();
    }

    public boolean overlaps(Box box) {
        return containsBox(box) || box.containsBox(this);
    }

    public boolean contains(Point point) {
        double lat = point.lat();
        double lon = point.lon();
        return min.lat() <= lat && lat < max.lat() &&
                min.lon() <= lon && lon < max.lon();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + min + "/" + max + "]";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Box &&
                Objects.equals(min, ((Box) o).min) &&
                Objects.equals(max, ((Box) o).max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    public Collection<Box> getScaledBoxes(Scale scale) {
        Box min = min().scaledBox(scale);
        Box max = max().scaledBox(scale);
        Box total = min.combined(max);

        double latSpan = total.latSpan();
        double lonSpan = total.lonSpan();

        double scaleLatSpan = 1.0D / scale.getLat();
        double scaleLonSpan = 1.0D / scale.getLon();

        int latX = (int) Math.round(latSpan / scaleLatSpan);
        int lonX = (int) Math.round(lonSpan / scaleLonSpan);

        return IntStream.range(0, latX).mapToObj(i ->
                IntStream.range(0, lonX).mapToObj(j -> {
                    double lat = min.min().lat() + i * scaleLatSpan;
                    double lon = min.min().lon() + j * scaleLonSpan;
                    return new Box(
                            new Point(
                                    lat,
                                    lon),
                            new Point(lat + scaleLatSpan, lon + scaleLonSpan));
                }))
                .flatMap(s -> s)
                .map(box -> box.scaledTo(scale))
                .collect(Collectors.toList());
    }

    private boolean containsBox(Box box) {
        return contains(box.min()) ||
                contains(box.max()) ||
                contains(new Point(box.max().lat(), box.min().lon())) ||
                contains(new Point(box.min().lat(), box.max().lon()));
    }

    private static Point min(Point min, Point max) {
        return new Point(
                Math.min(min.lat(), max.lat()),
                Math.min(min.lon(), max.lon()));
    }

    private static Point max(Point min, Point max) {
        return new Point(
                Math.max(min.lat(), max.lat()),
                Math.max(min.lon(), max.lon()));
    }
}
