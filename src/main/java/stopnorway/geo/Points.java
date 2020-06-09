package stopnorway.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Points {

    public static final Box NORWAY_BOX = point(57d, 4d).box(point(72d, 32d));

    static final String[] NONE = {};

    static final int ASCII_ZERO = 48;

    private static final int DIGITS = 6;

    static final int DECIMAL_DIMENSIONS = 100_000;

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
        return str == null || str.isBlank() ? Collections.emptyList() : points(str.trim());
    }

    public static Point charPoint(char[] lat, int latIndex, char[] lon, int lonIndex) {
        try {
            return new CodedPoint(toInt(lat, latIndex), toInt(lon, lonIndex));
        } catch (Exception e) {
            throw new IllegalArgumentException
                    ("Failed to point out " + Arrays.toString(lat) + " " + Arrays.toString(lon), e);
        }
    }

    private static int toInt(char[] v, int index) {
        int value = 0;
        boolean parsingDecimals = false;
        int decimalDimensions = DECIMAL_DIMENSIONS;
        for (int i = 0; i < index; i++) {
            if (v[i] == '.') {
                parsingDecimals = true;
                value *= CodedPoint.DEFAULT_DIMENSION;
            } else {
                int increment = v[i] - ASCII_ZERO;
                if (parsingDecimals) {
                    value += decimalDimensions * increment;
                    decimalDimensions /= 10;
                } else {
                    value *= 10;
                    value += increment;
                }
            }
        }
        if (!parsingDecimals) {
            value *= CodedPoint.DEFAULT_DIMENSION;
        }
        return round(value, v, index);
    }

    private static int round(int value, char[] v, int index) {
        if (index >= 10) {
            int roundIndex = 10;
            if (v[roundIndex] < ASCII_ZERO + 5) {
                return value;
            }
            return value + 1;
        }
        return value;
    }

    private static List<Point> points(String str) {
        int length = str.length();
        List<Point> points = new ArrayList<>(str.length() / 10);
        char[] lat = new char[30];
        char[] lon = new char[30];
        int latIndex = 0;
        int lonIndex = 0;
        boolean buildingLat = true;
        boolean buildingLon = false;
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == ' ' || c == '\t') {
                if (buildingLon) {
                    points.add(charPoint(lat, latIndex, lon, lonIndex));
                    buildingLat = true;
                    buildingLon = false;
                    latIndex = 0;
                    lonIndex = 0;
                } else if (buildingLat) {
                    buildingLat = false;
                    buildingLon = true;
                }
            } else if (buildingLat) {
                lat[latIndex++] = c;
            } else if (buildingLon) {
                lon[lonIndex++] = c;
            }
        }
        if (buildingLon) {
            points.add(charPoint(lat, latIndex, lon, lonIndex));
        }
        return points;
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
