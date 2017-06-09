package com.digitalvotingpass.blockchain;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by daan on 9-6-17.
 */

public class ConfirmationTracker extends DownloadProgressTracker {

    BlockchainCallBackListener listener;
    ArrayList<Transaction> transactions;

    ConfirmationTracker(BlockchainCallBackListener listener, ArrayList<Transaction> transactions) {
        this.listener = listener;
        this.transactions = transactions;
    }

    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        listener.onDownloadProgress(pct, blocksSoFar, date);
    }

    @Override
    protected void startDownload(int blocks) {
        super.startDownload(blocks);
        listener.onInitComplete();
    }

    @Override
    protected void doneDownload() {
        super.doneDownload();
        listener.onDownloadComplete();
    }
}

