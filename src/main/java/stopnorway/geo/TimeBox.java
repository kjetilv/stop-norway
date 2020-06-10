package stopnorway.geo;

import java.util.Objects;

public final class TimeBox {

    private final Box box;

    private final Timespan timespan;

    public TimeBox(Timespan timespan, Box box) {
        this.timespan = timespan;
        this.box = box;
    }

    public Box getBox() {
        return box;
    }

    public Timespan getTimespan() {
        return timespan;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof TimeBox &&
                Objects.equals(box, ((TimeBox) o).box) &&
                Objects.equals(timespan, ((TimeBox) o).timespan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(box, timespan);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + box + "@" + timespan + "]";
    }
}
