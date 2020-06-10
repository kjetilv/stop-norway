package stopnorway.entur;

import stopnorway.database.Id;

public final class ServiceLinkInJourneyPattern extends SequencedRef {

    public ServiceLinkInJourneyPattern(Id id, int order, Id serviceLinkRef) {
        super(id, order, serviceLinkRef);
    }

    public Id getServiceLinkRef() {
        return getRef();
    }
}
