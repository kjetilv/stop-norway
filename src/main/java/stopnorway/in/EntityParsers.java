package stopnorway.in;

import stopnorway.data.GPSCoordinate;
import stopnorway.data.LinkSequenceProjection;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.Map;

public final class EntityParsers {

    static EntityParser<ScheduledStopPoint> scheduledStopPointParser() {
        return new EntityParser<>(
                ScheduledStopPoint.class,
                EntityParsers::scheduledStopPoint,
                Field.Name);
    }

    static EntityParser<ServiceLink> serviceLinkParser() {
        return new EntityParser<>(
                ServiceLink.class,
                EntityParsers::serviceLink,
                Field.FromPointRef, Field.ToPointRef, Field.Distance
        ).withSublist(
                Sublist.projections, linkSequenceProjectionParser());
    }

    static ScheduledStopPoint scheduledStopPoint(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new ScheduledStopPoint(id, Field.Name.get(ids, ""));
    }

    private static EntityParser<LinkSequenceProjection> linkSequenceProjectionParser() {
        return new EntityParser<>(
                LinkSequenceProjection.class,
                EntityParsers::linkSequenceProjection,
                Field.posList);
    }

    private static LinkSequenceProjection linkSequenceProjection(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new LinkSequenceProjection(id,
                GPSCoordinate.sequence(Field.posList.get(ids, "")));
    }

    private static ServiceLink serviceLink(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new ServiceLink(id,
                Field.FromPointRef.get(ids),
                Field.ToPointRef.get(ids),
                Field.Distance.get(contents, null),
                Sublist.projections.get(lists, null));
    }
}
