package com.digitalvotingpass.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.digitalvotingpass.digitalvotingpass.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by wkmeijer on 13-6-17.
 */

public class CameraHandler {
    // The fragment this camera device is associated with
    private CameraFragment fragment;

    private String TAG = "CameraHandler";
    private final float SECONDS_TILL_SCAN_TIMEOUT = 10;

    /**
     * Max preview width and height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * A {@link Handler} for running tasks in the background.
     * Runs camera preview updater.
     */
    private Handler mBackgroundHandler;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * References to and attributes of the CameraDevice
     */
    private CameraDevice mCameraDevice;
    private String mCameraId;

    /**
     * Objects needed for the cameraPreview
     */
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    private boolean flashEnabled = false;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            fragment.mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            fragment.mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            fragment.mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = fragment.getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };



    /**
     * Constructor, needs a link to a fragment to be able to set parameters in accordence to the
     * size of the devices screen.
     * @param fragment - A fragment which handles the display of the camera preview.
     */
    public CameraHandler(CameraFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mBackgroundHandler.postDelayed(fragment.getScanningTakingLongTimeout(), (long) (SECONDS_TILL_SCAN_TIMEOUT * 1000));
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    public void stopBackgroundThread() {
        mBackgroundHandler.removeCallbacks(fragment.getScanningTakingLongTimeout());
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) fragment.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                Size mPreviewSize = setFragmentPreviewSize(width, height,
                        CameraFragmentUtil.needSwappedDimensions(fragment.getActivity(), characteristics), map);

                int orientation = fragment.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    fragment.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    fragment.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // An NPE is thrown when the Camera2API is used but not supported on the device this code runs.
            fragment.showInfoDialog(R.string.ocr_camera_error);
        }
    }

    /**
     * Sets the preview size of the fragment
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     * @param swappedDimensions - boolean indicating if dimensions need to be swapped
     * @param map - Configurationmap of the camera
     * @return mPreviewSize - the previewsize that is set in the fragment
     */
    private Size setFragmentPreviewSize(int width, int height, boolean swappedDimensions, StreamConfigurationMap map) {
        // For still image captures, we use the largest available size.
        Size largest = Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                new CameraFragmentUtil.CompareSizesByArea());

        Point displaySize = new Point();
        fragment.getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (swappedDimensions) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }
        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }
        // Attempting to use too large a preview size could  exceed the camera bus' bandwidth
        // limitation, resulting in gorgeous previews but the storage of garbage capture data.
        Size mPreviewSize = CameraFragmentUtil.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest);
        fragment.setPreviewSize(mPreviewSize);
        return mPreviewSize;
    }

    /**
     * Opens the camera specified by {@link #mCameraId}.
     */
    public void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            fragment.requestCameraPermission();
            return;
        }
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Cant start Camera due to no data permissions.");
            return;
        }
        setUpCameraOutputs(width, height);
        fragment.configureTransform(width, height);
        Activity activity = fragment.getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!fragment.mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    public void closeCamera() {
        try {
            fragment.mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            fragment.mCameraOpenCloseLock.release();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = fragment.getTextureView().getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(fragment.getPreviewSize().getWidth(), fragment.getPreviewSize().getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface), createCameraCaptureSessionStateCallBack()
                    , null );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallBack() {
        return new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                // The camera is already closed
                if (null == mCameraDevice) {
                    return;
                }
                // When the session is ready, we start displaying the preview.
                mCaptureSession = cameraCaptureSession;
                try {
                    MeteringRectangle meteringRectangle=new MeteringRectangle(CameraFragmentUtil.getScanRect(fragment.getScanSegment()),
                            MeteringRectangle.METERING_WEIGHT_MAX);
                    MeteringRectangle[] meteringRectangleArr={meteringRectangle};

                    // Auto focus should be continuous for camera preview.
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS,
                            meteringRectangleArr);

                    // Finally, we start displaying the camera preview.
                    mPreviewRequest = mPreviewRequestBuilder.build();
                    if (!fragment.isStateAlreadySaved())
                        mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                null, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(
                    @NonNull CameraCaptureSession cameraCaptureSession) {
                fragment.showToast("Failed");
            }
        };
    }

    /**
     * Turn the torch of the device on or off, when it has one.
     */
    public void toggleTorch() {
        try {
            if (!flashEnabled && mFlashSupported) {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                Log.e(TAG, "flash enabled");
            } else {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                Log.e(TAG, "flash disabled");
            }
            flashEnabled = !flashEnabled;
        } catch (CameraAccessException e ){
            e.printStackTrace();
        }
    }
}
