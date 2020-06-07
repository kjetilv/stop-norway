package stopnorway.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.entur.*;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatabaseImplSerializer extends AbstractSerializer<DatabaseImpl> {

    private static final List<Class<? extends Entity>> ENTITY_TYPES = Arrays.asList(
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
            Line.class);

    @Override
    public void write(Kryo kryo, Output output, DatabaseImpl object) {
        kryo.writeObject(output, object.getBox());
        kryo.writeObject(output, object.getScale());
        Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities = object.getTypedEntities();
        ENTITY_TYPES.forEach(
                entityType -> writeList(
                        kryo,
                        output,
                        typedEntities.get(entityType).values()));
    }

    @Override
    public DatabaseImpl read(Kryo kryo, Input input, Class<? extends DatabaseImpl> type) {
        Box box = kryo.readObject(input, Box.class);
        Scale scale = kryo.readObject(input, Scale.class);
        Map<Class<? extends Entity>, Map<Id, Entity>> typedEntities = new HashMap<>();
        ENTITY_TYPES.forEach(
                entityType -> typedEntities.put(
                        entityType,
                        readStream(kryo, input, entityType)
                                .collect(Collectors.toMap(
                                        Entity::getId,
                                        Function.identity()))));
        return new DatabaseImpl(box, scale, typedEntities);
    }

}
