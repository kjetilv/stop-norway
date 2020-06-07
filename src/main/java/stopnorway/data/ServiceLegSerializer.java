package stopnorway.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;

public final class ServiceLegSerializer extends AbstractSerializer<ServiceLeg> {

    @Override
    public void write(
            Kryo kryo, Output output, ServiceLeg object
    ) {
        writeId(kryo, output, object.getServiceLinkId());
        kryo.writeObject(output, object.getStartPoint());
        kryo.writeObject(output, object.getEndPoint());
        kryo.writeObject(output, object.getServiceLink());
    }

    @Override
    public ServiceLeg read(
            Kryo kryo, Input input, Class<? extends ServiceLeg> type
    ) {
        return new ServiceLeg(
                readId(kryo, input),
                kryo.readObject(input, ScheduledStopPoint.class),
                kryo.readObject(input, ScheduledStopPoint.class),
                kryo.readObject(input, ServiceLink.class));
    }
}
