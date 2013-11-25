/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.deprecated;

import java.math.BigInteger;

/**
 *
 * @author jorge
 */
public class XXHash {

    private static final int PRIME1 = (int) 2654435761L;
    private static final int PRIME2 = (int) 2246822519L;
    private static final int PRIME3 = (int) 3266489917L;
    private static final int PRIME4 = 668265263;
    private static final int PRIME5 = 0x165667b1;

    public static int digestSmall(byte[] data, int seed, boolean bigendian) {

        final EndianReader er = bigendian ? BEReader : LEReader;
        final int len = data.length;
        final int bEnd = len;
        final int limit = bEnd - 4;

        int idx = seed + PRIME1;
        int crc = PRIME5;
        int i = 0;

        while (i < limit) {
            crc += er.toInt(data, i) + (idx++);
            crc += Integer.rotateLeft(crc, 17) * PRIME4;
            crc *= PRIME1;
            i += 4;
        }

        while (i < bEnd) {
            crc += (data[i] & 0xFF) + (idx++);
            crc *= PRIME1;
            i++;
        }

        crc += len;

        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc ^= crc >>> 13;
        crc *= PRIME3;
        crc ^= crc >>> 16;

        return crc;

    }

    public static int digestFast32(byte[] data, int seed, boolean bigendian) {

        final int len = data.length;

        if (len < 16) {
            return digestSmall(data, seed, bigendian);
        }

        final EndianReader er = bigendian ? BEReader : LEReader;
        final int bEnd = len;
        final int limit = bEnd - 16;
        int v1 = seed + PRIME1;
        int v2 = v1 * PRIME2 + len;
        int v3 = v2 * PRIME3;
        int v4 = v3 * PRIME4;

        int i = 0;
        int crc = 0;
        while (i < limit) {
            v1 = Integer.rotateLeft(v1, 13) + er.toInt(data, i);
            i += 4;
            v2 = Integer.rotateLeft(v2, 11) + er.toInt(data, i);
            i += 4;
            v3 = Integer.rotateLeft(v3, 17) + er.toInt(data, i);
            i += 4;
            v4 = Integer.rotateLeft(v4, 19) + er.toInt(data, i);
            i += 4;
        }

        i = bEnd - 16;
        v1 += Integer.rotateLeft(v1, 17);
        v2 += Integer.rotateLeft(v2, 19);
        v3 += Integer.rotateLeft(v3, 13);
        v4 += Integer.rotateLeft(v4, 11);

        v1 *= PRIME1;
        v2 *= PRIME1;
        v3 *= PRIME1;
        v4 *= PRIME1;

        v1 += er.toInt(data, i);
        i += 4;
        v2 += er.toInt(data, i);
        i += 4;
        v3 += er.toInt(data, i);
        i += 4;
        v4 += er.toInt(data, i);

        v1 *= PRIME2;
        v2 *= PRIME2;
        v3 *= PRIME2;
        v4 *= PRIME2;

        v1 += Integer.rotateLeft(v1, 11);
        v2 += Integer.rotateLeft(v2, 17);
        v3 += Integer.rotateLeft(v3, 19);
        v4 += Integer.rotateLeft(v4, 13);

        v1 *= PRIME3;
        v2 *= PRIME3;
        v3 *= PRIME3;
        v4 *= PRIME3;

        crc = v1 + Integer.rotateLeft(v2, 3) + Integer.rotateLeft(v3, 6) + Integer.rotateLeft(v4, 9);
        crc ^= crc >>> 11;
        crc += (PRIME4 + len) * PRIME1;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc ^= crc >>> 13;

        return crc;
    }

    public static int digestStrong32(byte[] data, int seed, boolean bigendian) {

        final int len = data.length;

        if (len < 16) {
            return digestSmall(data, seed, bigendian);
        }

        final EndianReader er = bigendian ? BEReader : LEReader;
        final int bEnd = len;
        final int limit = bEnd - 16;
        int v1 = seed + PRIME1;
        int v2 = v1 * PRIME2 + len;
        int v3 = v2 * PRIME3;
        int v4 = v3 * PRIME4;

        int i = 0;
        int crc = 0;

        while (i < limit) {
            v1 += Integer.rotateLeft(v1, 13);
            v1 *= PRIME1;
            v1 += er.toInt(data, i);
            i += 4;

            v2 += Integer.rotateLeft(v2, 11);
            v2 *= PRIME1;
            v2 += er.toInt(data, i);
            i += 4;

            v3 += Integer.rotateLeft(v3, 17);
            v3 *= PRIME1;
            v3 += er.toInt(data, i);
            i += 4;

            v4 += Integer.rotateLeft(v4, 19);
            v4 *= PRIME1;
            v4 += er.toInt(data, i);
            i += 4;

        }

        i = bEnd - 16;
        v1 += Integer.rotateLeft(v1, 17);
        v2 += Integer.rotateLeft(v2, 19);
        v3 += Integer.rotateLeft(v3, 13);
        v4 += Integer.rotateLeft(v4, 11);

        v1 *= PRIME1;
        v2 *= PRIME1;
        v3 *= PRIME1;
        v4 *= PRIME1;

        v1 += er.toInt(data, i);
        i += 4;
        v2 += er.toInt(data, i);
        i += 4;
        v3 += er.toInt(data, i);
        i += 4;
        v4 += er.toInt(data, i);

        v1 *= PRIME2;
        v2 *= PRIME2;
        v3 *= PRIME2;
        v4 *= PRIME2;

        v1 += Integer.rotateLeft(v1, 11);
        v2 += Integer.rotateLeft(v2, 17);
        v3 += Integer.rotateLeft(v3, 19);
        v4 += Integer.rotateLeft(v4, 13);

        v1 *= PRIME3;
        v2 *= PRIME3;
        v3 *= PRIME3;
        v4 *= PRIME3;

        crc = v1 + Integer.rotateLeft(v2, 3) + Integer.rotateLeft(v3, 6) + Integer.rotateLeft(v4, 9);
        crc ^= crc >>> 11;
        crc += (PRIME4 + len) * PRIME1;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc ^= crc >>> 13;

        return crc;
    }

