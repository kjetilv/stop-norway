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

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DatabaseImpl implements Database, Serializable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseImpl.class);

    private final Box box;

    private final Scale scale;

    private final Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities;

    private final Map<Id, TripDefinition> tripDefinitions;

    private final Map<Box, Collection<TripDefinition>> boxedTripDefinitions;

    private final Map<TripDefinition, Collection<ServiceJourney>> scheduledTrips;

    private final int size;

    public DatabaseImpl(Box box, Scale scale, Stream<Entity> entities) {
        this(box, scale, map(Objects.requireNonNull(entities, "entities")));
    }

    public DatabaseImpl(
            Box box,
            Scale scale,
            Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities
    ) {
        this.box = box == null ? Points.NORWAY_BOX : box;
        this.scale = scale == null ? Scale.DEFAULT : scale;
        this.typedEntities = typedEntities;
        this.size = (int) this.typedEntities.values().stream().mapToLong(Map::size).sum();
        log.info("{} built from {} entities", this, size);

        this.tripDefinitions = stream(JourneyPattern.class)
                .map(this::tripDefinitions)
                .collect(Collectors.toMap(
                        TripDefinition::getJourneyPatternId,
                        Function.identity()));

        this.boxedTripDefinitions = new HashMap<>();
        this.tripDefinitions.values()
                .forEach(def -> def.scaledBoxes(scale)
                        .forEach(scaledBox -> add(this.boxedTripDefinitions, scaledBox, def)));
        log.info("{} indexed {} trips in {} boxes", this, tripDefinitions.size(), boxedTripDefinitions.size());

        this.scheduledTrips = getEntities(ServiceJourney.class)
                .collect(Collectors.groupingBy(
                        serviceJourney -> tripDefinitions.get(serviceJourney.getJourneyPatternRef()),
                        HashMap::new,
                        Collectors.toCollection(ArrayList::new)));
        log.info("{} collected {} scheduled trips", this, this.scheduledTrips.size());
    }

    public Scale getScale() {
        return scale;
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public Collection<TripDefinition> getTripDefinitions(Collection<Box> boxes) {
        return streamTripDefinitions(boxes)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ScheduledTrip> getScheduledTrips(Collection<Box> boxes) {
        return streamTripDefinitions(boxes)
                .flatMap(tripDefinition -> scheduledTrips.get(tripDefinition).stream())
                .map(serviceJourney ->
                             scheduledTrip(serviceJourney, tripDefinitions::get))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Entity> getEntities() {
        return typedEntities.values().stream().map(Map::values).flatMap(Collection::stream);
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

    private Stream<TripDefinition> streamTripDefinitions(Collection<Box> boxes) {
        return scaled(boxes)
                .flatMap(scaledBox ->
                                 boxed(this.boxedTripDefinitions, scaledBox))
                .filter(tripDefinition ->
                                overlapping(boxes, tripDefinition))
                .distinct();
    }

    private ScheduledTrip scheduledTrip(ServiceJourney serviceJourney, Function<Id, TripDefinition> patterns) {
        return new ScheduledTrip(
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
                timetabledPassingTime.getDepartureTime());
    }

    private boolean overlapping(Collection<Box> boxes, Boxed boxable) {
        return boxes.stream().anyMatch(boxable::overlaps);
    }

    private <E extends Entity> Stream<E> stream(Class<E> type) {
        return list(type).stream();
    }

    private <E extends Entity> Collection<E> list(Class<E> type) {
        return getEntityMap(type).values();
    }

    private <E extends Entity> Stream<E> getEntities(Class<E> type) {
        return typedEntities.get(type).values().stream().map(type::cast);
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

    private TripDefinition tripDefinitions(JourneyPattern journeyPattern) {
        Route route = getEntity(Route.class, journeyPattern.getRouteRef());
        Line line = route == null ? null : getEntity(Line.class, route.getLineRef());
        return new TripDefinition(
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
    private Stream<Box> scaled(Collection<Box> boxes) {
        return boxes.stream().map(box -> box.getScaledBoxes(scale)).flatMap(Collection::stream);
    }

    private <T> Stream<T> boxed(Map<Box, Collection<T>> boxed, Box bo) {
        return Optional.ofNullable(boxed.get(bo)).map(Collection::stream).stream().flatMap(s -> s);
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
