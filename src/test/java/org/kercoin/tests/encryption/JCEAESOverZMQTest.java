package org.kercoin.tests.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kercoin.tests.io.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class JCEAESOverZMQTest {

    private static final Logger log = LoggerFactory.getLogger(JCEAESOverZMQTest.class);

    Socket push;
    Socket pull;

    public final static int LOW = 128;
    public final static int MIDDLE = 192;
    public final static int HIGH = 256;

    Cipher cipher;
    Cipher decipher;

    @BeforeClass
    public static void withBouncyCastle() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        log.info("Using {}", Security.getProviders()[0].getInfo());
    }

    @Before
    public void setup() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Context ctx = ZMQ.context(1);
        push = ctx.socket(ZMQ.PUSH);
        pull = ctx.socket(ZMQ.PULL);
        String url = ZMQHelper.getInprocUrl(this, "aes-jce");
        pull.bind(url);
        push.connect(url);

        cipher = Cipher.getInstance("AES");
        decipher = Cipher.getInstance("AES");
        final SecretKeySpec key = randomAESSecretKey();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        decipher.init(Cipher.DECRYPT_MODE, key);
    }

    private SecretKeySpec randomAESSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(HIGH); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        
        log.debug("{} bits key: '{}'", raw.length * 8, new String(Base64.encode(raw)));

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        return skeySpec;
    }

    @After
    public void tearDown() {
        push.close();
        pull.close();
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

    private byte[] cipher(byte[] sent) throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(sent);
    }

    private byte[] decipher(byte[] recv) throws IllegalBlockSizeException, BadPaddingException {
        return decipher.doFinal(recv);
    }

}
