package stopnorway.database;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Point implements Serializable {

    private static final int R = 6371;

    private final double lat;

    private final double lon;

    public Point(String lat, String lon) {
        this(Double.parseDouble(Objects.requireNonNull(lat, "lat")),
                Double.parseDouble(Objects.requireNonNull(lon, "lon")));
    }

    public Point(double lat, double lon) {

        this.lat = lat;
        this.lon = lon;
    }

    public static List<Point> sequence(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return parse(str.split("\\s+"));
    }

    public Box scaledBox(Scale scale) {
        return new Box(downTo(scale), upTo(scale));
    }

    public Point downTo(Scale scale) {
        return new Point(
                Math.floor(lat * scale.getLat()) / scale.getLat(),
                Math.floor(lon * scale.getLon()) / scale.getLon());
    }

    public Point upTo(Scale scale) {
        return new Point(
                Math.ceil(lat * scale.getLat()) / scale.getLat(),
                Math.ceil(lon * scale.getLon()) / scale.getLon());
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

    public double distanceTo(Point point) {
        double phi1 = Math.toRadians(lat());
        double phi2 = Math.toRadians(point.lat());
        double dphi = Math.toRadians(point.lat() - lat);
        double dlambda = Math.toRadians(point.lon() - lon);
        double a =
                Math.pow(Math.sin(dphi / 2), 2) + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(dlambda / 2), 2);
        double c =
                2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;

    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Point &&
                Double.compare(((Point) o).lat, lat) == 0 &&
                Double.compare(((Point) o).lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return lat + ", " + lon;
    }

    private static List<Point> parse(String[] split) {
        List<Point> points = new ArrayList<>(split.length / 2);
        for (int i = 0; i < split.length; ) {
            points.add(new Point(split[i++], split[i++]));
        }
        return Collections.unmodifiableList(points);
    }
}
