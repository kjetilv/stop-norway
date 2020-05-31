package stopnorway.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static stopnorway.geo.Unit.*;

class DistanceTest {

    @Test
    void test_create_long() {
        Distance distance = Distance.of(350, MM);
        assertThat(distance.to(M)).isEqualTo(0.35d);
    }

    @Test
    void test_create_double() {
        assertThat(Distance.of(123_234.345d, M).toMeters()).isEqualTo(123_234.345D);
        assertThat(Distance.of(345.4d, CM).toMeters()).isEqualTo(3.454D);
        assertThat(Distance.of(345.4d, M).toMeters()).isEqualTo(345.4D);
        assertThat(Distance.of(345.4d, M).to(CM)).isEqualTo(34540.0D);
    }

    @Test
    void test_to_meters() {
        Distance distance = Distance.of(350, Unit.CM);
        assertThat(distance.toMeters()).isEqualTo(3.5d);
    }

}
