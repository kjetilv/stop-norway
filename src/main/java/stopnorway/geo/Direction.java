package stopnorway.geo;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import static stopnorway.geo.Direction.Delta.*;

enum Direction {

    NORTH(_1, _0),

    NORTH_EAST(_1_4, MIN_1_4),

    EAST(_0, _1),

    SOUTH_EAST(MIN_1_4, _1_4),

    SOUTH(MIN_1, _0),

    SOUTH_WEST(MIN_1_4, MIN_1_4),

    WEST(MIN_1, _0),

    NORTH_WEST(MIN_1_4, _1_4);

    private final Delta lat;

    private final Delta lon;

    Direction(
            Delta lat,
            Delta lon
    ) {
        this.lat = lat;
        this.lon = lon;
    }

    public Delta lat() {
        return lat;
    }

    public Delta lon() {
        return lon;
    }

    enum Delta implements DoubleUnaryOperator, LongFunction<Double>, IntFunction<Double> {

        _0 {
            @Override
            public double applyAsDouble(double value) {
                return .0D;
            }
        },

        _1 {
            @Override
            public double applyAsDouble(double value) {
                return value;
            }
        },

        _1_4 {
            @Override
            public double applyAsDouble(double value) {
                return SQRT * value;
            }
        },

        MIN_1 {
            @Override
            public double applyAsDouble(double value) {
                return -value;
            }
        },

        MIN_1_4 {
            @Override
            public double applyAsDouble(double value) {
                return -SQRT * value;
            }
        };

        static final double SQRT = Math.sqrt(2.0);

        @Override
        public Double apply(long value) {
            return applyAsDouble(value);
        }

        @Override
        public Double apply(int value) {
            return applyAsDouble(value);
        }
    }
}
