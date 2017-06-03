package com.digitalvotingpass.blockchain;

import android.util.Log;

import org.bitcoinj.core.listeners.DownloadProgressTracker;

import java.util.Date;

class ProgressTracker extends DownloadProgressTracker {

    BlockchainCallBackListener listener;

    ProgressTracker(BlockchainCallBackListener listener) {
        this.listener = listener;
    }

    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        Log.e("Progress", "Progress update");
        listener.onDownloadProgress(pct, blocksSoFar, date);
        if (pct == 100) {
            listener.onDownloadComplete();
        }
    }
}
