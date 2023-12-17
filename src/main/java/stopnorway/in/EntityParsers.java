package stopnorway.in;

import stopnorway.database.Entity;
import stopnorway.entur.*;
import stopnorway.geo.Point;
import stopnorway.geo.Points;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static stopnorway.in.Attr.order;
import static stopnorway.in.Field.*;

@SuppressWarnings("unchecked")
public final class EntityParsers {

    static List<EntityParser<? extends Entity>> all() {
        return all(null);
    }

    static List<EntityParser<? extends Entity>> all(BiFunction<String, String, Point> pointMaker) {
        return List.of(
            lineParser(),
            serviceLinkParser(),
            scheduledStopPointParser(),
            routePointParser(),
            routeParser(),
            journeyPatternParser(),
            serviceJourneyParser()
        );
    }

    static EntityParser<Line> lineParser() {
        return new EntityParser<>(
            Line.class,
            data -> new Line(
                data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode)
            ),
            List.of(Name, TransportMode)
        );
    }

    static EntityParser<ServiceJourney> serviceJourneyParser() {
        return new EntityParser<>(
            ServiceJourney.class,
            data -> new ServiceJourney(
                data.getId(),
                data.getContent(Name),
                data.getContent(TransportMode),
                data.getId(JourneyPatternRef),
                data.getId(LineRef),
                (Collection<TimetabledPassingTime>) data.getSublist(Sublist.passingTimes)
            ),
            List.of(Name, TransportMode, JourneyPatternRef, LineRef)
        ).withSublist(
            Sublist.passingTimes,
            new EntityParser<>(
                TimetabledPassingTime.class,
                data -> new TimetabledPassingTime(
                    data.getId(),
                    data.getId(StopPointInJourneyPatternRef),
                    data.getContent(ArrivalTime),
                    data.getIntContent(ArrivalDayOffset),
                    data.getContent(DepartureTime),
                    data.getIntContent(DepartureDayOffset)
                ),
                List.of(
                    ArrivalTime,
                    ArrivalDayOffset,
                    DepartureTime,
                    DepartureDayOffset,
                    StopPointInJourneyPatternRef
                )
            )
        );
    }

    static EntityParser<JourneyPattern> journeyPatternParser() {
        return new EntityParser<>(
            JourneyPattern.class,
            data2 -> new JourneyPattern(
                data2.getId(),
                data2.getContent(Name),
                data2.getId(RouteRef),
                (Collection<StopPointInJourneyPattern>) data2.getSublist(Sublist.pointsInSequence),
                (Collection<ServiceLinkInJourneyPattern>) data2.getSublist(Sublist.linksInSequence)
            ),
            List.of(RouteRef, Name)
        ).withSublist(
            Sublist.pointsInSequence,
            new EntityParser<>(
                StopPointInJourneyPattern.class,
                data -> new StopPointInJourneyPattern(
                    data.getId(),
                    data.getOrder(),
                    data.getId(ScheduledStopPointRef)
                ),
                List.of(ScheduledStopPointRef),
                List.of(order)
            )
        ).withSublist(
            Sublist.linksInSequence,
            new EntityParser<>(
                ServiceLinkInJourneyPattern.class,
                data -> new ServiceLinkInJourneyPattern(
                    data.getId(),
                    data.getOrder(),
                    data.getId(ServiceLinkRef)
                ),
                List.of(ServiceLinkRef),
                List.of(order)
            )
        );
    }

    static EntityParser<ScheduledStopPoint> scheduledStopPointParser() {
        return new EntityParser<>(
            ScheduledStopPoint.class,
            data -> new ScheduledStopPoint(
                data.getId(),
                data.getContent(Name)
            ),
            Name
        );
    }

    static EntityParser<Route> routeParser() {
        return new EntityParser<>(
            Route.class,
            data -> new Route(
                data.getId(),
                data.getContent(Name),
                data.getContent(ShortName),
                data.getId(LineRef),
                data.getContent(DirectionType),
                (Collection<PointOnRoute>) data.getSublist(Sublist.pointsInSequence)
            ),
            Name, ShortName, LineRef, DirectionType
        ).withSublist(
            Sublist.pointsInSequence,
            new EntityParser<>(
                PointOnRoute.class,
                data ->
                    new PointOnRoute(
                        data.getId(),
                        data.getId(RoutePointRef)
                    ),
                RoutePointRef
            )
        );
    }

    static EntityParser<ServiceLink> serviceLinkParser() {
        return new EntityParser<>(
            ServiceLink.class,
            data -> new ServiceLink(
                data.getId(),
                data.getId(FromPointRef),
                data.getId(ToPointRef),
                data.getContent(Distance),
                (Collection<LinkSequenceProjection>) data.getSublist(Sublist.projections)
            ),
            FromPointRef, ToPointRef, Distance
        ).withSublist(
            Sublist.projections,
            new EntityParser<>(
                LinkSequenceProjection.class,
                data ->
                    new LinkSequenceProjection(
                        data.getId(),
                        Points.sequence(data.getContent(posList))
                    ),
                posList
            )
        );
    }

    static EntityParser<RoutePoint> routePointParser() {
        return new EntityParser<>(
            RoutePoint.class,
            data ->
                new RoutePoint(
                    data.getId(),
                    (Collection<PointProjection>) data.getSublist(Sublist.projections)
                )
        ).withSublist(
            Sublist.projections,
            new EntityParser<>(
                PointProjection.class,
                data -> new PointProjection(
                    data.getId(),
                    data.getId(ProjectedPointRef)
                ),
                ProjectedPointRef
            )
        );
    }
}
