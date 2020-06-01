package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.entur.*;
import stopnorway.geo.Point;

import java.util.Collection;
import java.util.List;

import static stopnorway.in.Field.*;

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
                Name, TransportMode);
    }

    static EntityParser<ServiceJourney> serviceJourneyParser() {
        return new EntityParser<>(
                ServiceJourney.class,
                EntityParsers::serviceJourney,
                Name,
                JourneyPatternRef
        ).withSublist(Sublist.passingTimes,
                new EntityParser<>(
                        TimetabledPassingTime.class,
                        EntityParsers::timetabledPassingTime,
                        DepartureTime, StopPointInJourneyPatternRef, TransportMode));
    }

    static EntityParser<JourneyPattern> journeyPatternParser() {
        return new EntityParser<>(
                JourneyPattern.class,
                EntityParsers::journeyPattern,
                RouteRef, Name
        ).withSublist(Sublist.pointsInSequence, new EntityParser<>(
                StopPointInJourneyPattern.class,
                EntityParsers::stopPointInJourneyPattern,
                ScheduledStopPointRef, order)
        ).withSublist(Sublist.linksInSequence, new EntityParser<>(
                ServiceLinkInJourneyPattern.class,
                EntityParsers::serviceLinkInJourneyPattern,
                ServiceLinkRef, order));
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

    static EntityParser<ServiceLink> serviceLinkParser() {
        return new EntityParser<>(
                ServiceLink.class,
                EntityParsers::serviceLink,
                FromPointRef, ToPointRef, Distance
        ).withSublist(
                Sublist.projections,
                new EntityParser<>(
                        LinkSequenceProjection.class,
                        EntityParsers::linkSequenceProjection,
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
        return new Line(data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode));
    }

    private static TimetabledPassingTime timetabledPassingTime(EntityData data) {
        return new TimetabledPassingTime(data.getId(),
                data.getId(StopPointInJourneyPatternRef),
                data.getContent(DepartureTime));
    }

    private static ServiceJourney serviceJourney(EntityData data) {
        return new ServiceJourney(data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode),
                data.getId(JourneyPatternRef),
                (Collection<TimetabledPassingTime>) data.getSublist(Sublist.passingTimes));
    }

    private static StopPointInJourneyPattern stopPointInJourneyPattern(EntityData data) {
        return new StopPointInJourneyPattern(
                data.getId(),
                Integer.parseInt(data.getContent(order)),
                data.getId(ScheduledStopPointRef));
    }

    private static ServiceLinkInJourneyPattern serviceLinkInJourneyPattern(EntityData data) {
        return new ServiceLinkInJourneyPattern(
                data.getId(),
                Integer.parseInt(data.getContent(order)),
                data.getId(ServiceLinkRef));
    }

    private static JourneyPattern journeyPattern(EntityData data) {
        return new JourneyPattern(data.getId(),
                data.getContent(Name),
                data.getId(RouteRef),
                (Collection<StopPointInJourneyPattern>) data.getSublist(Sublist.pointsInSequence),
                (Collection<ServiceLinkInJourneyPattern>) data.getSublist(Sublist.linksInSequence));
    }

    private static ScheduledStopPoint scheduledStopPoint(EntityData data) {
        return new ScheduledStopPoint(data.getId(),
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
        return new RoutePoint(data.getId(),
                (Collection<PointProjection>) data.getSublist(Sublist.projections));
    }

    private static PointProjection pointProjection(EntityData data) {
        return new PointProjection(data.getId(),
                data.getId(ProjectedPointRef));
    }

    private static LinkSequenceProjection linkSequenceProjection(EntityData data) {
        return new LinkSequenceProjection(data.getId(),
                Point.sequence(data.getContent(posList)));
    }

    private static ServiceLink serviceLink(EntityData data) {
        return new ServiceLink(data.getId(),
                data.getId(FromPointRef),
                data.getId(ToPointRef),
                data.getContent(Distance),
                (Collection<LinkSequenceProjection>) data.getSublist(Sublist.projections));
    }
}
