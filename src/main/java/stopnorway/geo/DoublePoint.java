package stopnorway.geo;

public final class DoublePoint extends AbstractPoint {

    private final double lat;

    private final double lon;

    public DoublePoint(double lat, double lon) {

        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new DoublePoint(Math.floor(lat()), Math.floor(lon()));
        }
        return new DoublePoint(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new DoublePoint(Math.ceil(lat()), Math.ceil(lon()));
        }
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
    public boolean isSouthwestOf(Point point) {
        if (point instanceof DoublePoint) {
            DoublePoint dp = (DoublePoint) point;
            return lat < dp.lat && lon < dp.lon;
        }
        return super.isSouthwestOf(point);
    }

    @Override
    protected DoublePoint translate(Distance latTranslation, Distance lonTranslation) {

        return new DoublePoint(
                lat() + getDeltaLat(latTranslation),
                lon() + getDeltaLon(lonTranslation));
    }

    @Override
    public double lat() {
        return lat;
    }

    @Override
    public double lon() {
        return lon;
    }

}
