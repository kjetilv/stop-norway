package stopnorway.geo;

import java.util.Objects;

import static java.lang.Math.*;
import static stopnorway.geo.Unit.MM;

public abstract class AbstractPoint implements Point {

    static final Distance DEGREE_LAT = Distance.of(110_574_235, MM);

    static final Distance DEGREE_LON = Distance.of(110_572_833, MM);

    private static final Distance R = Distance.of(6_371_008_800L, MM);

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
        return lat() + "," + lon();
    }

    @Override
    public Distance distanceTo(Point point) {
        double phi1 = toRadians(lat());
        double phi2 = toRadians(point.lat());
        double dphi = toRadians(point.lat() - lat());
        double dlambda = toRadians(point.lon() - lon());
        double a = pow(sin(dphi / 2), 2) + cos(phi1) * cos(phi2) * pow(sin(dlambda / 2), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return Distance.of(R.toMillis() * c, Unit.MM);
    }

    @Override
    public final Point translate(Translation translation) {
        long l = translation.getDistance().toMillis();
        Distance latTranslation = Distance.of(translation.getDirection().lat().apply(l), MM);
        Distance lonTranslation = Distance.of(translation.getDirection().lon().apply(l), MM);

        return translate(latTranslation, lonTranslation);
    }

    protected abstract Point translate(Distance latTranslation, Distance lonTranslation);

    protected double getDeltaLon(Distance lonTranslation) {
        double latRadians = toRadians(lat());

        Distance degreeLonMeters =
                Distance.of(DEGREE_LON.toMeters() * cos(latRadians), Unit.M);
        return 1.0d * lonTranslation.toMillis() / degreeLonMeters.toMillis();
    }

    protected double getDeltaLat(Distance latTranslation) {
        return 1.0d * latTranslation.toMillis() / DEGREE_LAT.toMillis();
    }
}
