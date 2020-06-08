package stopnorway.geo;

public class CodedPoint extends AbstractPoint {

    public static final int DEFAULT_POW10 = 6;

    public static final int DEFAULT_DIMENSION = (int) Math.pow(10, DEFAULT_POW10);

    private final int lat;

    private final int lon;

    public CodedPoint(double lat, double lon) {
        this(
                scaledToInt(lat, DEFAULT_DIMENSION),
                scaledToInt(lon, DEFAULT_DIMENSION));
    }

    public CodedPoint(int lat, int lon) {

        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return coded(lat - lat % DEFAULT_DIMENSION, lon - lon % DEFAULT_DIMENSION);
        }
        return coded(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return coded(lat - lat % DEFAULT_DIMENSION + DEFAULT_DIMENSION, lon - lon % DEFAULT_DIMENSION +
                    DEFAULT_DIMENSION);
        }
        return coded(
                Math.ceil(lat() * scale.getLat()) / scale.getLat(),
                Math.ceil(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public int compareTo(Point point) {
        int latCompared = Integer.compare(lat, ((CodedPoint) point).lat);
        return latCompared != 0 ? latCompared : Integer.compare(lon, ((CodedPoint) point).lon);
    }

    @Override
    public boolean isSouthwestOf(Point point) {
        CodedPoint cp = (CodedPoint) point;
        return lat < cp.lat && lon < cp.lon;
    }

    @Override
    public double lat() {
        return 1.0d * this.lat / DEFAULT_DIMENSION;
    }

    @Override
    public double lon() {
        return 1.0d * this.lon / DEFAULT_DIMENSION;
    }

    @Override
    public Point lat(Point lat) {
        return coded(lat.intLat(), intLon());
    }

    @Override
    public Point lon(Point lon) {
        return coded(intLat(), lon.intLon());
    }

    @Override
    public int intLat() {
        return lat;
    }

    @Override
    public int intLon() {
        return lon;
    }

    int dimension() {
        return DEFAULT_DIMENSION;
    }

    @Override
    protected Point translate(Distance latTranslation, Distance lonTranslation) {
        return coded(
                intLat() + scaledToInt(getDeltaLat(latTranslation)),
                intLon() + scaledToInt(getDeltaLon(lonTranslation)));
    }

    private Point coded(double lat, double lon) {
        return coded(scaledToInt(lat), scaledToInt(lon));
    }

    private Point coded(int lat, int lon) {
        return new CodedPoint(lat, lon);
    }

    private int scaledToInt(double v) {
        return scaledToInt(v, DEFAULT_DIMENSION);
    }

    private static int scaledToInt(double v, int dimension) {
        return (int) Math.round(v * dimension);
    }
}
