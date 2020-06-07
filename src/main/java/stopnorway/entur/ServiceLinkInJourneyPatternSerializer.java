package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public class ServiceLinkInJourneyPatternSerializer extends AbstractSerializer<ServiceLinkInJourneyPattern> {

    @Override
    public void write(Kryo kryo, Output output, ServiceLinkInJourneyPattern object) {
        writeId(kryo, output, object);
        output.writeInt(object.getOrder());
        writeId(kryo, output, object.getServiceLinkRef());
    }

    @Override
    public ServiceLinkInJourneyPattern read(
            Kryo kryo,
            Input input,
            Class<? extends ServiceLinkInJourneyPattern> type
    ) {
        return new ServiceLinkInJourneyPattern(
                readId(kryo, input),
                input.readInt(),
                readId(kryo, input));
    }
}
