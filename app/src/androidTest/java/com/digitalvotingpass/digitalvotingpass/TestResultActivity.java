package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.digitalvotingpass.blockchain.BlockChain;

import net.sf.scuba.data.Gender;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PublicKey;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by rico on 1-6-17.
 */

@RunWith(AndroidJUnit4.class)
public class TestResultActivity {
    private ResultActivity resultActivity;
    private Voter voter = new Voter("Jannus Wallus", "de Vries", Gender.getInstance(Gender.MALE.toInt()));;

    PublicKey pubKey = null;

    /**
     * Start up the splashactivity so the blockchain is instantiated.
     */
    @Rule
    public ActivityTestRule splashActivityRule = new ActivityTestRule<>(
            SplashActivity.class, true, false);

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<ResultActivity>(
            ResultActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();

            //put in intent and return
            Intent result = new Intent(targetContext, MainActivity.class);
            result.putExtra("pubKey", pubKey);
            result.putExtra("voter", voter);
            return result;
        }

    };

    @Before
    public void setUp() {
        Intents.init();

        // Wait till the splashactivity is closed, indicating blockchain was instantiated has started
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        splashActivityRule.launchActivity(new Intent(targetContext, SplashActivity.class));
        while (!splashActivityRule.getActivity().isFinishing()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Context targetContext2 = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent intent = new Intent(targetContext2, ResultActivity.class);
        intent.putExtra("pubKey", pubKey);
        intent.putExtra("voter", voter);
        resultActivity = (ResultActivity) activityRule.launchActivity(intent);
    }

    @After
    public void destroy() {
        Intents.release();
        BlockChain.getInstance().disconnect();
        resultActivity.finish();
    }


    /**
     * Test if the 'next voter' button appears when authorization fails (cause no public key is given)
     */
    @Test
    public void testNextVoter() {
        onView(withText(R.string.proceed_home)).check(matches(isEnabled()));
    }

}
