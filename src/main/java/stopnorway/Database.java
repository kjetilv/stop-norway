package stopnorway;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.ServiceLeg;
import stopnorway.database.ServicePattern;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.geo.Box;
import stopnorway.geo.Points;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public interface Database {

    Box NORWAY = Points.point(57, 4).box(Points.point(72, 32));

    Box getBox();

    ScheduledStopPoint getScheduledStopPoint(Id id);

    <E extends Entity> Stream<E> get(Class<E> type);

    default Collection<ServiceLeg> getServiceLegs(Box... boxes) {
        return getServiceLegs(Arrays.asList(boxes));
    }

    Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes);

    Collection<ServicePattern> getServicePatterns(Collection<Box> boxes);
}
