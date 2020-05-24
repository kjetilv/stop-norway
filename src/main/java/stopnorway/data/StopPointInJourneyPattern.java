package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public class StopPointInJourneyPattern extends Entity {

    private final Id scheduledStopPointRef;

    public StopPointInJourneyPattern(Id id, Id scheduledStopPointRef) {
        super(id);
        this.scheduledStopPointRef = scheduledStopPointRef;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, scheduledStopPointRef);
    }

    public Id getScheduledStopPointRef() {
        return scheduledStopPointRef;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append("scheduledStopPointRef: ").append(scheduledStopPointRef);
    }
}
