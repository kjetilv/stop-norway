package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public class TimetabledPassingTime extends Entity {

    private final Id stopPointInJourneyPatternRef;
    private final String departureTime;

    public TimetabledPassingTime(Id id, Id stopPointInJourneyPatternRef, String departureTime) {
        super(id);
        this.stopPointInJourneyPatternRef = stopPointInJourneyPatternRef;
        this.departureTime = departureTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public Id getStopPointInJourneyPatternRef() {
        return stopPointInJourneyPatternRef;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb)
                .append("stopPointInJourneyPatternRef: ").append(stopPointInJourneyPatternRef)
                .append("departureTime: ").append(departureTime);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, stopPointInJourneyPatternRef);
    }
}
