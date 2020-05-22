package stopnorway.hash;

import java.util.UUID;

public final class Print {

    private static final int K = 1_024;

    private static final int M = K * K;

    private Print() {

    }

    public static String uuid(UUID uuid) {

        String s = uuid.toString();
        int endIndex = s.indexOf('-');
        return endIndex < 0 ? s : s.substring(0, endIndex);
    }

    public static String bytes(long bytes) {

        if (bytes > 10 * M) {
            return String.format("%dMb", bytes / M);
        }
        if (bytes > M) {
            return String.format("%.1fMb", bytes * 10 / M / 10.0D);
        }
        if (bytes > K) {
            return String.format("%dKb", bytes / K);
        }
        return String.format("%db", bytes);
    }

    private static String mul(int min, String er) {

        return min > 1 ? er : "";
    }
}
