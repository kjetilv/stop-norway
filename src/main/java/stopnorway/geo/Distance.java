package stopnorway.geo;

import java.util.Objects;

public final class Distance {

    private final long mm;

    private final Unit unit;

    private Distance(long mm, Unit unit) {

        this.mm = mm;
        this.unit = Objects.requireNonNull(unit, "unit");
        if (mm < 0) {
            throw new IllegalArgumentException("Invalid direction: " + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Distance && toMillis() == ((Distance) o).toMillis();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mm, unit);
    }

    public static Distance of(long count, Unit unit) {
        return new Distance(count, unit);
    }

    public static Distance of(double count, Unit unit) {
        return new Distance((long) (unit.containsNo(Unit.MM) * count), Unit.MM);
    }

    public Distance mult(double value) {
        return Distance.of(this.mm * value, Unit.MM);
    }

    @Override
    public String toString() {
        return mm + unit.name().toLowerCase();
    }

    public long getMm() {
        return mm;
    }

    public Unit getUnit() {
        return unit;
    }

    public double toMeters() {
        return to(Unit.M);
    }

    public double to(Unit unit) {
        return toMillis() * 1.0 / unit.containsNo(Unit.MM);
    }

    public long toMillis() {
        int scale = this.unit.containsNo(Unit.MM);
        Distance millis = Distance.of(1.0d * mm * scale, Unit.MM);
        return millis.getMm();
    }
}
