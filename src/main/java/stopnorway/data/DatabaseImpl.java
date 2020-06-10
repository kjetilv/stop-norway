package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.database.Boxed;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.entur.*;
import stopnorway.geo.Box;
import stopnorway.geo.Points;
import stopnorway.geo.Scale;
import stopnorway.geo.Timespan;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DatabaseImpl implements Database, Serializable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseImpl.class);

    private final Box box;

    private final Scale scale;

    private final Duration timescale;

    private final Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities;

    private final Map<Id, JourneySpecification> journeySpecifications;

    private final Map<Id, Journey> journeys;

    private final Map<Box, Collection<JourneySpecification>> boxedJourneySpecification;

    private final Map<Timespan, Collection<Journey>> timespannedJoureys;

    private final int size;

    public DatabaseImpl(Box box, Scale scale, Duration temporalScale, Stream<Entity> entities) {
        this(box, scale, temporalScale, map(Objects.requireNonNull(entities, "entities")));
    }

    public DatabaseImpl(
            Box box,
            Scale scale,
            Duration timescale,
            Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities
    ) {
        this.box = box == null ? Points.NORWAY_BOX : box;
        this.scale = scale == null ? Scale.DEFAULT : scale;
        this.timescale = timescale;
        this.typedEntities = typedEntities;
        this.size = (int) this.typedEntities.values().stream().mapToLong(Map::size).sum();
        log.info("{} built from {} entities", this, size);

        this.journeySpecifications = stream(JourneyPattern.class)
                .map(this::journeySpecification)
                .collect(Collectors.toMap(
                        JourneySpecification::getJourneyPatternId,
                        Function.identity()));

        this.boxedJourneySpecification = new HashMap<>();
        this.journeySpecifications.values()
                .forEach(spec -> spec.scaledBoxes(scale)
                        .forEach(scaledBox -> add(this.boxedJourneySpecification, scaledBox, spec)));
        log.info(
                "{} indexed {} trips in {} boxes",
                this,
                journeySpecifications.size(),
                boxedJourneySpecification.size());

        this.journeys = stream(ServiceJourney.class)
                .map(serviceJourney -> journey(
                        serviceJourney,
                        journeySpecifications::get))
                .collect(Collectors.toMap(
                        Journey::getId,
                        Function.identity()));

        this.timespannedJoureys = new HashMap<>();
        this.journeys.values()
                .forEach(journey -> journey.scaledTimespans(timescale)
                        .forEach(timespan -> add(this.timespannedJoureys, timespan, journey)));

        log.info("{} collected {} scheduled trips", this, this.journeys.size());
    }

    @Override
    public TemporalAmount getTimescale() {
        return timescale;
    }

    public Scale getScale() {
        return scale;
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public Collection<Journey> getJourneys(Collection<Timespan> boxes) {
        return null;
        //        temporallySpatiallyScaled(boxes)
        //                .flatMap(scaledBox ->
        //                        boxed(this.temporallyBoxedJourney, scaledBox))
        //                .filter(journey ->
        //                        overlapping(boxes, journey))
        //                .collect(Collectors.toList());
    }

    @Override
    public Collection<JourneySpecification> getJourneySpecifications(Collection<Box> boxes) {
        return spatiallyScaled(boxes)
                .flatMap(scaledBox ->
                                 boxed(this.boxedJourneySpecification, scaledBox))
                .filter(journeySpecification ->
                                overlapping(boxes, journeySpecification))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[" + box +
                " /" + scale +
                " typedEntities=" +
                typedEntities.entrySet().stream()
                        .map(e -> e.getKey().getSimpleName() + ":" + e.getValue().size())
                        .collect(Collectors.joining(", ")) +
                "]";
    }

    @Override
    public int getSize() {
        return size;
    }

    public Map<Class<? extends Entity>, Map<Id, Entity>> getTypedEntities() {
        return typedEntities;
    }

    private Journey journey(ServiceJourney serviceJourney, Function<Id, JourneySpecification> patterns) {
        return new Journey(
                serviceJourney.getId(),
                patterns.apply(serviceJourney.getJourneyPatternRef()),
                serviceJourney.getPassingTimes().stream()
                        .map(this::scheduledStop)
                        .collect(Collectors.toList()));
    }

    private ScheduledStop scheduledStop(TimetabledPassingTime timetabledPassingTime) {
        StopPointInJourneyPattern stopPointInJourneyPattern =
                getEntity(StopPointInJourneyPattern.class, timetabledPassingTime.getStopPointInJourneyPatternRef());
        ScheduledStopPoint scheduledStopPoint = stopPointInJourneyPattern == null
                ? null
                : getEntity(ScheduledStopPoint.class, stopPointInJourneyPattern.getScheduledStopPointRef());
        return new ScheduledStop(
                timetabledPassingTime.getId(),
                scheduledStopPoint,
                Timespan.timespan(
                        timetabledPassingTime.getArrivalTime(),
                        timetabledPassingTime.getArrivalDayOffset(),
                        timetabledPassingTime.getDepartureTime(),
                        timetabledPassingTime.getDepartureDayOffset()));
    }

    private boolean overlapping(Collection<Box> boxes, Boxed boxable) {
        return boxes.stream().anyMatch(boxable::overlaps);
    }

    private boolean overlapping(Collection<Timespan> boxes, Journey journey) {
        return boxes.stream().anyMatch(journey::overlaps);
    }

    private <E extends Entity> Stream<E> stream(Class<E> type) {
        return list(type).stream();
    }

    private <E extends Entity> Collection<E> list(Class<E> type) {
        return getEntityMap(type).values();
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity> Map<Id, E> getEntityMap(Class<E> type) {
        return (Map<Id, E>) typedEntities.get(type);
    }

    private <E extends Entity> E getEntity(Class<E> type, Id id) {
        if (id == null) {
            return null;
        }
        if (id.is(type)) {
            Entity obj = typedEntities.get(type).get(id);
            if (obj == null) {
                return null;
            }
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
            throw new IllegalArgumentException("Not a " + type + " for " + id + ": " + obj);
        }
        throw new IllegalArgumentException("Not a " + type + " id: " + id);
    }

    private JourneySpecification journeySpecification(JourneyPattern journeyPattern) {
        Route route = getEntity(Route.class, journeyPattern.getRouteRef());
        Line line = route == null ? null : getEntity(Line.class, route.getLineRef());
        return new JourneySpecification(
                journeyPattern.getId(),
                journeyPattern.getName(),
                route,
                line,
                stopPoints(journeyPattern),
                serviceLegs(journeyPattern));
    }

    private List<Map.Entry<StopPointInJourneyPattern, ScheduledStopPoint>> stopPoints(JourneyPattern journeyPattern) {
        return journeyPattern.getPointsInSequence()
                .stream()
                .sorted()
                .map(stopPointInJourneyPattern ->
                             new AbstractMap.SimpleEntry<>(
                                     stopPointInJourneyPattern,
                                     getEntity(
                                             ScheduledStopPoint.class,
                                             stopPointInJourneyPattern.getScheduledStopPointRef())))
                .collect(Collectors.toList());
    }

    private Collection<Map.Entry<ServiceLinkInJourneyPattern, ServiceLeg>> serviceLegs(JourneyPattern journeyPattern) {
        return journeyPattern.getLinksInSequence().stream().sorted().map(sequencedInJourneyPattern -> {
            ServiceLink serviceLink =
                    getEntity(ServiceLink.class, sequencedInJourneyPattern.getServiceLinkRef());
            if (serviceLink == null) {
                log.warn("No service link found: {} => {}", journeyPattern, sequencedInJourneyPattern);
            }
            return new AbstractMap.SimpleEntry<>(
                    sequencedInJourneyPattern,
                    serviceLink == null ? null : new ServiceLeg(
                            serviceLink.getId(),
                            getEntity(ScheduledStopPoint.class, serviceLink.getFromPoint()),
                            getEntity(ScheduledStopPoint.class, serviceLink.getToPoint()),
                            serviceLink,
                            sequencedInJourneyPattern.getOrder()));
        }).collect(Collectors.toList());
    }

    @NotNull
    private Stream<Box> spatiallyScaled(Collection<Box> boxes) {
        return boxes.stream().flatMap(box -> box.getScaledBoxes(scale));
    }

    //    @NotNull
    //    private Stream<Timespan> temporallySpatiallyScaled(Collection<Timespan> boxes) {
    //        return boxes.stream().flatMap(box -> box.scaledBoxes(scale, timescale));
    //    }

    private <K, T> Stream<T> boxed(Map<K, Collection<T>> boxed, K box) {
        return Optional.ofNullable(boxed.get(box)).map(Collection::stream).stream().flatMap(s -> s);
    }

    private static HashMap<Class<? extends Entity>, Map<Id, Entity>> map(Stream<Entity> entities) {
        return entities.collect(Collectors.groupingBy(
                Entity::getClass,
                HashMap::new,
                Collectors.toMap(
                        Entity::getId,
                        Function.identity()
                )));
    }

    private static <K, V> void add(Map<K, Collection<V>> map, K key, V item) {
        map.computeIfAbsent(key, __ -> new HashSet<>()).add(item);
    }
}
