package com.digitalvotingpass.ocrscanner;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.text.TextUtils;
import android.util.Log;

import com.digitalvotingpass.camera.Camera2BasicFragment;
import com.digitalvotingpass.digitalvotingpass.ManualInputActivity;
import com.digitalvotingpass.digitalvotingpass.SplashActivity;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class TesseractOCRTest {
    private TesseractOCR tesseractOCR;
    private static final String TAG = "TestTesseractOCR: ";
    // minimum accuracy for detecting the right strings with ocr
    private final double MINIMUM_ACCURACY = 0.8;

    private String image1 = "testimages/paspoort_mrz_1.jpg";
    private String image1Result = "P<NLDDE<BRUIJN<<WILLEKE<LISELOTTE<<<<<<<<<<<\nSPECI20142NLD6503101F2401151999999990<<<<<82";
    private String image2 = "testimages/paspoort_mrz_2.jpg";
    private String image2Result = "P<NLDNICOLAI<<ATZO<<<<<<<<<<<<<<<<<<<<<<<<<<\nNX6P975712NLD6002224M1108143000000000<<<<<94";
    private String imageId1 = "testimages/id_mrz_1.jpg";
    // Third line of id is not needed
    private String imageId1Result = "I<NLDSPECI20142999999990<<<<<8\n6503101F2403096NLD<<<<<<<<<<<8";

    @Mock
    Camera2BasicFragment fragmentMock;

    @Mock
    AssetManager assetmanagerMock;

    /**
     * Start an activity to be able to access the assets. Needed for loading the traineddata to the
     * emulator. ElectionChoice is one of the simplest activity in the app.
     */
    @Rule
    public ActivityTestRule<ElectionChoiceActivity> activityRule
            = new ActivityTestRule<>(
            ElectionChoiceActivity.class);

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

    // 17-06-01 similarity eng.traineddata 0.8876404494382022
    // 17-06-01 similarity nld.traineddata 0.9101123595505618
    // 17-06-01 similarity fra.traineddata 0.8314606741573034
    // 17-06-01 similarity rus.traineddata 0.5393258426966292
    // 17-06-01 similarity jpn.traineddata 0.6404494382022472
    // 17-06-01 similarity ita.traineddata 0.8426966292134831
    // 17-06-01 similarity pol.traineddata 0.8539325842696629

    // 17-06-01 similarity own traineddata 0.8089887640449438
    //17-06-01 similarity ocrb.traineddata 0.9550561797752809

    @Test
    public void testOCRInputPassport1() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(image1));
        assertNotNull(mrzImage);
        double similarity = stringSimilarity(image1Result, tesseractOCR.ocr(mrzImage).getText());
        Log.e(TAG, "Similarity passport1: " + similarity);
        assertTrue(similarity > MINIMUM_ACCURACY);
    }

    // 17-06-01 similarity eng.traineddata 0.9213483146067416
    // 17-06-01 similarity nld.traineddata 0.9325842696629213
    // 17-06-01 similarity fra.traineddata 0.9662921348314607
    // 17-06-01 similarity rus.traineddata 0.6966292134831461
    // 17-06-01 similarity jpn.traineddata 0.6629213483146067
    // 17-06-01 similarity ita.traineddata 0.9550561797752809
    // 17-06-01 similarity pol.traineddata 0.9213483146067416

    // 17-06-01 similarity own traineddata 0.8651685393258427
    //17-06-01 similarity ocrb.traineddata 0.9775280898876404



    @Test
    public void testOCRInputPassport2() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(image2));
        assertNotNull(mrzImage);
        double similarity = stringSimilarity(image2Result, tesseractOCR.ocr(mrzImage).getText());
        Log.e(TAG, "Similarity passport2: " + similarity);
        assertTrue(similarity > MINIMUM_ACCURACY);
    }

    // 17-06-01 similarity eng.traineddata 0.9672131147540983
    // 17-06-01 similarity nld.traineddata 0.9672131147540983
    // 17-06-01 similarity fra.traineddata 0.9672131147540983
    // 17-06-01 similarity rus.traineddata 0.7704918032786885
    // 17-06-01 similarity jpn.traineddata 0.9180327868852459
    // 17-06-01 similarity ita.traineddata 0.9672131147540983
    // 17-06-01 similarity pol.traineddata 1.0

    // 17-06-01 similarity own traineddata 0.9180327868852459
    //17-06-01 similarity ocrb.traineddata 1.0

    @Test
    public void testOCRInputId1() throws Exception {
        Bitmap mrzImage = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(imageId1));
        assertNotNull(mrzImage);
        double similarity = stringSimilarity(imageId1Result, tesseractOCR.ocr(mrzImage).getText());
        Log.e(TAG, "Similarity id1: " + similarity);
        assertTrue(similarity > MINIMUM_ACCURACY);
    }

    /**
     * Calculates the similarity of two strings, used to calculate the accuracy of the read MRZ
     * by tesseract.
     * @param string1
     * @param string2
     * @return
     */
    public static double stringSimilarity(String string1, String string2) {
        String longer = string1, shorter = string2;
        if (string1.length() < string2.length()) {
            longer = string2; shorter = string1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     * The distance depicts the minimum amount of character edits for the strings to be the same.
     * @param string1
     * @param string2
     * @return
     */
    public static int editDistance(String string1, String string2) {
        string1 = string1.toLowerCase();
        string2 = string2.toLowerCase();

        int[] costs = new int[string2.length() + 1];
        for (int i = 0; i <= string1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= string2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (string1.charAt(i - 1) != string2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[string2.length()] = lastValue;
        }
        return costs[string2.length()];
    }



}
