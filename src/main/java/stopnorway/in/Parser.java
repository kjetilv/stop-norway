package stopnorway.in;

import org.codehaus.stax2.XMLInputFactory2;
import org.jetbrains.annotations.NotNull;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Operator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Parser {

    private Parser() {

    }

    public static Map<Id, Entity> entities(Operator... operators) {
        return toMap(readers(operators).entrySet().stream().parallel()
                .flatMap(entry -> {
                    Operator operator = entry.getKey();
                    XMLEventReader eventReader = entry.getValue();
                    Collection<EntityParser<? extends Entity>> parsers =
                            updatedParsers(operator, eventReader);
                    return collected(parsers);
                }));
    }

    @NotNull
    public static Entity idCheck(Entity entity1, Entity entity2) {
        if (entity1.equals(entity2)) {
            return entity1;
        }
        throw new IllegalStateException(entity1 + " != " + entity2);
    }

    @NotNull
    public static HashMap<Id, Entity> toMap(Stream<? extends Map.Entry<Id, ? extends Entity>> entryStream) {
        return entryStream
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Parser::idCheck,
                        HashMap::new));
    }

    @NotNull
    static Collection<EntityParser<? extends Entity>> parsers() {
        return List.of(
                scheduledStopPointParser(),
                serviceLinkParser());
    }

    @NotNull
    private static Map<Operator, XMLEventReader> readers(Operator... operators) {
        return Arrays.stream(operators).collect(Collectors.toMap(
                Function.identity(),
                operatorSharedDataStreamer().andThen(eventReaderBuilder())));
    }

    @NotNull
    private static Stream<? extends Map.Entry<Id, ? extends Entity>> collected(Collection<EntityParser<? extends Entity>> parsers) {
        return parsers.stream()
                .map(EntityParser::get)
                .map(Map::entrySet)
                .flatMap(Collection::stream);
    }

    @NotNull
    private static Collection<EntityParser<? extends Entity>> updatedParsers(Operator operator, XMLEventReader eventReader) {
        Collection<EntityParser<? extends Entity>> parsers = parsers();
        while (eventReader.hasNext()) {
            XMLEvent event = event(eventReader);
            parsers.forEach(entityParser -> {
                try {
                    entityParser.accept(event);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to parse for " + operator + " with " + entityParser, e);
                }
            });
        }
        return parsers;
    }

    @NotNull
    private static EntityParser<ScheduledStopPoint> scheduledStopPointParser() {
        EntityParser.EntityMaker<ScheduledStopPoint> maker = (id, values) ->
                new ScheduledStopPoint(id, Field.Name.get(values, ""));
        return new EntityParser<>(
                ScheduledStopPoint.class,
                maker,
                Field.Name);
    }

    @NotNull
    private static EntityParser<ServiceLink> serviceLinkParser() {
        EntityParser.EntityMaker<ServiceLink> maker = (id, values) ->
                new ServiceLink(id,
                        Field.FromPointRef.<Id>get(values),
                        Field.ToPointRef.<Id>get(values),
                        Field.Distance.<String>getOpt(values).orElse(null));
        return new EntityParser<>(
                ServiceLink.class,
                maker,
                Field.FromPointRef, Field.ToPointRef, Field.Distance);
    }

    @NotNull
    private static Function<Operator, InputStream> operatorSharedDataStreamer() {
        return operator -> {
            File file = new File(new File(
                    new File(
                            new File(System.getProperty("user.home")),
                            "Documents"),
                    "rb_norway-aggregated-netex"),
                    sharedData(operator));
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(sharedData(operator), e);
            }
        };
    }

    @NotNull
    private static XMLEvent event(XMLEventReader reader) {
        try {
            return Objects.requireNonNull(reader.nextEvent(), "reader.nextEvent()");
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to read", e);
        }
    }

    @NotNull
    private static Function<InputStream, XMLEventReader> eventReaderBuilder() {
        return is -> {
            try {
                return getXmlInputFactory().createXMLEventReader(is, StandardCharsets.UTF_8.name());
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Failed to create factory", e);
            }
        };
    }

    @NotNull
    private static XMLInputFactory getXmlInputFactory() {
        return XMLInputFactory2.newFactory();
    }

    @NotNull
    private static String sharedData(Operator operator) {
        return String.format("_%s_shared_data.xml", operator.name());
    }
}
