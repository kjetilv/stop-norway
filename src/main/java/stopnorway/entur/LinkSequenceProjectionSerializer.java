package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.database.Id;
import stopnorway.geo.CodedPoint;
import stopnorway.geo.Point;

import java.util.Collection;

public final class LinkSequenceProjectionSerializer extends AbstractSerializer<LinkSequenceProjection> {

    @Override
    public void write(Kryo kryo, Output output, LinkSequenceProjection object) {
        writeId(kryo, output, object.getId());
        writeList(kryo, output, object.getTrajectory());
    }

    @Override
    public LinkSequenceProjection read(Kryo kryo, Input input, Class<? extends LinkSequenceProjection> type) {
        Id id = readId(kryo, input);
        Collection<Point> points = readList(kryo, input, CodedPoint.class);
        return new LinkSequenceProjection(id, points);
    }
}
