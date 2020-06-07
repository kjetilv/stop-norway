package stopnorway.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLinkInJourneyPattern;
import stopnorway.entur.StopPointInJourneyPattern;

public class TripDefinitionSerializer extends AbstractSerializer<TripDefinition> {

    @Override
    public void write(
            Kryo kryo, Output output, TripDefinition object
    ) {
        writeId(kryo, output, object.getJourneyPatternId());
        writeString(output, object.getName());
        writeMapEntries(kryo, output, object.getStopPoints());
        writeMapEntries(kryo, output, object.getServiceLegs());
    }

    @Override
    public TripDefinition read(
            Kryo kryo, Input input, Class<? extends TripDefinition> type
    ) {
        return new TripDefinition(
                readId(kryo, input),
                readString(input),
                readMapEntries(kryo, input, StopPointInJourneyPattern.class, ScheduledStopPoint.class),
                readMapEntries(kryo, input, ServiceLinkInJourneyPattern.class, ServiceLeg.class));
    }
}
