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

import java.math.BigInteger;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

class CustomDHIO implements DHIO {

    static private final class MyPublicKey implements DHPublicKey {
        private static final long serialVersionUID = 1L;

        private final byte[] bytes;
        private final String format;
        private final String algorithm;
        private final BigInteger y;
        private final DHParameterSpec params;

        MyPublicKey(byte[] bytes, String format, String algorithm, BigInteger y, DHParameterSpec params) {
            this.bytes = bytes;
            this.format = format;
            this.algorithm = algorithm;
            this.y = y;
            this.params = params;
        }

        @Override
        public String getAlgorithm() {
            return algorithm;
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public byte[] getEncoded() {
            return bytes;
        }

        @Override
        public BigInteger getY() {
            return y;
        }

        @Override
        public DHParameterSpec getParams() {
            return params;
        }
    }

    @Override
    public void send(Socket socket, DHPublicKey publicKey) {
        sendDHPublicKeyCustom(socket, publicKey);
    }

    @Override
    public DHPublicKey recv(Socket socket) {
        return recvDHPublicKeyCustom(socket);
    }

    private void sendDHPublicKeyCustom(Socket socket, DHPublicKey publicKey) {
        socket.send(publicKey.getEncoded(), ZMQ.SNDMORE);
        socket.send(publicKey.getAlgorithm().getBytes(), ZMQ.SNDMORE);
        socket.send(publicKey.getFormat().getBytes(), ZMQ.SNDMORE);
        socket.send(publicKey.getY().toByteArray(), ZMQ.SNDMORE);
        sendDHParameterSpecCustom(socket, publicKey.getParams());
    }

    private DHPublicKey recvDHPublicKeyCustom(Socket socket) {
        byte[] alicePKBytes = socket.recv(0);
        String alicePKAlgorithm = new String(socket.recv(0));
        String alicePKFormat = new String(socket.recv(0));
        BigInteger y = new BigInteger(socket.recv(0));
        DHParameterSpec params = recvDHParameterSpecCustom(socket);
        return new MyPublicKey(alicePKBytes, alicePKFormat, alicePKAlgorithm, y, params);
    }

    private DHParameterSpec recvDHParameterSpecCustom(Socket socket) {
        BigInteger g = new BigInteger(socket.recv(0));
        BigInteger p = new BigInteger(socket.recv(0));
        return new DHParameterSpec(p, g);
    }

    private void sendDHParameterSpecCustom(Socket socket, DHParameterSpec params) {
        socket.send(params.getG().toByteArray(), ZMQ.SNDMORE);
        socket.send(params.getP().toByteArray(), 0);
    }

}