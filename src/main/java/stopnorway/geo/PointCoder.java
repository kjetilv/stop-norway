package stopnorway.geo;

import java.io.Serializable;
import java.util.Objects;

public final class PointCoder implements Serializable {

    private final int dimension;

    public PointCoder(Box box, int digits) {
        this(Objects.requireNonNull(box, "box").min(), box.max(), digits);
    }

    public PointCoder(Point min, Point max, int digits) {
        this.dimension = verifiedDimension(digits, min, max);
    }

    public Point coded(double lat, double lon) {
        return new CodedPoint(dimension, lat, lon);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + dimension + "]";
    }

    private static int verifiedDimension(int digits, Point min, Point max) {
        if (digits > 6) {
            throw new IllegalArgumentException("Too many digits, <=6 allowed: " + digits);
        }
        int dimension = (int) Math.round(Math.pow(10.0D, digits));
        Point minDown = min.downTo(Scale.INTEGER);
        Point maxUp = max.upTo(Scale.INTEGER);
        long latPointsLong = Math.round(dimension * maxUp.lat() - minDown.lat());
        long lonPointsLong = Math.round(dimension * maxUp.lon() - minDown.lon());
        if (latPointsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many lat's: " + digits + " digits " + minDown + " -> " + maxUp);
        }
        if (lonPointsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many lon's: " + digits + " digits " + minDown + " -> " + maxUp);
        }
        return dimension;
    }
}
