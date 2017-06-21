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
 */


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.digitalvotingpass.digitalvotingpass.MainActivity;
import com.digitalvotingpass.digitalvotingpass.R;

public class CameraActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }
    }

    /**
     * Pass data from ManualInputActivity to the MainActivity.
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data The data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.GET_DOC_INFO && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            //Clear this activity
            finish();
        }
    }
}
