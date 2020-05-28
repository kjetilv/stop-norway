package stopnorway.database;

public final class Sample {

    private static final double DEGREE_LAT_M = 110_574.235;
    private static final double DEGREE_LON_M = 110_572.833;
    private final Point point;
    private final double accuracyMeters;

    public Sample(Point point, double accuracyMeters) {

        this.point = point;
        this.accuracyMeters = accuracyMeters;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[=" + point + " ~" + accuracyMeters + "m]";
    }

    public Box getBox() {

        double latRadians = Math.toRadians(point.lat());

        double degreeLonMeters = DEGREE_LON_M * Math.cos(latRadians);

        double deltaLat = accuracyMeters / DEGREE_LAT_M;
        double deltaLon = accuracyMeters / degreeLonMeters;

        return new Box(
                new DoublePoint(point.lat() - deltaLat, point.lon() - deltaLon),
                new DoublePoint(point.lat() + deltaLat, point.lon() + deltaLon));
    }
}
