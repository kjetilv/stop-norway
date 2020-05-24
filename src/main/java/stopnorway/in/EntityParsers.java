package stopnorway.in;

import stopnorway.data.*;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.List;
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
        return new ScheduledStopPoint(id, contents.get(Field.Name));
    }

    static EntityParser<RoutePoint> routePointParser() {
        return new EntityParser<>(
                RoutePoint.class,
                EntityParsers::routePoint
        ).withSublist(Sublist.projections, new EntityParser<>(
                PointProjection.class,
                EntityParsers::pointProjection,
                Field.ProjectedPointRef
        ));
    }

    private static EntityParser<LinkSequenceProjection> linkSequenceProjectionParser() {
        return new EntityParser<>(
                LinkSequenceProjection.class,
                EntityParsers::linkSequenceProjection,
                Field.posList);
    }

    @SuppressWarnings("unchecked")
    private static RoutePoint routePoint(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new RoutePoint(id,
                (Collection<PointProjection>) lists.get(Sublist.projections));
    }

    private static PointProjection pointProjection(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new PointProjection(id,
                ids.get(Field.ProjectedPointRef));
    }

    private static LinkSequenceProjection linkSequenceProjection(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> lists
    ) {
        return new LinkSequenceProjection(id,
                GPSCoordinate.sequence(contents.get(Field.posList)));
    }

    @SuppressWarnings("unchecked")
    private static ServiceLink serviceLink(
            Id id,
            Map<Field, Id> ids,
            Map<Field, String> contents,
            Map<Sublist, Collection<?>> sublists
    ) {
        return new ServiceLink(id,
                ids.get(Field.FromPointRef),
                ids.get(Field.ToPointRef),
                contents.get(Field.Distance),
                (Collection<LinkSequenceProjection>) sublists.get(Sublist.projections));
    }
}
