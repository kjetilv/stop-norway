package stopnorway.geo;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class TimeBox {

    private final Box box;

    private final Instant start;

    private final Instant end;

    private final Duration duration;

    public TimeBox(Box box, Instant start, Instant end) {
        this(
                Objects.requireNonNull(box, "box"),
                Objects.requireNonNull(start, "start"),
                Objects.requireNonNull(end, "end"),
                null);
    }

    public TimeBox(Box box, Instant start, Duration duration) {
        this(
                Objects.requireNonNull(box, "box"),
                Objects.requireNonNull(start, "start"),
                null,
                Objects.requireNonNull(duration, "duration"));
    }

    private TimeBox(Box box, Instant start, Instant end, Duration duration) {

        this.box = box;
        this.start = start;
        this.end = end == null ? start.plus(duration) : end;
        this.duration = duration == null ? Duration.between(start, end) : duration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + box + " " + start + " => " + end + " : " + duration + "]";
    }

    public Box getBox() {
        return box;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public Duration getDuration() {
        return duration;
    }
}
