package stopnorway.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Tuple<T> {

    private final T t1;
    private final T t2;

    public Tuple(T t1, T t2) {

        this.t1 = Objects.requireNonNull(t1, "t1");
        this.t2 = Objects.requireNonNull(t2, "t2");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + t1 + " " + t2 + "]";
    }

    public T getT1() {
        return t1;
    }

    public T getT2() {
        return t2;
    }

    public boolean exists(Predicate<T> pred) {
        return pred.test(t1) && pred.test(t2);
    }

    public <R> Tuple<R> map(Function<T, R> fun) {
        return new Tuple<>(fun.apply(t1), fun.apply(t2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Tuple &&
                Objects.equals(t1, ((Tuple<?>) o).t1) &&
                Objects.equals(t2, ((Tuple<?>) o).t2);
    }
}
