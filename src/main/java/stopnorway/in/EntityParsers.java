package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.entur.*;
import stopnorway.geo.Point;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public final class EntityParsers {

    static List<EntityParser<? extends Entity>> all() {
        return List.of(
                lineParser(),
                serviceLinkParser(),
                scheduledStopPointParser(),
                routePointParser(),
                routeParser(),
                journeyPatternParser(),
                serviceJourneyParser());
    }

    static EntityParser<Line> lineParser() {
        return new EntityParser<>(
                Line.class,
                EntityParsers::line,
                Field.Name, Field.TransportMode);
    }

    static EntityParser<ServiceJourney> serviceJourneyParser() {
        return new EntityParser<>(
                ServiceJourney.class,
                EntityParsers::serviceJourney,
                Field.Name,
                Field.JourneyPatternRef
        ).withSublist(Sublist.passingTimes,
                new EntityParser<>(
                        TimetabledPassingTime.class,
                        EntityParsers::timetabledPassingTime,
                        Field.DepartureTime, Field.StopPointInJourneyPatternRef));
    }

    static EntityParser<JourneyPattern> journeyPatternParser() {
        return new EntityParser<>(
                JourneyPattern.class,
                EntityParsers::journeyPattern
        ).withSublist(Sublist.pointsInSequence, new EntityParser<>(
                StopPointInJourneyPattern.class,
                EntityParsers::stopPointInJourneyPattern,
                Field.ScheduledStopPointRef));
    }

    static EntityParser<ScheduledStopPoint> scheduledStopPointParser() {
        return new EntityParser<>(
                ScheduledStopPoint.class,
                EntityParsers::scheduledStopPoint,
                Field.Name);
    }

    static EntityParser<Route> routeParser() {
        return new EntityParser<>(
                Route.class,
                EntityParsers::route,
                Field.Name, Field.ShortName, Field.DirectionType
        ).withSublist(
                Sublist.pointsInSequence,
                new EntityParser<>(
                        PointOnRoute.class,
                        EntityParsers::pointOnRoute,
                        Field.RoutePointRef));
    }

    static EntityParser<ServiceLink> serviceLinkParser() {
        return new EntityParser<>(
                ServiceLink.class,
                EntityParsers::serviceLink,
                Field.FromPointRef, Field.ToPointRef, Field.Distance
        ).withSublist(
                Sublist.projections,
                new EntityParser<>(
                        LinkSequenceProjection.class,
                        EntityParsers::linkSequenceProjection,
                        Field.posList));
    }

    static EntityParser<RoutePoint> routePointParser() {
        return new EntityParser<>(
                RoutePoint.class,
                EntityParsers::routePoint
        ).withSublist(
                Sublist.projections,
                new EntityParser<>(
                        PointProjection.class,
                        EntityParsers::pointProjection,
                        Field.ProjectedPointRef));
    }

    private static Line line(EntityData data) {
        return new Line(data.getId(),
                data.getContent(Field.Name),
                data.getContent(Field.TransportMode));
    }

    private static TimetabledPassingTime timetabledPassingTime(EntityData data) {
        return new TimetabledPassingTime(data.getId(),
                data.getId(Field.StopPointInJourneyPatternRef),
                data.getContent(Field.DepartureTime));
    }

    private static ServiceJourney serviceJourney(EntityData data) {
        return new ServiceJourney(data.getId(),
                data.getContent(Field.Name),
                data.getId(Field.JourneyPatternRef),
                (Collection<TimetabledPassingTime>) data.getSublist(Sublist.passingTimes));
    }

    private static StopPointInJourneyPattern stopPointInJourneyPattern(EntityData data) {
        return new StopPointInJourneyPattern(
                data.getId(),
                data.getId(Field.ScheduledStopPointRef)
        );
    }

    private static JourneyPattern journeyPattern(EntityData data) {
        return new JourneyPattern(data.getId(),
                (Collection<StopPointInJourneyPattern>) data.getSublist(Sublist.pointsInSequence));
    }

    private static ScheduledStopPoint scheduledStopPoint(EntityData data) {
        return new ScheduledStopPoint(data.getId(),
                data.getContent(Field.Name));
    }

    private static Route route(EntityData data) {
        return new Route(
                data.getId(),
                data.getContent(Field.Name),
                data.getContent(Field.ShortName),
                data.getContent(Field.DirectionType),
                (Collection<PointOnRoute>) data.getSublist(Sublist.pointsInSequence));
    }

    private static PointOnRoute pointOnRoute(EntityData data) {
        return new PointOnRoute(
                data.getId(),
                data.getId(Field.RoutePointRef));
    }

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
                Point.sequence(data.getContent(Field.posList)));
    }

    private static ServiceLink serviceLink(EntityData data) {
        return new ServiceLink(data.getId(),
                data.getId(Field.FromPointRef),
                data.getId(Field.ToPointRef),
                data.getContent(Field.Distance),
                (Collection<LinkSequenceProjection>) data.getSublist(Sublist.projections));
    }
}
