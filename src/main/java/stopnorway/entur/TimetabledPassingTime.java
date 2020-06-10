package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.time.LocalTime;
import java.util.function.Consumer;

public final class TimetabledPassingTime extends Entity {

    private final Id stopPointInJourneyPatternRef;

    private final LocalTime departureTime;

    private final int departureDayOffset;

    private final LocalTime arrivalTime;

    private final int arrivalDayOffset;

    public TimetabledPassingTime(
            Id id,
            Id stopPointInJourneyPatternRef,
            String arrivalTime,
            int arrivalDayOffset,
            String departureTime,
            int departureDayOffset
    ) {
        this(
                id,
                stopPointInJourneyPatternRef,
                toLocalTime(arrivalTime),
                arrivalDayOffset,
                toLocalTime(departureTime),
                departureDayOffset
        );
    }

    public TimetabledPassingTime(
            Id id,
            Id stopPointInJourneyPatternRef,
            LocalTime arrivalTime,
            int arrivalDayOffset,
            LocalTime departureTime,
            int departureDayOffset
    ) {
        super(id);
        this.stopPointInJourneyPatternRef = stopPointInJourneyPatternRef;
        this.arrivalTime = arrivalTime;
        this.arrivalDayOffset = arrivalDayOffset;
        this.departureTime = departureTime;
        this.departureDayOffset = departureDayOffset;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public int getArrivalDayOffset() {
        return arrivalDayOffset;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public int getDepartureDayOffset() {
        return departureDayOffset;
    }

    public Id getStopPointInJourneyPatternRef() {
        return stopPointInJourneyPatternRef;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, stopPointInJourneyPatternRef);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb)
                .append(stopPointInJourneyPatternRef).append("@").append(departureTime);
    }

    private static LocalTime toLocalTime(String data) {
        if (data == null) {
            return null;
        }
        int length = data.length();
        if (length == 0) {
            return null;
        }
        if (data.endsWith(":00")) {
            int relevantLength = length - 3;
            int hours = 0;
            int minutes = 0;
            boolean parsingMinutes = false;
            for (int i = 0; i < relevantLength; i++) {
                char c = data.charAt(i);
                if (c == ':') {
                    parsingMinutes = true;
                } else {
                    int v = c - 48;
                    if (parsingMinutes) {
                        minutes *= 10;
                        minutes += v;
                    } else {
                        hours *= 10;
                        hours += v;
                    }
                }
            }
            if (hours < 24 && minutes < 60) {
                return LocalTime.of(hours, minutes);
            }

        }
        return LocalTime.parse(data);
    }
}
