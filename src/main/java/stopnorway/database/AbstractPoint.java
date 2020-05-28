package stopnorway.database;

import java.util.Objects;

import static java.lang.Math.*;

abstract class AbstractPoint implements Point {

    private static final int R = 6371;
    private static final int K = 1000;

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Point &&
                Double.compare(((Point) o).lat(), lat()) == 0 &&
                Double.compare(((Point) o).lon(), lon()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat(), lon());
    }

    @Override
    public String toString() {
        return lat() + ", " + lon();
    }

    @Override
    public double distanceTo(Point point) {
        double phi1 = toRadians(lat());
        double phi2 = toRadians(point.lat());
        double dphi = toRadians(point.lat() - lat());
        double dlambda = toRadians(point.lon() - lon());
        double a = pow(sin(dphi / 2), 2) + cos(phi1) * cos(phi2) * pow(sin(dlambda / 2), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return R * c * K;
    }

    @Override
    public Box scaledBox(Scale scale) {
        return new Box(downTo(scale), upTo(scale));
    }
}
