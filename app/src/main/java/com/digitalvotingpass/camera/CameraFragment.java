package com.digitalvotingpass.camera;/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file was modified by the Digital Voting Pass group. (https://github.com/digital-voting-pass)
 */

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalvotingpass.digitalvotingpass.DocumentData;
import com.digitalvotingpass.digitalvotingpass.MainActivity;
import com.digitalvotingpass.digitalvotingpass.ManualInputActivity;
import com.digitalvotingpass.digitalvotingpass.R;
import com.digitalvotingpass.ocrscanner.Mrz;
import com.digitalvotingpass.ocrscanner.TesseractOCR;
import com.digitalvotingpass.utilities.ErrorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CameraFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback {
    // tag for the log and the error dialog
    private static final String TAG = "CameraFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int DELAY_BETWEEN_OCR_THREADS_MILLIS = 500;
    private List<TesseractOCR> tesseractThreads = new ArrayList<>();
    private boolean resultFound = false;
    private Runnable scanningTakingLongTimeout = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    manualInput.setVisibility(View.VISIBLE);
                    overlay.setMargins(0,0,0,infoText.getHeight() + manualInput.getHeight());
                }
            });
        }
    };

    private ImageView scanSegment;
    private Overlay overlay;
    private Button manualInput;
    private TextView infoText;
    private View controlPanel;

    // Conversion from screen rotation to JPEG orientation.
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_WRITE_PERMISSIONS = 3;

    boolean mIsStateAlreadySaved = false;
    boolean mPendingShowDialog = false;

    // listener for detecting orientation changes
    private OrientationEventListener orientationListener = null;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            orientationListener.enable();
            mCameraHandler.openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            orientationListener.disable();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    public Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * Handler for the connection with the camera
     */
    private CameraHandler mCameraHandler;


    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    /**
     * Handles the setup that can start when the fragment is created.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientationListener = new OrientationEventListener(this.getActivity()) {
            public void onOrientationChanged(int orientation) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        };
        int threadsToStart = Runtime.getRuntime().availableProcessors() / 2;
        createOCRThreads(threadsToStart);
        mCameraHandler = new CameraHandler(this);
    }

    /**
     * Create the threads where the OCR will run on.
     * @param amount
     */
    private void createOCRThreads(int amount) {
        for (int i = 0; i < amount; i++) {
            tesseractThreads.add(new TesseractOCR("Thread no " + i, this, getActivity().getAssets()));
        }
        Log.e(TAG, "Running threads: " + amount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    /**
     * Setup the layout and setup the actions associated with the button.
     */
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        scanSegment = (ImageView) view.findViewById(R.id.scan_segment);
        manualInput = (Button) view.findViewById(R.id.manual_input_button);
        overlay = (Overlay) view.findViewById(R.id.overlay);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ManualInputActivity.class);
                getActivity().startActivityForResult(intent, MainActivity.GET_DOC_INFO);
            }
        });
        infoText = (TextView) view.findViewById(R.id.info_text);
        Typeface typeFace= Typeface.createFromAsset(getActivity().getAssets(), "fonts/ro.ttf");
        infoText.setTypeface(typeFace);
        manualInput.setTypeface(typeFace);
        controlPanel = view.findViewById(R.id.control);
        final ViewTreeObserver observer= view.findViewById(R.id.control).getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set the margins when the view is available.
                overlay.setMargins(0, 0, 0, controlPanel.getHeight());
                view.findViewById(R.id.control).getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraHandler.startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            mCameraHandler.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        mIsStateAlreadySaved = false;
        if(mPendingShowDialog){
            mPendingShowDialog = false;
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                showInfoDialog(R.string.ocr_camera_permission_explanation);
            } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                showInfoDialog(R.string.storage_permission_explanation);
            }
        } else {
            startOCRThreads();
        }
    }

    /**
     * Displays an information dialog with the given string.
     * @param stringId
     */
    public void showInfoDialog(int stringId) {
        ErrorDialog.newInstance(getString(stringId))
                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }

    @Override
    public void onPause() {
        mCameraHandler.closeCamera();
        mCameraHandler.stopBackgroundThread();
        stopTesseractThreads();
        mIsStateAlreadySaved = true;
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void requestCameraPermission() {
        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showInfoDialog(R.string.ocr_camera_permission_explanation);
        } else {
            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void requestStoragePermissions() {
        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showInfoDialog(R.string.storage_permission_explanation);
        } else {
            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if(mIsStateAlreadySaved){
                    mPendingShowDialog = true;
                } else {
                    showInfoDialog(R.string.ocr_camera_permission_explanation);
                }
            }
        } else if (requestCode == REQUEST_WRITE_PERMISSIONS) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if(mIsStateAlreadySaved){
                    mPendingShowDialog = true;
                } else {
                    showInfoDialog(R.string.storage_permission_explanation);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Start the threads that will run the OCR scanner.
     */
    private void startOCRThreads() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Cant start OCR due to no camera permissions.");
            return;
        }
        int i = 0;
        for(TesseractOCR ocr : tesseractThreads) {
            ocr.initialize();
            ocr.startScanner(i);
            i += DELAY_BETWEEN_OCR_THREADS_MILLIS;
        }
    }



    private void stopTesseractThreads() {
        for (TesseractOCR ocr : tesseractThreads) {
            ocr.stopScanner();
        }
    }



    /**
     * Method for delivering correct MRZ when found. This method returns the MRZ as result data and
     * then exits the activity. This method is synchronized and checks for a boolean to make sure
     * it is only executed once in this fragments lifetime.
     * @param mrz Mrz
     */
    public synchronized void scanResultFound(final Mrz mrz) {
        if (!resultFound) {
            for (TesseractOCR thread : tesseractThreads) {
                thread.stopping = true;
            }
            Intent returnIntent = new Intent();
            DocumentData data = mrz.getPrettyData();
            returnIntent.putExtra(DocumentData.identifier, data);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            resultFound = true;
            getActivity().finish();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    public void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
        overlay.setRect(CameraFragmentUtil.getScanRect(scanSegment));
    }

    /**
     * Extract a bitmap from the textureview of this fragment.
     * @return
     */
    public Bitmap extractBitmap() {
        try {
            Bitmap bitmap = mTextureView.getBitmap();
            int rotate = Surface.ROTATION_0;
            switch (getActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    rotate = 0;
                    break;
                case Surface.ROTATION_90:
                    rotate = 270;
                    break;
                case Surface.ROTATION_180:
                    rotate = 180;
                    break;
                case Surface.ROTATION_270:
                    rotate = 90;
                    break;
            }
            if (rotate != Surface.ROTATION_0) {
                bitmap = CameraFragmentUtil.rotateBitmap(bitmap, rotate);
            }
            Bitmap croppedBitmap = CameraFragmentUtil.cropBitmap(bitmap, scanSegment);

            return CameraFragmentUtil.getResizedBitmap(croppedBitmap, croppedBitmap.getWidth(), croppedBitmap.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public AutoFitTextureView getTextureView() {
        return mTextureView;
    }

    public ImageView getScanSegment() {
        return scanSegment;
    }

    public boolean isStateAlreadySaved() {
        return mIsStateAlreadySaved;
    }

    public Runnable getScanningTakingLongTimeout() {
        return scanningTakingLongTimeout;
    }

    public void setPreviewSize(Size size) {
        mPreviewSize = size;
    }

    /**
     * Sets the aspect ratio of the textureview
     * @param width
     * @param height
     */
    public void setAspectRatio(int width, int height) {
        mTextureView.setAspectRatio(width, height);
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    public void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}