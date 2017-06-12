package com.digitalvotingpass.blockchain;

import android.content.Context;
import android.os.Environment;
import android.provider.Telephony;
import android.util.Log;

import com.digitalvotingpass.passportconnection.PassportConnection;
import com.digitalvotingpass.passportconnection.PassportTransactionFormatter;
import com.digitalvotingpass.digitalvotingpass.R;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.transactionhistory.TransactionHistoryItem;
import com.digitalvotingpass.utilities.MultiChainAddressGenerator;
import com.google.common.util.concurrent.MoreExecutors;
import com.digitalvotingpass.utilities.Util;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Asset;
import org.bitcoinj.core.AssetBalance;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MultiChainParams;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;

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
    private ProgressTracker progressTracker;

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
    private Address masterAddress = Address.fromBase58(params, "1GoqgbPZUV2yuPZXohtAvB2NZbjcew8Rk93mMn");

    private BlockChain(Context ctx) {
        this.context = ctx;
        try {
            peeraddr = InetAddress.getByName(PEER_IP);
            progressTracker = new ProgressTracker();
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

    /**
     * Add a listener.
     * @param listener The listener.
     */
    public void addListener(BlockchainCallBackListener listener) {
        progressTracker.addListener(listener);
    }

    /**
     * Remove a listener.
     * @param listener a listener.
     */
    public void removeListener(BlockchainCallBackListener listener) {
        progressTracker.removeListener(listener);
    }

    public void startDownload() {
        if (!initialized) {
            BriefLogFormatter.init();
            String filePrefix = "voting-wallet";
            File walletFile = new File(Environment.getExternalStorageDirectory() + "/" + Util.FOLDER_DIGITAL_VOTING_PASS);
            if (!walletFile.exists()) {
                walletFile.mkdirs();
            }
            kit = new WalletAppKit(params, walletFile, filePrefix);

            //set the observer
            kit.setDownloadListener(progressTracker);

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
        if(pubKey != null && mcAsset != null) {
            Address mcAddress = Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
            return (int) kit.wallet().getAssetBalance(mcAsset, mcAddress).getBalance();
        } else {
            return 0;
        }
    }

    public ArrayList<Asset> getAssets() {
        return kit.wallet().getAvailableAssets();
    }

    public boolean assetExists(Asset asset) {
        if(asset != null) {
            for (Asset a : getAssets()) {
                if (a.getName().equals(asset.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the balance of a public key based on the information on the blockchain.
     * @param pubKey
     * @return
     */
    public AssetBalance getVotingPassBalance(PublicKey pubKey, Asset asset) {
        Address address = Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
        return kit.wallet().getAssetBalance(asset, address);
    }

    /**
     * Spends all outputs in this balance to the master address.
     * @param balance
     * @param pcon
     */
    public ArrayList<byte[]> getSpendUtxoTransactions(PublicKey pubKey, AssetBalance balance, PassportConnection pcon) throws Exception {
        ArrayList<byte[]> transactions = new ArrayList<byte[]>();

        for (TransactionOutput utxo : balance) {
            transactions.add(utxoToSignedTransaction(pubKey, utxo, masterAddress, pcon));
        }
        return transactions;
    }

    /**
     * Create a new transaction, signes it with the travel document.
     * @param utxo
     * @param destination
     * @param pcon
     */
    public byte[] utxoToSignedTransaction(PublicKey pubKey, TransactionOutput utxo, Address destination, PassportConnection pcon) throws Exception {
        return new PassportTransactionFormatter(utxo, destination)
                .buildAndSign(pubKey, pcon);
    }


    /**
     * Broadcasts the list of signed transactions.
     * @param transactionsRaw
     */
    public ArrayList<Transaction> broadcastTransactions(ArrayList<byte[]> transactionsRaw) {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        for (byte[] transactionRaw : transactionsRaw) {
            final Wallet.SendResult result = new Wallet.SendResult();
            result.tx = new Transaction(params, transactionRaw);

            result.broadcast = kit.peerGroup().broadcastTransaction(result.tx);
            result.broadcastComplete = result.broadcast.future();

            result.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Asset spent! txid: " + result.tx.getHashAsString());
                }
            }, MoreExecutors.directExecutor());

            transactions.add(result.tx);
        }
        return transactions;
    }


    /**
     * Returns the address corresponding to the pubkey.
     * @param pubKey
     * @return Address
     */
    public Address getAddress(PublicKey pubKey) {
        return Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
    }

    /**
     * Load transactions that involve the given public key, either incomming or outgoing.
     * @param pubKey PublicKey comming from epassport
     * @param assetFilter Asset for which transactions needs to be checked.
     * @return List containing interesting transactions.
     */
    public List<TransactionHistoryItem> getMyTransactions(PublicKey pubKey, Asset assetFilter) {
        List<TransactionHistoryItem> result = new ArrayList<>();
        Address address = Address.fromBase58(params, MultiChainAddressGenerator.getPublicAddress(version, Long.toString(addressChecksum), pubKey));
        List<Transaction> ts = kit.wallet().getAssetTransactions(address, assetFilter);

        for (Transaction transaction : ts) {
            for (TransactionOutput o : transaction.getOutputs()) {
                boolean sentToAddr = o.getScriptPubKey().isSentToAddress();
                boolean isReturn = o.getScriptPubKey().isOpReturn();
                if (sentToAddr && !isReturn) {
                    byte[] metaData = o.getScriptPubKey().getChunks().get(5).data;
                    byte[] quantity = Arrays.copyOfRange(metaData, 20, 28);
                    int amount = ByteBuffer.wrap(quantity).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    Address toAddress = o.getScriptPubKey().getToAddress(this.params);
                    Address fromAddress = transaction.getInput(0).getFromAddress();
                    Date date = transaction.getUpdateTime();
                    result.add(createTransactionHistoryItem(address, fromAddress, toAddress, date, assetFilter, amount));
                }
            }
        }
        return result;
    }

    private TransactionHistoryItem createTransactionHistoryItem(Address myAddress, Address fromAddress, Address toAddress, Date date, Asset assetFilter, int amount) {
        String titleFormat = "";
        String detailFormat = "";

        if (myAddress.equals(fromAddress)) {
            titleFormat = context.getString(R.string.transaction_sent_item_format_title);
            detailFormat = context.getString(R.string.transaction_sent_item_format_detail);
        } else if (myAddress.equals(toAddress)) {
            titleFormat = context.getString(R.string.transaction_received_item_format_title);
            detailFormat = context.getString(R.string.transaction_received_item_format_detail);
        }
        if (titleFormat.equals("")) {
            return null;
        }
        return new TransactionHistoryItem(
                String.format(titleFormat,
                        amount,
                        Election.parseElection(assetFilter, context).getKind(),
                        Election.parseElection(assetFilter, context).getPlace()),
                date,
                String.format(detailFormat, translateAddress(toAddress.toString())));
    }

    /**
     * Translate a MultiChain address to a meaningful String value if such a value is defined for
     * that address in strings.xml
     * @param address String value of MultiChain address
     * @return String containing defined mapped value or {@code address} if no mapping was found.
     */
    public String translateAddress(String address) {
        Map<String, String> addresses = Util.getKeyValueFromStringArray(context);
        if (addresses.containsKey(address))
            return addresses.get(address);
        else
            return address;
    }
}
