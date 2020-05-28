package stopnorway.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DoublePoint extends AbstractPoint {

    private final double lat;

    private final double lon;

    public DoublePoint(String lat, String lon) {
        this(Double.parseDouble(Objects.requireNonNull(lat, "lat")),
                Double.parseDouble(Objects.requireNonNull(lon, "lon")));
    }

    public DoublePoint(double lat, double lon) {

        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        return new DoublePoint(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        return new DoublePoint(
                Math.ceil(lat() * scale.getLat()) / scale.getLat(),
                Math.ceil(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point lat(Point lat) {
        return new DoublePoint(lat.lat(), lon);
    }

    @Override
    public Point lon(Point lon) {
        return new DoublePoint(lat, lon.lon());
    }

    @Override
    public double lat() {
        return lat;
    }

    @Override
    public double lon() {
        return lon;
    }

    static List<Point> parse(String[] split) {
        List<Point> points = new ArrayList<>(split.length / 2);
        for (int i = 0; i < split.length; ) {
            points.add(new DoublePoint(split[i++], split[i++]));
        }
        return Collections.unmodifiableList(points);
    }
}
