package com.digitalvotingpass.passportconnection;

import com.google.common.base.Preconditions;
import com.google.common.math.BigIntegerMath;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionMultiSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by daan on 6-6-17.
 */
public class PassportTransaction extends Transaction {

    private Address destination;
    private TransactionOutput utxo;

    /**
     * Class overwrites some signing functions
     *
     * @param params
     */
    public PassportTransaction(NetworkParameters params) {
        super(params);
    }

    /**
     * Set the address of the public key which can spend this output.
     *
     * @param destination
     */
    public void setDestinationAddress(Address destination) {
        this.destination = destination;
    }

    /**
     * Set the UTXO we would like to spend.
     *
     * @param utxo
     */
    public void setUTXO(TransactionOutput utxo) {
        this.utxo = utxo;
    }

    /**
     * Creates a byte array which contains all the element for a valid transaction.
     * Follows the steps in this answer: https://bitcoin.stackexchange.com/a/5241
     *
     * @return
     */
    public byte[] buildRawTransaction() {

        // Version
        byte[] step1 = new BigInteger("01000000", 16).toByteArray();

        // Number of outputs
        byte[] step2 = new BigInteger("01", 16).toByteArray();

        // Transaction hash
        byte[] step3 = utxo.getParentTransactionHash().getBytes();

        // Output index
        byte[] step4 = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(utxo.getIndex())).array();

        // Length of scriptsig (scriptpubkey)
        byte   step5 = (byte) (utxo.getScriptBytes().length & 0xFF);

        // Scriptpubkey of output we want to redeem
        byte[] step6 = utxo.getScriptBytes();

        // Unused sequence
        byte[] step7 = new BigInteger("FFFFFFFF", 16).toByteArray();

        // Number of outputs in transaction
        byte   step8 = 0x01;

        // Spend amount
        byte[] step9 = new BigInteger("0000000000000000", 16).toByteArray();

        // Redeem script (copies output and replaces address)
        byte[] step10 = step6
        System.arraycopy(this.destination.getHash160(), 0, step10, 3, 20);

        // Lock time
        byte[] step11 = new BigInteger("00000000", 16).toByteArray();

        // Hash code type
        byte[] step12 = new BigInteger("01000000", 16).toByteArray();
        
    }

    /**
     * Proxy function
     *
     * @param prevOut
     * @param scriptPubKey
     * @param pcon
     * @return
     * @throws ScriptException
     */
    public TransactionInput addPassportSignedInput(TransactionOutPoint prevOut, Script scriptPubKey, PassportConnection pcon) throws ScriptException {
        return addPassportSignedInput(prevOut, scriptPubKey, pcon, SigHash.ALL, false);
    }

    /**
     * Proxy function
     *
     * @param output
     * @param pcon
     * @return
     */
    public TransactionInput addPassportSignedInput(TransactionOutput output, PassportConnection pcon) {
        return addPassportSignedInput(output.getOutPointFor(), output.getScriptPubKey(), pcon);
    }

    /**
     * Signs the sha256 hash in 4 parts using the passport connected to PassportConnection
     *
     * @param prevOut
     * @param scriptPubKey
     * @param pcon
     * @param sigHash
     * @param anyoneCanPay
     * @return
     * @throws ScriptException
     */
    public TransactionInput addPassportSignedInput(TransactionOutPoint prevOut, Script scriptPubKey, PassportConnection pcon, Transaction.SigHash sigHash, boolean anyoneCanPay) throws ScriptException {
        Preconditions.checkState(!this.getOutputs().isEmpty(), "Attempting to sign tx without outputs.");
        TransactionInput input = new TransactionInput(this.params, this, new byte[0], prevOut);
        this.addInput(input);
        Sha256Hash hash = this.hashForSignature(this.getInputs().size() - 1, scriptPubKey, sigHash, anyoneCanPay);
        TransactionMultiSignature txSig = new TransactionMultiSignature();

        byte[] multiSignature = new byte[320];
        byte[] hashPart;

        for (int i = 0; i < 4; i++) {
            hashPart = Arrays.copyOfRange(hash.getBytes(), i * 8, i * 8 + 8);
            System.out.println(Utils.HEX.encode(hashPart));
            System.arraycopy(pcon.signData(hashPart), 0, multiSignature, i * 80, 80);
        }

        byte[] pubkeyBytes = pcon.getAAPublicKey().getEncoded();
        byte[] sigBytes = multiSignature;

        System.out.println(Utils.HEX.encode(pubkeyBytes));
        System.out.println(Utils.HEX.encode(sigBytes));

        Script scriptSig =  (new ScriptBuilder()).data(sigBytes).data(pubkeyBytes).build();

        if (scriptPubKey.isSentToAddress()) {
            input.setScriptSig(scriptSig);
            return input;
        } else {
            throw new ScriptException("Don\'t know how to sign for this kind of scriptPubKey: " + scriptPubKey);
        }
    }

}
