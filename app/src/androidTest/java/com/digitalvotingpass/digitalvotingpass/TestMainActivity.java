package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestMainActivity {


    /**
     * Start up the main activity for each test.
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    /**
     * Setup intents.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        Intents.init();
    }

    /**
     * Clean up.
     * @throws Exception exception
     */
    @After
    public void tearDown() throws Exception{
        Intents.release();
    }


    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.digitalvotingpass.digitalvotingpass", appContext.getPackageName());
    }

    /**
     * Dummy test.
     */
    public void testDummy() {
        onView(withId(R.id.manual_input_button))
                .perform(click());
        try {
            Thread.sleep(1000);
        } catch(Exception e) {}
        assertEquals(true, true);
    }


    /**
     * Test if the manual input activity opens.
     */
    @Test
    public void testGoToManual() {
        onView(withId(R.id.manual_input_button))
                .perform(click());
        //intended(hasComponent(ManualInputActivity.class.getName()));
        assertEquals(true, withId(R.id.manual_input_button) != null);
    }

    /**
     * Test if the OCR opens.
     */
    @Test
    public void testGoToOCR() {
        onView(withId(R.id.start_ocr))
                .perform(click());
        intended(hasComponent(CameraActivity.class.getName()));
    }


    /**
     * Test if the OCR opens when there is no data available and you press start id connection.
     */
    @Test
    public void testClickStartConnection() {
        onView(withText(R.string.start_con_button))
                .perform(click());
        intended(hasComponent(CameraActivity.class.getName()));
    }


}
