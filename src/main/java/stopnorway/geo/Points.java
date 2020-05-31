package stopnorway.geo;

public final class Points {

    private Points() {

    }

    public static Point point(double lat, double lon) {
        return new DoublePoint(lat, lon);
    }
}
