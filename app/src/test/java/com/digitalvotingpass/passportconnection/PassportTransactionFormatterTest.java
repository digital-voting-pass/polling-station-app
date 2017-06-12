package com.digitalvotingpass.passportconnection;

import android.util.Log;

import com.google.common.primitives.Bytes;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.MultiChainParams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.math.BigInteger;
import java.util.Arrays;



import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by rico on 12-6-17.
 */

public class PassportTransactionFormatterTest {
    private long addressChecksum = 0xcc350cafL;
    private String[] version = {"00", "62", "8f", "ed"};

    private MainNetParams mp = MultiChainParams.get(
            "00d7fa1a62c5f1eadd434b9f7a8a657a42bd895f160511af6de2d2cd690319b8",
            "01000000000000000000000000000000000000000000000000000000000000000000000059c075b5dd26a328e185333ce1464b7279d476fbe901c38a003e694906e01c073b633559ffff0020ae0000000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1704ffff002001040f4d756c7469436861696e20766f7465ffffffff0200000000000000002f76a91474f585ec0e5f452a80af1e059b9d5079ec501d5588ac1473706b703731000000000000ffffffff3b633559750000000000000000131073706b6e0200040101000104726f6f74756a00000000",
            6799,
            Integer.parseInt(Arrays.toString(version).replaceAll(", |\\[|\\]", ""), 16),
    addressChecksum,
            0xf5dec1feL
            );


    @Mock
    private TransactionOutput to;
    @Mock
    private Address address;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private PassportTransactionFormatter ptf;


    @Before
    public void setUp() {

        when(to.getParentTransactionHash()).thenReturn( new Sha256Hash("f2b3eb2deb76566e7324307cd47c35eeb88413f971d88519859b1834307ecfec"));
        when(to.getScriptBytes()).thenReturn(new BigInteger("76a914010966776006953d5567439e5e39f86a0d273bee88ac", 16).toByteArray());
        when(to.getIndex()).thenReturn(1);

        when(address.getHash160()).thenReturn(new BigInteger("76a914010966776006953d5567439e5e39f86a0d273bee88ac", 16).toByteArray());

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
        byte[] correctHash = new BigInteger("09AB317A17BBEB4F46EFA2BDA80F137059608AA6696FF5155F0E2A72DC6C249E", 16).toByteArray();
        for(int i=0; i< correctHash.length; i++) {
            assertEquals(correctHash[i], hashRaw[i]);
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
