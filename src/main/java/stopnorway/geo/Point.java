package stopnorway.geo;

import java.io.Serializable;

public interface Point extends Serializable, Comparable<Point> {

    default Box box(Point max) {
        return Box.box(this, max);
    }

    default Box scaledBox(Scale scale) {
        return downTo(scale).box(upTo(scale));
    }

    Point downTo(Scale scale);

    Point upTo(Scale scale);

    double lat();

    Point lat(Point lat);

    double lon();

    Point lon(Point lon);

    Distance distanceTo(Point point);

    Point translate(Translation translation);

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
