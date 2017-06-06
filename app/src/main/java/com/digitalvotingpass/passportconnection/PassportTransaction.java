package com.digitalvotingpass.passportconnection;

import com.google.common.base.Preconditions;

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

import java.util.Arrays;

/**
 * Created by daan on 6-6-17.
 */
public class PassportTransaction extends Transaction {

    /**
     * Class overwrites some signing functions
     *
     * @param params
     */
    public PassportTransaction(NetworkParameters params) {
        super(params);
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
