package stopnorway.database;

import jdk.jshell.spi.SPIResolutionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.entur.*;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;
import stopnorway.hash.AbstractHashable;
import stopnorway.util.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DatabaseImpl extends AbstractHashable implements Database {

    private static final Logger log = LoggerFactory.getLogger(DatabaseImpl.class);

    private final Box box;
    private final Map<Id, Entity> entities;

    private final Scale scale;

    private final Map<Id, ScheduledStopPoint> scheduledStopPointMap;

    private final Map<Id, ServiceLink> serviceLinkMap;

    private final Map<ScheduledStopPoint, Collection<ServiceLeg>> serviceLegsFrom;

    private final Map<ScheduledStopPoint, Collection<ServiceLeg>> serviceLegsTo;

    private final Map<Tuple<ScheduledStopPoint>, Collection<ServiceLeg>> serviceLegsFromTo;

    private final Map<ScheduledStopPoint, Point> stopPointPoints;

    private final Map<Box, Collection<ServiceLeg>> boxedServiceLegs;
    private final List<ServiceLeg> serviceLegs;
    private Map<Id, JourneyPattern> journeyPatternMap;
    private List<ServicePattern> servicePatterns;

    public DatabaseImpl(Map<Id, Entity> entities, Scale scale) {
        this(null, entities, scale);
    }

    public DatabaseImpl(Box box, Map<Id, Entity> entities, Scale scale) {
        this.box = box == null ? NORWAY : box;
        this.entities = Objects.requireNonNull(entities, "entities");
        this.scale = Objects.requireNonNull(scale, "scale");

        log.info("{} builds from {} entities", this, entities.size());

        this.scheduledStopPointMap = map(ScheduledStopPoint.class);
        this.serviceLinkMap = map(ServiceLink.class);

        this.serviceLegs = serviceLinkMap.values().stream()
                .map(this::serviceLeg)
                .collect(Collectors.toList());
        log.info("{} collected {} service legs", this, serviceLegs.size());

        serviceLegsFrom = group(serviceLegs, ServiceLeg::getFrom);
        serviceLegsTo = group(serviceLegs, ServiceLeg::getTo);
        serviceLegsFromTo = group(serviceLegs, DatabaseImpl::getFromTo);

        journeyPatternMap = map(JourneyPattern.class);

        stopPointPoints = serviceLegsFrom.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                        e.getKey(),
                        e.getValue().stream().map(ServiceLeg::getStartPoint).flatMap(Optional::stream).findFirst()))
                .filter(e ->
                        e.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()));

        log.info("{} collected {} stop points", this, stopPointPoints.size());

        servicePatterns = journeyPatternMap.values().stream()
                .map(this::servicePattern)
                .collect(Collectors.toList());

        boxedServiceLegs = new ConcurrentHashMap<>();
        serviceLegs.forEach(leg ->
                leg.scaledBoxes(scale).forEach(scaledBox ->
                        boxedServiceLegs.computeIfAbsent(
                                scaledBox,
                                __ ->
                                        new HashSet<>()
                        ).add(leg))
        );

        log.info("{} indexed {} service legs in {} boxes", this, serviceLegs.size(), boxedServiceLegs.size());
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
        return scheduledStopPointMap.get(id);
    }

    @Override
    public <E extends Entity> Stream<E> get(Class<E> type) {
        return getAll(type);
    }

    @Override
    public Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes) {
        return scaled(boxes)
                .flatMap(this::serviceLegs)
                .filter(serviceLeg ->
                        boxes.stream().anyMatch(serviceLeg::overlaps))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ServicePattern> getServicePatterns(Collection<Box> boxes) {
        return null;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append("ids: ").append(entities.size()).append(" scale: ").append(scale);
    }

    @NotNull
    private ServiceLeg serviceLeg(ServiceLink serviceLink) {
        return new ServiceLeg(
                getScheduledStopPoint(serviceLink.getFromPoint()),
                getScheduledStopPoint(serviceLink.getToPoint()),
                serviceLink);
    }

    @NotNull
    private ServicePattern servicePattern(JourneyPattern journeyPattern) {
        return new ServicePattern(
                journeyPattern.getName(),
                stopPoints(journeyPattern),
                serviceLegs(journeyPattern));
    }

    private List<ScheduledStopPoint> stopPoints(JourneyPattern journeyPattern) {
        return journeyPattern.getPointsInSequence().stream()
                .sorted(Comparator.comparing(StopPointInJourneyPattern::getOrder))
                .map(StopPointInJourneyPattern::getScheduledStopPointRef)
                .map(scheduledStopPointMap::get)
                .collect(Collectors.toList());
    }

    private Collection<ServiceLeg> serviceLegs(JourneyPattern journeyPattern) {
        return journeyPattern.getLinksInSequence().stream()
                .sorted(Comparator.comparing(ServiceLinkInJourneyPattern::getOrder))
                .map(sequencedInJourneyPatter -> {
                    ServiceLink serviceLink = serviceLinkMap.get(sequencedInJourneyPatter.getServiceLinkRef());
                    return new ServiceLeg(
                            getScheduledStopPoint(serviceLink.getFromPoint()),
                            getScheduledStopPoint(serviceLink.getToPoint()),
                            serviceLink,
                            sequencedInJourneyPatter.getOrder());
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private Stream<Box> scaled(Collection<Box> boxes) {
        return boxes.stream()
                .map(box ->
                        box.getScaledBoxes(scale))
                .flatMap(Collection::stream);
    }

    private Stream<ServiceLeg> serviceLegs(Box scaledBox) {
        return Optional.ofNullable(boxedServiceLegs.get(scaledBox))
                .map(Collection::stream)
                .stream()
                .flatMap(s -> s);
    }

    private <K> HashMap<K, Collection<ServiceLeg>> group(Collection<ServiceLeg> legs, Function<ServiceLeg, K> key) {
        return legs.stream().collect(Collectors.groupingBy(key, HashMap::new,
                Collectors.collectingAndThen(
                        Collectors.toList(), serviceLegs ->
                                serviceLegs.stream().distinct().collect(Collectors.toList()))));
    }

    private <E extends Entity> Map<Id, E> map(Class<E> type) {
        return getAll(type)
                .collect(Collectors.toMap(
                        Entity::getId,
                        Function.identity(),
                        (e, e2) -> e,
                        LinkedHashMap::new));
    }

    @NotNull
    private <E extends Entity> Stream<E> getAll(Class<E> type) {
        return entities.values().stream()
                .filter(entity ->
                        entity.getId().getType().equals(type.getSimpleName()))
                .map(type::cast);
    }

    private static Tuple<ScheduledStopPoint> getFromTo(ServiceLeg serviceLeg) {
        return new Tuple<>(serviceLeg.getFrom(), serviceLeg.getTo());
    }
}
