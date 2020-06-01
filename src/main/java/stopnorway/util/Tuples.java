package stopnorway.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Tuples {

    private Tuples() {
    }

    public static <T> Stream<Tuple<T>> of(Collection<T> ts) {
        if (ts == null || ts.isEmpty()) {
            return Stream.empty();
        }
        int size = ts.size();
        if (size == 1) {
            throw new IllegalArgumentException("Cannot tuplify single-element list");
        }
        List<T> arr = new ArrayList<>(ts);
        int lastTupleStart = size - 1;
        Spliterators.AbstractSpliterator<Tuple<T>> tupler = new Spliterators.AbstractSpliterator<>(
                size - 1,
                Spliterator.ORDERED
        ) {
            private int walker = 0;

            @Override
            public boolean tryAdvance(Consumer<? super Tuple<T>> action) {
                action.accept(new Tuple<>(arr.get(walker), arr.get(walker + 1)));
                walker++;
                return walker < lastTupleStart;
            }
        };
        return StreamSupport.stream(tupler, false);
    }
}
