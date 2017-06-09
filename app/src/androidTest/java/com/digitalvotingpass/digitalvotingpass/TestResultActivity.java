package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.sf.scuba.data.Gender;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by rico on 1-6-17.
 */

@RunWith(AndroidJUnit4.class)
public class TestResultActivity {
    /**
     * Start up the main activity for each test.
     */
    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<ResultActivity>(
            ResultActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            //create voter object
            Voter voter = new Voter("Jannus Wallus", "de Vries", Gender.getInstance(Gender.MALE.toInt()));

            //put in intent and return
            Intent result = new Intent(targetContext, MainActivity.class);
            result.putExtra("voter", voter);
            return result;
        }

    };

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void destroy() {
        Intents.release();
    }

    /**
     * Test if the 'next voter' button appears after pressing case vote.
     */
    @Test
    public void testConfirmVote() {
        onView(withText(R.string.proceed_cast_vote)).perform(click());
        onView(withText(R.string.proceed_home)).check(matches(isEnabled()));
    }

}
