package com.digitalvotingpass.transactionhistory;

import java.util.Date;

/**
 * Class used to store the transactions that are displayed in the transaction history activity.
 * TODO: refactor this to make use of actual details of the transactions
 */
public class Transaction {
    public String title;
    public Date time;
    public String transactionDetails;

    public Transaction(String title, Date time, String transactionDetails) {
        this.title = title;
        this.time = time;
        this.transactionDetails = transactionDetails;
    }
}
