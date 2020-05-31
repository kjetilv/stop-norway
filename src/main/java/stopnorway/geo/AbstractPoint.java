package stopnorway.geo;

import java.util.Objects;

import static java.lang.Math.*;
import static stopnorway.geo.Unit.MM;

public abstract class AbstractPoint implements Point {

    static final Distance DEGREE_LAT = Distance.of(110_574_235, MM);

    static final Distance DEGREE_LON = Distance.of(110_572_833, MM);

    private static final int R = 6371;

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
    public Distance distanceTo(Point point) {
        double phi1 = toRadians(lat());
        double phi2 = toRadians(point.lat());
        double dphi = toRadians(point.lat() - lat());
        double dlambda = toRadians(point.lon() - lon());
        double a = pow(sin(dphi / 2), 2) + cos(phi1) * cos(phi2) * pow(sin(dlambda / 2), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return Distance.of(R * c * 1000.D, Unit.M);
    }

    @Override
    public Box scaledBox(Scale scale) {
        return downTo(scale).box(upTo(scale));
    }
}
