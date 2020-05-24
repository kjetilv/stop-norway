package stopnorway.database;

import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public interface Database {

    ScheduledStopPoint getScheduledStopPoint(Id id);

    ServiceLink getServiceLink(Id id);

    <E extends Entity> Stream<E> get(Class<E> type);

    default Collection<ServiceLeg> getServiceLegs(Box... boxes) {
        return getServiceLegs(Arrays.asList(boxes));
    }

    Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes);
}
