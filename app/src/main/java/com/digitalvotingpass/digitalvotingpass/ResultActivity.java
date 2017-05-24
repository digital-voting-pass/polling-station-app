package com.digitalvotingpass.digitalvotingpass;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.security.PublicKey;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.activity_result);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        TextView textPubKey = (TextView) findViewById(R.id.pubKey);
        TextView textSignedData = (TextView) findViewById(R.id.signedData);

        PublicKey pubKey = (PublicKey) extras.get("pubKey");
        byte[] signedData = (byte[]) extras.get("signedData");
        if (pubKey != null) {
            textPubKey.setText(Util.byteArrayToHexString(pubKey.getEncoded()));
        }
        textSignedData.setText((Util.byteArrayToHexString(signedData)));
    }

}
