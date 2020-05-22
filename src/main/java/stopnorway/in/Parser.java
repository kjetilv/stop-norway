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

    static Collection<EntityParser<? extends Entity>> parsers() {
        return List.of(
                scheduledStopPointParser(),
                serviceLinkParser());
    }

    private Parser() {

    }

    public static Map<Id, Entity> entities(Operator... operators) {
        Map<Operator, XMLEventReader> eventReaders =
                Arrays.stream(operators).collect(Collectors.toMap(
                        Function.identity(),
                        operatorSharedDataStreamer().andThen(eventReaderBuilder())));
        return eventReaders.entrySet().stream().parallel()
                .flatMap(entry -> {
                    XMLEventReader eventReader = entry.getValue();
                    Collection<EntityParser<? extends Entity>> parsers = parsers();
                    while (eventReader.hasNext()) {
                        XMLEvent event = event(eventReader);
                        parsers.forEach(entityParser -> {
                            try {
                                entityParser.accept(event);
                            } catch (Exception e) {
                                throw new IllegalStateException(
                                        "Failed to parse for " + entry.getKey() + " with " + entityParser, e);
                            }
                        });
                    }
                    return parsers.stream()
                            .map(EntityParser::get)
                            .map(Map::entrySet)
                            .flatMap(Collection::stream);
                })
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

    private static XMLEvent event(XMLEventReader reader) {
        try {
            return reader.nextEvent();
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
