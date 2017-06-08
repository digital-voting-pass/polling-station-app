package com.digitalvotingpass.passportconnection;

import com.google.common.primitives.Bytes;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by daan on 6-6-17.
 */
public class PassportTransactionFormatter {

    private Address destination;
    private TransactionOutput utxo;
    private byte[] data;

    /**
     * Class builds the bytes needed for a transaction
     */
    public PassportTransactionFormatter(TransactionOutput utxo, Address destination) {
        this.destination = destination;
        this.utxo = utxo;
    }

    /**
     * Set the address of the public key which can spend this output.
     * @param destination
     */
    public void setDestinationAddress(Address destination) {
        this.destination = destination;
    }

    /**
     * Set the UTXO we would like to spend.
     * @param utxo
     */
    public void setUTXO(TransactionOutput utxo) {
        this.utxo = utxo;
    }

    /**
     * Builds the raw transaction and signes using the passport.
     * @param pcon
     */
    public PassportTransactionFormatter buildAndSign(PassportConnection pcon) {
        byte[][] parts = buildRawTransaction();
        this.data = signRawTransaction(parts, pcon);
        return this;
    }

    /**
     * Creates a byte array which contains all the element for a valid transaction.
     * Follows the steps in this answer: https://bitcoin.stackexchange.com/a/5241
     * @return byte[] rawTransaction
     */
    public byte[][] buildRawTransaction() {

        // Version
        byte[] step1 = new BigInteger("01000000", 16).toByteArray();

        // Number of outputs
        byte[] step2 = new byte[]{0x01};

        // Transaction hash
        byte[] step3 = utxo.getParentTransactionHash().getReversedBytes();

        // Output index
        byte[] step4 = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(utxo.getIndex())).array();

        // Length of scriptsig (scriptpubkey)
        byte[] step5 = new byte[] {(byte) (utxo.getScriptBytes().length & 0xFF)};

        // Scriptpubkey of output we want to redeem
        byte[] step6 = utxo.getScriptBytes();

        // Unused sequence
        byte[] step7 = hexToByte("FFFFFFFF");

        // Number of outputs in transaction
        byte[] step8 = hexToByte("01");

        // Spend amount
        byte[] step9 = hexToByte("0000000000000000");

        // Size of redeem script
        byte[] step10 = new byte[]{(byte) (step6.length & 0xFF)};

        // Redeem script (copies output and replaces address)
        byte[] step11 = step6.clone();
        System.arraycopy(this.destination.getHash160(), 0, step11, 3, 20);

        // Lock time
        byte[] step12 = hexToByte("00000000");

        // Hash code type
        byte[] step13 = hexToByte("01000000");

        return new byte[][]{step1, step2, step3, step4, step5, step6, step7, step8, step9,
                step10, step11, step12, step13};
    }

    /**
     * Signs the raw bytes using a travel document.
     * Follows the steps in this answer: https://bitcoin.stackexchange.com/a/5241
     * @return signedRawTransaction
     */
    public byte[] signRawTransaction(byte[][] parts, PassportConnection pcon) {

        byte[] rawTransaction = Bytes.concat(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                parts[6], parts[7], parts[8], parts[9], parts[10], parts[11], parts[12]);

        // Double hash transaction
        byte[] step14 = Sha256Hash.hash(Sha256Hash.hash(rawTransaction));

        // Generate signature and get publickey
        byte[] multiSignature = new byte[320];
        byte[] hashPart;

        for (int i = 0; i < 4; i++) {
            hashPart = Arrays.copyOfRange(step14, i * 8, i * 8 + 8);
            System.arraycopy(pcon.signData(hashPart), 0, multiSignature, i * 80, 80);
        }

        byte[] signatureLength = hexToByte("fd97014d4101");
        byte[] hashCodeType = hexToByte("01");
        byte[] publicKeyASN = pcon.getAAPublicKey().getEncoded();

        byte[] publicKey = new byte[81];
        System.arraycopy(publicKeyASN, publicKeyASN.length-81, publicKey, 0, 81);

        byte[] publickeyLength = hexToByte("4c51");

        // Set signature and pubkey in format
        byte[] step16 = Bytes.concat(signatureLength, multiSignature, hashCodeType, publickeyLength, publicKey);

        // Update transaction with signature and remove hash code type
        byte[] step19 = Bytes.concat(parts[0], parts[1], parts[2], parts[3], step16, parts[6],
                parts[7], parts[8], parts[9], parts[10], parts[11], parts[12]);

        return step19;
    }

    /**
     * Wraw the bytes in a real broadcastable transaction.
     * @param params
     * @return
     */
    public Transaction getTransaction(NetworkParameters params) {
        return new Transaction(params, this.data);
    }

    /**
     * Helper function, to fix the BigInteger leading zero issues.
     * @param s input string
     * @return byte array
     */
    private static byte[] hexToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
