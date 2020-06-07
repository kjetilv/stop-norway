package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.Database;

import static org.assertj.core.api.Assertions.assertThat;

class ParserProfilingTest extends ParserTestCase {

    @Test
    void parse_prof_once() {
        Database entities = run(1, 0);
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

}
