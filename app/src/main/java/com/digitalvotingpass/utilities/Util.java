package com.digitalvotingpass.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.digitalvotingpass.digitalvotingpass.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Util {


    /**
     * Returns the height of the status bar in pixels
     * @param resources Resources object required to get the height attribute.
     * @return int
     */
    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Sets up a top-padding for the given app bar equal to the height of the status bar.
     * This increases the length of the app bar so it fits nicely below the status bar.
     * This method also sets the status bar transparency.
     * @param appBar Toolbar to set padding to
     * @param activity Activity - current activity
     */
    public static void setupAppBar(Toolbar appBar, Activity activity) {
        appBar.setPadding(0, getStatusBarHeight(activity.getResources()), 0, 0);

        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(Color.parseColor("#10000000"));
    }

    /**
     * Copies an InputStream into a File.
     * This is used to copy an InputStream from the assets folder to a file in the FileSystem.
     * Creates nay non-existant parent folders to f.
     * @param is InputStream to be copied.
     * @param f File to copy data to.
     */
    public static void copyAssetsFile(InputStream is, File f) throws IOException {
        OutputStream os = null;
        if (!f.exists()) {
            if (!f.getParentFile().mkdirs()) { //getParent because otherwise it creates a folder with that filename, we just need the dirs
                Log.e("Util", "Cannot create path!");
            }
        }
        os = new FileOutputStream(f, true);

        final int buffer_size = 1024 * 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;)
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
            is.close();
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method for converting a hexString to a byte array.
     * This method is used for signing transaction hashes (which are in hex).
     */
    public static byte[] hexStringToByteArray(String hStr) {
        if(hStr != null) {
            int len = hStr.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hStr.charAt(i), 16) << 4)
                        + Character.digit(hStr.charAt(i + 1), 16));
            }
            return data;
        }
        return new byte[0];
    }

    /**
     * Method for converting a byte array to a hexString.
     * This method is used for converting a signed 8-byte array back to a hashString in order to
     * display it readable.
     */
    public static String byteArrayToHexString(byte[] bArray) {
        if (bArray != null) {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bArray.length * 2];
            for (int j = 0; j < bArray.length; j++) {
                int v = bArray[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        return "";
    }

    public static Map<String, String> getKeyValueFromStringArray(Context ctx) {
        String[] array = ctx.getResources().getStringArray(R.array.address_array);
        Map<String, String> result = new HashMap<>();
        for (String str : array) {
            String[] splittedItem = str.split("\\|");
            result.put(splittedItem[0], splittedItem[1]);
        }
        return result;
    }
}
