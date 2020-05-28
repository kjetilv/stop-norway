package stopnorway.database;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public interface Point extends Serializable {
    static List<Point> sequence(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return DoublePoint.parse(str.split("\\s+"));
    }

    Box scaledBox(Scale scale);

    Point downTo(Scale scale);

    Point upTo(Scale scale);

    double lat();

    Point lat(Point lat);

    double lon();

    Point lon(Point lon);

    double distanceTo(Point point);
}
