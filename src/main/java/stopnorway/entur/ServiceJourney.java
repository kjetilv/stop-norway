package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.util.Accept;

import java.util.Collection;
import java.util.function.Consumer;

public class ServiceJourney extends Entity {

    private final String transportMode;

    private final String name;

    private final Id journeyPatternRef;

    private final Id lineRef;

    private final Collection<TimetabledPassingTime> passingTimes;

    public ServiceJourney(
            Id id,
            String transportMode,
            String name,
            Id journeyPatternRef,
            Id lineRef,
            Collection<TimetabledPassingTime> passingTimes
    ) {
        super(id);
        this.transportMode = transportMode;
        this.name = name;
        this.journeyPatternRef = journeyPatternRef;
        this.lineRef = lineRef;
        this.passingTimes = Accept.list(passingTimes);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name, transportMode);
        hash(h, journeyPatternRef);
        hash(h, passingTimes);
    }

    public String getName() {
        return name;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public Id getJourneyPatternRef() {
        return journeyPatternRef;
    }

    public Collection<TimetabledPassingTime> getPassingTimes() {
        return passingTimes;
    }

    public Id getLineRef() {
        return lineRef;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append("Name: ").append(name);
    }
}
