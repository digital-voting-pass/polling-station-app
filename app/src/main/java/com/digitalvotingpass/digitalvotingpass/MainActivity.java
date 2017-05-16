package com.digitalvotingpass.digitalvotingpass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int GET_DOC_INFO = 1;
    Button startOCR;
    private TextView resultData;
    HashMap<String, String> documentData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity thisActivity = this;
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DOC_INFO) {
            if (resultCode == RESULT_OK) {
                HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("result");
                resultData.setText("");
                for (String key : map.keySet()) {
                    resultData.append(key + ": " + map.get(key) + "\n");
                }
            }
        }
    }
}
