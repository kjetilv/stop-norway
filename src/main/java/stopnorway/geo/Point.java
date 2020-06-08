package stopnorway.geo;

import java.io.Serializable;

public interface Point extends Serializable, Comparable<Point> {

    default Box box(Point max) {
        return Box.box(this, max);
    }

    default Box scaledBox(Scale scale) {
        return downTo(scale).box(upTo(scale));
    }

    default boolean isSouthwestOf(Point point) {
        return !equals(point) && lat() <= point.lat() && lon() <= point.lon();
    }

    Point downTo(Scale scale);

    Point upTo(Scale scale);

    double lat();

    double lon();

    int intLat();

    int intLon();

    Point lat(Point lat);

    Point lon(Point lon);

    Distance distanceTo(Point point);

    Point translate(Translation translation);

    default boolean isWithin(Box box) {
        return box.contains(this);
    }

    default boolean isWithinRadius(Point point, Distance radius) {
        return distanceTo(point).isShorterThanOrEqualTo(radius);
    }

    default Box squareBox(Distance sideLength) {
        Point max = this
                .translate(Translation.towards(Direction.NORTH, sideLength))
                .translate(Translation.towards(Direction.EAST, sideLength));
        return this.box(max);
    }

    @Override
    default int compareTo(Point point) {
        int latCompared = Double.compare(lat(), point.lat());
        return latCompared != 0
                ? latCompared
                : Double.compare(lon(), point.lon());
    }
}
