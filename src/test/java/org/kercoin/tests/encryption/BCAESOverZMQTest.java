package org.kercoin.tests.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kercoin.tests.io.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class BCAESOverZMQTest {

    private static final Logger log = LoggerFactory.getLogger(BCAESOverZMQTest.class);

    Socket push;
    Socket pull;

    BufferedBlockCipher cipher;
    BufferedBlockCipher decipher;

    @Before
    public void setup() {
        Context ctx = ZMQ.context(1);
        push = ctx.socket(ZMQ.PUSH);
        pull = ctx.socket(ZMQ.PULL);
        String url = ZMQHelper.getInprocUrl(this, "aes-bc");
        pull.bind(url);
        push.connect(url);

        cipher = newAESCipher();
        decipher = newAESCipher();
        CipherParameters params = randomCipherParameters();
        decipher.init(false, params);
        cipher.init(true, params);
    }

    BufferedBlockCipher newAESCipher() {
        return new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    }

    CipherParameters randomCipherParameters() {
        byte[] AESkey = random(32);
        log.debug("{} bits key: '{}'", AESkey.length * 8, new String(Base64.encode(AESkey)));

        byte[] AESinitV = random(16);
        return new ParametersWithIV(new KeyParameter(AESkey), AESinitV);
    }

    final SecureRandom sr = new SecureRandom();

    byte[] random(int size) {
        byte[] bytes = new byte[size];
        sr.nextBytes(bytes);
        return bytes;
    }

    @After
    public void tearDown() {
        push.close();
        pull.close();
    }

    @Test(timeout=1000)
    public void readWrite() throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        // given
        final String inmsg = "hello world";

        // when
        byte[] sent = inmsg.getBytes();
        push.send(Bits.writeInt(sent.length), 0);
        push.send(cipher(sent), 0);
        int length = Bits.readInt(pull.recv(0));
        byte[] received = decipher(pull.recv(0));
        final String outmsg = new String(received, 0, length);

        // then
        assertEquals(inmsg, outmsg);
    }

    @Test(timeout=1000)
    public void readWrite_long() throws Exception {
        // given
        final String inmsg = "hello beautiful world";
        assertTrue(inmsg.length() > 16);

        // when
        byte[] sent = inmsg.getBytes();
        push.send(Bits.writeInt(sent.length), 0);
        push.send(cipher(sent), 0);
        int length = Bits.readInt(pull.recv(0));
        byte[] received = decipher(pull.recv(0));
        final String outmsg = new String(received, 0, length);

        // then
        assertEquals(inmsg, outmsg);
    }

    @Test
    public void readWrite_packets() throws Exception {
        // given
        final int PACKET_SIZE = 16;
        final String inmsg = "hello beautiful world";
        assertTrue(inmsg.length() > PACKET_SIZE);

        // when
        {
            byte[] sent = inmsg.getBytes();
            push.send(Bits.writeInt(sent.length), 0);
            int offset = 0;
            while (offset < sent.length) {
                int length = PACKET_SIZE;
                if (offset + length > sent.length) {
                    length = sent.length - offset;
                }
                push.send(Bits.writeInt(offset), ZMQ.SNDMORE);
                push.send(cipher(sent, offset, length), 0);
                offset += length;
            }
        }
        int length = Bits.readInt(pull.recv(0));
        ByteBuffer buffer = ByteBuffer.allocate(length);
        {
            int written = 0;
            while (written < length) {
                int roffset = Bits.readInt(pull.recv(0));
                int rlength = PACKET_SIZE;
                if (roffset + rlength > length) {
                    rlength = length - roffset;
                }
                byte[] received = decipher(pull.recv(0));
                buffer.put(received, 0, rlength);
                written += rlength;
            }
        }
        buffer.position(0);
        byte[] received = new byte[length];
        buffer.get(received, 0, length);
        final String outmsg = new String(received, 0, length);

        // then
        assertEquals(inmsg, outmsg);
    }

    @Test
    public void readWrite_packets_singleFramed() throws Exception {
        // given
        final int HEADER_SIZE = 4;
        final int PAYLOAD_SIZE = 32 - HEADER_SIZE;
        final int PACKET_SIZE = HEADER_SIZE + PAYLOAD_SIZE;
        final String inmsg = "That's a very nice test case we got there!";
        assertTrue(inmsg.length() > PACKET_SIZE);

        // when
        {
            ByteBuffer frame = ByteBuffer.allocate(PACKET_SIZE);
            byte[] data = new byte[PACKET_SIZE];
            byte[] sent = inmsg.getBytes();
            push.send(Bits.writeInt(sent.length), 0);
            int offset = 0;
            while (offset < sent.length) {
                int length = PAYLOAD_SIZE;
                if (offset + length > sent.length) {
                    length = sent.length - offset;
                }
                frame.position(0);
                frame.put(Bits.writeInt(offset));
                frame.put(sent, offset, length);
                frame.position(0);
                frame.get(data);
                push.send(cipher(data), 0);
                offset += length;
            }
        }
        int length = Bits.readInt(pull.recv(0));
        ByteBuffer buffer = ByteBuffer.allocate(length);
        {
            int written = 0;
            while (written < length) {
                byte[] data = pull.recv(0);
                byte[] received = decipher(data, 0, data.length);
                int roffset = Bits.readInt(Arrays.copyOf(received, 4));
                int rlength = PACKET_SIZE;
                if (roffset + rlength > length) {
                    rlength = length - roffset;
                }
                buffer.position(roffset);
                buffer.put(received, 4, rlength);
                written += rlength;
            }
        }
        buffer.position(0);
        byte[] received = new byte[length];
        buffer.get(received, 0, length);
        final String outmsg = new String(received, 0, length);

        // then
        assertEquals(inmsg, outmsg);
    }

    @Test
    public void readWrite_packets_singleFramed_reversedOrder() throws Exception {
        // given
        final int HEADER_SIZE = 4;
        final int PAYLOAD_SIZE = 32 - HEADER_SIZE;
        final int PACKET_SIZE = HEADER_SIZE + PAYLOAD_SIZE;
        final String inmsg = "That's a very nice test case we got there!";
        assertTrue(inmsg.length() > PACKET_SIZE);

        // when
        {
            ByteBuffer frame = ByteBuffer.allocate(PACKET_SIZE);
            byte[] data = new byte[PACKET_SIZE];
            byte[] sent = inmsg.getBytes();
            push.send(Bits.writeInt(sent.length), 0);
            int lastPacketSize = sent.length % PAYLOAD_SIZE;
            int offset = sent.length - lastPacketSize;
            while (offset >= 0) {
                int length = PAYLOAD_SIZE;
                if (offset + length > sent.length) {
                    length = sent.length - offset;
                }
                frame.position(0);
                frame.put(Bits.writeInt(offset));
                frame.put(sent, offset, length);
                frame.position(0);
                frame.get(data);
                push.send(cipher(data, 0, length + HEADER_SIZE), 0);
                offset -= PAYLOAD_SIZE;
            }
        }
        int length = Bits.readInt(pull.recv(0));
        ByteBuffer buffer = ByteBuffer.allocate(length);
        {
            int written = 0;
            while (written < length) {
                byte[] data = pull.recv(0);
                byte[] received = decipher(data, 0, data.length);
                int roffset = Bits.readInt(Arrays.copyOf(received, 4));
                int rlength = PAYLOAD_SIZE;
                if (roffset + rlength > length) {
                    rlength = length - roffset;
                }
                buffer.position(roffset);
                buffer.put(received, HEADER_SIZE, rlength);
                written += rlength;
            }
        }
        buffer.position(0);
        byte[] received = new byte[length];
        buffer.get(received, 0, length);
        final String outmsg = new String(received, 0, length);

        // then
        assertEquals(inmsg, outmsg);
    }

    private byte[] cipher(byte[] sent) throws DataLengthException, InvalidCipherTextException {
        return cipher(sent, 0, sent.length);
    }

    private byte[] cipher(byte[] sent, int offset, int length) throws DataLengthException, InvalidCipherTextException {
        return cipher(true, cipher, sent, offset, length);
    }

    private byte[] decipher(byte[] recv) throws DataLengthException, InvalidCipherTextException {
        return decipher(recv, 0, recv.length);
    }

    private byte[] decipher(byte[] recv, int offset, int length) throws DataLengthException, InvalidCipherTextException {
        return cipher(false, decipher, recv, offset, length);
    }

    private byte[] cipher(boolean isIn, BufferedBlockCipher cipher, byte[] in, int offset, int length) throws DataLengthException, InvalidCipherTextException {
        int olength = cipher.getOutputSize(length);
        byte[] out = new byte[olength];
        int written = cipher.processBytes(in, offset, length, out, 0);
        cipher.doFinal(out, written);
        return out;
    }

}
