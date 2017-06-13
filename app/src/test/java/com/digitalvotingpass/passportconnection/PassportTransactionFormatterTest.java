package com.digitalvotingpass.passportconnection;


import com.digitalvotingpass.utilities.Util;
import com.google.common.primitives.Bytes;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.security.PublicKey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

/**
 * Created by rico on 12-6-17.
 */

public class PassportTransactionFormatterTest {


    @Mock
    private TransactionOutput to;
    @Mock
    private Address address;
    @Mock
    private PassportConnection pCon;
    @Mock
    private PublicKey pubKey;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private PassportTransactionFormatter ptf;


    @Before
    public void setUp() {

        when(to.getParentTransactionHash()).thenReturn( new Sha256Hash("f2b3eb2deb76566e7324307cd47c35eeb88413f971d88519859b1834307ecfec"));
        when(to.getScriptBytes()).thenReturn(Util.hexStringToByteArray("76a914010966776006953d5567439e5e39f86a0d273bee88ac"));
        when(to.getIndex()).thenReturn(1);

        when(address.getHash160()).thenReturn(Util.hexStringToByteArray("76a914010966776006953d5567439e5e39f86a0d273bee88ac"));


        ptf = new PassportTransactionFormatter(to, address);

    }


    @Test
    public void testBuildRawTransaction() {
        byte[][] transaction = ptf.buildRawTransaction();

        assertEquals(1, value(transaction[0]));
        assertEquals(1, value(transaction[1]));

        //check for number of outputs
        assertEquals(1, value(transaction[7]));

        //check for zero coins
        assertEquals(0, value(transaction[8]));

        //check length of script
        assertEquals(25, value(transaction[9]));


        // Create the raw transaction and hash it
        byte[] raw = Bytes.concat(transaction[0], transaction[1], transaction[2], transaction[3], transaction[4], transaction[5],
                transaction[6], transaction[7], transaction[8], transaction[9], transaction[10], transaction[11], transaction[12]);
        byte[] hashRaw = Sha256Hash.hash(Sha256Hash.hash(raw));


        //check for the correct hash
        byte[] correctHash = Util.hexStringToByteArray("09AB317A17BBEB4F46EFA2BDA80F137059608AA6696FF5155F0E2A72DC6C249E");
        for(int i=0; i< correctHash.length; i++) {
            assertEquals(correctHash[i], hashRaw[i]);
        }
    }

    @Test
    public void testSignRawTransaction() {
        try {
            //Return signed data
            when(pCon.signData((byte[])notNull())).thenReturn(
                    Util.hexStringToByteArray("0A5FADA8967CF77C8F00FAF3A8CF73D4615BC970E9BAA76883A2C4E8B9DA56C4ECC9F72E66F045D70A22918431F6263BDB6737A89393D2F5C3931007DF2532C86FDF9390A7163C98F72DE1590D21D34A"),
                    Util.hexStringToByteArray("76732217AC784D75B1A89BAD2348271F5092B6D303F72E26D7374B8ED47F5D3A3324A41BBEE6781967DAF8706A83D6BE5D8990F1CA17FABF07E2FB003C21177EF9C270F64E22828A09E65FE4ECE14039"),
                    Util.hexStringToByteArray("9585FCCCF6A430ECA5E3F9E2C52CDF7627F1E62D04042A69CB0478BFBA60AA2708F55E92FA6518578F9498B7B0A6CCBA14676D921BFB74081970E5E5B944C11BD46F123764F97988A6479D8358FFA6F3"),
                    Util.hexStringToByteArray("606D9D27341FB36052F1F04914E34F3B5C25A6073A6A0565B7BABC2A45C4B964B5B41311FE539A06AB2640B3C68ABFF1A3C78FA946C2BDF01896442956B0A675531374B3E3AE02661D59F91BA0E34557"));
            //return the public key
            when(pubKey.getEncoded()).thenReturn(Util.hexStringToByteArray("04b41f0dda6797b6afd9dcfab9b9cc99744644705e67b5872540860cc015044b86de5cc13da2b3f23564f4c4da996c9321d3c8ea25642bd8176761eecc8fbc3fcdbf846b5c927bd488f42afa9f193517e6"));

            //build and sign raw transaction
            byte[][] rawTransaction = ptf.buildRawTransaction();
            byte[] transaction = ptf.signRawTransaction(pubKey, rawTransaction, pCon);

            //correct format
            byte[] correctTransaction = Util.hexStringToByteArray("01000000" +
                    "01" + //num inputs
                    "ECCF7E3034189B851985D871F91384B8EE357CD47C3024736E5676EB2DEBB3F2" + //script
                    "01000000" + //index
                    "FD97014D4101" + //opcodes + length. next 4 lines signatures
                    "0A5FADA8967CF77C8F00FAF3A8CF73D4615BC970E9BAA76883A2C4E8B9DA56C4ECC9F72E66F045D70A22918431F6263BDB6737A89393D2F5C3931007DF2532C86FDF9390A7163C98F72DE1590D21D34A" +
                    "76732217AC784D75B1A89BAD2348271F5092B6D303F72E26D7374B8ED47F5D3A3324A41BBEE6781967DAF8706A83D6BE5D8990F1CA17FABF07E2FB003C21177EF9C270F64E22828A09E65FE4ECE14039" +
                    "9585FCCCF6A430ECA5E3F9E2C52CDF7627F1E62D04042A69CB0478BFBA60AA2708F55E92FA6518578F9498B7B0A6CCBA14676D921BFB74081970E5E5B944C11BD46F123764F97988A6479D8358FFA6F3" +
                    "606D9D27341FB36052F1F04914E34F3B5C25A6073A6A0565B7BABC2A45C4B964B5B41311FE539A06AB2640B3C68ABFF1A3C78FA946C2BDF01896442956B0A675531374B3E3AE02661D59F91BA0E34557" +
                    "014C" + //opcodes
                    "51" + //length
                    "04B41F0DDA6797B6AFD9DCFAB9B9CC99744644705E67B5872540860CC015044B86DE5CC13DA2B3F23564F4C4DA996C9321D3C8EA25642BD8176761EECC8FBC3FCDBF846B5C927BD488F42AFA9F193517E6" +
                    "FFFFFFFF" + //magic
                    "010000000" + //num inputs
                    "000000000" + //0 coin base
                    "19" + //length
                    "76A91476A914010966776006953D5567439E5E39F86A0D88AC" + //script to
                    "00000000" + //magic
                    "01000000");

            //Check for the length.
            assertEquals(correctTransaction.length, transaction.length);

            //Check all bytes
            for(int i=0; i< correctTransaction.length; i++) {
                assertEquals("Error at byte " + i, correctTransaction[i], transaction[i]);
            }

        } catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Calculate the value of a byte array.
     * @param by The byte array.
     * @return The value.
     */
    private long value(byte[] by) {
        long value = 0;
        for (int i = 0; i < by.length; i++) {
            value += ((long) by[i] & 0xffL) << (8 * i);
        }
        return value;
    }
}
