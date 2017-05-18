package com.digitalvotingpass.digitalvotingpass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextView resultData;
    HashMap<String, String> documentData = new HashMap<>();
    Button manualInput;
    Button startOCR;

    public static final int GET_DOC_INFO = 1;
    public static final int GET_MANUAL_DOC_INFO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity thisActivity = this;
        setContentView(R.layout.activity_main);

        manualInput = (Button) findViewById(R.id.manual_input_button);
        resultData = (TextView) findViewById(R.id.result_data);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ManualInputActivity.class);
                startActivityForResult(intent, GET_MANUAL_DOC_INFO);
            }
        });

        startOCR = (Button) findViewById(R.id.start_ocr);
        resultData = (TextView) findViewById(R.id.result_data);
        startOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, CameraActivity.class);
                startActivityForResult(intent, GET_DOC_INFO);
            }
        });
    }

    /** Called when the user taps the "Start Reading ID" button */
    public void startReading(View view) {
        Intent intent = new Intent(this, PassportConActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Both switches do the same thing, this might change in the future
        switch(requestCode) {
            case (GET_MANUAL_DOC_INFO): {
                if (resultCode == RESULT_OK) {
                    HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("result");
                    resultData.setText("");
                    for (String key : map.keySet()) {
                        resultData.append(key + ": " + map.get(key) + "\n");
                    }
                }
                break;
            }
            case (GET_DOC_INFO): {
                if (resultCode == RESULT_OK) {
                    HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("result");
                    resultData.setText("");
                    for (String key : map.keySet()) {
                        resultData.append(key + ": " + map.get(key) + "\n");
                    }
                }
                break;
            }
        }
    }

}
