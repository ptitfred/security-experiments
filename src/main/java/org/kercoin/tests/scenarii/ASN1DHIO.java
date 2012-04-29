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

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.interfaces.DHPublicKey;

import org.zeromq.ZMQ.Socket;

class ASN1DHIO implements DHIO {

    @Override
    public void send(Socket socket, DHPublicKey publicKey) {
        sendPublicKey(socket, publicKey);
    }

    @Override
    public DHPublicKey recv(Socket socket) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        return recvPublicKey(socket);
    }

    private void sendPublicKey(Socket socket, Key publicKey) {
        socket.send(publicKey.getEncoded(), 0);
    }

    private DHPublicKey recvPublicKey(Socket socket)
            throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        byte[] alicePKBytes = socket.recv(0);
        return (DHPublicKey) KeyFactory.getInstance("DH", "BC").generatePublic(new X509EncodedKeySpec(alicePKBytes));
    }

}