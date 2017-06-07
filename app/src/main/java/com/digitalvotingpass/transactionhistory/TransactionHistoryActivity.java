package com.digitalvotingpass.transactionhistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.digitalvotingpass.R;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.utilities.Util;
import com.google.gson.Gson;

import org.bitcoinj.core.Asset;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransactionHistoryActivity extends AppCompatActivity {
    private ListView transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        Bundle extras = getIntent().getExtras();

        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        Util.setupAppBar(appBar, this);

        transactionList = (ListView) findViewById(R.id.transaction_list);

        // create a transaction history array with all the transactions and add them to the list
        ArrayList<Transaction> transactionHistory = new ArrayList<>();
        TransactionsAdapter adapter = new TransactionsAdapter(this, transactionHistory);
        transactionList.setAdapter(adapter);

        PublicKey pubKey = (PublicKey) extras.get("pubKey");

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
        Asset mcAsset = gson.fromJson(json, Election.class).getAsset();

        List<Transaction> transactions = null;
        try {
            transactions = BlockChain.getInstance(null).getTransactions(pubKey, mcAsset);
            Collections.sort(transactions);
            for (Transaction t: transactions) {
                adapter.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
