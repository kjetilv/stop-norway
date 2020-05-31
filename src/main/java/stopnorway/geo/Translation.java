package stopnorway.geo;

import java.util.Objects;

public final class Translation {

    private final Direction direction;

    private final Distance distance;

    public Translation(Direction direction, Distance distance) {

        this.direction = Objects.requireNonNull(direction, "direction");
        this.distance = Objects.requireNonNull(distance, "distance");
    }

    public Direction getDirection() {
        return direction;
    }

    public Distance getDistance() {
        return distance;
    }

    public static Translation towards(Direction direction, Distance distance) {
        return new Translation(direction, distance);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Translation &&
                direction == ((Translation) o).direction &&
                Objects.equals(distance, ((Translation) o).distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, distance);
    }
}
