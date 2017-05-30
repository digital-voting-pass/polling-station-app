package com.digitalvotingpass.passportconnection;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import com.digitalvotingpass.digitalvotingpass.MainActivity;
import com.digitalvotingpass.utilities.Util;

import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG15File;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.LDSFileUtil;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.HashMap;

public class PassportConnection {

    /**
     * Opens a connection with the ID by doing BAC
     * Uses hardcoded parameters for now
     *
     * @param tag - NFC tag that started this activity (ID NFC tag)
     * @return PassportService - passportservice that has an open connection with the ID
     */
    public PassportService openConnection(Tag tag, final HashMap<String,String> docData) {
        PassportService ps = null;
        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            ps = new PassportService(cs);
            ps.open();

            // Get the information needed for BAC from the data provided by OCR
            ps.sendSelectApplet(false);
            BACKeySpec bacKey = new BACKeySpec() {
                @Override
                public String getDocumentNumber() {
                    return docData.get(MainActivity.DOCUMENT_NUMBER);
                }

                @Override
                public String getDateOfBirth() { return docData.get(MainActivity.DATE_OF_BIRTH); }

                @Override
                public String getDateOfExpiry() { return docData.get(MainActivity.EXPIRATION_DATE); }
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
     *
     * @return Publickey - returns the publickey used for AA
     */
    public PublicKey getAAPublicKey(PassportService ps) {
        InputStream is15 = null;
        try {
            is15 = ps.getInputStream(PassportService.EF_DG15);
            DG15File dg15 = new DG15File(is15);
            return dg15.getPublicKey();
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
     * Signs 8 bytes by the passport using the AA functionality.
     *
     * @return byte[] - signed byte array
     */
    public byte[] signData(PassportService ps) {
        InputStream is15 = null;
        try {
            is15 = ps.getInputStream(PassportService.EF_DG15);
            // test 8 byte string for testing purposes
            byte[] data = Util.hexStringToByteArray("0a1b3c4d5e6faabb");
            // doAA of JMRTD library only returns signed data, and does not have the AA functionality yet
            // there is no need for sending public key information with the method.
            return ps.doAA(null, null, null, data);
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
     * Get the BSN from datagroup1 to confirm the ID was scanned correctly
     * This is for testing purposes
     */
    public String getBSN(PassportService ps) {
        InputStream is = null;
        try {
            is = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
            return dg1.getMRZInfo().getPersonalNumber();
        } catch (Exception ex) {
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
}
