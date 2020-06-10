/*
 *     This file is part of Unearth.
 *
 *     Unearth is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Unearth is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Unearth.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *     This file is part of Unearth.
 *
 *     Unearth is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Unearth is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Unearth.  If not, see <https://www.gnu.org/licenses/>.
 */

package stopnorway.hash;

import stopnorway.util.Print;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractHashable
        implements Hashable, Serializable {

    private static final String HASH = "MD5";

    private static final ThreadLocal<MessageDigest> MD5 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance(HASH);
        } catch (Exception e) {
            throw new IllegalStateException("Expected " + HASH + " implementation", e);
        }
    });

    private static final long serialVersionUID = -2993413752909173835L;

    private final AtomicReference<UUID> hash = new AtomicReference<>();

    private final AtomicReference<String> toString = new AtomicReference<>();

    @Override
    public final UUID getUuid() {

        return hash.updateAndGet(v -> v == null ? uuid() : v);
    }

    @Override
    public final int hashCode() {

        return getUuid().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {

        return obj == this || obj != null && obj.getClass() == getClass()
                && ((Hashed) obj).getUuid().equals(getUuid());
    }

    @Override
    public final String toString() {

        return toString.updateAndGet(v -> v == null ? build() : v);
    }

    protected static void hash(Consumer<byte[]> hash, byte[]... bytes) {

        for (byte[] bite : bytes) {
            hash.accept(bite);
        }
    }

    protected static void hash(Consumer<byte[]> hash, String... strings) {

        hashStrings(hash, Arrays.asList(strings));
    }

    protected static void hash(Consumer<byte[]> hash, Enum<?>... enums) {

        hashStrings(hash, Arrays.stream(enums).map(e -> e.getClass() + "." + e.name()));
    }

    protected static void hash(Consumer<byte[]> hash, int... values) {

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * values.length);
        for (Integer value : values) {
            if (value != null) {
                buffer.putInt(value);
            }
        }
        hash.accept(buffer.array());
    }

    protected static void hash(Consumer<byte[]> hash, float... values) {

        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * values.length);
        for (float value : values) {
            buffer.putFloat(value);
        }
        hash.accept(buffer.array());
    }

    protected static void hash(Consumer<byte[]> hash, double... values) {

        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * values.length);
        for (double value : values) {
            buffer.putDouble(value);
        }
        hash.accept(buffer.array());
    }

    protected static void hash(Consumer<byte[]> h, Hashable... hasheds) {

        hash(h, Arrays.asList(hasheds));
    }

    protected static void hash(Consumer<byte[]> h, Collection<? extends Hashable> hasheds) {

        for (Hashable hashable : hasheds) {
            if (hashable != null) {
                hashable.hashTo(h);
            }
        }
    }

    protected abstract StringBuilder withStringBody(StringBuilder sb);

    private String build() {

        return withStringContents(
                withStringIdentifier(
                        new StringBuilder(getClass().getSimpleName())
                                .append('['))
                        .append("<")
        ).append(">]").toString();
    }

    private StringBuilder withStringIdentifier(StringBuilder sb) {

        return sb.append(Print.uuid(getUuid()));
    }

    private UUID uuid() {

        MessageDigest md5 = md5();
        try {
            hashTo(md5::update);
            return UUID.nameUUIDFromBytes(md5.digest());
        } finally {
            md5.reset();
        }
    }

    private StringBuilder withStringContents(StringBuilder sb) {

        int length = sb.length();
        StringBuilder sb2 = sb.append(' ');
        StringBuilder sb3 = withStringBody(sb2);
        if (sb3.length() == sb2.length()) {
            return sb2.delete(length, length + 1);
        }
        return sb2;
    }

    private static MessageDigest md5() {
        return MD5.get();
    }

    private static void hashStrings(Consumer<byte[]> hash, Collection<String> strings) {

        hashStrings(hash, strings.stream());
    }

    private static void hashStrings(Consumer<byte[]> hash, Stream<String> stream) {
        stream
                .filter(Objects::nonNull)
                .forEach(s ->
                        hash.accept(s.getBytes(StandardCharsets.UTF_8)));
    }
}
