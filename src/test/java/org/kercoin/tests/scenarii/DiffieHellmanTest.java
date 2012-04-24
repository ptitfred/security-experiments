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
import java.security.Security;

import javax.crypto.spec.DHParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ptitfred
 * 
 */
public class DiffieHellmanTest {

    static Logger log = LoggerFactory.getLogger(DiffieHellmanTest.class);

    DHParameterSpec dhParameterSpec;

    Peer alice;
    Peer bob;

    @Before
    public void setup() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        AlgorithmParameterGenerator generator = AlgorithmParameterGenerator.getInstance("DH", "BC");
        generator.init(256);
        AlgorithmParameters algoParameters = generator.generateParameters();
        dhParameterSpec = algoParameters.getParameterSpec(DHParameterSpec.class);
    }

    @Test
    public void aliceSaysHelloToBob() throws Exception {
        // given
        alice = new PeerImpl(dhParameterSpec);
        bob = new PeerImpl(dhParameterSpec);

        // when
        final byte[] aliceShared = alice.agree().with(bob.getPublic()).getSharedKey();
        final byte[] bobShared = bob.agree().with(alice.getPublic()).getSharedKey();

        // then
        assertThat(aliceShared).isEqualTo(bobShared);
        log.info("Shared {}", new String(Base64.encode(aliceShared)));
    }

}
