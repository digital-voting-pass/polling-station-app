package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.transactionhistory.TransactionHistoryActivity;
import com.digitalvotingpass.utilities.Util;
import com.google.gson.Gson;

import net.sf.scuba.data.Gender;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;

import java.security.PublicKey;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
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
    private final int CONFIRMED = 3;
    private int votingPasses;
    private PublicKey pubKey;
    private Asset mcAsset;
    private ArrayList<byte[]> signedTransactions;
    private ArrayList<Transaction> pendingTransactions;
    private TSnackbar snack;

    private String preamble = "";

    /**
     * Checks if every pending transaction is confirmed and updates some view elements.
     */
    private TransactionConfidence.Listener confidenceListener = new TransactionConfidence.Listener() {
        @Override
        public void onConfidenceChanged(TransactionConfidence transactionConfidence, TransactionConfidence.Listener.ChangeReason
        changeReason) {
            if (checkAllPendingConfirmed()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAuthorizationStatus(CONFIRMED);
                        textVoterName.setText(getString(R.string.please_hand, preamble));
                        textVotingPasses.setText(getResources().getQuantityString(R.plurals.ballot_paper, votingPasses));
                        butProceed.setEnabled(true);
                        butProceed.getBackground().setColorFilter(null);
                        getSupportActionBar().setTitle(getTitle());
                    }
                });
                removeAllListeners();
            }
        }
    };

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
        signedTransactions = (ArrayList<byte[]>) extras.get("signedTransactions");

        setContentView(R.layout.activity_result);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        Util.setupAppBar(appBar, this);
        textVoterName = (TextView) findViewById(R.id.voter_name);
        textVotingPassAmount = (TextView) findViewById(R.id.voting_pass_amount);
        textVotingPasses = (TextView) findViewById(R.id.voting_passes);
        butTransactionHistory = (Button) findViewById(R.id.transactionHistory);
        butProceed = (Button) findViewById(R.id.proceed);

        butTransactionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, TransactionHistoryActivity.class);
                intent.putExtra("pubKey", pubKey);
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
     * Updates the snack showed at the top of the result screen.
     * @param text
     * @param backgroundColor
     * @param textColor
     */
    private void showSnack(CharSequence text, int backgroundColor, int textColor) {
        if (snack != null) { snack.dismiss(); }

        snack = TSnackbar.make(findViewById(R.id.result), "", TSnackbar.LENGTH_INDEFINITE);
        snack.setText(text);

        // Update background color
        View snackbarView = snack.getView();
        snackbarView.setBackgroundColor(getResources().getColor(backgroundColor));

        // Update text color
        TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(textColor));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

        snack.show();

        // Disable slide-to-dismiss feature.
        snack.getView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                snack.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                ((CoordinatorLayout.LayoutParams) snack.getView().getLayoutParams()).setBehavior(null);
                return true;
            }
        });
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
     * @param extras some information about the previous activity
     */
    public void handleData(Bundle extras) {
        Voter voter = (Voter) extras.get("voter");
        preamble = createPreamble(voter);
        try {
            if(pubKey != null && mcAsset != null) {
                votingPasses = BlockChain.getInstance(null).getVotingPassAmount(pubKey, mcAsset);
            } else {
                votingPasses = 0;
            }
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
                textVotingPasses.setText(getResources().getQuantityString(R.plurals.ballot_paper, votingPasses));
            }
            textVotingPassAmount.setText(Integer.toString(votingPasses));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // Only show a preamble when the voter is a male or female
        if (gender == Gender.FEMALE || gender == Gender.MALE) {
            // Capitalize the first word since this is sometimes van or van de.
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
     *  - Succesful (There are )
     *  - Confirmed (transaction was accepted on the blockchain)
     *  - Waiting (waiting for confirmation or rejection of transaction from blockchain)
     *  - Failed (request of balance showed no voting passes left or transaction was rejected)
     *
     *  Only show cancel button when state is succesful (otherwise nothing to cancel)
     */
    public void setAuthorizationStatus(int newState) {
        if (authorizationState == newState) { return; }
        authorizationState = newState;
        switch (newState) {
            case FAILED:
                showSnack(
                    getResources().getText(R.string.authorization_failed),
                    R.color.redFailed,
                    R.color.white
                );
                butProceed.setText(R.string.proceed_home);
                if(cancelAction != null) {
                    cancelAction.setVisible(false);
                }
                break;
            case WAITING:
                showSnack(
                    getResources().getText(R.string.authorization_wait),
                    R.color.orangeWait,
                    R.color.white
                );
                butProceed.setText(R.string.proceed_home);
                if(cancelAction != null) {
                    cancelAction.setVisible(true);
                }
                break;
            case SUCCES:
                showSnack(
                    getResources().getQuantityString(R.plurals.authorization_successful, votingPasses),
                    R.color.govLightBlue,
                    R.color.govDarkBlue
                );
                butProceed.setText(R.string.proceed_cast_vote);
                if(cancelAction != null) {
                    cancelAction.setVisible(true);
                }
                break;
            case CONFIRMED:
                System.out.println("confirmed");
                showSnack(
                    getResources().getQuantityString(R.plurals.authorization_confirmed, votingPasses, votingPasses),
                    R.color.greenSucces,
                    R.color.white
                );
                butProceed.setText(R.string.proceed_home);
                if(cancelAction != null) {
                    cancelAction.setVisible(true);
                }
                break;
            default:
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
     */
    public void nextVoter() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Send the transaction to the blockchain and wait for a confirmation.
     */
    public void confirmVote() {
        try {
            this.pendingTransactions = BlockChain.getInstance(null).broadcastTransactions(signedTransactions);
            setAuthorizationStatus(this.WAITING);
            butProceed.setText(R.string.waiting_confirmation);
            butProceed.setEnabled(false);
            butProceed.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            getSupportActionBar().setTitle(R.string.waiting_confirmation);
            attachTransactionListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets listeners on the pending transactions to keep an eye on the confirmations.
     */
    public void attachTransactionListeners() {
        for (final Transaction pendingTx : this.pendingTransactions) {
            pendingTx.getConfidence().addEventListener(confidenceListener);
        }
    }

    /**
     * Remove all pending transaction listeners.
     */
    public void removeAllListeners() {
        for (Transaction transaction : pendingTransactions) {
            transaction.getConfidence().removeEventListener(confidenceListener);
        }
    }

    /**
     * Checks if every pending transaction has at least one confirmation.s
     * @return
     */
    public boolean checkAllPendingConfirmed() {
        for (Transaction tx : pendingTransactions) {
            if (tx.getConfidence().getDepthInBlocks() < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cancel the voting process for the current voter and return to the mainactivity for starting
     * a new process for the next voter.
     */
    public void cancelVoting() {
        nextVoter();
    }

}
