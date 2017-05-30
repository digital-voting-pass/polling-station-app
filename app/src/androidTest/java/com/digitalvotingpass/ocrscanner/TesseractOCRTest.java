package com.digitalvotingpass.ocrscanner;

import android.app.Instrumentation;
import android.content.Intent;
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
import com.digitalvotingpass.digitalvotingpass.MainActivity;
import com.digitalvotingpass.digitalvotingpass.R;
import com.digitalvotingpass.digitalvotingpass.SplashActivity;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

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
    private String image2 = "paspoort_mrz_2.jpg";
    private String image2Result = "P<NLDNICOLAI<<ATZO<<<<<<<<<<<<<<<<<<<<<<<<<<\nNX6P975712NLD6002224M1108143000000000<<<<<94";
    private String imageId1 = "id_mrz_1.jpg";
    private String imageId1Result = "I<NLDSPECI20142999999990<<<<<8\n6503101F2403096NLD<<<<<<<<<<<8\nDE<BRUIJN<<WILLEKE<LISELOTTE<<";

    @Mock
    Camera2BasicFragment fragmentMock;

    @Mock
    AssetManager assetmanagerMock;

    /**
     * Start an activity to be able to access the assets. Needed for loading the traineddata to the
     * emulator. SplashActivity is the simplest activity in the app.
     */
    @Rule
    public ActivityTestRule<SplashActivity> activityRule
            = new ActivityTestRule<>(
            SplashActivity.class);

    @Before
    public void init() throws Exception {
        AssetManager assetmanager;
        assetmanager = activityRule.getActivity().getAssets();
        tesseractOCR = new TesseractOCR("test", fragmentMock, assetmanager);
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
    public void testOCRInputPassport1() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(image1));
        assertNotNull(mrzImage);
        assertEquals(image1Result, tesseractOCR.ocr(mrzImage).getText());
    }

    @Test
    public void testOCRInputPassport2() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(image2));
        assertNotNull(mrzImage);
        assertEquals(image2Result, tesseractOCR.ocr(mrzImage).getText());
    }

    @Test
    public void testOCRInputId1() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(imageId1));
        assertNotNull(mrzImage);
        assertEquals(imageId1Result, tesseractOCR.ocr(mrzImage).getText());
    }

}