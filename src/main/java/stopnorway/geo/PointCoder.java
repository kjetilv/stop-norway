package stopnorway.geo;

import stopnorway.Database;

import java.util.Objects;

public final class PointCoder {

    private final Point min;

    private final Point max;

    private final int digits;

    private final int dimension;


    public PointCoder(Box box, int digits) {
        this(Objects.requireNonNull(box, "box").min(), box.max(), digits);
    }

    public PointCoder(Point min, Point max, int digits) {
        if (digits > 6) {
            throw new IllegalArgumentException("Too many digits, <=6 allowed: " + digits);
        }
        this.dimension = (int) Math.round(Math.pow(10.0D, digits));
        this.min = min.downTo(Scale.INTEGER);
        this.max = max.upTo(Scale.INTEGER);
        this.digits = digits;
        long latPointsLong = Math.round(dimension * this.max.lat() - this.min.lat());
        long lonPointsLong = Math.round(dimension * this.max.lon() - this.min.lon());
        if (latPointsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("To many lat's: " + digits + " digits " + this.min + " -> " + this.max);
        }
        if (lonPointsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("To many lon's: " + digits + " digits " + this.min + " -> " + this.max);
        }
    }

    public final Point coded(double lat, double lon) {
        int intLat = scaledDouble(lat);
        int intLon = scaledDouble(lon);
        return new CodedPoint(this, intLat, intLon);
    }

    public int addLat(CodedPoint codedPoint, double delta) {
        return codedPoint.intLat() + scaledDouble(delta);
    }

    public int addLon(CodedPoint codedPoint, double delta) {
        return codedPoint.intLon() + scaledDouble(delta);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + min + " => " + max + ", digits=" + digits + "]";
    }

    public int intLat(Point point) {
        return point instanceof CodedPoint
                ? ((CodedPoint) point).intLat()
                : scaledDouble(point.lat());
    }

    public int intLon(Point point) {
        return point instanceof CodedPoint ?
                ((CodedPoint) point).intLon()
                : scaledDouble(point.lon());
    }

    int scaledLat(int lat, Scale scale) {
        int decimals = lat - (lat / dimension * dimension);
        return lat / scale.getLat() * scale.getLat();
    }

    int scaledLon(int lon, Scale scale) {
        return lon / scale.getLon() * scale.getLon();
    }

    double toDouble(int scaled) {
        return 1.0d * scaled / getDimension();
    }

    int getDimension() {
        return dimension;
    }

    private int scaledDouble(double v) {
        return (int) Math.round(v * getDimension());
    }
}
