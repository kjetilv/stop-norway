package stopnorway.database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.Database;
import stopnorway.geo.Point;
import stopnorway.entur.RoutePoint;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;
import stopnorway.hash.AbstractHashable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DatabaseImpl extends AbstractHashable implements Database {

    private static final Logger log = LoggerFactory.getLogger(DatabaseImpl.class);

    private static final String SERVICE_LINK_TYPE = ServiceLink.class.getSimpleName();

    private static final String SCHEDULED_STOP_POINT_TYPE = ScheduledStopPoint.class.getSimpleName();

    private static final String ROUTE_POINT_TYPE = RoutePoint.class.getSimpleName();

    private final Box box;
    private final Map<Id, Entity> entities;

    private final Scale scale;

    private final Map<Id, ScheduledStopPoint> scheduledStopPointMap;

    private final Map<Id, ServiceLink> serviceLinkMap;

    private final Map<ScheduledStopPoint, Collection<ServiceLeg>> serviceLegsFrom;

    private final Map<ScheduledStopPoint, Collection<ServiceLeg>> serviceLegsTo;

    private final Map<ScheduledStopPoint, Point> stopPoints;

    private final Map<Box, Collection<ServiceLeg>> boxedServiceLegs;
    private final List<ServiceLeg> serviceLegs;

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

        this.serviceLegs = serviceLinkMap.values().stream().map(serviceLink ->
                new ServiceLeg(
                        getScheduledStopPoint(serviceLink.getFromPoint()),
                        getScheduledStopPoint(serviceLink.getToPoint()),
                        serviceLink))
                .collect(Collectors.toList());
        log.info("{} collected {} service legs", this, serviceLegs.size());

        serviceLegsFrom = group(serviceLegs, ServiceLeg::getFrom);
        serviceLegsTo = group(serviceLegs, ServiceLeg::getTo);

        stopPoints = serviceLegsFrom.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                        e.getKey(),
                        e.getValue().stream().map(ServiceLeg::getStartPoint).flatMap(Optional::stream).findFirst()))
                .filter(e ->
                        e.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()));

        log.info("{} collected {} stop points", this, stopPoints.size());

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
        return scheduledStopPointMap.get(typed(id, SCHEDULED_STOP_POINT_TYPE));
    }

    @Override
    public ServiceLink getServiceLink(Id id) {
        return serviceLinkMap.get(typed(id, SERVICE_LINK_TYPE));
    }

    @Override
    public <E extends Entity> Stream<E> get(Class<E> type) {
        return getAll(type);
    }

    @Override
    public Collection<ServiceLeg> getServiceLegs(Collection<Box> boxes) {
        return boxes.stream()
                .map(box ->
                        box.getScaledBoxes(scale))
                .flatMap(Collection::stream)
                .flatMap(this::serviceLegs)
                .filter(serviceLeg ->
                        boxes.stream().anyMatch(serviceLeg::overlaps))
                .collect(Collectors.toList());
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append("ids: ").append(entities.size()).append(" scale: ").append(scale);
    }

    private Stream<ServiceLeg> serviceLegs(Box scaledBox) {
        return Optional.ofNullable(boxedServiceLegs.get(scaledBox))
                .map(Collection::stream)
                .stream()
                .flatMap(s -> s);
    }

    private HashMap<ScheduledStopPoint, Collection<ServiceLeg>> group(
            Collection<ServiceLeg> legs,
            Function<ServiceLeg, ScheduledStopPoint> key
    ) {
        return legs.stream().collect(Collectors.groupingBy(
                key,
                HashMap::new,
                Collectors.collectingAndThen(
                        Collectors.toList(), serviceLegs -> serviceLegs)));
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

    private Id typed(Id id, String type) {
        if (id.getType().equals(type)) {
            return id;
        }
        throw new IllegalArgumentException("Not a " + type + " id: " + id);
    }
}
