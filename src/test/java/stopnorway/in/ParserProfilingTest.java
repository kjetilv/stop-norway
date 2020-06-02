package stopnorway.in;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.data.DatabaseImpl;
import stopnorway.data.Operator;
import stopnorway.data.ServiceLeg;
import stopnorway.entur.LinkSequenceProjection;
import stopnorway.geo.Points;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.database.*;
import stopnorway.geo.Scale;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParserProfilingTest extends ParserTestCase {

    private static final Logger log = LoggerFactory.getLogger(ParserProfilingTest.class);

    @Test
    void parse_prof_once() {
        Database entities = run(1, 0, Operator.RUT);
        assertThat(entities).isNotNull();
    }

    @Test
    void parse_prof_fast() {
        Database entities = run(5, 2);
        assertThat(entities).isNotNull();
    }

    @Test
    void parse_prof() {
        Database entities1 = run(30, 3);
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

        Collection<ServiceLeg> legs = database.getServiceLegs(
                Points.point(59.9142744, 10.7294832) /* sjakkmatt */
                        .box(Points.point(59.9150603, 10.7330858) /* nordvegan*/));

        assertThat(legs).isNotEmpty();
    }

}
