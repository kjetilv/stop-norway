package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.entur.*;
import stopnorway.geo.Point;
import stopnorway.geo.Points;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static stopnorway.in.Attr.order;
import static stopnorway.in.Field.*;

@SuppressWarnings("unchecked")
public final class EntityParsers {

    static List<EntityParser<? extends Entity>> all() {
        return all(null);
    }

    static List<EntityParser<? extends Entity>> all(BiFunction<Double, Double, Point> pointMaker) {
        return List.of(
                lineParser(),
                serviceLinkParser(pointMaker),
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
                List.of(Name, TransportMode));
    }

    static EntityParser<ServiceJourney> serviceJourneyParser() {
        return new EntityParser<>(
                ServiceJourney.class,
                EntityParsers::serviceJourney,
                List.of(Name, JourneyPatternRef, LineRef)
        ).withSublist(
                Sublist.passingTimes,
                new EntityParser<>(
                        TimetabledPassingTime.class,
                        EntityParsers::timetabledPassingTime,
                        List.of(DepartureTime, StopPointInJourneyPatternRef, TransportMode)));
    }

    static EntityParser<JourneyPattern> journeyPatternParser() {
        return new EntityParser<>(
                JourneyPattern.class,
                EntityParsers::journeyPattern,
                List.of(RouteRef, Name)
        ).withSublist(
                Sublist.pointsInSequence,
                new EntityParser<>(
                        StopPointInJourneyPattern.class,
                        EntityParsers::stopPointInJourneyPattern,
                        List.of(ScheduledStopPointRef),
                        List.of(order))
        ).withSublist(
                Sublist.linksInSequence,
                new EntityParser<ServiceLinkInJourneyPattern>(
                        ServiceLinkInJourneyPattern.class,
                        EntityParsers::serviceLinkInJourneyPattern,
                        List.of(ServiceLinkRef),
                        List.of(order)));
    }

    static EntityParser<ScheduledStopPoint> scheduledStopPointParser() {
        return new EntityParser<>(
                ScheduledStopPoint.class,
                EntityParsers::scheduledStopPoint,
                Name);
    }

    static EntityParser<Route> routeParser() {
        return new EntityParser<>(
                Route.class,
                EntityParsers::route,
                Name, ShortName, DirectionType
        ).withSublist(
                Sublist.pointsInSequence,
                new EntityParser<>(
                        PointOnRoute.class,
                        EntityParsers::pointOnRoute,
                        RoutePointRef));
    }

    static EntityParser<ServiceLink> serviceLinkParser(BiFunction<Double, Double, Point> pointMaker) {
        return new EntityParser<>(
                ServiceLink.class,
                EntityParsers::serviceLink,
                FromPointRef, ToPointRef, Distance
        ).withSublist(
                Sublist.projections,
                new EntityParser<>(
                        LinkSequenceProjection.class,
                        data ->
                                linkSequenceProjection(
                                        data,
                                        Points.pointsSequencer(pointMaker)),
                        posList));
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
                        ProjectedPointRef));
    }

    private static Line line(EntityData data) {
        return new Line(
                data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode));
    }

    private static TimetabledPassingTime timetabledPassingTime(EntityData data) {
        return new TimetabledPassingTime(
                data.getId(),
                data.getId(StopPointInJourneyPatternRef),
                data.getContent(DepartureTime));
    }

    private static ServiceJourney serviceJourney(EntityData data) {
        return new ServiceJourney(
                data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode),
                data.getId(JourneyPatternRef),
                data.getId(LineRef),
                (Collection<TimetabledPassingTime>) data.getSublist(Sublist.passingTimes));
    }

    private static StopPointInJourneyPattern stopPointInJourneyPattern(EntityData data) {
        return new StopPointInJourneyPattern(
                data.getId(),
                data.getIntAttribute(order),
                data.getId(ScheduledStopPointRef));
    }

    private static ServiceLinkInJourneyPattern serviceLinkInJourneyPattern(EntityData data) {
        return new ServiceLinkInJourneyPattern(
                data.getId(),
                data.getIntAttribute(order),
                data.getId(ServiceLinkRef));
    }

    private static JourneyPattern journeyPattern(EntityData data) {
        return new JourneyPattern(
                data.getId(),
                data.getContent(Name),
                data.getId(RouteRef),
                (Collection<StopPointInJourneyPattern>) data.getSublist(Sublist.pointsInSequence),
                (Collection<ServiceLinkInJourneyPattern>) data.getSublist(Sublist.linksInSequence));
    }

    private static ScheduledStopPoint scheduledStopPoint(EntityData data) {
        return new ScheduledStopPoint(
                data.getId(),
                data.getContent(Name));
    }

    private static Route route(EntityData data) {
        return new Route(
                data.getId(),
                data.getContent(Name),
                data.getContent(ShortName),
                data.getContent(DirectionType),
                (Collection<PointOnRoute>) data.getSublist(Sublist.pointsInSequence));
    }

    private static PointOnRoute pointOnRoute(EntityData data) {
        return new PointOnRoute(
                data.getId(),
                data.getId(RoutePointRef));
    }

    private static RoutePoint routePoint(EntityData data) {
        return new RoutePoint(
                data.getId(),
                (Collection<PointProjection>) data.getSublist(Sublist.projections));
    }

    private static PointProjection pointProjection(EntityData data) {
        return new PointProjection(
                data.getId(),
                data.getId(ProjectedPointRef));
    }

    private static LinkSequenceProjection linkSequenceProjection(
            EntityData data,
            Function<String, Collection<Point>> pointsSequencer
    ) {
        return new LinkSequenceProjection(
                data.getId(),
                pointsSequencer.apply(data.getContent(posList)));
    }

    private static ServiceLink serviceLink(EntityData data) {
        return new ServiceLink(
                data.getId(),
                data.getId(FromPointRef),
                data.getId(ToPointRef),
                data.getContent(Distance),
                (Collection<LinkSequenceProjection>) data.getSublist(Sublist.projections));
    }
}
