package com.digitalvotingpass.digitalvotingpass;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG15File;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.LDSFileUtil;

import java.io.InputStream;
import java.security.PublicKey;

/**
 * Created by wkmeijer on 16-5-17.
 */

public class PassportConnection {

    /**
     * Opens a connection with the ID by doing BAC
     * Uses hardcoded parameters for now
     * @param tag - NFC tag that started this activity (ID NFC tag)
     * @return PassportService - passportservice that has an open connection with the ID
     */
    public PassportService openConnection(Tag tag) {
        PassportService ps = null;
        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            ps = new PassportService(cs);
            ps.open();

            // Get the information needed for BAC, hardcoded for now
            // TODO: link this with OCR functionality
            ps.sendSelectApplet(false);
            BACKeySpec bacKey = new BACKeySpec() {
                @Override
                public String getDocumentNumber() {
                    return "NP0811B03";
                }
                @Override
                public String getDateOfBirth() {
                    return "940610";
                }
                @Override
                public String getDateOfExpiry() {
                    return "180624";
                }
            };

            ps.doBAC(bacKey);
            return ps;
        } catch (Exception ex) {
            ex.printStackTrace();
            ps.close();
        }
        return null;
    }

    /**
     * Retrieves the public key used for Active Authentication from datagroup 15.
     * @return Publickey - returns the publickey used for AA
     */
    public PublicKey getAAPublicKey(PassportService ps) {
        InputStream is15 = null;
        try {
            is15 = ps.getInputStream(PassportService.EF_DG15);
            DG15File dg15 = new DG15File(is15);
            return dg15.getPublicKey();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            try {
                is15.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Signs 8 bytes by the passport using the AA functionality.
     * @return byte[] - signed byte array
     */
    public byte[] signData(PassportService ps, PublicKey pubk) {
        InputStream is15 = null;
        try {
            is15 = ps.getInputStream(PassportService.EF_DG15);
            // test 8 byte string for testing purposes
            byte[] data = hexStringToByteArray("0a1b3c4d5e6faabb");
            return ps.doAA(pubk, null, null, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                is15.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     *  Get the BSN from datagroup1 to confirm the ID was scanned correctly
     *  This is for testing purposes
     */
    public String getBSN(PassportService ps) {
        InputStream is = null;
        try {
            is = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
            return dg1.getMRZInfo().getPersonalNumber();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Method for converting a hexString to a byte array.
     * This method is used for signing transaction hashes (which are in hex).
     */
    public static byte[] hexStringToByteArray(String hStr) {
        if(hStr != null) {
            int len = hStr.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hStr.charAt(i), 16) << 4)
                        + Character.digit(hStr.charAt(i + 1), 16));
            }
            return data;
        }
        return new byte[0];
    }

    /**
     * Method for converting a byte array to a hexString.
     * This method is used for converting a signed 8-byte array back to a hashString in order to
     * display it readable.
     */
    public static String byteArrayToHexString(byte[] bArray) {
        if (bArray != null) {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bArray.length * 2];
            for (int j = 0; j < bArray.length; j++) {
                int v = bArray[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        return "";
    }
}
