package stopnorway.in;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.data.*;
import stopnorway.database.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    private static final String SERVICE_LINK_TYPE = ServiceLink.class.getSimpleName();

    private static final String SCHEDULED_STOP_POINT_TYPE = ScheduledStopPoint.class.getSimpleName();

    private static final String ROUTE_POINT_TYPE = RoutePoint.class.getSimpleName();

    private static final Logger log = LoggerFactory.getLogger(ParserTest.class);

    @Test
    void parse_prof_once() {
        DatabaseImpl entities = run(1, 0, Operator.RUT);
        assertThat(entities).isNotNull();
    }

    @Test
    void parse_prof_fast() {
        DatabaseImpl entities = run(5, 2);
        assertThat(entities).isNotNull();
    }

    @Test
    void parse_prof() {
        DatabaseImpl entities1 = run(30, 3);
        assertThat(entities1).isNotNull();
    }

    @Test
    void parse_ssp() {
        Parser parser = new Parser(false, true, EntityParsers::all);

        Map<Id, Entity> entities = parser.entities(Operator.values());

        assertThat(entities).isNotEmpty();

        System.out.println("Entities parsed:");
        System.out.println("  " + entities.size());

        System.out.println("Scheduled stop points: ");
        System.out.println("  " +
                entities.keySet().stream()
                        .filter(id ->
                                id.getType().equals(SCHEDULED_STOP_POINT_TYPE))
                        .count());

        System.out.println("Service links: ");
        System.out.println("  " +
                serviceLinks(entities)
                        .count());

        System.out.println("Route points: ");
        System.out.println("  " +
                routePoints(entities)
                        .count());

        System.out.println("Routes: ");
        System.out.println("  " +
                routes(entities)
                        .count());

        System.out.println("Link sequence projections: ");
        System.out.println("  " +
                serviceLinks(entities)
                        .mapToLong(serviceLink ->
                                serviceLink.getProjections().size())
                        .sum());

        System.out.println("Service link total: ");
        System.out.println("  " +
                serviceLinks(entities)
                        .mapToDouble(ServiceLink::getDistance)
                        .sum() / 1_000_000 + " 1000 km");

        System.out.println("GPS coords total: ");
        System.out.println("  " +
                serviceLinks(entities)
                        .map(ServiceLink::getProjections)
                        .flatMap(Collection::stream)
                        .map(LinkSequenceProjection::getTrajectory)
                        .mapToLong(Collection::size)
                        .sum());
    }

    @Test
    void parse_playground() {
        Parser parser = new Parser(false, true, EntityParsers::all);
        Map<Id, Entity> entities = parser.entities(Operator.RUT);

        ScheduledStopPoint akerBrygge = scheduledStopPoints(entities)
                .filter(scheduledStopPoint ->
                        scheduledStopPoint.getName().equalsIgnoreCase("Aker Brygge"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        ScheduledStopPoint ruselokka = scheduledStopPoints(entities)
                .filter(scheduledStopPoint ->
                        scheduledStopPoint.getName().equalsIgnoreCase("Ruseløkka"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        ServiceLink serviceLink = serviceLinks(entities)
                .filter(serviceLink1 ->
                        serviceLink1.getFromPoint().equals(akerBrygge.getId()) &&
                                serviceLink1.getToPoint().equals(ruselokka.getId()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        System.out.println(akerBrygge + " => " + ruselokka + ": " + serviceLink);
    }

    @Test
    void parse_database() {
        Parser parser = new Parser(false, true, EntityParsers::all);
        Map<Id, Entity> entities = parser.entities(Operator.RUT);
        Database database = new DatabaseImpl(entities, Scale.DEFAULT);

        Collection<ServiceLeg> legs = database.getServiceLegs(new Box(
                new DoublePoint(59.9142744, 10.7294832), /* sjakkmatt */
                new DoublePoint(59.9150603, 10.7330858) /* nordvegan*/));

        assertThat(legs).isNotEmpty();
    }

    private DatabaseImpl run(int times, int warmup, Operator... operators) {
        Map<Id, Entity> map = null;
        Duration total = Duration.ZERO;
        for (int i = 0; i < times; i++) {
            Parser parser = new Parser(
                    false,
                    times > 1 && operators.length >  1,
                    EntityParsers::all);

            Instant time = Instant.now();
            map = parser.entities(operators);
            Duration dur = Duration.between(time, Instant.now());
            if (i >= warmup) {
                total = total.plus(dur);
            }
            log.info("Run #{}: {}, avg {}", i + 1, dur, i >= warmup ? avg(total, i + 1, warmup) : "...");
        }
        log.info("Avg: {}", avg(total, times, warmup));
        return new DatabaseImpl(map, Scale.DEFAULT);
    }

    private Duration avg(Duration total, int times, int warmup) {
        return Duration.ofMillis(total.toMillis() / (times - warmup));
    }

    private Stream<ScheduledStopPoint> scheduledStopPoints(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(ParserTest.SCHEDULED_STOP_POINT_TYPE))
                .map(ScheduledStopPoint.class::cast);
    }

    private Stream<ServiceLink> serviceLinks(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(SERVICE_LINK_TYPE))
                .map(ServiceLink.class::cast);
    }

    private Stream<RoutePoint> routePoints(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(ROUTE_POINT_TYPE))
                .map(RoutePoint.class::cast);
    }

    private Stream<Route> routes(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(Route.class.getSimpleName()))
                .map(Route.class::cast);
    }
}
