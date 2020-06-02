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
import stopnorway.geo.Scale;
import stopnorway.hash.AbstractHashable;
import stopnorway.util.Tuple;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DatabaseImpl extends AbstractHashable implements Database, Serializable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseImpl.class);

    private final Box box;

    private final Scale scale;

    private final Map<Id, Entity> entities;

    private final Map<Class<? extends Entity>, Map<Id, ? extends Entity>> typedEntities;

    private final Map<Box, Collection<ServiceLeg>> boxedServiceLegs;

    private final Map<Box, Collection<TripDefinition>> boxedTripDefinitions;

    private final List<ServiceLeg> serviceLegs;

    private final Map<Id, TripDefinition> tripDefinitions;

    private final List<ScheduledTrip> scheduledTrips;

    public DatabaseImpl(Map<Id, Entity> entities, Scale scale) {
        this(null, entities, scale);
    }

    public DatabaseImpl(Box box, Map<Id, Entity> entities, Scale scale) {
        this.box = box == null ? NORWAY : box;
        this.entities = Objects.requireNonNull(entities, "entities");
        this.scale = Objects.requireNonNull(scale, "scale");

        Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities = new HashMap<>();
        entities.forEach(
                (id, entity) ->
                        typedEntities.computeIfAbsent(entity.getClass(), type -> new HashMap<>())
                                .put(id, entity));
        this.typedEntities = Map.copyOf(typedEntities);

        log.info("{} builds from {} entities", this, entities.size());

        this.serviceLegs = stream(ServiceLink.class)
                .map(this::serviceLeg)
                .collect(Collectors.toList());
        log.info("{} collected {} service legs", this, serviceLegs.size());

        this.boxedServiceLegs = new HashMap<>();
        this.serviceLegs.forEach(leg -> leg.scaledBoxes(scale)
                .forEach(scaledBox -> with(this.boxedServiceLegs, scaledBox, leg)));

        this.tripDefinitions = stream(JourneyPattern.class)
                .map(this::servicePattern)
                .collect(Collectors.toMap(
                        TripDefinition::getJourneyPatternId,
                        Function.identity()
                ));
        this.boxedTripDefinitions = new HashMap<>();
        this.tripDefinitions.values()
                .forEach(def -> def.scaledBoxes(scale)
                        .forEach(scaledBox -> with(this.boxedTripDefinitions, scaledBox, def)));

        this.scheduledTrips = stream(ServiceJourney.class)
                .map(this::scheduledTrip)
                .sorted()
                .collect(Collectors.toList());

        log.info("{} indexed {} service legs in {} boxes", this, serviceLegs.size(), boxedTripDefinitions.size());
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        hash(h, entities.keySet());
    }

    @Override
    public ScheduledStopPoint getScheduledStopPoint(Id id) {
        return getEntity(ScheduledStopPoint.class, id);
    }

    @Override
    public Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes) {
        return scaled(boxes)
                .flatMap(scaledBox ->
                                 boxed(this.boxedServiceLegs, scaledBox))
                .filter(serviceLeg ->
                                overlapping(boxes, serviceLeg))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<TripDefinition> getTripDefinitions(Collection<Box> boxes) {
        return scaled(boxes)
                .flatMap(scaledBox ->
                                 boxed(this.boxedTripDefinitions, scaledBox))
                .filter(tripDefinition ->
                                overlapping(boxes, tripDefinition))
                .collect(Collectors.toList());
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append("ids: ").append(entities.size()).append(" scale: ").append(scale);
    }

    private ScheduledTrip scheduledTrip(ServiceJourney serviceJourney) {
        return new ScheduledTrip(
                serviceJourney.getId(),
                tripDefinitions.get(serviceJourney.getJourneyPatternRef()),
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

    @NotNull
    private ServiceLeg serviceLeg(ServiceLink serviceLink) {
        return new ServiceLeg(
                serviceLink.getId(),
                getScheduledStopPoint(serviceLink.getFromPoint()),
                getScheduledStopPoint(serviceLink.getToPoint()),
                serviceLink);
    }

    private <E extends Entity> Stream<E> stream(Class<E> type) {
        return list(type).stream();
    }

    private <E extends Entity> Collection<E> list(Class<E> type) {
        return getEntities(type).values();
    }

    @SuppressWarnings("unchecked") private <E extends Entity> Map<Id, E> getEntities(
            Class<E> type
    ) {
        return (Map<Id, E>) typedEntities.get(type);
    }

    private <T extends Entity> T getEntity(
            Class<T> type,
            Id id
    ) {
        if (id.is(type)) {
            Entity obj = entities.get(id);
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

    private TripDefinition servicePattern(JourneyPattern journeyPattern) {
        return new TripDefinition(
                journeyPattern.getId(),
                journeyPattern.getName(),
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
                log.warn("No service link found: " + journeyPattern + " => " + sequencedInJourneyPattern);
            }
            return new AbstractMap.SimpleEntry<>(
                    sequencedInJourneyPattern,
                    serviceLink == null ? null : new ServiceLeg(
                            serviceLink.getId(),
                            getScheduledStopPoint(serviceLink.getFromPoint()),
                            getScheduledStopPoint(serviceLink.getToPoint()),
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

    private static <K, V> Map<K, Collection<V>> with(Map<K, Collection<V>> map, K key, V item) {
        map.computeIfAbsent(key, __ -> new HashSet<>()).add(item);
        return map;
    }

    private static Tuple<ScheduledStopPoint> getFromTo(ServiceLeg serviceLeg) {
        return new Tuple<>(serviceLeg.getFrom(), serviceLeg.getTo());
    }
}
