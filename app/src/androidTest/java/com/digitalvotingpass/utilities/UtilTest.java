package com.digitalvotingpass.utilities;

import android.content.res.Resources;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilTest {

    @Test
    public void getStatusBarHeightTest() throws Exception {
        Resources r = mock(Resources.class);
        when(r.getIdentifier(Util.STATUS_BAR_HEIGHT, Util.DEF_TYPE, Util.DEF_PACKAGE)).thenReturn(10);

        assertEquals(Util.getStatusBarHeight(r), 10);
    }

}
