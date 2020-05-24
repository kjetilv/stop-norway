package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void parse_ssp() {
        Parser parser = new Parser(true,
                () -> List.of(
                        EntityParsers.serviceLinkParser(),
                        EntityParsers.scheduledStopPointParser()));

        Map<Id, Entity> entities = parser.entities(Operator.values());

        assertThat(entities).isNotEmpty();

        System.out.println("Service links: " +
                entities.keySet().stream()
                        .filter(id ->
                                id.getType().equals(ServiceLink.class.getSimpleName()))
                        .count());

        System.out.println("Scheduled stop points: " +
                entities.keySet().stream()
                        .filter(id ->
                                id.getType().equals(ScheduledStopPoint.class.getSimpleName()))
                        .count());
    }

}
