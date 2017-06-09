package com.digitalvotingpass.transactionhistory;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class TransactionHistoryActivity extends AppCompatActivity {
    private TransactionsAdapter adapter;

    private Runnable loadTransactions = new Runnable() {
        @Override
        public void run() {
            try {
                Bundle extras = getIntent().getExtras();
                PublicKey pubKey = (PublicKey) extras.get("pubKey");

                // Get elections
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
                Asset mcAsset = gson.fromJson(json, Election.class).getAsset();

                // Set address to app bar and click listener.
                BlockChain bc = BlockChain.getInstance(null);
                getSupportActionBar().setSubtitle("Address: " + bc.getAddress(pubKey).toString());
                setAddressClickListener(bc.getAddress(pubKey).toString());

                // Load transactions
                final List<TransactionHistoryItem> transactions = bc.getMyTransactions(pubKey, mcAsset, getApplicationContext());
                Collections.sort(transactions); //sort transactions desc on date
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    findViewById(R.id.loading_circle).setVisibility(View.GONE);
                    if (transactions.size() > 0) {
                        adapter.addAll(transactions);
                        adapter.notifyDataSetChanged();
                    } else {
                        findViewById(R.id.no_transactions).setVisibility(View.VISIBLE);
                    }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        Util.setupAppBar(appBar, this);
        ListView transactionList = (ListView) findViewById(R.id.transaction_list);

        // create a transaction history array with all the transactions and add them to the list
        ArrayList<TransactionHistoryItem> transactionHistory = new ArrayList<>();
        adapter = new TransactionsAdapter(this, transactionHistory);
        transactionList.setAdapter(adapter);

        // Load transactions in separate thread since this can take a while.
        HandlerThread thread = new HandlerThread("transactions");
        thread.start();
        new Handler(thread.getLooper()).post(loadTransactions);
    }

    /**
     * Sets listener on the app bar to copy the address to clipboard.
     * @param address string
     */
    protected void setAddressClickListener(final String address) {
        findViewById(R.id.app_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add address to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText((CharSequence)"Address", (CharSequence)address);
                clipboard.setPrimaryClip(clip);

                // Show snackbar
                Snackbar.make(findViewById(R.id.transaction_list), "Blockchain address copied to clipboard", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
