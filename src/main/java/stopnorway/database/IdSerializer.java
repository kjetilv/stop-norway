package stopnorway.database;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class IdSerializer extends AbstractSerializer<Id> {

    private final Map<String, Integer> opOrdinals;

    private final Map<Integer, String> opNames;

    private final Map<String, Integer> classBytes;

    private final Map<Integer, String> byteClasses;

    public IdSerializer(Class<? extends Enum<?>> opType, Class<?>... classes) {

        Enum<?>[] ops = Objects.requireNonNull(opType, "opType").getEnumConstants();
        opOrdinals = Arrays.stream(ops).collect(Collectors.toMap(Enum::name, Enum::ordinal));
        opNames = Arrays.stream(ops).collect(Collectors.toMap(Enum::ordinal, Enum::name));

        AtomicInteger typeIndex = new AtomicInteger();
        classBytes = Arrays.stream(classes).collect(Collectors.toMap(
                Class::getSimpleName,
                type -> typeIndex.getAndIncrement()));

        AtomicInteger numberIndex = new AtomicInteger();
        this.byteClasses = Arrays.stream(classes).collect(Collectors.toMap(
                type -> numberIndex.getAndIncrement(),
                Class::getSimpleName));
    }

    @Override
    public void write(Kryo kryo, Output output, Id object) {
        Integer opNo = opOrdinals.get(object.getOperator());
        Integer classNo = classBytes.get(object.getType());
        String id = object.getId();
        output.writeVarInt(opNo, true);
        output.writeVarInt(classNo, true);
        writeString(output, id);
    }

    @Override
    public Id read(Kryo kryo, Input input, Class<? extends Id> type) {
        int opNo = input.readVarInt(true);
        int classNo = input.readVarInt(true);
        String id = readString(input);
        try {
            return Id.id(
                    opNames.get(opNo),
                    byteClasses.get(classNo),
                    id);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to find " +
                            "op#" + opNo + ":class#" + classNo + ":" + id +
                            ": " + opNames + ", " + byteClasses, e);
        }
    }
}
