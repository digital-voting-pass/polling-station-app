package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by rico on 30-5-17.
 */

@RunWith(AndroidJUnit4.class)
public class TestManualInputActivity {
    /**
     * Start up the main activity for each test.
     */
    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<ManualInputActivity>(
            ManualInputActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, MainActivity.class);
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

    @Test
    public void testInvalidData() {
        closeSoftKeyboard();
        onView(withId(R.id.submit_button)).perform(click());
        onView(ViewMatchers.withId(R.id.doc_num))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasErrorText(
                                activityRule.getActivity().getString(R.string.errInputDocNum))));
    }

}
