package stopnorway.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.entur.*;

public class JourneySpecificationSerializer extends AbstractSerializer<JourneySpecification> {

    @Override
    public void write(
            Kryo kryo, Output output, JourneySpecification object
    ) {
        writeId(kryo, output, object.getJourneyPatternId());
        writeString(output, object.getName());
        kryo.writeObject(output, object.getRoute());
        kryo.writeObject(output, object.getLine());
        writeMapEntries(kryo, output, object.getStopPoints());
        writeMapEntries(kryo, output, object.getServiceLegs());
    }

    @Override
    public JourneySpecification read(
            Kryo kryo, Input input, Class<? extends JourneySpecification> type
    ) {
        return new JourneySpecification(
                readId(kryo, input),
                readString(input),
                kryo.readObject(input, Route.class),
                kryo.readObject(input, Line.class),
                readMapEntries(kryo, input, StopPointInJourneyPattern.class, ScheduledStopPoint.class),
                readMapEntries(kryo, input, ServiceLinkInJourneyPattern.class, ServiceLeg.class));
    }
}
