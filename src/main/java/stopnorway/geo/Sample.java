package stopnorway.geo;

import static stopnorway.geo.Unit.MM;

public final class Sample {

    private static final Distance DEGREE_LAT = Distance.of(110_574_235, MM);

    private static final Distance DEGREE_LON = Distance.of(110_572_833, MM);

    private final Point point;

    private final Distance accuracy;

    public Sample(Point point, Distance accuracy) {

        this.point = point;
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[=" + point + " ~" + accuracy + "m]";
    }

    public Box getBox() {

        return point.squareBox(accuracy);
    }
}
