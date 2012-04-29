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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyAgreement;

class AgreementImpl implements Agreement {

    private final PrivateKey privateKey;
    private byte[] sharedKey;

    AgreementImpl(PrivateKey privateKey) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        this.privateKey = privateKey;
    }

    @Override
    public Agreement with(PublicKey peerPK) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchProviderException {
        KeyAgreement agreement = KeyAgreement.getInstance("DH", "BC");
        agreement.init(privateKey);
        agreement.doPhase(peerPK, true);
        MessageDigest hash = MessageDigest.getInstance("SHA256", "BC");
        sharedKey = hash.digest(agreement.generateSecret());
        return this;
    }

    @Override
    public byte[] getSharedKey() {
        return sharedKey;
    }

}