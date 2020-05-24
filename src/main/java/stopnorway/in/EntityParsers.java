package stopnorway.in;

import stopnorway.data.*;

import java.util.Collection;

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

    static ScheduledStopPoint scheduledStopPoint(EntityData data) {
        return new ScheduledStopPoint(data.getId(),
                data.getContent(Field.Name));
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
    private static RoutePoint routePoint(EntityData data) {
        return new RoutePoint(data.getId(),
                (Collection<PointProjection>) data.getSublist(Sublist.projections));
    }

    private static PointProjection pointProjection(EntityData data) {
        return new PointProjection(data.getId(),
                data.getId(Field.ProjectedPointRef));
    }

    private static LinkSequenceProjection linkSequenceProjection(EntityData data) {
        return new LinkSequenceProjection(data.getId(),
                GPSCoordinate.sequence(data.getContent(Field.posList)));
    }

    @SuppressWarnings("unchecked")
    private static ServiceLink serviceLink(EntityData data) {
        return new ServiceLink(data.getId(),
                data.getId(Field.FromPointRef),
                data.getId(Field.ToPointRef),
                data.getContent(Field.Distance),
                (Collection<LinkSequenceProjection>) data.getSublist(Sublist.projections));
    }
}
