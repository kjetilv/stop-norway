package stopnorway.database;

import stopnorway.geo.Box;

import java.util.Optional;

public interface Boxed {

    Optional<Box> getBox();

    default boolean overlaps(Box box) {
        return getBox().filter(b -> b.overlaps(box)).isPresent();
    }
}
