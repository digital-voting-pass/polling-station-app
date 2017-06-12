package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.digitalvotingpass.camera.CameraActivity;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;
import com.digitalvotingpass.passportconnection.PassportConActivity;
import com.digitalvotingpass.utilities.Util;
import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main activity";
    public DocumentData documentData = new DocumentData();
    public Election election;

    private Button manualInput;
    private Button startOCR;

    public static final int GET_DOC_INFO = 1;
    public static final int CHOOSE_ELECTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity thisActivity = this;

        setContentView(R.layout.activity_main);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        Util.setupAppBar(appBar, this);

        // set the text of the appbar to the selected election
        setElectionInAppBar();

        manualInput = (Button) findViewById(R.id.manual_input_button);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ManualInputActivity.class);
                // send the docData to the manualinput in case a user wants to edit the existing docdata
                intent.putExtra(DocumentData.identifier, documentData);
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
     * Set the main_menu setup to the app bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles the action buttons on the app bar.
     * In our case it is only one that needs to be handled, the edit election action.
     * Starts the ElectionChoiceActivity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_election:
                startActivityForResult(new Intent(this, ElectionChoiceActivity.class), CHOOSE_ELECTION);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if we got documentdata and set the documentData attribute
        if(requestCode == GET_DOC_INFO && resultCode == RESULT_OK) {

            documentData = (DocumentData) data.getExtras().get(DocumentData.identifier);
            Intent intent = new Intent(this, PassportConActivity.class);
            intent.putExtra(DocumentData.identifier, documentData);
            startActivity(intent);
        }
        // reload the election choice from sharedpreferences
        if(requestCode == CHOOSE_ELECTION && resultCode == RESULT_OK) {
            setElectionInAppBar();
        }
    }

    /**
     * Gets the Election Object from sharedpreferences, sets the election attribute to the found
     * election and updates the textfields in the appbar to display the selected election.
     */
    public void setElectionInAppBar() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
        election = gson.fromJson(json, Election.class);

        if(election != null) {
            getSupportActionBar().setTitle(election.getKind());
            getSupportActionBar().setSubtitle(election.getPlace());
        }
    }

}
