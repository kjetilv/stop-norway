package stopnorway;

import stopnorway.data.ScheduledTrip;
import stopnorway.data.TripDefinition;
import stopnorway.geo.Box;

import java.util.Arrays;
import java.util.Collection;

public interface Database {

    Box getBox();

    default Collection<TripDefinition> getTripDefinitions(Box... boxes) {
        return getTripDefinitions(Arrays.asList(boxes));
    }

    Collection<TripDefinition> getTripDefinitions(Collection<Box> boxes);

    default Collection<ScheduledTrip> getScheduledTrips(Box... boxes) {
        return getScheduledTrips(Arrays.asList(boxes));
    }

    Collection<ScheduledTrip> getScheduledTrips(Collection<Box> boxes);
}
