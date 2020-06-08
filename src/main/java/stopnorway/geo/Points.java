package stopnorway.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Points {

    public static final Box NORWAY_BOX = point(57d, 4d).box(point(72d, 32d));

    private static final int DIGITS = 6;

    private Points() {

    }

    public static Point point(int lat, int lon) {
        return new CodedPoint(lat, lon);
    }

    public static Point point(double lat, double lon) {
        return new CodedPoint(lat, lon);
    }

    public static Point point(String lat, String lon) {
        try {
            return new CodedPoint(toInt(lat), toInt(lon));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to point out " + lat + " " + lon, e);
        }
    }

    public static List<Point> sequence(String str) {
        return str == null || str.isBlank() ? Collections.emptyList() : parse(str.split("\\s+"));
    }

    private static List<Point> parse(String... split) {
        List<Point> points = new ArrayList<>(split.length / 2);
        for (int i = 0; i < split.length; ) {
            points.add(point(split[i++], split[i++]));
        }
        return Collections.unmodifiableList(points);
    }

    private static int toInt(String string) {
        int i = string.indexOf('.');
        if (i > 0) {
            return parse(string, string.substring(0, i), string.substring(i + 1));
        }
        return Integer.parseInt(string);
    }

    private static int parse(String string, String intPart, String decPart) {
        int count = decPart.length();
        return count > DIGITS ? (int) Math.round(Double.parseDouble(string) * CodedPoint.DEFAULT_DIMENSION)
                : count == DIGITS ? Integer.parseInt(intPart + decPart)
                        : Integer.parseInt(intPart + decPart + zeroes(count));
    }

    private static String zeroes(int digitCount) {
        switch (DIGITS - digitCount) {
            case 1:
                return "0";
            case 2:
                return "00";
            case 3:
                return "000";
            case 4:
                return "0000";
            case 5:
                return "00000";
            case 6:
                return "000000";
        }
        throw new IllegalStateException("Could not pad " + digitCount + " - " + DIGITS + " digits");
    }
}
