package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class ServiceLinkSerializer extends AbstractSerializer<ServiceLink> {

    @Override
    public void write(Kryo kryo, Output output, ServiceLink object) {
        writeId(kryo, output, object);
        writeId(kryo, output, object.getFromPoint());
        writeId(kryo, output, object.getToPoint());
        writeString(output, object.getDistance());
        writeList(kryo, output, object.getProjections());
    }

    @Override
    public ServiceLink read(Kryo kryo, Input input, Class<? extends ServiceLink> type) {
        return new ServiceLink(
                readId(kryo, input),
                readId(kryo, input),
                readId(kryo, input),
                readString(input),
                readList(kryo, input, LinkSequenceProjection.class));
    }
}
