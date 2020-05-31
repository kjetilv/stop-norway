package stopnorway.geo;

public enum Unit {

    MM {
        @Override
        public int containsNo(Unit unit) {
            return unit == Unit.MM ? 1 : 0;
        }
    },

    CM {
        @Override
        public int containsNo(Unit unit) {
            if (unit == this) {
                return 1;
            }
            return unit == Unit.MM ? 10 : 0;
        }
    },

    M {
        @Override
        public int containsNo(Unit unit) {
            if (unit == this) {
                return 1;
            }
            switch (unit) {
                case MM:
                    return 1000;
                case CM:
                    return 100;
            }
            return 0;
        }
    },

    KM {
        @Override
        public int containsNo(Unit unit) {
            if (unit == this) {
                return 1;
            }
            switch (unit) {
                case MM:
                    return 1_000_000;
                case CM:
                    return 100_000;
            }
            return 1_000;
        }
    };

    public int get(Unit unit) {
        return containsNo(unit);
    }

    public final double toMeters(long value) {
        return to(M, value);
    }

    public double to(Unit m, long value) {
        return 1.0d * value / m.containsNo(this);
    }

    public abstract int containsNo(Unit unit);
}
