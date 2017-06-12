package com.digitalvotingpass.ocrscanner;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.digitalvotingpass.camera.Camera2BasicFragment;
import com.digitalvotingpass.utilities.Util;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class TesseractOCR {
    private static final String TAG = "TesseractOCR";

    private static final long INTER_SCAN_DELAY_MILLIS = 500;
    private static final long OCR_SCAN_TIMEOUT_MILLIS = 5000;

    private static final String trainedData = "ocrb.traineddata";

    private static final String FOLDER_TESSERACT_DATA = "tessdata";
    private static final String TRAINED_DATA_EXTENSION = ".traineddata";


    private final String name;

    private TessBaseAPI baseApi;
    private HandlerThread myThread;
    private Handler myHandler;
    private Handler cleanHandler;
    private Handler timeoutHandler;

    private AssetManager assetManager;
    private Camera2BasicFragment fragment;
    public boolean stopping = false;
    public boolean isInitialized = false;

    // Filled with OCR run times for analysis
    private ArrayList<Long> times = new ArrayList<>();

    /**
     * Lock to ensure only one thread can start copying to device storage.
     */
    private static Semaphore mDeviceStorageAccessLock = new Semaphore(1);

    /**
     * CURRENTLY NOT FUNCTIONAL
     * Timeout Thread, should end OCR detection when timeout occurs
     */
    private Runnable timeout = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "TIMEOUT");
//            baseApi.stop(); // Does not stop baseApi.getUTF8Text()
        }
    };


    private Runnable scan = new Runnable() {
        @Override
        public void run() {
            while (!stopping) {
                Log.v(TAG, "Start Scan");
                timeoutHandler.postDelayed(timeout, OCR_SCAN_TIMEOUT_MILLIS);
                long time = System.currentTimeMillis();
                Bitmap b = fragment.extractBitmap();
                Mrz mrz = ocr(b);
                long timetook = System.currentTimeMillis() - time;
                Log.i(TAG, "took " + timetook / 1000f + " sec");
                times.add(timetook);
                if (mrz != null && mrz.valid()) {
                    fragment.scanResultFound(mrz);
                }
                timeoutHandler.removeCallbacks(timeout);
                try {
                    Thread.sleep(INTER_SCAN_DELAY_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "Stopping scan");
        }
    };

    public TesseractOCR(String name, Camera2BasicFragment fragment, final AssetManager assetManager) {
        this.assetManager = assetManager;
        this.fragment = fragment;
        this.name = name;
    }

    /**
     * Starts OCR scan routine with delay in msec
     * @param delay int how msec before start
     */
    public void startScanner(int delay) {
        myHandler.postDelayed(scan, delay);
    }

    /**
     * Starts (enqueues) a stop routine in a new thread, then returns immediately.
     */
    public void stopScanner() {
        cleanHandler = new Handler();
        cleanHandler.post(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        });
    }

    /**
     * Starts a new thread to do OCR and enqueues an initialization task;
     */
    public void initialize() {
        myThread = new HandlerThread(name);
        myThread.start();
        timeoutHandler = new Handler();
        myHandler = new Handler(myThread.getLooper());
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                init();
                Log.e(TAG, "INIT DONE");
            }
        });
        isInitialized = true;
    }

    /**
     * Initializes Tesseract library using traineddata file.
     * Should not be called directly, is public for testing.
     */
    public void init() {
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        String path = Environment.getExternalStorageDirectory() + "/" + Util.FOLDER_DIGITAL_VOTING_PASS + "/";
        File trainedDataFile = new File(path, TesseractOCR.FOLDER_TESSERACT_DATA + "/" + trainedData);
        try {
            mDeviceStorageAccessLock.acquire();
            if (!trainedDataFile.exists()) {
                Log.i(TAG, "No existing trained data found, copying from assets..");
                Util.copyAssetsFile(assetManager.open(trainedData), trainedDataFile);
            } else {
                Log.i(TAG, "Existing trained data found");
            }
            mDeviceStorageAccessLock.release();
            baseApi.init(path, trainedData.replace(TesseractOCR.TRAINED_DATA_EXTENSION, "")); //extract language code from trained data file
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //TODO show error to user, coping failed
        }
    }

    /**
     * Performs OCR scan to bitmap provided, if tesseract is initialized and not currently stopping.
     * Should not be called directly, is public for testing.
     * @param bitmap Bitmap image to be scanned
     * @return Mrz Object containing result data
     */
    public Mrz ocr(Bitmap bitmap) {
        if (bitmap == null) return null;
        if (isInitialized && !stopping) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Log.v(TAG, "Image dims x: " + bitmap.getWidth() + ", y: " + bitmap.getHeight());
            baseApi.setImage(bitmap);
            baseApi.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ<");
            String recognizedText = baseApi.getUTF8Text();
            Log.v(TAG, "OCR Result: " + recognizedText);
            return new Mrz(recognizedText);
        } else {
            Log.e(TAG, "Trying ocr() while not initalized or stopping!");
            return null;
        }
    }

    /**
     * Cleans memory used by Tesseract library and closes OCR thread.
     * After this has been called initialize() needs to be called to restart the thread and init Tesseract
     */
    public void cleanup () {
        if (isInitialized) {
            giveStats();
            stopping = true;
            myThread.quitSafely();
            myHandler.removeCallbacks(scan);
            timeoutHandler.removeCallbacks(timeout);
            try {
                myThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myThread = null;
            myHandler = null;
            baseApi.end();
            isInitialized = false;
            stopping = false;
        }
    }

    /**
     * Prints some statistics about the run time of the OCR scanning
     */
    private void giveStats() {
        long max = 0;
        long curravg = 0;
        for (int i=0; i < times.size(); i++) {
            if (times.get(i) > max) max = times.get(i);
            curravg += times.get(i);
        }
        // prevent divide by zero
        if(times.size()>0) {
            Log.e(TAG, "Max runtime was " + max / 1000f + " sec and avg was " + curravg / times.size() / 1000f + " tot tries: " + times.size());
        }
    }
}
