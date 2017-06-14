package com.digitalvotingpass.blockchain;

import java.util.Date;

public interface BlockchainCallBackListener {
    void onInitComplete();
    void onDownloadComplete();
    void onDownloadProgress(double pct, int blocksSoFar, Date date);
}
