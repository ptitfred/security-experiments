package org.kercoin.tests.io;

public final class Bits {

    private Bits() {
    }

    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_LONG = 8;

    public static byte[] writeLong(long value) {
        byte buffer[] = new byte[SIZEOF_LONG];
        writeLong(value, buffer, ZERO_OFFSET);
        return buffer;
    }

    public static void writeLong(long value, byte[] buffer, int offset) {
        buffer[offset + BYTE0] = (byte) (value >>> BYTE7_POS);
        buffer[offset + BYTE1] = (byte) (value >>> BYTE6_POS);
        buffer[offset + BYTE2] = (byte) (value >>> BYTE5_POS);
        buffer[offset + BYTE3] = (byte) (value >>> BYTE4_POS);
        buffer[offset + BYTE4] = (byte) (value >>> BYTE3_POS);
        buffer[offset + BYTE5] = (byte) (value >>> BYTE2_POS);
        buffer[offset + BYTE6] = (byte) (value >>> BYTE1_POS);
        buffer[offset + BYTE7] = (byte) (value >>> BYTE0_POS);
    }

    public static long readLong(byte[] buffer) {
        return readLong(buffer, ZERO_OFFSET);
    }

    public static long readLong(byte[] buffer, int offset) {
        return (((long) buffer[offset + BYTE0] << BYTE7_POS)
                + ((long) (buffer[offset + BYTE1] & BYTE_CAP) << BYTE6_POS)
                + ((long) (buffer[offset + BYTE2] & BYTE_CAP) << BYTE5_POS)
                + ((long) (buffer[offset + BYTE3] & BYTE_CAP) << BYTE4_POS)
                + ((long) (buffer[offset + BYTE4] & BYTE_CAP) << BYTE3_POS)
                + ((buffer[offset + BYTE5] & BYTE_CAP) << BYTE2_POS)
                + ((buffer[offset + BYTE6] & BYTE_CAP) << BYTE1_POS)
                + ((buffer[offset + BYTE7] & BYTE_CAP) << BYTE0_POS));
    }

    public static byte[] writeInt(int value) {
        byte buffer[] = new byte[SIZEOF_INT];
        writeInt(value, buffer, ZERO_OFFSET);
        return buffer;
    }

    public static void writeInt(int value, byte[] buffer, int offset) {
        buffer[offset + BYTE0] = (byte) ((value >>> BYTE3_POS) & BYTE_CAP);
        buffer[offset + BYTE1] = (byte) ((value >>> BYTE2_POS) & BYTE_CAP);
        buffer[offset + BYTE2] = (byte) ((value >>> BYTE1_POS) & BYTE_CAP);
        buffer[offset + BYTE3] = (byte) ((value >>> BYTE0_POS) & BYTE_CAP);
    }

    public static int readInt(byte[] buffer) {
        return readInt(buffer, ZERO_OFFSET);
    }

    public static int readInt(byte[] buffer, int offset) {
        return (buffer[offset + BYTE0] << BYTE3_POS)
                + ((buffer[offset + BYTE1] & BYTE_CAP) << BYTE2_POS)
                + ((buffer[offset + BYTE2] & BYTE_CAP) << BYTE1_POS)
                + ((buffer[offset + BYTE3] & BYTE_CAP) << BYTE0_POS);
    }

    private static final int BYTE_CAP = 0xFF;

    private static final int ZERO_OFFSET = 0;

    private static final int BYTE0 = 0;
    private static final int BYTE1 = 1;
    private static final int BYTE2 = 2;
    private static final int BYTE3 = 3;
    private static final int BYTE4 = 4;
    private static final int BYTE5 = 5;
    private static final int BYTE6 = 6;
    private static final int BYTE7 = 7;

    private static final int BYTE0_POS = 0;
    private static final int BYTE1_POS = 8;
    private static final int BYTE2_POS = 16;
    private static final int BYTE3_POS = 24;
    private static final int BYTE4_POS = 32;
    private static final int BYTE5_POS = 40;
    private static final int BYTE6_POS = 48;
    private static final int BYTE7_POS = 56;

}
