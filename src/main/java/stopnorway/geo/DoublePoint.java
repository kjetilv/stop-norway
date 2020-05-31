package stopnorway.geo;

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
    public Point translate(Translation translation) {
        long l = translation.getDistance().toMillis();
        Distance latTranslation = Distance.of(translation.getDirection().lat().apply(l), Unit.MM);
        Distance lonTranslation = Distance.of(translation.getDirection().lon().apply(l), Unit.MM);

        double latRadians = Math.toRadians(lat());

        Distance degreeLonMeters =
                Distance.of(DEGREE_LON.toMeters() * Math.cos(latRadians), Unit.M);

        double deltaLat = 1.0d * latTranslation.toMillis() / DEGREE_LAT.toMillis();
        double deltaLon = 1.0d * lonTranslation.toMillis() / degreeLonMeters.toMillis();

        return new DoublePoint(lat() + deltaLat, lon() + deltaLon);
    }

    @Override
    public Box squareBox(Distance sideLength) {
        Point max = this
                .translate(Translation.towards(Direction.NORTH, sideLength))
                .translate(Translation.towards(Direction.EAST, sideLength));
        return this.box(max);
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
