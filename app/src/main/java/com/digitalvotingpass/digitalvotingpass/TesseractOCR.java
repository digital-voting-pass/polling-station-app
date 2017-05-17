package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;

/**
 * Created by jonathan on 5/16/17.
 */

public class TesseractOCR {

    public static final String TAG = "TesseractOCR";
    private static final String trainedData = "mrz.traineddata";
    private TessBaseAPI baseApi;
    private boolean initialized = false;

    public void initialize(Activity activity) {
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        String path = Environment.getExternalStorageDirectory() + "/";
        File trainedDataFile = new File(Environment.getExternalStorageDirectory(), "/tessdata/" + trainedData);
        AssetManager assetManager = activity.getAssets();
        try {
            if (!trainedDataFile.exists()) {
                Log.i(TAG, "No existing trained data found, copying from assets..");
                Util.copyAssetsFile(assetManager.open(trainedData), trainedDataFile);
            } else {
                Log.i(TAG, "Existing trained data found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO show error to user
        }
        baseApi.init(path, trainedData.replace(".traineddata", "")); //extract language code from trained data file
        initialized = true;
    }

    protected Mrz ocr(Bitmap bitmap, Activity activity) {
        if (initialized) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Log.e(TAG, "before crop: x: " + bitmap.getWidth() + ", y: " + bitmap.getHeight());
    //        Log.e(TAG, "after crop x: " + croppedBitmap.getWidth() + ", y: " + croppedBitmap.getHeight());
            Log.v(TAG, "Orientation: " + activity.getWindowManager().getDefaultDisplay().getRotation());

            baseApi.setImage(bitmap);
            baseApi.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ<");
            String recognizedText = baseApi.getUTF8Text();
            Log.v(TAG, "OCR Result: " + recognizedText);
            return new Mrz(recognizedText);
        } else {
            return null;
        }
    }

    public void cleanup () {
        baseApi.end();
    }
}
