package stopnorway.data;

import java.util.*;

public final class GPSCoordinate {

    private final float lat;
    private final float lon;

    public GPSCoordinate(String lat, String lon) {

        this.lat = Float.parseFloat(lat);
        this.lon = Float.parseFloat(lon);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + lat + " " + lon + "]";
    }

    public static List<GPSCoordinate> sequence(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        String[] split = Arrays.stream(str.split("\\s+"))
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
        if (split.length % 2 == 1) {
            throw new IllegalStateException("Illegal trajectory, " + split.length +
                    " parts: [" + String.join(", ", split) + "]");
        }
        List<GPSCoordinate> gpsCoordinates = new ArrayList<>(split.length / 2);
        for (int i = 0; i < split.length; ) {
            gpsCoordinates.add(new GPSCoordinate(split[i++], split[i++]));
        }
        return List.copyOf(gpsCoordinates);
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof GPSCoordinate &&
                Float.compare(((GPSCoordinate) o).lat, lat) == 0 &&
                Float.compare(((GPSCoordinate) o).lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }
}
