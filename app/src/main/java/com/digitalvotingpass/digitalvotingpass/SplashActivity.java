package com.digitalvotingpass.digitalvotingpass;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.digitalvotingpass.camera.Camera2BasicFragment;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MultiChainParams;
import org.bitcoinj.params.RegTestParams;

import java.io.File;
import java.net.InetAddress;

public class SplashActivity extends Activity{
    // Duration of splash screen in millis
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private String TAG = getClass().getSimpleName();

    /**
     * Creates a splash screen
     * @param bundle
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash_screen);
        final Activity thisActivity= this;
        //Start MainActivity after SPLASH_DISPLAY_LENGTH
        //This delay can be removed when we need to actually load data
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Create an Intent that will start the MainActivity
//                Intent mainIntent = new Intent(SplashActivity.this, ElectionChoiceActivity.class);
//                SplashActivity.this.startActivity(mainIntent);
//                SplashActivity.this.finish();
//                receiveBlockChain();
                try {
                    receiveBlockChain();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);

        try {
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Could not load blockchain");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 5) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                if(mIsStateAlreadySaved){
//                    mPendingShowDialog = true;
//                } else {
//                    Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.ocr_storage_permission_explanation))
//                            .show(getChildFragmentManager(), FRAGMENT_DIALOG);
//                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.ocr_storage_permission_explanation))
                    .show(getFragmentManager(), "");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
        }
    }

    public void receiveBlockChain() throws Exception {
        String key = "17UGUPmLNtPRzT7DUHvWmQyLHcoKDUoH99DeEm";
        final NetworkParameters params = MultiChainParams.get(
                "00a9b1b476c6909ac1c8b6393a8721052a435e10367aedbda4b92899ec8d6a8b",
                "010000000000000000000000000000000000000000000000000000000000000000000000b2e938f89a844a23ca2c1a7f5c1b20f83b4c92c279f48fdc55960c4bf5020cbe19482459ffff0020750100000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1704ffff002001040f4d756c7469436861696e20766f7465ffffffff0200000000000000002f76a9142fdf35a8cac6bb3dc4fa216303fe312b8ed40b8488ac1473706b703731000000000000ffffffff19482459750000000000000000131073706b6e0200040101000104726f6f74756a00000000"
        );

        String filePrefix = "forwarding-service21" + Math.round(Math.random() * 100);

        // Parse the address given as the first parameter.
        Address forwardingAddress = Address.fromBase58(params, key);

        File walletFile = new File(Environment.getExternalStorageDirectory() + "/wallet.dat");
        // Start up a basic app using a class that automates some boilerplate.
        WalletAppKit kit = new WalletAppKit(params, walletFile, filePrefix);

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        PeerAddress peer = new PeerAddress(params, InetAddress.getByName("188.226.149.56"));


        kit.setPeerNodes(peer);

        //  Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();

    }

}
