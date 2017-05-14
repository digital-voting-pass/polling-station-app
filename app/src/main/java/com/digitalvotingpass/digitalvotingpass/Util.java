package com.digitalvotingpass.digitalvotingpass;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jonathan on 5/14/17.
 */

public class Util {

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
                return;
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
}
