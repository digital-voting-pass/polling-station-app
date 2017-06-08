package com.digitalvotingpass.blockchain;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.digitalvotingpass.transactionhistory.Transaction;
import com.digitalvotingpass.utilities.MultiChainAddressGenerator;
import com.digitalvotingpass.utilities.Util;
import com.google.common.util.concurrent.Service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Asset;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MultiChainParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.utils.BriefLogFormatter;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockChain {
    public static final String PEER_IP = "188.226.149.56";
    private static BlockChain instance;
    private WalletAppKit kit;
    private BlockchainCallBackListener listener;
    private boolean initialized = false;
    private Context context;

    private InetAddress peeraddr;
    private long addressChecksum = 0xcc350cafL;
    private String[] version = {"00", "62", "8f", "ed"};
    final NetworkParameters params = MultiChainParams.get(
            "00d7fa1a62c5f1eadd434b9f7a8a657a42bd895f160511af6de2d2cd690319b8",
            "01000000000000000000000000000000000000000000000000000000000000000000000059c075b5dd26a328e185333ce1464b7279d476fbe901c38a003e694906e01c073b633559ffff0020ae0000000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1704ffff002001040f4d756c7469436861696e20766f7465ffffffff0200000000000000002f76a91474f585ec0e5f452a80af1e059b9d5079ec501d5588ac1473706b703731000000000000ffffffff3b633559750000000000000000131073706b6e0200040101000104726f6f74756a00000000",
            6799,
            Integer.parseInt(Arrays.toString(version).replaceAll(", |\\[|\\]", ""), 16),
            addressChecksum,
            0xf5dec1feL
    );

    private BlockChain(Context ctx) {
        this.context = ctx;
        try {
            peeraddr = InetAddress.getByName(PEER_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static synchronized BlockChain getInstance(Context ctx) throws Exception {
        if (instance == null) {
            if (ctx == null) throw new Exception("Context cannot be null on first call");
            instance = new BlockChain(ctx);
        }
        return instance;
    }

    public void setCallBackListener(BlockchainCallBackListener listener) {
        this.listener = listener;
        if (kit != null && listener != null)
            kit = kit.setDownloadListener(new ProgressTracker(listener));
    }

    public void startDownload() {
        if (!initialized) {
            BriefLogFormatter.init();
            String filePrefix = "voting-wallet";

            File walletFile = new File(Environment.getExternalStorageDirectory() + "/DigitalVotingPass");
            if (!walletFile.exists()) {
                if (!walletFile.mkdirs()) { //getParent because otherwise it creates a folder with that filename, we just need the dirs
                    Log.e("BlockChain", "Cannot create path!");
                }
            }
            // Start up a basic app using a class that automates some boilerplate.
            kit = new WalletAppKit(params, walletFile, filePrefix);

            if (params == RegTestParams.get()) {
                // Regression test mode is designed for testing and development only, so there's no public network for it.
                // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
                kit.connectToLocalHost();
            }

            if (listener != null)
                kit = kit.setDownloadListener(new ProgressTracker(listener));
            kit.setBlockingStartup(false);

            PeerAddress peer = new PeerAddress(params, peeraddr);
            kit.setPeerNodes(peer);
            kit.startAsync();
        }
    }

    public void disconnect() {
        kit.stopAsync();
    }

    /**
     * Gets the amount of voting tokens associated with the given public key.
     * @param pubKey - The Public Key read from the ID of the voter
     * @param mcAsset - The asset (election) that is chosen at app start-up.
     * @return - The amount of voting tokens available
     */
    public int getVotingPassAmount(PublicKey pubKey, Asset mcAsset) {
        Log.e(this.toString(), "total assets: " + kit.wallet().getAvailableAssets().size());
        Address mcAddress = Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
        Log.e(this.toString(), "asset 0: " + kit.wallet().getAssetBalance(mcAsset, mcAddress).getBalance());
        return (int) kit.wallet().getAssetBalance(mcAsset, mcAddress).getBalance();
    }

    public ArrayList<Asset> getAssets() {
        return kit.wallet().getAvailableAssets();
    }

    public boolean assetExists(Asset asset) {
        for(Asset a : getAssets()) {
            if(a.getName().equals(asset.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * Load transactions that involve the given public key, either incomming or outgoing.
     * @param pubKey PublicKey comming from epassport
     * @param assetFilter Asset for which transactions needs to be checked.
     * @return List containing interesting transactions.
     */
    public List<Transaction> getTransactions(PublicKey pubKey, Asset assetFilter) {
        List<Transaction> result = new ArrayList<>();
//        Address address = Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
//        String myAddress = address.toString();
        String myAddress =  "1HLv3p2ih3KrLJXoPzsGo9AWtvNbaTBN23Tdgd";
        Set<org.bitcoinj.core.Transaction> ts = kit.wallet().getTransactions(true);
        for (org.bitcoinj.core.Transaction t:ts) {
            if (!t.isCoinBase()){
                boolean checkedInputs = false;
                for (TransactionOutput o : t.getOutputs()) {
                    boolean sentToAddr = o.getScriptPubKey().isSentToAddress();
                    boolean isReturn = o.getScriptPubKey().isOpReturn();
                    if (sentToAddr && !isReturn) {
                        byte[] metaData = o.getScriptPubKey().getChunks().get(5).data;
                        byte[] identifier = Arrays.copyOfRange(metaData, 0, 4);
                        byte[] asset = Arrays.copyOfRange(metaData, 4, 20);
                        if(Arrays.equals(identifier, (new BigInteger("73706b71", 16)).toByteArray())
                                && Arrays.equals(asset, assetFilter.getId())) {
                            // Transaction is correct asset
                            byte[] quantity = Arrays.copyOfRange(metaData, 20, 28);
                            int amount = ByteBuffer.wrap(quantity).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            Address toAddress = o.getScriptPubKey().getToAddress(this.params);

                            // Check inputs of transaction to see if we sent it.
                            // Only when we know the transaction is actually the correct asset
                            if (!checkedInputs)
                                for (TransactionInput in : t.getInputs()) {
                                    Address fromAddress = in.getScriptSig().getFromAddress(params);
                                    if (myAddress.equals(fromAddress.toString())) {
                                        Date date = t.getUpdateTime();
                                        com.digitalvotingpass.transactionhistory.Transaction newTransaction =
                                                new com.digitalvotingpass.transactionhistory.Transaction("Sent " + amount, date, "To " + translateAddress(toAddress.toString()));
                                        result.add(newTransaction);
                                    }
                                    checkedInputs = true;
                                }

                            if (toAddress.toString().equals(myAddress)) {
                                if(t.getInputs().size() != 1) Log.e("BlockChain", "More than 1 inputs in transaction!");

                                // Method is deprecated because it is generally considered bad to use the from address of a transaction.
                                // This is because a transaction can have multiple inputs and the sender may not be in control of the given address (eg. exchange).
                                // This is not an issue for us since each transaction has 1 input only.
                                Address fromAddress = t.getInput(0).getScriptSig().getFromAddress(params);
                                Date date = t.getUpdateTime();
                                Transaction newTransaction =
                                        new Transaction("Received " + amount, date, "From " + translateAddress(fromAddress.toString()));
                                result.add(newTransaction);
                            }
                        }
                    }
                }
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String translateAddress(String address) {
        Map<String, String> addresses = Util.getKeyValueFromStringArray(context);
        if (addresses.containsKey(address))
            return addresses.get(address);
        else
            return address;
    }
}
