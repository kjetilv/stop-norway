package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public final class TimetabledPassingTime extends Entity {

    private final Id stopPointInJourneyPatternRef;

    private final String departureTime;

    private final int departureDayOffset;

    private final String arrivalTime;

    private final int arrivalDayOffset;

    public TimetabledPassingTime(
            Id id,
            Id stopPointInJourneyPatternRef,
            String arrivalTime,
            int arrivalDayOffset,
            String departureTime,
            int departureDayOffset
    ) {
        super(id);
        this.stopPointInJourneyPatternRef = stopPointInJourneyPatternRef;
        this.arrivalTime = arrivalTime;
        this.arrivalDayOffset = arrivalDayOffset;
        this.departureTime = departureTime;
        this.departureDayOffset = departureDayOffset;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public int getArrivalDayOffset() {
        return arrivalDayOffset;
    }

    public String getDepartureTime() {
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
}
