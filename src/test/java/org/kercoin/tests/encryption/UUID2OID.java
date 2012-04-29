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
package org.kercoin.tests.encryption;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ptitfred
 *
 */
public class UUID2OID {

    static Logger logger = LoggerFactory.getLogger(UUID2OID.class);

    public static void main(String[] args) {
        String uuid = "11886540-193a-11da-b272-0002a5d5c51b";
        logger.info("urn:uuid:{}", uuid);
        BigInteger bi = new BigInteger(uuid.replaceAll("-", ""), 16);
        logger.info("urn:oid:2.25.{}", bi.toString(10));
    }
}
