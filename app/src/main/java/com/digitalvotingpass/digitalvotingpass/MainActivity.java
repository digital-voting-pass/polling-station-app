package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    private static final int RC_OCR_CAPTURE = 9003;

    private Button startOCR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity mainActivity = this;
        setContentView(R.layout.activity_main);

        startOCR = (Button) findViewById(R.id.button_start_ocr);

        startOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true/*autoFocus.isChecked()*/);
                intent.putExtra(OcrCaptureActivity.UseFlash, false/*useFlash.isChecked()*/);

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });
    }

}
