package com.digitalvotingpass.digitalvotingpass;

import android.nfc.Tag;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.PublicKey;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by wkmeijer on 16-5-17.
 */
public class PassportConnectionTest {
    private String validDocNum = "GHD777O86";
    private String validBirthDate = "900522";
    private String validExpiryDate = "180626";

    @Test
    public void testConstructor() {

    }

    @Test
    public void testOpenConnectionNullInput() throws Exception {
        PassportService resPs = PassportConnection.openConnection(null, null);
        assertNull(resPs);
    }

    @Test
    public void testOpenConnectionNoTag() throws Exception {
        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);

        PassportService resPs = PassportConnection.openConnection(null, docData);
        assertNull(resPs);
    }

    @Test
    public void testOpenConnectionNoDocData() throws Exception {
        Tag tag = Mockito.mock(Tag.class);

        PassportService resPs = PassportConnection.openConnection(tag, null);
        assertNull(resPs);
    }

    @Test
    public void testOpenConnectionMockedTag() throws Exception {
        Tag tag = Mockito.mock(Tag.class);

        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);

        PassportService resPs = PassportConnection.openConnection(tag, docData);
        assertNull(resPs);
    }

    @Test
    public void testCreateBACKeySpec() throws Exception {
        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);
        BACKeySpec resBACKeySpec = PassportConnection.createBACKeySpec(docData);
        assertNotNull(resBACKeySpec);
    }

    @Test
    public void testCreateBACKeySpecNullInput() throws Exception {
        BACKeySpec resBACKeySpec = PassportConnection.createBACKeySpec(null);
        assertNull(resBACKeySpec);
    }

    @Test
    public void testBACKeySpecGetDocumentNumber() throws Exception {
        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);
        BACKeySpec resBACKeySpec = PassportConnection.createBACKeySpec(docData);
        assertEquals(validDocNum, resBACKeySpec.getDocumentNumber());
    }

    @Test
    public void testBACKeySpecGetDateOfBirth() throws Exception {
        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);
        BACKeySpec resBACKeySpec = PassportConnection.createBACKeySpec(docData);
        assertEquals(validBirthDate,resBACKeySpec.getDateOfBirth());
    }

    @Test
    public void testBACKeySpecGetDateOfExpiry() throws Exception {
        HashMap<String,String> docData = new HashMap<>();
        docData.put(MainActivity.DOCUMENT_NUMBER, validDocNum);
        docData.put(MainActivity.DATE_OF_BIRTH, validBirthDate);
        docData.put(MainActivity.EXPIRATION_DATE, validExpiryDate);
        BACKeySpec resBACKeySpec = PassportConnection.createBACKeySpec(docData);
        assertEquals(validExpiryDate, resBACKeySpec.getDateOfExpiry());
    }

    @Test
    public void testGetAAPublicKeyNoIs() throws Exception {
        PassportService psMock = Mockito.mock(PassportService.class);
        when(psMock.getInputStream(PassportService.EF_DG15)).thenReturn(null);

        PublicKey resPubKey = PassportConnection.getAAPublicKey(psMock);
        assertNull(resPubKey);
    }

    @Test
    public void testGetAAPublicKeyNoPs() throws Exception {
        PublicKey resPubKey = PassportConnection.getAAPublicKey(null);
        assertNull(resPubKey);
    }

    @Test
    public void testSignDataNoIs() throws Exception {
        PassportService psMock = Mockito.mock(PassportService.class);
        when(psMock.getInputStream(PassportService.EF_DG15)).thenReturn(null);

        byte[] resSignedData = PassportConnection.signData(psMock);
        assertNull(resSignedData);
    }

    @Test
    public void testSignDataNoPs() throws Exception {
        byte[] resSignedData = PassportConnection.signData(null);
        assertNull(resSignedData);
    }

    @Test
    public void testGetBSNNoIs() throws Exception {
        PassportService psMock = Mockito.mock(PassportService.class);
        when(psMock.getInputStream(PassportService.EF_DG15)).thenReturn(null);

        String resBSN = PassportConnection.getBSN(psMock);
        assertNull(resBSN);
    }

    @Test
    public void testGetBSNNoPs() throws Exception {
        String resBSN = PassportConnection.getBSN(null);
        assertNull(resBSN);
    }
}