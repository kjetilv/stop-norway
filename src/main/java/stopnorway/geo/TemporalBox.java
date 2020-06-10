package stopnorway.geo;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.Stream;

public final class TemporalBox {

    private final Box box;

    private final LocalTime start;

    private final LocalTime end;

    private final Duration duration;

    public TemporalBox(Box box, LocalTime start, LocalTime end) {
        this(
                Objects.requireNonNull(box, "box"),
                Objects.requireNonNull(start, "start"),
                Objects.requireNonNull(end, "end"),
                null);
    }

    public TemporalBox(Box box, LocalTime start, Duration duration) {
        this(
                Objects.requireNonNull(box, "box"),
                Objects.requireNonNull(start, "start"),
                null,
                Objects.requireNonNull(duration, "duration"));
    }

    private TemporalBox(Box box, LocalTime start, LocalTime end, Duration duration) {

        this.box = box;
        this.start = start;
        this.end = end == null ? start.plus(duration) : end;
        this.duration = duration == null ? Duration.between(start, end) : duration;
    }

    public TemporalBox combined(TemporalBox temporalBox) {
        LocalTime start = Instant.from(this.start).isBefore(Instant.from(temporalBox.start))
                ? this.start
                : temporalBox.start;
        LocalTime end = Instant.from(this.end).isAfter(Instant.from(temporalBox.end))
                ? this.end
                : temporalBox.end;
        return new TemporalBox(box.combined(temporalBox.getBox()), start, end);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + box + " " + start + " => " + end + " : " + duration + "]";
    }

    private TemporalBox withBox(Box box) {
        return new TemporalBox(box, start, end, duration);
    }

    public Stream<TemporalBox> scaledBoxes(Scale scale, Duration timescale) {
        return box.getScaledBoxes(scale).map(scaledBox(timescale)::withBox);
    }

    public TemporalBox scaledBox(Duration temporalAccuracy) {
        return scaledBox(temporalAccuracy, this.box);
    }

    private TemporalBox scaledBox(Duration temporalAccuracy, Box box) {
        long secondsAccuracy = temporalAccuracy.toSeconds();
        LocalTime adjustedStart = LocalTime.ofSecondOfDay(secondsAccuracy * ((long) seconds(start) / secondsAccuracy));
        LocalTime adjustedEnd = LocalTime.ofSecondOfDay((1 + (long) seconds(end) / secondsAccuracy) * secondsAccuracy);
        return new TemporalBox(box, adjustedStart, adjustedEnd);
    }

    private int seconds(LocalTime start) {
        return start.getHour() * 3600 + start.getMinute() * 60 + start.getSecond();
    }

    public Box getBox() {
        return box;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Duration getDuration() {
        return duration;
    }
}
