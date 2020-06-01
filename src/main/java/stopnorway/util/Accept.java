package stopnorway.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Accept {

    public static <T> List<T> list(Collection<T> ts) {
        return ts == null || ts.isEmpty() ? Collections.emptyList() : List.copyOf(ts);
    }

    public static <K, V> Map<K, V> map(Map<K, V> kvs) {
        return kvs == null || kvs.isEmpty() ? Collections.emptyMap() : Map.copyOf(kvs);
    }

    private Accept() {

    }
}
