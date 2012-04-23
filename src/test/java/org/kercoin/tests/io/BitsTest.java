package org.kercoin.tests.io;

import static org.fest.assertions.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.kercoin.tests.io.Bits;

public class BitsTest {

    @Test
    public void writeLongShouldRespectDataOutputStreamFormat()
            throws IOException {
        // given
        byte[] bytes = Bits.writeLong(142L);

        // when
        long l = new DataInputStream(new ByteArrayInputStream(bytes))
                .readLong();

        // then
        assertThat(l).isEqualTo(142L);
    }

    @Test
    public void writeLongShouldRespectDataOutputStreamFormat_0copy()
            throws IOException {
        // given
        byte[] bytes = new byte[Bits.SIZEOF_LONG + 2];
        Bits.writeLong(142L, bytes, 2);

        // when
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        in.skip(2);
        long l = new DataInputStream(in).readLong();

        // then
        assertThat(l).isEqualTo(142L);
    }

    @Test
    public void readLongShouldRespectDataInputStreamFormat() throws IOException {
        // given
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(baos);
        out.writeLong(142L);
        byte[] bytes = baos.toByteArray();

        // when
        long l = Bits.readLong(bytes);

        // then
        assertThat(l).isEqualTo(142L);
    }

    @Test
    public void writeIntShouldRespectDataOutputStreamFormat()
            throws IOException {
        // given
        byte[] bytes = Bits.writeInt(142);

        // when
        int l = new DataInputStream(new ByteArrayInputStream(bytes)).readInt();

        // then
        assertThat(l).isEqualTo(142);
    }

    @Test
    public void writeIntShouldRespectDataOutputStreamFormat_0copy()
            throws IOException {
        // given
        byte[] bytes = new byte[Bits.SIZEOF_INT + 2];
        Bits.writeInt(142, bytes, 2);

        // when
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        in.skip(2);
        int l = new DataInputStream(in).readInt();

        // then
        assertThat(l).isEqualTo(142);
    }

    @Test
    public void readIntShouldRespectDataInputStreamFormat() throws IOException {
        // given
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(142);
        byte[] bytes = baos.toByteArray();

        // when
        long l = Bits.readInt(bytes);

        // then
        assertThat(l).isEqualTo(142L);
    }

    @Test
    public void readIntFromWideArray() throws Exception {
        byte[] data = new byte[] { 0, 0, 0, 1, 1 };
        // when
        assertThat(Bits.readInt(data, 0)).isEqualTo(1);
        assertThat(Bits.readInt(data, 1)).isEqualTo(257);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void readIntFromNarrowArray() throws Exception {
        // when
        Bits.readInt(new byte[] { 0, 0, 0 });
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void readIntFromNarrowArray_offset() throws Exception {
        // when
        Bits.readInt(new byte[] { 0, 0, 0, 0 }, 1);
    }

    @Test
    public void readLongFromWideArray() throws Exception {
        byte[] data = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1, 1 };
        // when
        assertThat(Bits.readLong(data, 0)).isEqualTo(1);
        assertThat(Bits.readLong(data, 1)).isEqualTo(257);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void readLongFromNarrowArray() throws Exception {
        // when
        Bits.readLong(new byte[] { 1, 2, 3, 4, 5, 6, 7 });
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void readLongFromNarrowArray_offset() throws Exception {
        // when
        Bits.readLong(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 }, 1);
    }

}
