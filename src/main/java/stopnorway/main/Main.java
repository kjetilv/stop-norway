package stopnorway.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.Databases;
import stopnorway.data.Operator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final Path ZIP = Path.of(System.getProperty("user.home"))
            .resolve("Downloads")
            .resolve("rb_norway-aggregated-netex.zip");

    public static void main(String[] args) {
        Database database =
                new Databases(ZIP, Operator.class)
                        .get(args.length == 0
                                     ? Collections.emptyList()
                                     : Arrays.stream(args)
                                             .map(Operator::valueOf)
                                             .collect(Collectors.toList()));
        log.info("{}", database);
    }
}
