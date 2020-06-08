package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Named;

import java.util.Collection;
import java.util.function.Consumer;

public final class Route extends Entity implements Named {

    private final String name;

    private final String shortName;

    private final Id lineRef;

    private final String directionType;

    private final Collection<PointOnRoute> pointsInSequence;

    public Route(
            Id id,
            String name,
            String shortName,
            Id lineRef,
            String directionType,
            Collection<PointOnRoute> pointsInSequence
    ) {
        super(id);
        this.name = name;
        this.shortName = shortName;
        this.lineRef = lineRef;
        this.directionType = directionType;
        this.pointsInSequence = pointsInSequence;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Id getLineRef() {
        return lineRef;
    }

    public String getDirectionType() {
        return directionType;
    }

    public Collection<PointOnRoute> getPointsInSequence() {
        return pointsInSequence;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name, shortName, directionType);
        hash(h, pointsInSequence);
    }
}
