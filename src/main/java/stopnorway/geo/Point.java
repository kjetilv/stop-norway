package stopnorway.geo;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public interface Point extends Serializable, Comparable<Point> {
    static List<Point> sequence(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return DoublePoint.parse(str.split("\\s+"));
    }

    default Box box(Point max) {
        return Box.box(this, max);
    }

    Box scaledBox(Scale scale);

    Point downTo(Scale scale);

    Point upTo(Scale scale);

    double lat();

    Point lat(Point lat);

    double lon();

    Point lon(Point lon);

    Distance distanceTo(Point point);

    Point translate(Translation translation);

    Box squareBox(Distance sides);

    @Override
    default int compareTo(@NotNull Point point) {
        int lat = Double.compare(lat(), point.lat());
        return lat != 0
                ? lat
                : Double.compare(lon(), point.lon());
    }
}
