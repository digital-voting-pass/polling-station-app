package com.digitalvotingpass.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wkmeijer on 12-6-17.
 */

public class CameraFragmentUtil {
    public final static String TAG = "CameraFragmentUtil";

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Rotate the bitmap a given amount of degrees. This is used to get the correct bitmap when
     * the device is in landscape mode.
     * @param bitmap
     * @param degrees
     * @return a rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        // Setting pre rotate
        Matrix mtx = new Matrix();
        mtx.preRotate(degrees);
        // Rotating Bitmap
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        return Bitmap.createScaledBitmap(rotated, bitmap.getWidth(), bitmap.getHeight(), true);
    }

    /**
     * Resize a bitmap and return the resized one.
     * @param bm - Initial bitmap
     * @param newWidth
     * @param newHeight
     * @return a resized bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /**
     * Crop the bitmap to only the part of the scansegment. The bitmap should only contain the part
     * that displays the MRZ of a travel document.
     * @param bitmap - The bitmap created from the camerapreview
     * @param scanSegment - Scansegment, the segment that should be scanned with OCR
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, ImageView scanSegment) {
        int startX = (int) scanSegment.getX();
        int startY = (int) scanSegment.getY();
        int width = scanSegment.getWidth();
        int length = scanSegment.getHeight();
        return Bitmap.createBitmap(bitmap, startX, startY, width, length);
    }

    /**
     * Get the scan rectangle.
     * @return The rectangle.
     */
    public static Rect getScanRect(ImageView scanSegment) {
        int startX = (int) scanSegment.getX();
        int startY = (int) scanSegment.getY();
        int width = scanSegment.getWidth();
        int length = scanSegment.getHeight();
        return new Rect(startX, startY, startX + width, startY + length);
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Find out if we need to swap dimensions to get the preview size relative to sensor coordinate.
     * @param activity - The associated activity from which the camera is loaded.
     * @param characteristics - CameraCharacteristics corresponding to the current started cameradevice
     * @return swappedDimensions - A boolean value indicating if the the dimensions need to be swapped.
     */
    public static boolean needSwappedDimensions(Activity activity, CameraCharacteristics characteristics) {
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        //noinspection ConstantConditions
        int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_90:
                if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_180:
            case Surface.ROTATION_270:
                if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
            default:
                Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }
        return swappedDimensions;
    }
}
