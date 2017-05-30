package com.digitalvotingpass.ocrscanner;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityTestCase;
import android.test.InstrumentationTestCase;

import com.digitalvotingpass.camera.Camera2BasicFragment;
import com.digitalvotingpass.camera.CameraActivity;
import com.digitalvotingpass.digitalvotingpass.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class TesseractOCRTest {//extends InstrumentationTestCase {
    private TesseractOCR tesseractOCR;
    private String image1 = "paspoort_mrz_1.jpg";
    private String image1Result = "P<NLDDE<BRUIJN<<WILLEKE<LISELOTTE<<<<<<<<<<<\nSPECI20142NLD6503101F2401151999999990<<<<<82";

    @Mock
    Camera2BasicFragment fragmentMock;

    @Mock
    AssetManager assetmanagerMock;

    @Rule
    public ActivityTestRule<CameraActivity> activityRule

            = new ActivityTestRule<>(
            CameraActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent

    @Before
    public void init() throws Exception {
        tesseractOCR = new TesseractOCR("test", fragmentMock, assetmanagerMock);
        tesseractOCR.init();
        tesseractOCR.isInitialized = true;
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(tesseractOCR);
    }

    @Test
    public void testOcrNullInput() throws Exception {
        assertNull(tesseractOCR.ocr(null));
    }

    @Test
    public void testOCRInputSimple() throws Exception {
        assertNotNull(tesseractOCR.ocr(Bitmap.createBitmap(10,10,Bitmap.Config.ARGB_8888)));
    }

    @Test
    public void testOCRInputComplex() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(image1));
        assertNotNull(mrzImage);
        assertEquals(image1Result, tesseractOCR.ocr(mrzImage).getText());
    }

}