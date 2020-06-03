package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.data.DatabaseImpl;
import stopnorway.data.Operator;
import stopnorway.database.Entity;
import stopnorway.entur.Route;
import stopnorway.entur.RoutePoint;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Scale;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

public class ParserTestCase {

    private static final Logger log = LoggerFactory.getLogger(ParserTestCase.class);

    static final String ROUTE_POINT_TYPE = RoutePoint.class.getSimpleName();

    static final String SCHEDULED_STOP_POINT_TYPE = ScheduledStopPoint.class.getSimpleName();

    static final String SERVICE_LINK_TYPE = ServiceLink.class.getSimpleName();

    protected Database run(Operator... operators) {
        return run(1, 0, operators);
    }

    protected Database run(int times, int warmup, Operator... operators) {
        Collection<Entity> entities = null;
        Duration total = Duration.ZERO;
        for (int i = 0; i < times; i++) {
            Parser parser = new Parser(
                    false,
                    true,
                    () -> EntityParsers.all());

            Instant time = Instant.now();
            entities = parser.entities(operators.length == 0 ? Operator.values() : operators);
            Duration dur = Duration.between(time, Instant.now());
            if (i >= warmup) {
                total = total.plus(dur);
            }
            log.info("Run #{}: {}, avg {}", i + 1, dur, i >= warmup ? avg(total, i + 1, warmup) : "...");
        }
        log.info("Avg: {}", avg(total, times, warmup));
        return new DatabaseImpl(entities, Scale.DEFAULT);
    }

    protected Stream<ScheduledStopPoint> scheduledStopPoints(Collection<Entity> entities2) {
        return entities2.stream()
                .filter(entity ->
                        entity.getId().getType().equals(ParserTestCase.SCHEDULED_STOP_POINT_TYPE))
                .map(ScheduledStopPoint.class::cast);
    }

    protected Stream<ServiceLink> serviceLinks(Collection<Entity> entities2) {
        return entities2.stream()
                .filter(entity ->
                        entity.getId().getType().equals(SERVICE_LINK_TYPE))
                .map(ServiceLink.class::cast);
    }

    protected Stream<RoutePoint> routePoints(Collection<Entity> entities2) {
        return entities2.stream()
                .filter(entity ->
                        entity.getId().getType().equals(ROUTE_POINT_TYPE))
                .map(RoutePoint.class::cast);
    }

    protected Stream<Route> routes(Collection<Entity> entities2) {
        return entities2.stream()
                .filter(entity ->
                        entity.getId().getType().equals(Route.class.getSimpleName()))
                .map(Route.class::cast);
    }

    private Duration avg(Duration total, int times, int warmup) {
        return Duration.ofMillis(total.toMillis() / (times - warmup));
    }
}
