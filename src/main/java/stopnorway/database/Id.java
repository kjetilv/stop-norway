package stopnorway.database;

import stopnorway.hash.AbstractHashable;

import java.util.Objects;
import java.util.function.Consumer;

public final class Id extends AbstractHashable {

    public static Id id(Enum<?> operator, String type, String id) {
        return id(operator.name(), type, id);
    }

    public static Id id(String operator, String type, String id) {
        return new Id(operator, type, id);
    }

    public static Id parse(String attribute) {
        int idMark = attribute.indexOf(':', OP_MARK + 1);
        return new Id(
            attribute.substring(0, OP_MARK),
            attribute.substring(OP_MARK + 1, idMark),
            attribute.substring(idMark + 1)
        );
    }

    private final String operator;

    private final String type;

    private final String id;

    public Id(Enum<?> operator, Class<? extends Entity> type, String id) {
        this(Objects.requireNonNull(operator, "operator").name(), type, id);
    }

    public Id(String operator, Class<? extends Entity> type, String id) {
        this(operator, Objects.requireNonNull(type, "type").getSimpleName(), id);
    }

    public Id(String operator, String type, String id) {

        this.operator = Objects.requireNonNull(operator, "operator");
        this.type = Objects.requireNonNull(type, "type");
        this.id = Objects.requireNonNull(id, "id");
    }

    public String getOperator() {
        return operator;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        hash(h, operator, id, type);
    }

    public boolean is(Class<? extends Entity> type) {
        return this.type.equals(type.getSimpleName());
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append(operator)
            .append(":").append(type)
            .append(":").append(id);
    }

    private static final int OP_MARK = 3;
}
