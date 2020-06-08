package stopnorway.geo;

public class CodedPoint extends AbstractPoint {

    private final int dimension;

    private final int lat;

    private final int lon;

    public CodedPoint(int dimension, double lat, double lon) {
        this(dimension, scaled(lat, dimension), scaled(lon, dimension));
    }

    public CodedPoint(int dimension, int lat, int lon) {

        this.dimension = dimension;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new CodedPoint(
                    dimension,
                    lat - lat % dimension,
                    lon - lon % dimension);
        }
        return coded(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new CodedPoint(
                    dimension, lat - lat % dimension + dimension,
                    lon - lon % dimension + dimension);
        }
        return coded(
                Math.ceil(lat() * scale.getLat()) / scale.getLat(),
                Math.ceil(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public int compareTo(Point point) {
        if (point instanceof CodedPoint) {
            int latCompared = Integer.compare(lat, ((CodedPoint) point).lat);
            return latCompared != 0
                    ? latCompared
                    : Integer.compare(lon, ((CodedPoint) point).lon);
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
        return new CodedPoint(dimension, intLat(), intLon());
    }

    @Override
    public Point lon(Point lon) {
        return new CodedPoint(dimension, intLat(), intLon());
    }

    public int dimension() {
        return dimension;
    }

    public int intLon() {
        return lon;
    }

    public int intLat() {
        return lat;
    }

    @Override
    protected CodedPoint translate(Distance latTranslation, Distance lonTranslation) {
        return new CodedPoint(
                dimension,
                intLat() + scaled(getDeltaLat(latTranslation)),
                intLon() + scaled(getDeltaLon(lonTranslation)));
    }

    private Point coded(double lat, double lon) {
        int intLat = scaled(lat);
        int intLon = scaled(lon);
        return new CodedPoint(dimension, intLat, intLon);
    }

    private int scaled(double v) {
        return scaled(v, this.dimension);
    }

    private static int scaled(double v, int dimension) {
        return (int) Math.round(v * dimension);
    }
}
