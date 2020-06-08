package stopnorway.geo;

public class CodedPoint extends AbstractPoint {

    static final int DEFAULT_POW10 = 6;

    static final int DEFAULT_DIMENSION = (int)Math.pow(10, DEFAULT_POW10);

    private final int dimension;

    private final int lat;

    private final int lon;

    public CodedPoint(int dimension, double lat, double lon) {
        this(dimension, scaledToInt(lat, dimension), scaledToInt(lon, dimension));
    }

    public CodedPoint(int dimension, int lat, int lon) {

        this.dimension = dimension;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return coded(lat - lat % dimension, lon - lon % dimension);
        }
        return coded(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return coded(lat - lat % dimension + dimension, lon - lon % dimension + dimension);
        }
        return coded(
                Math.ceil(lat() * scale.getLat()) / scale.getLat(),
                Math.ceil(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public int compareTo(Point point) {
        if (point instanceof CodedPoint) {
            int latCompared = Integer.compare(lat, ((CodedPoint) point).lat);
            return latCompared != 0 ? latCompared : Integer.compare(lon, ((CodedPoint) point).lon);
        }
        return super.compareTo(point);
    }

    @Override
    public boolean isSouthwestOf(Point point) {
        if (point instanceof CodedPoint) {
            CodedPoint cp = (CodedPoint) point;
            return lat < cp.lat && lon < cp.lon;
        }
        return super.isSouthwestOf(point);
    }

    @Override
    public double lat() {
        return 1.0d * this.lat / dimension;
    }

    @Override
    public double lon() {
        return 1.0d * this.lon / dimension;
    }

    @Override
    public Point lat(Point lat) {
        return coded(intLat(), intLon());
    }

    @Override
    public Point lon(Point lon) {
        return coded(intLat(), intLon());
    }

    int dimension() {
        return dimension;
    }

    int intLon() {
        return lon;
    }

    int intLat() {
        return lat;
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
        return new CodedPoint(dimension, lat, lon);
    }

    private int scaledToInt(double v) {
        return scaledToInt(v, this.dimension);
    }

    private static int scaledToInt(double v, int dimension) {
        return (int) Math.round(v * dimension);
    }
}
