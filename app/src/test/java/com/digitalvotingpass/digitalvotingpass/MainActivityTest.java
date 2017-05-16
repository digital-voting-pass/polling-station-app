package com.digitalvotingpass.digitalvotingpass;

import android.view.View;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by wkmeijer on 16-5-17.
 */
public class MainActivityTest {
    private MainActivity activity;

    @Before
    public void setup() {
        activity = new MainActivity();
    }

    // move to androidtest
    @Test
    public void testStartReading() throws Exception {
        activity.startReading(null);
        assertTrue(intended(hasComponent(PassportConActivity.class.getName())));

    }

}