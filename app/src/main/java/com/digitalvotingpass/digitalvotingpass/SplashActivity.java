package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;
import com.google.gson.Gson;

public class SplashActivity extends Activity{
    // Duration of splash screen in millis
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /**
     * Creates a splash screen
     * @param bundle
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash_screen);
        final Activity thisActivity = this;

        //Start MainActivity after SPLASH_DISPLAY_LENGTH
        //This delay can be removed when we need to actually load data
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Create an Intent that will start either the Election choice or the mainactivity
                // based on whether or not an election was already selected.
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
                String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "not found");
                Intent intent;
                if(json.equals("not found")) {
                    intent = new Intent(SplashActivity.this, ElectionChoiceActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                thisActivity.startActivity(intent);
                thisActivity.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}
