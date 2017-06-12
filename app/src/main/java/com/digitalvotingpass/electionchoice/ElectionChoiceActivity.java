package com.digitalvotingpass.electionchoice;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.digitalvotingpass.MainActivity;
import com.digitalvotingpass.digitalvotingpass.R;
import com.digitalvotingpass.utilities.Util;
import com.google.gson.Gson;

import org.bitcoinj.core.Asset;

import java.util.ArrayList;

public class ElectionChoiceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private ListView electionListView;
    private ElectionsAdapter electionsAdapter;
    private MenuItem searchItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_choice);
        final ElectionChoiceActivity thisActivity = this;

        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setTitle(getString(R.string.election_choice));
        Util.setupAppBar(appBar, this);

        electionListView = (ListView) findViewById(R.id.election_list);
        try {
            electionsAdapter = new ElectionsAdapter(this, loadElections(BlockChain.getInstance(null).getAssets()));
            electionListView.setAdapter(electionsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        electionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                // Get the election associated with the clicked listItem and save it to sharedpreferences
                saveElection((Election) parent.getItemAtPosition(position));

                // If the activity was not started for result (from mainactivity) start mainActivity.
                // Otherwise finish this activity to return to mainactivity and set flag that something changed.
                if(getCallingActivity() == null) {
                    Intent intent = new Intent(thisActivity, MainActivity.class);
                    startActivity(intent);
                } else {
                    thisActivity.setResult(Activity.RESULT_OK, new Intent());
                    finish();
                }
            }
        });
    }

    public ElectionsAdapter getAdapter() {
        return electionsAdapter;
    }

    /**
     * Saves an election object to the sharedpreferences so other activities can access it.
     * @param election
     */
    private void saveElection(Election election) {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(election);
        prefsEditor.putString(getString(R.string.shared_preferences_key_election), json);
        prefsEditor.commit();
    }

    /**
     * Creates an election array by getting all the assets available on the blockchain and add them to the list.
     * Sets the kind field of an Election object based on the prefix found in the asset name
     * T_ = R.string.tweedekamer
     * P_ = R.string.provinciaal
     * G_ = R.string.gemeente
     * W_ = R.string.waterschap
     *
     * Asset name must be of the format "K_Place"
     *
     * Sets the place field of an Election object based on the asset name
     * @Param assetList - a list of asset from which Election objects can be created
     * @return electionChoices - a list of current elections that can be chosen from
     */
    public ArrayList<Election> loadElections(ArrayList<Asset> assetList) {
        ArrayList<Election> electionChoices = new ArrayList<>();
        for(Asset a : assetList) {
            electionChoices.add(Election.parseElection(a, getApplicationContext()));
        }
        return electionChoices;
    }

    /**
     * Handles the tasks that need to be performed when the menu is created.
     * Sets the custom menu layout and sets up the search field handlers.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choice_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    /**
     * This method must be overridden.
     * No use for submitting the text, because list is updated live. Therefore return false.
     * @param query - the input in the search field
     * @return false - this method is not used
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * When the input of the search field changes, call the custom filter so the list is updated
     * @param newText - New input to the search field
     * @return true - the method is called
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        electionsAdapter.getFilter().filter(newText);
        return true;
    }
}
