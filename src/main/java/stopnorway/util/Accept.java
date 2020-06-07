package stopnorway.util;

import java.util.*;

public final class Accept {

    private Accept() {

    }

    public static <T> List<T> list(Collection<T> ts) {
        return ts == null || ts == Collections.EMPTY_LIST || ts.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(ts);
    }

    public static <K, V> Map<K, V> map(Map<K, V> kvs) {
        return kvs == null || kvs == Collections.EMPTY_MAP || kvs.isEmpty()
                ? Collections.emptyMap()
                : new HashMap<>(kvs);
    }
}
