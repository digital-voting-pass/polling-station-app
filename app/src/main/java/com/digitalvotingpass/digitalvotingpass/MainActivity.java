package com.digitalvotingpass.digitalvotingpass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextView resultData;
    HashMap<String, String> documentData = new HashMap<>();
    Button manualInput;
    public static final int GET_MANUAL_DOC_INFO = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MainActivity thisActivity = this;

        manualInput = (Button) findViewById(R.id.manual_input_button);
        resultData = (TextView) findViewById(R.id.result_data);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ManualInputActivity.class);
                startActivityForResult(intent, GET_MANUAL_DOC_INFO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_MANUAL_DOC_INFO) {
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
