package com.digitalvotingpass.digitalvotingpass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.PublicKey;

public class ResultActivity extends AppCompatActivity {
    private Button transactionHistory;
    private TextView textPubKey;
    private TextView textSignedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ResultActivity thisActivity = this;
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.activity_result);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        textPubKey = (TextView) findViewById(R.id.pubKey);
        textSignedData = (TextView) findViewById(R.id.signedData);

        PublicKey pubKey = (PublicKey) extras.get("pubKey");
        byte[] signedData = (byte[]) extras.get("signedData");
        if (pubKey != null) {
            textPubKey.setText(Util.byteArrayToHexString(pubKey.getEncoded()));
        }
        textSignedData.setText((Util.byteArrayToHexString(signedData)));

        transactionHistory = (Button) findViewById(R.id.transactionHistory);
        transactionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, TransactionHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

}
