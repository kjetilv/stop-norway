package stopnorway.geo;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Timespan {

    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    private final LocalTime start;

    private final LocalTime end;

    private final int startOffset;

    private final int endOffset;

    public Timespan(LocalTime start, LocalTime end) {
        this(start, 0, end, 0);
    }

    public Timespan(LocalTime start, LocalTime end, int endOffset) {
        this(start, 0, end, endOffset);
    }

    public Timespan(LocalTime start, int startOffset, LocalTime end, int endOffset) {
        this.start = start;
        this.startOffset = startOffset;
        this.end = end;
        this.endOffset = endOffset;
        if (this.startOffset < 0) {
            throw new IllegalArgumentException("Invalid start offset: " + startOffset);
        }
        if (this.endOffset < startOffset) {
            throw new IllegalArgumentException("Invalid end offset < startOffset " + startOffset + ": " + endOffset);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                start + "/" + (startOffset > 0 ? startOffset : "") +
                "->" +
                end + "/" + (endOffset > 0 ? endOffset : "") +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Timespan &&
                startOffset == ((Timespan) o).startOffset &&
                endOffset == ((Timespan) o).endOffset &&
                Objects.equals(start, ((Timespan) o).start) &&
                Objects.equals(end, ((Timespan) o).end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, startOffset, endOffset);
    }

    public Timespan earliest(Timespan other) {
        return startSeconds() < other.startSeconds() ? this : other;
    }

    public Timespan latest(Timespan other) {
        return endSeconds() > other.endSeconds() ? this : other;
    }

    public Timespan combined(Timespan timespan) {
        Timespan earliest = earliest(timespan);
        Timespan latest = earliest != this ? this : timespan;
        return new Timespan(
                earliest.start, earliest.startOffset, latest.end, latest.endOffset);
    }

    public Stream<Timespan> timespans(Duration temporalAccuracy) {
        int secondsAccuracy = Math.toIntExact(temporalAccuracy.toSeconds());
        int secondsStart = Math.toIntExact(secondsAccuracy * ((long) seconds(start) / secondsAccuracy)) +
                startOffset * SECONDS_PER_DAY;
        int secondsEnd = Math.toIntExact((1 + seconds(end) / secondsAccuracy) * secondsAccuracy) +
                endOffset * SECONDS_PER_DAY;
        int timespans = Math.toIntExact((secondsEnd - secondsStart) / secondsAccuracy);
        return IntStream.range(0, timespans)
                .mapToObj(i -> {
                    int start = secondsStart + i * secondsAccuracy;
                    int end = secondsStart + (1 + i) * secondsAccuracy;
                    return new Timespan(
                            localTime(start), offset(start),
                            localTime(end), offset(end));
                });
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Duration getDuration() {
        return Duration.ofSeconds(endSeconds() - startSeconds());
    }

    private int endSeconds() {
        return seconds(end) + endOffset * SECONDS_PER_DAY;
    }

    private int startSeconds() {
        return seconds(start) + startOffset * SECONDS_PER_DAY;
    }

    private LocalTime localTime(int seconds) {
        return LocalTime.ofSecondOfDay(seconds % SECONDS_PER_DAY);
    }

    private int offset(int seconds) {
        return seconds / SECONDS_PER_DAY;
    }

    private int seconds(LocalTime start) {
        return start.getHour() * 3600 + start.getMinute() * 60 + start.getSecond();
    }
}
