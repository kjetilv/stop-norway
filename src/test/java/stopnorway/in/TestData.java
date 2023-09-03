package stopnorway.in;


import stopnorway.Database;
import stopnorway.Databases;
import stopnorway.data.Operator;

import java.nio.file.Path;

public class TestData {
    public static final Path ZIP = Path.of(System.getProperty("user.home"))
            .resolve("Downloads")
            .resolve("rb_norway-aggregated-netex.zip");


    public static Database getDatabase(Operator... operators) {
        Databases databases = new Databases(ZIP, Operator.class);
        return databases.adhoc(operators);
    }
}
