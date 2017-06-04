package com.digitalvotingpass.blockchain;

import android.os.Environment;
import android.util.Log;

import com.digitalvotingpass.digitalvotingpass.SplashActivity;
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
    InetAddress peeraddr;
    WalletAppKit kit;
    private boolean initialized = false;

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

    public void startDownload(BlockchainCallBackListener listener) {
        if (!initialized) {
            BriefLogFormatter.init();
            final NetworkParameters params = MultiChainParams.get(
                    "00ea493df401cee6694c68a35d2b50dbdd197bd630cc2a95875933e16b7d0590",
                    "010000000000000000000000000000000000000000000000000000000000000000000000b59757b81c569f8b3854d83fe1b09b9a69a7b6ea1c33863a2a1640ee865ce1dc0f373059ffff0020a70100000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1704ffff002001040f4d756c7469436861696e20766f7465ffffffff0200000000000000002f76a914a18876083c6b7e3a76b0549dcd49cadf7222a05788ac1473706b703731000000000000ffffffff0f373059750000000000000000131073706b6e0200040101000104726f6f74756a00000000"
            );

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
