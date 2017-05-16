package com.digitalvotingpass.digitalvotingpass;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by wkmeijer on 16-5-17.
 */
public class PassportConActivityTest {

    @Test
    public void testHexStringToByteArray_hexString_input() throws Exception {
        String hStr = "00000000";
        byte[] expected = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(hStr));
    }

    @Test
    public void testHexStringToByteArray_hexString_input_2() throws Exception {
        String hStr = "01234567";
        byte[] expected = new byte[] { (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67 };
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(hStr));
    }

    @Test
    public void testHexStringToByteArray_hexString_input_3() throws Exception {
        String hStr = "0123456789abcdef";
        byte[] expected = new byte[] { (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89,
                (byte)0xab, (byte)0xcd, (byte)0xef };
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(hStr));
    }

    @Test
    public void testHexStringToByteArray_hexString_input_4() throws Exception {
        String hStr = "0123456789ABCDEF";
        byte[] expected = new byte[] { (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89,
                (byte)0xab, (byte)0xcd, (byte)0xef };
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(hStr));
    }

    @Test
    public void testHexStringToByteArray_empty_input() throws Exception {
        String hStr = "";
        byte[] expected = new byte[] {};
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(hStr));
    }

    @Test
    public void testHexStringToByteArray_null_input() throws Exception {
        byte[] expected = new byte[] {};
        assertArrayEquals(expected, PassportConActivity.hexStringToByteArray(null));
    }

    @Test
    public void testByteArrayToHexString_Byte_input() throws Exception {
        byte[] bArray = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
        String expected = "00000000";
        assertEquals(expected, PassportConActivity.byteArrayToHexString(bArray));
    }

    @Test
    public void testByteArrayToHexString_Byte_input_2() throws Exception {
        byte[] bArray = new byte[] { (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67 };
        String expected = "01234567";
        assertEquals(expected, PassportConActivity.byteArrayToHexString(bArray));
    }

    @Test
    public void testByteArrayToHexString_Byte_input_3() throws Exception {
        byte[] bArray = new byte[] { (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89,
                (byte)0xab, (byte)0xcd, (byte)0xef };
        String expected = "0123456789ABCDEF";
        assertEquals(expected, PassportConActivity.byteArrayToHexString(bArray));
    }

    @Test
    public void testByteArrayToHexString_empty_input() throws Exception {
        byte[] bArray = new byte[] { };
        String expected = "";
        assertEquals(expected, PassportConActivity.byteArrayToHexString(bArray));
    }

    @Test
    public void testByteArrayToHexString_null_input() throws Exception {
        String expected = "";
        assertEquals(expected, PassportConActivity.byteArrayToHexString(null));
    }
}