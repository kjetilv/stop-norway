package stopnorway;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TimeSerializers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.data.*;
import stopnorway.database.Id;
import stopnorway.database.IdSerializer;
import stopnorway.entur.*;
import stopnorway.geo.*;
import stopnorway.in.Importer;
import stopnorway.in.Parser;
import stopnorway.in.ParserFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public final class Databases {

    private static final Logger log = LoggerFactory.getLogger(Databases.class);

    private final Path zipFile;

    private final Collection<? extends Enum<?>> operators;

    private final Box box;

    private final Scale scale;

    private final Duration temporalScale;

    public Databases(Path zipFile, Class<? extends Enum<?>> operators) {
        this(zipFile, operators, null, null, null);
    }

    public Databases(
            Path zipFile,
            Class<? extends Enum<?>> operators,
            Box box,
            Scale scale,
            Duration temporalScale) {
        this.zipFile = zipFile;
        this.operators = Set.of(operators.getEnumConstants());
        this.box = box == null ? Points.NORWAY_BOX : box;
        this.scale = scale == null ? Scale.DEFAULT : scale;
        this.temporalScale = temporalScale == null ? Duration.ofHours(1) : temporalScale;
    }

    public Database get(Enum<?>... operators) {
        return get(null, null, Arrays.asList(operators));
    }

    public Database get(Collection<? extends Enum<?>> operators) {
        return get(null, null, operators);
    }

    public Database rebuild(Enum<?>... operators) {
        return rebuild(Arrays.asList(operators));
    }

    public Database rebuild(Collection<? extends Enum<?>> operators) {
        return get(
                Objects.requireNonNull(zipFile, "zipFile"),
                true,
                false,
                all(operators),
                resolve(operators));
    }

    public Database adhoc(Enum<?>... operators) {
        return adhoc(Arrays.asList(operators));
    }

    public Database adhoc(Collection<? extends Enum<?>> operators) {
        return get(
                Objects.requireNonNull(zipFile, "zipFile"),
                false,
                false,
                all(operators),
                resolve(operators));
    }

    public Database get(Box box, Scale scale, Collection<? extends Enum<?>> operators) {
        return get(
                Objects.requireNonNull(zipFile, "zipFile"),
                box == null && scale == null,
                true,
                all(operators),
                resolve(operators));
    }

    private Collection<? extends Enum<?>> resolve(Collection<? extends Enum<?>> operators) {
        return all(operators)
                ? this.operators
                : Set.copyOf(operators);
    }

    private boolean all(Collection<? extends Enum<?>> operators) {
        return operators == null || operators.isEmpty();
    }

    private Database get(
            Path zipFile,
            boolean dump,
            boolean reuse,
            boolean all,
            Collection<? extends Enum<?>> operators
    ) {
        Path serialForm = serialForm(zipFile, all, operators);
        if (reuse && exists(serialForm)) {
            log.info("Database will be read from {}", serialForm);
            return read(serialForm);
        }
        if (dump) {
            log.info("Database will be dumped to {}", serialForm);
        }
        Path directory = Importer.unzipped(zipFile, operators);
        ParserFactory parserFactory = new ParserFactory(directory, operators);
        try (Parser parser = parserFactory.create(false, true)) {
            Database database =
                    new DatabaseImpl(box, scale, temporalScale, parser.entities());
            if (dump) {
                write(database, serialForm);
            }
            return database;
        }
    }

    private static boolean exists(Path serialForm) {
        return serialForm.toFile().isFile() && serialForm.toFile().length() > 0;
    }

    private static void write(Database database, Path serialForm) {
        log.info("Writing to {}: {}", serialForm, database);
        Kryo kryo = kryo();
        try (
                OutputStream os = new FileOutputStream(serialForm.toFile());
                Output output = new Output(os)
        ) {
            kryo.writeObject(
                    output,
                    database,
                    new DatabaseImplSerializer());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write database to " + serialForm.toAbsolutePath(), e);
        } finally {
            log.info("DB written to {}", serialForm);
        }
    }

    private static Database read(Path serialForm) {
        log.info("Reading db from {}...", serialForm);
        Kryo kryo = kryo();
        try (
                InputStream is = new FileInputStream(serialForm.toFile())
        ) {
            return kryo.readObject(
                    new Input(is),
                    DatabaseImpl.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read database from " + serialForm.toAbsolutePath(), e);
        } finally {
            log.info("DB read from {}", serialForm);
        }
    }

    @NotNull
    private static Kryo kryo() {
        Kryo kryo = new Kryo();
        kryo.setWarnUnregisteredClasses(true);
        kryo.setRegistrationRequired(true);
        register(
                kryo,
                Id.class,
                new IdSerializer(
                        Operator.class,
                        StopPointInJourneyPattern.class,
                        ServiceLinkInJourneyPattern.class,
                        Route.class,
                        ServiceLink.class,
                        JourneyPattern.class,
                        RoutePoint.class,
                        TimetabledPassingTime.class,
                        ServiceJourney.class,
                        PointOnRoute.class,
                        PointProjection.class,
                        ScheduledStopPoint.class,
                        LinkSequenceProjection.class,
                        Line.class));
        register(kryo, DatabaseImpl.class, new DatabaseImplSerializer());
        register(kryo, Box.class, new BoxSerializer());
        register(kryo, Scale.class, new ScaleSerializer());
        register(kryo, Timespan.class, new TimespanSerializer());
        register(kryo, Duration.class, new TimeSerializers.DurationSerializer());
        register(kryo, LocalTime.class, new TimeSerializers.LocalTimeSerializer());
        register(kryo, CodedPoint.class, new CodedPointSerializer());
        register(kryo, JourneyPattern.class, new JourneyPatternSerializer());
        register(kryo, Line.class, new LineSerializer());
        register(
                kryo,
                LinkSequenceProjection.class,
                new LinkSequenceProjectionSerializer());
        register(kryo, PointOnRoute.class, new PointOnRouteSerializer());
        register(kryo, PointProjection.class, new PointProjectionSerializer());
        register(kryo, Route.class, new RouteSerializer());
        register(kryo, RoutePoint.class, new RoutePointSerializer());
        register(kryo, ScheduledStopPoint.class, new ScheduledStopPointSerializer());
        register(kryo, ServiceJourney.class, new ServiceJourneySerializer());
        register(kryo, ServiceLink.class, new ServiceLinkSerializer());
        register(kryo, ServiceLinkInJourneyPattern.class, new ServiceLinkInJourneyPatternSerializer());
        register(kryo, StopPointInJourneyPattern.class, new StopPointInJourneyPatternSerializer());
        register(kryo, TimetabledPassingTime.class, new TimetabledPassingTimeSerializer());

        register(kryo, JourneySpecification.class, new JourneySpecificationSerializer());
        register(kryo, ServiceLeg.class, new ServiceLegSerializer());

        return kryo;
    }

    private static <T> void register(Kryo kryo, Class<T> type, Serializer<T> ser) {
        kryo.register(type, ser);
    }

    private static Path serialForm(Path directory, boolean all, Collection<? extends Enum<?>> operators) {
        String qualifier = all
                ? ""
                : operators.stream()
                        .sorted(Comparator.comparing(Enum::ordinal))
                        .map(Enum::name)
                        .map(name -> "-" + name)
                        .collect(Collectors.joining());
        return Importer
                .targetPath(directory)
                .resolve("database" + qualifier + ".ser");
    }

}
