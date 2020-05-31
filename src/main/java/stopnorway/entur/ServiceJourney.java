package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ServiceJourney extends Entity {

    private final String name;
    private final Id journeyPatternRef;
    private final Collection<TimetabledPassingTime> passingTimes;

    public ServiceJourney(Id id, String name, Id journeyPatternRef, Collection<TimetabledPassingTime> passingTimes) {
        super(id);
        this.name = name;
        this.journeyPatternRef = journeyPatternRef;
        this.passingTimes = passingTimes == null || passingTimes.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(passingTimes);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name);
        hash(h, journeyPatternRef);
        hash(h, passingTimes);
    }

    public String getName() {
        return name;
    }

    public Id getJourneyPatternRef() {
        return journeyPatternRef;
    }

    public Collection<TimetabledPassingTime> getPassingTimes() {
        return passingTimes;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append("Name: ").append(name);
    }
}
