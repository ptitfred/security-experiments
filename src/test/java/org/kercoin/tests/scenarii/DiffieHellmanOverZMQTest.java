/*
Copyright 2011 Frederic Menou

This file is part of Magrit.

Magrit is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Magrit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with Magrit.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.kercoin.tests.scenarii;

import static org.fest.assertions.Assertions.assertThat;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kercoin.tests.encryption.ZMQHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @author ptitfred
 *
 */
public class DiffieHellmanOverZMQTest {

    static Logger log = LoggerFactory.getLogger(DiffieHellmanOverZMQTest.class);

    Socket aliceOut;
    Socket aliceIn;
    Socket bobOut;
    Socket bobIn;

    Peer alice;
    Peer bob;

    @BeforeClass
    public static void setupProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void setup() throws Exception {
        String urlAlice = ZMQHelper.getInprocUrl(this, "alice");
        String urlBob = ZMQHelper.getInprocUrl(this, "bob");
        Context ctx = ZMQ.context(1);
        aliceIn = ctx.socket(ZMQ.SUB);
        aliceIn.subscribe("alice".getBytes());
        aliceOut = ctx.socket(ZMQ.PUB);
        bobIn = ctx.socket(ZMQ.SUB);
        bobIn.subscribe("bob".getBytes());
        bobOut = ctx.socket(ZMQ.PUB);

        aliceIn.bind(urlAlice);
        bobIn.bind(urlBob);

        aliceOut.connect(urlBob);
        bobOut.connect(urlAlice);

        alice = new PeerImpl(generateDHParameters(256));
    }

    final static int ITERATIONS = 250;

    @Test(timeout=20000)
    public void aliceSaysHelloToBob_ASN1() throws Exception {
        long start=System.currentTimeMillis();
        DHIO io = new ASN1DHIO();
        for (int i=0; i<ITERATIONS; i++) {
            testExchange(io);
        }
        long end = System.currentTimeMillis();
        log.info("ASN.1: {}ms", end - start);
    }

    @Test(timeout=20000)
    public void aliceSaysHelloToBob_Custom() throws Exception {
        long start=System.currentTimeMillis();
        DHIO io = new CustomDHIO();
        for (int i=0; i<ITERATIONS; i++) {
            testExchange(io);
        }
        long end = System.currentTimeMillis();
        log.info("Custom: {}ms", end - start);
    }

    private void testExchange(DHIO io) throws InvalidKeySpecException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException {
        {
            aliceOut.send("bob".getBytes(), ZMQ.SNDMORE);
            io.send(aliceOut, (DHPublicKey) alice.getPublic());
        }
        byte[] bobShared = null;
        {
            bobIn.recv(0);
            DHPublicKey alicePK = io.recv(bobIn);
            bob = new PeerImpl(alicePK.getParams());

            bobShared = bob.agree().with(alicePK).getSharedKey();

            bobOut.send("alice".getBytes(), ZMQ.SNDMORE);
            io.send(bobOut, (DHPublicKey) bob.getPublic());
        }
        byte[] aliceShared = null;
        {
            aliceIn.recv(0);
            DHPublicKey bobPK = io.recv(aliceIn);
            aliceShared = alice.agree().with(bobPK).getSharedKey();
        }
        assertThat(aliceShared).isEqualTo(bobShared);
    }

    private DHParameterSpec generateDHParameters(int keySize)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException {
        AlgorithmParameterGenerator generator = AlgorithmParameterGenerator.getInstance("DH", "BC");
        generator.init(keySize);
        AlgorithmParameters algoParameters = generator.generateParameters();
        return algoParameters.getParameterSpec(DHParameterSpec.class);
    }

}
