package com.digitalvotingpass.digitalvotingpass;

import android.os.Parcel;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by rico on 12-6-17.
 */

public class TestDocumentData {

    private String dateOfBirth = "030393";
    private String expiryDate = "030323";
    private String docNum = "IWJA1234";

    @Test
    public void testParcel() {
        DocumentData data = new DocumentData();
        data.setDateOfBirth(dateOfBirth);
        data.setExpiryDate(expiryDate);
        data.setDocumentNumber(docNum);


        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        DocumentData createdFromParcel = (DocumentData) DocumentData.CREATOR.createFromParcel(parcel);

        assertEquals(data.getDateOfBirth(), createdFromParcel.getDateOfBirth());
        assertEquals(data.getExpiryDate(), createdFromParcel.getExpiryDate());
        assertEquals(data.getDocumentNumber(), createdFromParcel.getDocumentNumber());

    }
}
