package com.digitalvotingpass.blockchain;

import org.bitcoinj.core.listeners.DownloadProgressTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ProgressTracker extends DownloadProgressTracker {

    private List<BlockchainCallBackListener> listeners;

    ProgressTracker() {
        this.listeners = new ArrayList<>();
    }


    /**
     * Add a listener.
     * @param listener the listener.
     */
    public void addListener(BlockchainCallBackListener listener) {
        if(listener != null)
            this.listeners.add(listener);
    }

    /**
     * Remove a listener.
     * @param listener the listener.
     */
    public void removeListener(BlockchainCallBackListener listener) {
        if(listener != null)
            this.listeners.remove(listener);
    }

    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        for(BlockchainCallBackListener listener : listeners) {
            listener.onDownloadProgress(pct, blocksSoFar, date);
        }
    }

    @Override
    protected void startDownload(int blocks) {
        super.startDownload(blocks);
        for(BlockchainCallBackListener listener : listeners) {
            listener.onInitComplete();
        }
    }

    @Override
    protected void doneDownload() {
        super.doneDownload();
        for(BlockchainCallBackListener listener : listeners) {
            listener.onDownloadComplete();
        }
    }
}
