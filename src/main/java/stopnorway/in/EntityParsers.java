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

    static ScheduledStopPoint scheduledStopPoint(EntityParser.EntityMaterials entityMaterials) {
        return new ScheduledStopPoint(entityMaterials.getId(),
                entityMaterials.getContents().get(Field.Name));
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
    private static RoutePoint routePoint(EntityParser.EntityMaterials entityMaterials) {
        return new RoutePoint(entityMaterials.getId(),
                (Collection<PointProjection>) entityMaterials.getLists().get(Sublist.projections));
    }

    private static PointProjection pointProjection(EntityParser.EntityMaterials entityMaterials) {
        return new PointProjection(entityMaterials.getId(),
                entityMaterials.getIds().get(Field.ProjectedPointRef));
    }

    private static LinkSequenceProjection linkSequenceProjection(EntityParser.EntityMaterials entityMaterials) {
        return new LinkSequenceProjection(entityMaterials.getId(),
                GPSCoordinate.sequence(entityMaterials.getContents().get(Field.posList)));
    }

    @SuppressWarnings("unchecked")
    private static ServiceLink serviceLink(EntityParser.EntityMaterials entityMaterials) {
        return new ServiceLink(entityMaterials.getId(),
                entityMaterials.getIds().get(Field.FromPointRef),
                entityMaterials.getIds().get(Field.ToPointRef),
                entityMaterials.getContents().get(Field.Distance),
                (Collection<LinkSequenceProjection>) entityMaterials.getLists().get(Sublist.projections));
    }
}
