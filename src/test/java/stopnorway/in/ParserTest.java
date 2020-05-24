package stopnorway.in;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.data.RoutePoint;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    static final String SERVICE_LINK_TYPE = ServiceLink.class.getSimpleName();
    static final String SCHEDULED_STOP_POINT_TYPE = ScheduledStopPoint.class.getSimpleName();
    private static final Logger log = LoggerFactory.getLogger(ParserTest.class);
    static final String ROUTE_POINT_TYPE = RoutePoint.class.getSimpleName();

    @Test
    void parse_prof() {
        Map<Id, Entity> entities1 = null;
        Duration total = Duration.ZERO;
        int times = 30;
        int warmup = 5;
        for (int i = 0; i < times; i++) {
            Parser parser = new Parser(true, true,
                    () -> List.of(
                            EntityParsers.serviceLinkParser(),
                            EntityParsers.scheduledStopPointParser(),
                            EntityParsers.routePointParser()));

            Instant time = Instant.now();
            entities1 = parser.entities(Operator.values());
            Duration dur = Duration.between(time, Instant.now());
            if (i >= warmup) {
                total = total.plus(dur);
            }
            log.info("Run #{}: {}, avg {}", i + 1, dur, i >= warmup ? avg(total, i + 1, warmup) : "...");
        }
        log.info("Avg: {}", avg(total, times, warmup));
        assertThat(entities1).isNotEmpty();
    }

    @Test
    void parse_ssp() {
        Parser parser = new Parser(false,
                () -> List.of(
                        EntityParsers.serviceLinkParser(),
                        EntityParsers.scheduledStopPointParser(),
                        EntityParsers.routePointParser()));

        Map<Id, Entity> entities = parser.entities(Operator.RUT);//values());

        assertThat(entities).isNotEmpty();

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
    }

    private Duration avg(Duration total, int times, int warmup) {
        return Duration.ofMillis(total.toMillis() / (times - warmup));
    }

    @NotNull
    private Stream<ServiceLink> serviceLinks(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(SERVICE_LINK_TYPE))
                .map(ServiceLink.class::cast);
    }

    @NotNull
    private Stream<RoutePoint> routePoints(Map<Id, Entity> entities2) {
        return entities2.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(ROUTE_POINT_TYPE))
                .map(RoutePoint.class::cast);
    }
}
