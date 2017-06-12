package com.digitalvotingpass.transactionhistory;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Class used to store the transactions that are displayed in the transaction history activity.
 */
public class TransactionHistoryItem implements Comparable<TransactionHistoryItem> {
    public String title;
    public Date time;
    public String transactionDetails;

    public TransactionHistoryItem(String title, Date time, String transactionDetails) {
        this.title = title;
        this.time = time;
        this.transactionDetails = transactionDetails;
    }

    @Override
    public int compareTo(@NonNull TransactionHistoryItem o) {
        return (int) (time.getTime() - o.time.getTime());
    }
}
