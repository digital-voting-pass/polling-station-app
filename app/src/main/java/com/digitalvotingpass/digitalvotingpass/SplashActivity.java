package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by rico on 29-5-17.
 */

public class SplashActivity extends Activity{
    // Duration of splash screen
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /**
     * Create splash screen/
     * @param bundle
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash_screen);

        //Start MainActivity after SPLASH_DISPLAY_LENGTH
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}
