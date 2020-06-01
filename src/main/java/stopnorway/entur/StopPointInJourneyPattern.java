package stopnorway.entur;

import stopnorway.database.Id;

public class StopPointInJourneyPattern extends SequencedRef {

    public StopPointInJourneyPattern(Id id, int order, Id scheduledStopPointRef) {
        super(id, order, scheduledStopPointRef);
    }

    public Id getScheduledStopPointRef() {
        return getRef();
    }
}
