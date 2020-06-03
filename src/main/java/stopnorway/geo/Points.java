package stopnorway.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Points {

    public static final Box NORWAY_BOX = point(57, 4).box(point(72, 32));

    public static final PointCoder NORWAY_CODER = new PointCoder(NORWAY_BOX, 6);

    private Points() {

    }

    public static Function<String, Collection<Point>> pointsSequencer(BiFunction<Double, Double, Point> pointMaker) {
        return s -> sequence(pointMaker, s);
    }

    public static Point point(double lat, double lon) {
        return new DoublePoint(lat, lon);
    }

    public static List<Point> sequence(
            BiFunction<Double, Double, Point> pointMaker,
            String str
    ) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return parse(pointMaker, str.split("\\s+"));
    }

    private static List<Point> parse(
            BiFunction<Double, Double, Point> pointMaker,
            String... split
    ) {
        BiFunction<Double, Double, Point>
                doubleDoublePointBiFunction =
                pointMaker == null ? NORWAY_CODER::coded : pointMaker;
        List<Point> points = new ArrayList<>(split.length / 2);
        for (int i = 0; i < split.length; ) {
            points.add(doubleDoublePointBiFunction.apply(
                    Double.parseDouble(split[i++]),
                    Double.parseDouble(split[i++])));
        }
        return Collections.unmodifiableList(points);
    }
}
