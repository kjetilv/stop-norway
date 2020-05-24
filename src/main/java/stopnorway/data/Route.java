package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.function.Consumer;

public final class Route extends Entity {

    private final String name;

    private final String shortName;

    private final String directionType;

    private final Collection<PointOnRoute> pointsInSequence;

    public Route(
            Id id,
            String name,
            String shortName,
            String directionType,
            Collection<PointOnRoute> pointsInSequence
    ) {
        super(id);
        this.name = name;
        this.shortName = shortName;
        this.directionType = directionType;
        this.pointsInSequence = pointsInSequence;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name, shortName, directionType);
        hash(h, pointsInSequence);
    }
}
