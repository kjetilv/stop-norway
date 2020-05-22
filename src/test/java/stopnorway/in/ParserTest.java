package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void parse_ssp() {
        Map<Id, Entity> map = Parser.toMap(Arrays.stream(Operator.values()).parallel()
                .map(Parser::entities)
                .map(Map::entrySet)
                .flatMap(Collection::stream));

        assertThat(map).isNotEmpty();

        System.out.println("Service links: " +
                map.keySet().stream()
                        .filter(id -> id.getType().equals(ServiceLink.class.getSimpleName()))
                        .count());

        System.out.println("Scheduled stop points: " +
                map.keySet().stream()
                        .filter(id -> id.getType().equals(ScheduledStopPoint.class.getSimpleName()))
                        .count());
    }

}
