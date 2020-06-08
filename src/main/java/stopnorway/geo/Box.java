package stopnorway.geo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static stopnorway.geo.Points.point;

public final class Box implements Serializable, Comparable<Box> {

    private final Point min;

    private final Point max;

    private final double area;

    private Box(Point min, Point max) {

        Point[] points = minMax(min, max);
        this.min = points[0];
        this.max = points[1];

        this.area =
                this.min.distanceTo(this.max.lon(this.min)).toMeters() *
                        this.min.distanceTo(this.min.lon(this.max)).toMeters();
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
        return point(
                Math.min(min().intLat(), box.min().intLat()),
                Math.min(min().intLon(), box.min().intLon())
        ).box(point(
                Math.max(max().intLat(), box.max().intLat()),
                Math.max(max().intLon(), box.max().intLon())));
    }

    public Distance height() {
        return min().distanceTo(max().lon(min()));
    }

    public Distance width() {
        return min().distanceTo(min().lon(max()));
    }

    public double areaSqMeters() {
        return area;
    }

    public boolean overlaps(Box box) {
        return containsBox(box) || box.containsBox(this);
    }

    public boolean contains(Point point) {
        return point.isSouthwestOf(max()) && min().isSouthwestOf(point);
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

        return IntStream.range(0, latX)
                .mapToObj(i ->
                                  IntStream.range(0, lonX).mapToObj(j -> {
                                      double lat = min.min().lat() + i * scaleLatSpan;
                                      double lon = min.min().lon() + j * scaleLonSpan;

                                      Point minCorner = point(lat, lon);
                                      Point maxCorner = point(lat + scaleLatSpan, lon + scaleLonSpan);

                                      return minCorner.box(maxCorner);
                                  }))
                .flatMap(s -> s)
                .map(box -> box.scaledTo(scale))
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(Box box) {
        return min().compareTo(box.min());
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
        if (min.isSouthwestOf(max)) {
            return new Point[] { min, max };
        }

        int minLat = min.intLat();
        int minLon = min.intLon();
        int maxLon = max.intLon();
        int maxLat = max.intLat();

        return new Point[] {
                point(Math.min(minLat, maxLat), Math.min(minLon, maxLon)),
                point(Math.max(minLat, maxLat), Math.max(minLon, maxLon))
        };
    }
}
