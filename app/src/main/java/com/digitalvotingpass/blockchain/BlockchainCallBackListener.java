package com.digitalvotingpass.blockchain;

import java.util.Date;

/**
 * Created by jonathan on 6/2/17.
 */

public interface BlockchainCallBackListener {
    void onInitComplete();
    void onDownloadComplete();
    void onDownloadProgress(double pct, int blocksSoFar, Date date);
}
