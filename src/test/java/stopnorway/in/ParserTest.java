package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void parse_ssp() {
        Map<Id, Entity> map =
                Arrays.stream(Operator.values()).parallel()
                        .map(Parser::entities)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (entity1, entity2) -> {
                                    if (entity1.equals(entity2)) {
                                        return entity1;
                                    }
                                    throw new IllegalStateException(entity1 + " != " + entity2);
                                },
                                HashMap::new));

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
