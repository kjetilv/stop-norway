package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class ServiceJourneySerializer extends AbstractSerializer<ServiceJourney> {

    @Override
    public void write(Kryo kryo, Output output, ServiceJourney object) {
        writeId(kryo, output, object);
        writeString(output, object.getTransportMode());
        writeString(output, object.getName());
        writeNullableId(kryo, output, object.getJourneyPatternRef());
        writeNullableId(kryo, output, object.getLineRef());
        writeList(kryo, output, object.getPassingTimes());
    }

    @Override
    public ServiceJourney read(Kryo kryo, Input input, Class<? extends ServiceJourney> type) {
        return new ServiceJourney(
                readId(kryo, input),
                readString(input),
                readString(input),
                readNullableId(kryo, input),
                readNullableId(kryo, input),
                readList(kryo, input, TimetabledPassingTime.class));
    }
}
