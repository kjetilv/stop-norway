package stopnorway.database;

import stopnorway.geo.Box;

public interface Boxed {

    Box getBox();

    default boolean overlaps(Box box) {
        return getBox().overlaps(box);
    }
}
