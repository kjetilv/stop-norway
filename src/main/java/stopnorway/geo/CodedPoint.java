package stopnorway.geo;

public class CodedPoint extends AbstractPoint {

    private final PointCoder code;

    private final int lat;

    private final int lon;

    public CodedPoint(PointCoder code, int lat, int lon) {

        this.code = code;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public Point downTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new CodedPoint(
                    code,
                    lat - lat % code.getDimension(),
                    lon - lon % code.getDimension());
        }
        return code.coded(
                Math.floor(lat() * scale.getLat()) / scale.getLat(),
                Math.floor(lon() * scale.getLon()) / scale.getLon());
    }

    @Override
    public Point upTo(Scale scale) {
        if (scale == Scale.INTEGER) {
            return new CodedPoint(
                    code,
                    lat - lat % code.getDimension() + code.getDimension(),
                    lon - lon % code.getDimension() + code.getDimension());
        }
        return code.coded(
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
    public double lat() {
        return 1.0d * this.lat / code.getDimension();
    }

    @Override
    public double lon() {
        return 1.0d * this.lon / code.getDimension();
    }

    @Override
    public Point lat(Point lat) {
        return new CodedPoint(code, code.intLat(lat), code.intLon(this));
    }

    @Override
    public Point lon(Point lon) {
        return new CodedPoint(code, code.intLat(this), code.intLon(lon));
    }

    int intLon() {
        return lon;
    }

    int intLat() {
        return lat;
    }

    @Override
    protected CodedPoint translate(Distance latTranslation, Distance lonTranslation) {
        return new CodedPoint(
                code,
                code.addLat(this, getDeltaLat(latTranslation)),
                code.addLon(this, getDeltaLon(lonTranslation)));
    }
}
