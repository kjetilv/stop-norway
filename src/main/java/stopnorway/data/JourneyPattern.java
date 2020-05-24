package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class JourneyPattern extends Entity {

    private final Collection<StopPointInJourneyPattern> pointsInSequence;

    public JourneyPattern(Id id, Collection<StopPointInJourneyPattern> pointsInSequence) {
        super(id);
        this.pointsInSequence = pointsInSequence == null || pointsInSequence.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(pointsInSequence);
    }

    public Collection<StopPointInJourneyPattern> getPointsInSequence() {
        return pointsInSequence;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, pointsInSequence);
    }
}
