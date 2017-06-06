package com.digitalvotingpass.passportconnection;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import com.digitalvotingpass.digitalvotingpass.Voter;
import com.digitalvotingpass.digitalvotingpass.DocumentData;
import com.digitalvotingpass.utilities.Util;

import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG15File;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.MRZInfo;

import java.io.InputStream;
import java.security.PublicKey;

public class PassportConnection {

    private PassportService ps;

    /**
     * Opens a connection with the ID by doing BAC
     * Uses hardcoded parameters for now
     *
     * @param tag - NFC tag that started this activity (ID NFC tag)
     * @return PassportService - passportservice that has an open connection with the ID
     */
    public PassportService openConnection(Tag tag, final DocumentData docData) {
        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            this.ps = new PassportService(cs);
            this.ps.open();

            // Get the information needed for BAC from the data provided by OCR
            this.ps.sendSelectApplet(false);
            BACKeySpec bacKey = new BACKeySpec() {
                @Override
                public String getDocumentNumber() {
                    return docData.getDocumentNumber();
                }

                @Override
                public String getDateOfBirth() { return docData.getDateOfBirth(); }

                @Override
                public String getDateOfExpiry() { return docData.getExpiryDate(); }
            };

            this.ps.doBAC(bacKey);
            return this.ps;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.ps.close();
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

    public PublicKey getAAPublicKey() {
        return this.getAAPublicKey(this.ps);
    }

    /**
     * Signs 8 bytes by the passport using the AA functionality.
     *
     * @return byte[] - signed byte array
     */
    public byte[] signData(PassportService ps, byte[] data) {
        InputStream is15 = null;
        try {
            is15 = ps.getInputStream(PassportService.EF_DG15);
            // test 8 byte string for testing purposes
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

    public byte[] signData(byte[] data) {
        return this.signData(this.ps, data);
    }

    /**
     * Get personal information about a voter from datagroup1.
     * @return Voter - Voter object containing personal data.
     */
    public Voter getVoter(PassportService ps) {
        InputStream is = null;
        try {
            is = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
            MRZInfo mrzInfo = dg1.getMRZInfo();
            //Replace '<' with spaces since JMRTD does not remove these.
            return new Voter(mrzInfo.getSecondaryIdentifier().replaceAll("<", " ").trim(),
                    mrzInfo.getPrimaryIdentifier().replaceAll("<", " ").trim(),
                    mrzInfo.getGender());
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
