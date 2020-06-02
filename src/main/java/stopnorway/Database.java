package stopnorway;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.data.ServiceLeg;
import stopnorway.data.TripDefinition;
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

    default Collection<ServiceLeg> getServiceLegs(Box... boxes) {
        return getServiceLegs(Arrays.asList(boxes));
    }

    Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes);

    default Collection<TripDefinition> getServicePatterns(Box... boxes) {
        return getTripDefinitions(Arrays.asList(boxes));
    }

    Collection<TripDefinition> getTripDefinitions(Collection<Box> boxes);
}
