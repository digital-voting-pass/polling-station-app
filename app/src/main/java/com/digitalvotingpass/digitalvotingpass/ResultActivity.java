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
import android.widget.TextView;
import android.widget.Toast;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.transactionhistory.TransactionHistoryActivity;
import com.digitalvotingpass.utilities.Util;
import com.google.gson.Gson;

import net.sf.scuba.data.Gender;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Block;

import java.security.PublicKey;

public class ResultActivity extends AppCompatActivity {
    private TextView textAuthorization;
    private TextView textVoterName;
    private TextView textVotingPassAmount;
    private TextView textVotingPasses;
    private Button butTransactionHistory;
    private Button butProceed;
    private MenuItem cancelAction;

    private int authorizationState = 1;
    private final int FAILED = 0;
    private final int WAITING = 1;
    private final int SUCCES = 2;
    private PublicKey pubKey;
    private Asset mcAsset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ResultActivity thisActivity = this;
        Bundle extras = getIntent().getExtras();

        // get election from sharedpreferences
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
        mcAsset = gson.fromJson(json, Election.class).getAsset();

        pubKey = (PublicKey) extras.get("pubKey");

        setContentView(R.layout.activity_result);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        Util.setupAppBar(appBar, this);

        textAuthorization = (TextView) findViewById(R.id.authorization);
        textVoterName = (TextView) findViewById(R.id.voter_name);
        textVotingPassAmount = (TextView) findViewById(R.id.voting_pass_amount);
        textVotingPasses = (TextView) findViewById(R.id.voting_passes);
        butTransactionHistory = (Button) findViewById(R.id.transactionHistory);
        butProceed = (Button) findViewById(R.id.proceed);

        butTransactionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, TransactionHistoryActivity.class);
                startActivity(intent);
            }
        });

        butProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceed();
            }
        });
    }

    /**
     * Set the result_menu setup to the app bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu, menu);
        cancelAction = menu.findItem(R.id.action_cancel);
        // Start handleData when menu is fully loaded. This method is loaded after onCreate()
        // TODO: move to a better place
        Bundle extras = getIntent().getExtras();
        handleData(extras);
        return true;
    }

    /**
     * Handles the action buttons on the app bar.
     * In our case it is only one that needs to be handled, the cancel button.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                cancelVoting();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays all the data gotten from the blockchain and the passport. Transferred in the extras
     * field of the intent.
     *
     */
    public void handleData(Bundle extras) {
        Voter voter = (Voter) extras.get("voter");
        String preamble = createPreamble(voter);
        int votingPasses;
        votingPasses = BlockChain.getInstance().getVotingPassAmount(pubKey, mcAsset);
        if(votingPasses == 0) {
            setAuthorizationStatus(FAILED);
        } else {
            setAuthorizationStatus(SUCCES);
        }

        textVoterName.setText(getString(R.string.has_right, preamble));
        // display singular or plural form of voting passes based on amount
        if(votingPasses == 1) {
            textVotingPasses.setText(R.string.voting_pass);
        } else {
            textVotingPasses.setText(R.string.voting_passes);
        }
        textVotingPassAmount.setText(Integer.toString(votingPasses));
    }

    /**
     * Create preamble string, so Mrs de Vries or Mr de Vries.
     * @param voter The voter.
     * @return The preamble string.
     */
    private String createPreamble(Voter voter) {
        //set the gender strings, this is necessary because we can't get
        //the strings in the voter class and passing a Context object might
        //cause memory leaks
        voter.setGenderStrings(getString(R.string.gender_male), getString(R.string.gender_female),
                getString(R.string.gender_unspecified), getString(R.string.gender_unknown));

        Gender gender = voter.getGender();
        String preamble;
        //Only show a preamble when the voter is a male or female
        if(gender == Gender.FEMALE || gender == Gender.MALE) {
            //Capitalize the first word since this is sometimes van or van de.
            String firstWord = Voter.capitalizeFirstLetter(voter.getLastName().split(" ")[0]);
            preamble = voter.genderToString() + " " + firstWord + " " + voter.getLastName().substring(firstWord.length());
        } else {
            preamble = voter.getFirstName() + " " + voter.getLastName();
        }
        return preamble.trim();

    }
    /**
     * Sets the textview which displays the authorization status, based on the current state of the
     * process to one of the following:
     *  - Succesful (transaction was accepted on the blockchain)
     *  - Waiting (waiting for confirmation or rejection of transaction from blockchain)
     *  - Failed (request of balance showed no voting passes left or transaction was rejected)
     *
     *  Only show cancel button when state is succesful (otherwise nothing to cancel)
     */
    public void setAuthorizationStatus(int newState) {
        //TODO: implement actual conditions for either one of the three states.
        authorizationState = newState;
        switch (newState) {
            case FAILED:
                textAuthorization.setTextColor(getResources().getColor(R.color.redFailed));
                textAuthorization.setText(R.string.authorization_failed);
                textAuthorization.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.not_approve,0);
                butProceed.setText(R.string.proceed_home);
                if(cancelAction != null) {
                    cancelAction.setVisible(false);
                }
                break;
            case WAITING:
                textAuthorization.setTextColor(getResources().getColor(R.color.orangeWait));
                textAuthorization.setText(R.string.authorization_wait);
                butProceed.setText(R.string.proceed_home);
                if(cancelAction != null) {
                    cancelAction.setVisible(true);
                }
                break;
            case SUCCES:
                textAuthorization.setTextColor(getResources().getColor(R.color.greenSucces));
                textAuthorization.setText(R.string.authorization_succesful);
                textAuthorization.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.approve,0);
                butProceed.setText(R.string.proceed_cast_vote);
                if(cancelAction != null) {
                    cancelAction.setVisible(true);
                }
                break;
            default:
                textAuthorization.setTextColor(getResources().getColor(R.color.orangeWait));
                textAuthorization.setText(R.string.authorization_wait);
        }
    }

    /**
     * Handles which step must be taken next when the proceed button is clicked.
     * Either calls nextVoter or confirmVote methods, based on the current text in the button.
     * This would be the action that the user wants to perform.
     */
    public void proceed() {
        String currentText = butProceed.getText().toString();
        if(currentText.equals(getString(R.string.proceed_home))) {
            nextVoter();
        } else if(currentText.equals(getString(R.string.proceed_cast_vote))){
            confirmVote();
        }
    }

    /**
     * Return to the main activity for starting the process for the next voter.
     * TODO: implement this method
     */
    public void nextVoter() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Send the transaction to the blockchain and wait for a confirmation.
     * TODO: implement this method
     */
    public void confirmVote() {
        Toast.makeText(this, "Transaction sent", Toast.LENGTH_LONG).show();
        butProceed.setText(R.string.proceed_home);
    }

    /**
     * Cancel the voting process for the current voter and return to the mainactivity for starting
     * a new process for the next voter.
     * TODO: implement this method
     */
    public void cancelVoting() {

        nextVoter();
    }

}
