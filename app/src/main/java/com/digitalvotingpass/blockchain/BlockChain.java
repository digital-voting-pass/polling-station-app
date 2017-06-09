package com.digitalvotingpass.blockchain;

import android.os.Environment;
import android.util.Log;

import com.digitalvotingpass.utilities.Util;
import com.google.common.util.concurrent.Service;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MultiChainParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.utils.BriefLogFormatter;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BlockChain {
    public static final String PEER_IP = "188.226.149.56";
    private static BlockChain instance;
    private WalletAppKit kit;
    private BlockchainCallBackListener listener;
    private boolean initialized = false;

    private InetAddress peeraddr;

    private BlockChain() {
        try {
            peeraddr = InetAddress.getByName(PEER_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static synchronized BlockChain getInstance() {
        if (instance == null) {
            instance = new BlockChain();
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
            final NetworkParameters params = MultiChainParams.get(
                    "00d7fa1a62c5f1eadd434b9f7a8a657a42bd895f160511af6de2d2cd690319b8",
                    "01000000000000000000000000000000000000000000000000000000000000000000000059c075b5dd26a328e185333ce1464b7279d476fbe901c38a003e694906e01c073b633559ffff0020ae0000000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1704ffff002001040f4d756c7469436861696e20766f7465ffffffff0200000000000000002f76a91474f585ec0e5f452a80af1e059b9d5079ec501d5588ac1473706b703731000000000000ffffffff3b633559750000000000000000131073706b6e0200040101000104726f6f74756a00000000",
                    6799,
                    Integer.parseInt("00628fed", 16),
                    0xcc350cafL,
                    0xf5dec1feL
            );

            String filePrefix = "voting-wallet";
            File walletFile = new File(Environment.getExternalStorageDirectory() + "/" + Util.FOLDER_DIGITAL_VOTING_PASS);
            if (!walletFile.exists()) {
                walletFile.mkdirs();
            }
            kit = new WalletAppKit(params, walletFile, filePrefix);

            if (listener != null)
                kit = kit.setDownloadListener(new ProgressTracker(listener));
            kit.setBlockingStartup(false);

            PeerAddress peer = new PeerAddress(params, peeraddr);
            kit.setPeerNodes(peer);
            kit.startAsync();
        }
    }

    public Service.State state() {
        if (kit == null)
            return null;
        return kit.state();
    }

    public void disconnect() {
        kit.stopAsync();
    }

}
