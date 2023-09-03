package stopnorway.util;

import java.util.Collection;
import java.util.List;

public final class Safe {
    public static <T> Collection<T> list(List<T> scheduledStops) {
        return List.copyOf(scheduledStops);
    }

    private Safe() {
    }
}
