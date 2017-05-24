package com.digitalvotingpass.digitalvotingpass;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class TransactionHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        setContentView(R.layout.activity_transaction_history);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        TextView textTransaction = (TextView) findViewById(R.id.transaction);
        textTransaction.setText("Here a transaction is showed");
    }
}