    public static interface EndianReader {

        int toInt(byte[] b, int i);

        long toLong(byte[] b, int i);
    }

    private static class LittleEndianReader implements EndianReader {

        public int toInt(byte[] b, int i) {
            return toIntLE(b, i);
        }

        public long toLong(byte[] b, int i) {
            return toLongLE(b, i);
        }
    }

    private static class BigEndianReader implements EndianReader {

        public int toInt(byte[] b, int i) {
            return toIntBE(b, i);
        }

        public long toLong(byte[] b, int i) {
            return toLongBE(b, i);
        }
    }

    public static final LittleEndianReader LEReader = new LittleEndianReader();
    public static final BigEndianReader BEReader = new BigEndianReader();

    /**
     * byte配列のi番目から4バイト読み取り、BigEndianとみなした整数を返す。
     *
     * @param b データ
     * @param i オフセット
     * @return BigEndianとみなした整数
     */
    public static int toIntBE(byte[] b, int i) {
        return (((b[i + 0] & 255) << 24) + ((b[i + 1] & 255) << 16) + ((b[i + 2] & 255) << 8) + ((b[i + 3] & 255) << 0));
    }

    /**
     * byte配列のi番目から4バイト読み取り、LittleEndianとみなした整数を返す。
     *
     * @param b データ
     * @param i オフセット
     * @return LittleEndianとみなした整数
     */
    public static int toIntLE(byte[] b, int i) {
        return (((b[i + 3] & 255) << 24) + ((b[i + 2] & 255) << 16) + ((b[i + 1] & 255) << 8) + ((b[i + 0] & 255) << 0));
    }

    /**
     * byte配列のi番目から8バイト読み取り、BigEndianとみなした整数を返す。
     *
     * @param b データ
     * @param i オフセット
     * @return BigEndianとみなした整数
     */
    public static long toLongBE(byte[] b, int i) {

        return (((long) b[i + 0] << 56)
                + ((long) (b[i + 1] & 255) << 48)
                + ((long) (b[i + 2] & 255) << 40)
                + ((long) (b[i + 3] & 255) << 32)
                + ((long) (b[i + 4] & 255) << 24)
                + ((b[i + 5] & 255) << 16)
                + ((b[i + 6] & 255) << 8)
                + ((b[i + 7] & 255) << 0));

    }

    /**
     * byte配列のi番目から8バイト読み取り、LittleEndianとみなした整数を返す。
     *
     * @param b データ
     * @param i オフセット
     * @return LittleEndianとみなした整数
     */
    public static long toLongLE(byte[] b, int i) {

        return (((long) b[i + 7] << 56)
                + ((long) (b[i + 6] & 255) << 48)
                + ((long) (b[i + 5] & 255) << 40)
                + ((long) (b[i + 4] & 255) << 32)
                + ((long) (b[i + 3] & 255) << 24)
                + ((b[i + 2] & 255) << 16)
                + ((b[i + 1] & 255) << 8)
                + ((b[i + 0] & 255) << 0));

    }

    /**
     * 8バイト整数をビッグエンディアンのバイト配列に変換。
     *
     * @param v
     * @return
     */
    public static byte[] toBytesBE(long v) {
        return new byte[]{
            (byte) (v >>> 56),
            (byte) (v >>> 48),
            (byte) (v >>> 40),
            (byte) (v >>> 32),
            (byte) (v >>> 24),
            (byte) (v >>> 16),
            (byte) (v >>> 8),
            (byte) (v >>> 0),};
    }

    /**
     * 4バイト整数をビッグエンディアンのバイト配列に変換。
     *
     * @param v
     * @return
     */
    public static byte[] toBytesBE(int v) {
        return new byte[]{
            (byte) ((v >>> 24) & 0xFF),
            (byte) ((v >>> 16) & 0xFF),
            (byte) ((v >>> 8) & 0xFF),
            (byte) ((v >>> 0) & 0xFF)
        };
    }

    /**
     * unsignedな値に変換する。
     *
     * @param value signed-int値
     * @return unsigned-int値
     */
    public static long toUnsigned(int value) {
        return 0xffffffffL & value;
    }

    /**
     * unsignedな値に変換する。
     *
     * @param value
     * @return
     */
    public static BigInteger toUnsigned(long value) {
        byte[] v = toBytesBE(value);
        byte[] vv = new byte[v.length + 1];
        System.arraycopy(v, 0, vv, 1, v.length);
        return new BigInteger(vv);
    }

    /**
     * unsignedな値に変換する。
     *
     * @param values
     * @return
     */
    public static BigInteger toUnsigned(int[] values) {

        byte[] buffer = new byte[values.length * 4 + 1];
        for (int i = 0; i < values.length; i++) {
            byte[] ival = toBytesBE(values[i]);
            System.arraycopy(ival, 0, buffer, i * 4 + 1, 4);
        }

        return new BigInteger(buffer);

    }

    /**
     * unsignedな値に変換する。
     *
     * @param values
     * @return
     */
    public static BigInteger toUnsigned(long[] values) {

        byte[] buffer = new byte[values.length * 8 + 1];
        for (int i = 0; i < values.length; i++) {
            byte[] ival = toBytesBE(values[i]);
            System.arraycopy(ival, 0, buffer, i * 8 + 1, 8);
        }

        return new BigInteger(buffer);

    }

}
