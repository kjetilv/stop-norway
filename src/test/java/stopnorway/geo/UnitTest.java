package stopnorway.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static stopnorway.geo.Unit.*;
import static stopnorway.geo.Unit.CM;

class UnitTest {

    @Test
    void test_cm() {
        assertThat(CM.toMeters(1000)).isEqualTo(10.0D);
        assertThat(CM.containsNo(MM)).isEqualTo(10);
        assertThat(CM.containsNo(CM)).isEqualTo(1);
        assertThat(CM.containsNo(M)).isEqualTo(0);
        assertThat(CM.containsNo(KM)).isEqualTo(0);
    }

    @Test
    void test_mm() {
        assertThat(MM.toMeters(1000)).isEqualTo(1.0D);
        assertThat(MM.containsNo(MM)).isEqualTo(1);
        assertThat(MM.containsNo(M)).isEqualTo(0);
        assertThat(MM.containsNo(KM)).isEqualTo(0);
    }

    @Test
    void test_m() {
        assertThat(M.toMeters(1000)).isEqualTo(1000.0D);
        assertThat(M.containsNo(MM)).isEqualTo(1000);
        assertThat(M.containsNo(M)).isEqualTo(1);
        assertThat(M.containsNo(KM)).isEqualTo(0);
    }

}
