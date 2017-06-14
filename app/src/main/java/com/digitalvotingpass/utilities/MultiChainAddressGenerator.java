package com.digitalvotingpass.utilities;

import net.sf.scuba.smartcards.BuildConfig;

import org.bitcoinj.core.Base58;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class MultiChainAddressGenerator {

    /**
     * Converts a given public key to a valid MultiChain address.
     * See {@link <a href="http://www.multichain.com/developers/address-key-format/">MultiChain Documentation</a>}
     * @param pubKey byte array containing the public key
     * @return String representing the corresponding address.
     */
    public static String getPublicAddress(String[] version, String addressChecksum, byte[] pubKey) {
        //Step 3
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] hash = digest.digest(pubKey);

            //Step 4
            RIPEMD160Digest ripemd = new RIPEMD160Digest();
            ripemd.update(hash, 0, hash.length);
            byte[] out = new byte[20];
            ripemd.doFinal(out, 0);
            String hashStr = Util.byteArrayToHexString(out);

            //Step 5
            String step5 = "";
            if (BuildConfig.DEBUG && version.length != 4) throw new AssertionError("Version length != 4");
            for (int i = 0; i < 4; i++) { //Assumes version.length == 4
                step5 += version[i] + hashStr.substring((i*10),(i*10)+10);
            }
            digest.reset();

            //Step 6
            byte[] step6 = digest.digest(Util.hexStringToByteArray(step5));
            digest.reset();

            //Step 7
            byte[] step7 = digest.digest(step6);
            digest.reset();

            //Step 8
            byte[] checksum = new byte[]{ step7[0],step7[1],step7[2],step7[3] };

            //Step 9
            byte[] byteAddressChecksum = Util.hexStringToByteArray(addressChecksum);
            byte[] xor = new byte[4];
            for (int i = 0; i < 4; i++) {
                int xorvalue = (int)checksum[i] ^ (int)byteAddressChecksum[i];
                xor[i] = (byte)(0xff & xorvalue);
            }

            //Step 10
            String addressbytes = step5 + Util.byteArrayToHexString(xor);

            //Step 11
            String address = Base58.encode(Util.hexStringToByteArray(addressbytes));
            return address;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * TESTED, works
     * Takes a java.security.PublicKey and returns a valid MultiChain address.
     * Uses the {@link #getPublicAddress(String[], String, byte[]) getPublicAddress method to generate the address.
     * Gets the last 81 bytes of encoded pubKey (this is the actual Public Key)
     * @param pubKey java.security.PublicKey containing the public key
     * @return String representing the corresponding address.
     */
    public static String getPublicAddress(String[] version, String addressChecksum, PublicKey pubKey) {
        byte[] pubKeyBytes = new byte[81];
        byte[] src = pubKey.getEncoded();
        System.arraycopy(src, src.length-81, pubKeyBytes, 0, 81);
        return getPublicAddress(version, addressChecksum, pubKeyBytes);
    }
}
