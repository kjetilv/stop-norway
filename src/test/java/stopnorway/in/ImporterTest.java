package stopnorway.in;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImporterTest {

    @Test
    @Disabled
    void copy() {
        Path path = Importer.unzipped(Path.of(System.getProperty("user.home"))
                                              .resolve("Downloads")
                                              .resolve("rb_norway-aggregated-netex.zip"));
        assertThat(path.toFile().exists());
    }

}
