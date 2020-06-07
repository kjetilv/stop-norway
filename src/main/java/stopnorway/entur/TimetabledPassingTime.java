package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.time.LocalTime;
import java.util.function.Consumer;

public class TimetabledPassingTime extends Entity {

    private final Id stopPointInJourneyPatternRef;

    private final String departureTime;

    public TimetabledPassingTime(Id id, Id stopPointInJourneyPatternRef, String departureTime) {
        super(id);
        this.stopPointInJourneyPatternRef = stopPointInJourneyPatternRef;
        this.departureTime = departureTime == null || departureTime.isBlank() ? null : departureTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public LocalTime getParsedDepartureTime() {
        return departureTime == null ? null : LocalTime.parse(departureTime);
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
                .append("").append(stopPointInJourneyPatternRef)
                .append(" @ ").append(departureTime);
    }
}
