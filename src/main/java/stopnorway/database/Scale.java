package stopnorway.database;

import java.io.Serializable;
import java.util.Objects;

public final class Scale implements Serializable {

    public static final Scale DEFAULT = new Scale(100, 50);

    private final int lat;

    private final int lon;

    public Scale(int last, int lon) {
        this.lat = last;
        this.lon = lon;
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
        return getClass().getSimpleName() + "[lat 1:" + lat + " lon 1:" + lon + "]";
    }
}
