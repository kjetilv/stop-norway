package stopnorway.in;

import org.codehaus.stax2.XMLInputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser {

    private static final Logger log = LoggerFactory.getLogger(Parser.class);
    private final boolean parallel;
    private final Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier;

    public Parser(boolean parallel, Supplier<Collection<EntityParser<? extends Entity>>> parsersSupplier) {

        this.parallel = parallel;
        this.parsersSupplier = parsersSupplier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[parallel:" + parallel + "]";
    }

    public static HashMap<Id, Entity> toMap(Stream<? extends Map.Entry<Id, ? extends Entity>> entryStream) {
        return entryStream
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Parser::idCheck,
                        LinkedHashMap::new));
    }

    public Map<Id, Entity> entities(Operator... operators) {
        return entities(Arrays.asList(operators));
    }

    public Map<Id, Entity> entities(Collection<Operator> operators) {
        log.info("Processing {} operators: {}",
                operators.stream().map(Enum::name).collect(Collectors.joining(", ")),
                operators.size());
        Instant totalStartTime = Instant.now();
        Map<Operator, XMLEventReader> sources = operatorSources(operators);
        Stream<? extends Map.Entry<Id, ? extends Entity>> entries =
                stream(sources).flatMap(entry -> {
                    Operator operator = entry.getKey();
                    log.info("Processing {}...", operator);
                    XMLEventReader eventReader = entry.getValue();
                    Instant startTime = Instant.now();
                    Collection<EntityParser<? extends Entity>> parsers = this.parsersSupplier.get();
                    try {
                        update(parsers, operator, eventReader);
                        return collected(parsers);
                    } catch (Exception e) {
                        if (parallel) {
                            log.error("Failed", e);
                            return collected(parsers);
                        }
                        throw new IllegalStateException
                                (this + " failed to update " + parsers.size() + " parsers for " + operator, e);
                    } finally {
                        log.info("Processed {} in {}", operator, Duration.between(startTime, Instant.now()));
                    }
                });
        log.info("Processed {} operators in {}", operators.size(), Duration.between(totalStartTime, Instant.now()));
        return toMap(entries);
    }

    private Stream<Map.Entry<Operator, XMLEventReader>> stream(Map<Operator, XMLEventReader> sources) {
        Stream<Map.Entry<Operator, XMLEventReader>> stream = sources.entrySet().stream();
        return parallel ? stream.parallel() : stream;
    }

    private void update(
            Collection<EntityParser<? extends Entity>> parsers,
            Operator operator,
            XMLEventReader eventReader
    ) {
        while (eventReader.hasNext()) {
            XMLEvent event = event(eventReader);
            parsers.forEach(parser -> {
                if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                    String data = event.asCharacters().getData();
                    if (data == null || data.isBlank()) {
                        return;
                    }
                }
                try {
                    parser.accept(event);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            this + " failed to parse for " + operator + " with " + parser, e);
                }
            });
        }
    }

    private static Entity idCheck(Entity entity1, Entity entity2) {
        if (entity1.equals(entity2)) {
            return entity1;
        }
        throw new IllegalStateException(entity1 + " != " + entity2);
    }

    private static Map<Operator, XMLEventReader> operatorSources(Collection<Operator> operators) {
        return operators.stream().collect(Collectors.toMap(
                Function.identity(),
                operatorFile().andThen(fileStream()).andThen(eventReaderBuilder())));
    }

    private static Stream<? extends Map.Entry<Id, ? extends Entity>> collected(
            Collection<EntityParser<? extends Entity>> parsers
    ) {
        return parsers.stream()
                .map(EntityParser::get)
                .map(Map::entrySet)
                .flatMap(Collection::stream);
    }

    private static Function<Operator, File> operatorFile() {
        return operator ->
                new File(new File(
                        new File(
                                new File(System.getProperty("user.home")),
                                "Documents"),
                        "rb_norway-aggregated-netex"),
                        sharedData(operator));
    }

    private static Function<File, InputStream> fileStream() {
        return file -> {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(file.getAbsolutePath(), e);
            }
        };
    }

    private static XMLEvent event(XMLEventReader reader) {
        try {
            return Objects.requireNonNull(reader.nextEvent(), "reader.nextEvent()");
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to read", e);
        }
    }

    private static Function<InputStream, XMLEventReader> eventReaderBuilder() {
        return is -> {
            try {
                return getXmlInputFactory().createXMLEventReader(is, StandardCharsets.UTF_8.name());
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Failed to create factory", e);
            }
        };
    }

    private static XMLInputFactory getXmlInputFactory() {
        return XMLInputFactory2.newFactory();
    }

    private static String sharedData(Operator operator) {
        return String.format("_%s_shared_data.xml", operator.name());
    }
}
