package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class LinkSequenceProjection extends Entity {

    private final List<GPSCoordinate> trajectory;

    public LinkSequenceProjection(Id id, List<GPSCoordinate> trajectory) {
        super(id);
        this.trajectory = trajectory == null || trajectory.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(trajectory);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        trajectory.forEach(gpsCoordinate ->
                hash(h, gpsCoordinate.getLat(), gpsCoordinate.getLon()));
    }

    public Collection<GPSCoordinate> getTrajectory() {
        return trajectory;
    }

    public long getTrajectoryLength() {
        return trajectory.size();
    }
}
