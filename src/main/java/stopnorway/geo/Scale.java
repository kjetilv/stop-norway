package stopnorway.geo;

import java.io.Serializable;
import java.util.Objects;

public final class Scale implements Serializable {

    public static final Scale DEFAULT = new Scale(100, 50);

    public static final Scale INTEGER = new Scale(0, 0);

    private final int lat;

    private final int lon;

    private Scale(int lat, int lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public static Scale of(int lat, int lon) {
        if (lat > 0 && lon > 0) {
            return new Scale(lat, lon);
        }
        throw new IllegalArgumentException("Bad scale: " + lat + "/" + lon);
    }

    public int getLat() {
        return lat;
    }

    public int getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Scale &&
                lat == ((Scale) o).lat &&
                lon == ((Scale) o).lon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[lat=1:" + lat + " lon=1:" + lon + "]";
    }
}
