package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.camera.CameraActivity;
import com.digitalvotingpass.passportconnection.PassportConActivity;
import com.google.gson.Gson;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main activity";
    public HashMap<String, String> documentData = new HashMap<>();
    public Election election;

    private Button manualInput;
    private Button startOCR;

    public static final int GET_DOC_INFO = 1;

    public static final String DOCUMENT_NUMBER = "Document Number";
    public static final String DATE_OF_BIRTH = "Date of Birth";
    public static final String EXPIRATION_DATE = "Expiration Date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity thisActivity = this;
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
        election = gson.fromJson(json, Election.class);

        setContentView(R.layout.activity_main);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        // set the text of the appbar to the selected election
        if(election != null) {
            getSupportActionBar().setTitle(election.getKind());
            getSupportActionBar().setSubtitle(election.getPlace());
        }

        manualInput = (Button) findViewById(R.id.manual_input_button);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ManualInputActivity.class);
                // send the docData to the manualinput in case a user wants to edit the existing docdata
                intent.putExtra("docData", documentData);
                startActivityForResult(intent, GET_DOC_INFO);
            }
        });
        startOCR = (Button) findViewById(R.id.start_ocr);
        startOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, CameraActivity.class);
                startActivityForResult(intent, GET_DOC_INFO);
            }
        });
    }

    /**
     * Called when the user taps the "Start Reading ID" button
     */
    public void startReading(View view) {
        if(documentData.isEmpty()) {
            Toast.makeText(this,R.string.scan_doc_details, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, GET_DOC_INFO);
        } else {
            Intent intent = new Intent(this, PassportConActivity.class);
            intent.putExtra("docData", documentData);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Both switches do the same thing, this might change in the future
        if(requestCode == GET_DOC_INFO){
            if (resultCode == RESULT_OK) {
                documentData = (HashMap<String, String>) data.getSerializableExtra("result");
            }
        }
    }

}
