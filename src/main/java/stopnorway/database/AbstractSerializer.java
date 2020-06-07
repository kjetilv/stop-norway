package stopnorway.database;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractSerializer<S> extends Serializer<S> {

    protected <K, V> void writeMapEntries(Kryo kryo, Output output, Collection<Map.Entry<K, V>> ts) {
        output.writeVarInt(ts.size(), true);
        ts.forEach(t -> {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getValue());
        });
    }

    protected <K, V> Collection<Map.Entry<K, V>> readMapEntries(
            Kryo kryo,
            Input input,
            Class<? extends K> k,
            Class<? extends V> v
    ) {
        return IntStream.range(0, input.readInt(true))
                .mapToObj(i -> new AbstractMap.SimpleEntry<K, V>(
                        kryo.readObject(input, k),
                        kryo.readObject(input, v)))
                .collect(Collectors.toList());
    }

    protected <T> void writeList(Kryo kryo, Output output, Collection<? extends T> ts) {
        int size = ts.size();
        output.writeVarInt(size, true);
        ts.forEach(t -> kryo.writeObject(output, t));
    }

    protected <T> Collection<T> readList(Kryo kryo, Input input, Class<? extends T> type) {
        Stream<? extends T> stream = readStream(kryo, input, type);
        return stream.collect(Collectors.toList());
    }

    @NotNull
    protected <T> Stream<? extends T> readStream(Kryo kryo, Input input, Class<? extends T> type) {
        int size = input.readInt(true);
        return IntStream.range(0, size).mapToObj(i -> kryo.readObject(input, type));
    }

    protected void writeId(Kryo kryo, Output output, Entity entity) {
        writeId(kryo, output, entity.getId());
    }

    protected void writeId(Kryo kryo, Output output, Id id) {
        kryo.writeObject(output, id);
    }

    protected Id readId(Kryo kryo, Input input) {
        return kryo.readObject(input, Id.class);
    }

    protected void writeNullableId(Kryo kryo, Output output, Id id) {
        kryo.writeObjectOrNull(output, id, Id.class);
    }

    protected Id readNullableId(Kryo kryo, Input input) {
        return kryo.readObjectOrNull(input, Id.class);
    }

    protected void writeString(Output output, String name) {
        output.writeString(name);
    }

    protected String readString(Input input) {
        return input.readString();
    }
}
