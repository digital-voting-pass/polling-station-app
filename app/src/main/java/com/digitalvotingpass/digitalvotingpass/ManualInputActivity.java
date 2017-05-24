package com.digitalvotingpass.digitalvotingpass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;

public class ManualInputActivity extends AppCompatActivity {
    private EditText docNumber;
    private EditText dateBirth;
    private EditText expiryDate;

    // Define the length of document details here, because getting maxLength from EditText is complex
    private final int DOC_NUM_LENGTH = 9;
    private final int DATE_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        docNumber = (EditText) findViewById(R.id.doc_num);
        dateBirth = (EditText) findViewById(R.id.date_birth);
        expiryDate = (EditText) findViewById(R.id.expiry_date);

        // When docData was previously filled in, update text fields
        putData(getIntent().getExtras());

        Button submitBut = (Button) findViewById(R.id.submit_button);
        submitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verifyInput()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", getData());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    /**
     * Update textFields with docData if it was previously filled in, else leave as is.
     */
    public void putData(Bundle extras) {
        HashMap<String, String> docData = (HashMap<String, String>) extras.get("docData");
        if(docData != null) {
            docNumber.setText(docData.get(MainActivity.DOCUMENT_NUMBER));
            dateBirth.setText(docData.get(MainActivity.DATE_OF_BIRTH));
            expiryDate.setText(docData.get(MainActivity.EXPIRATION_DATE));
        }
    }

    /**
     * Create a hashmap of the input date in the same way as the OCR scanner does.
     * @return data - A String Hashmap of the required document data for BAC.
     */
    public HashMap<String, String> getData() {
        HashMap<String, String> data = new HashMap<>();

        data.put(MainActivity.DOCUMENT_NUMBER, docNumber.getText().toString().toUpperCase());
        data.put(MainActivity.DATE_OF_BIRTH, dateBirth.getText().toString());
        data.put(MainActivity.EXPIRATION_DATE, expiryDate.getText().toString());

        return data;
    }

    /**
     * Check if all the fields have been filled in and check for wrong length input.
     * @return valid - boolean which indicates whether the input is valid.
     */
    public boolean verifyInput() {
        boolean valid = true;
        int docNumLength = docNumber.getText().toString().length();
        if(docNumLength != DOC_NUM_LENGTH ) {
            valid = false;
            if(docNumLength == 0) {
                docNumber.setError(getResources().getString(R.string.errInputDocNum));
            }
            else {
                docNumber.setError(getResources().getString(R.string.errFormatDocNum));
            }
        }
        int dateBirthLength = dateBirth.getText().toString().length();
        if(dateBirthLength != DATE_LENGTH) {
            valid = false;
            if(dateBirthLength == 0) {
                dateBirth.setError(getResources().getString(R.string.errInputDateBirth));
            } else {
                dateBirth.setError(getResources().getString(R.string.errFormatDateBirth));
            }
        }
        int expiryDateLength = expiryDate.getText().toString().length();
        if(expiryDateLength != DATE_LENGTH) {
            valid = false;
            if(expiryDateLength == 0) {
                expiryDate.setError(getResources().getString(R.string.errInputExpiryDate));
            } else {
                expiryDate.setError(getResources().getString(R.string.errFormatExpiryDate));
            }
        }
        return valid;
    }

}
