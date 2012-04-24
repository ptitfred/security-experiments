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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.spec.DHParameterSpec;


class PeerImpl implements Peer {

    private final KeyPair keyPair;

    public PeerImpl(DHParameterSpec dhParameterSpec)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        super();
        KeyPairGenerator keyGen1 = KeyPairGenerator.getInstance("DH", "BC");
        keyGen1.initialize(dhParameterSpec, new SecureRandom());
        this.keyPair = keyGen1.generateKeyPair();
    }

    @Override
    public PublicKey getPublic() {
        return keyPair.getPublic();
    }

    @Override
    public Agreement agree() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        return new AgreementImpl(this.keyPair.getPrivate());
    }

}