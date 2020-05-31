package stopnorway.geo;

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

    private Box(Point min, Point max) {

        Point[] points = minMax(min, max);
        this.min = points[0];
        this.max = points[1];

        this.area = MostlyOnce.get(() ->
                this.min.distanceTo(this.max.lon(this.min)).toMeters() *
                        this.min.distanceTo(this.min.lon(this.max)).toMeters());
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
        return min().downTo(scale).box(max().upTo(scale));
    }

    public Box combined(Box box) {
        return Points.point(
                Math.min(min().lat(), box.min().lat()),
                Math.min(min().lon(), box.min().lon())
        ).box(Points.point(
                Math.max(max().lat(), box.max().lat()),
                Math.max(max().lon(), box.max().lon())));
    }

    public Distance height() {
        return min().distanceTo(max().lon(min()));
    }

    public Distance width() {
        return min().distanceTo(min().lon(max()));
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
                    return Points.point(lat, lon).box(
                            Points.point(lat + scaleLatSpan, lon + scaleLonSpan));
                }))
                .flatMap(s -> s)
                .map(box -> box.scaledTo(scale))
                .collect(Collectors.toList());
    }

    static Box box(Point min, Point max) {
        return new Box(min, max);
    }

    private boolean containsBox(Box box) {
        return contains(box.min()) ||
                contains(box.max()) ||
                contains(box.max().lon(box.min())) ||
                contains(box.min().lon(box.max()));
    }

    private static Point[] minMax(Point min, Point max) {
        double minLat = min.lat();
        double minLon = min.lon();
        double maxLon = max.lon();
        double maxLat = max.lat();
        return minLat < maxLat && minLon < maxLon ? new Point[]{min, max}
                : new Point[]{
                Points.point(Math.min(minLat, maxLat), Math.min(minLon, maxLon)),
                Points.point(Math.max(min.lat(), max.lat()), Math.max(min.lon(), max.lon()))
        };
    }
}
