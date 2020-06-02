package stopnorway.database;

import org.jetbrains.annotations.NotNull;

public interface Ordered extends Comparable<Ordered> {

    int getOrder();

    @Override
    default int compareTo(@NotNull Ordered ordered) {
        return Integer.compare(getOrder(), ordered.getOrder());
    }
}
