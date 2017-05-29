package com.digitalvotingpass.transactionhistory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.digitalvotingpass.digitalvotingpass.R;

import java.util.ArrayList;
import java.util.Date;

public class TransactionHistoryActivity extends AppCompatActivity {
    private ListView transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        transactionList = (ListView) findViewById(R.id.transaction_list);

        // create a transaction history array with all the transactions and add them to the list
        // TODO: handle actual transaction history input data
        ArrayList<Transaction> transactionHistory = new ArrayList<>();

        TransactionsAdapter adapter = new TransactionsAdapter(this, transactionHistory);
        transactionList.setAdapter(adapter);

        Transaction newTransaction = new Transaction("Received voting pass", new Date(), "some details about the transaction");
        Transaction newTransaction2 = new Transaction("Cast vote", new Date(), "some details about the transaction");

        adapter.add(newTransaction);
        adapter.add(newTransaction2);

    }
}
