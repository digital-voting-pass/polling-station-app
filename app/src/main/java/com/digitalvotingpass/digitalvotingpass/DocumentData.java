package com.digitalvotingpass.digitalvotingpass;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Represents document data.
 * Created by rico on 1-6-17.
 */

public class DocumentData implements Parcelable {
    public final static String identifier = "docData";
    private final static int DOCUMENT_NUMBER_SIZE = 9;

    private String documentNumber;
    private String expiryDate;
    private String dateOfBirth;

    public DocumentData() {
    }

    private DocumentData(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        setDocumentNumber(data[0]);
        setExpiryDate(data[1]);
        setDateOfBirth(data[2]);
    }


    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Check if the document data is valid.
     * @return True if valid, false otherwise.
     */
    public boolean isValid() {
        return getDocumentNumber() != null &&
                getDocumentNumber().length()  == DOCUMENT_NUMBER_SIZE &&
                getExpiryDate() != null && getDateOfBirth() != null &&
                getExpiryDate().length() == 6 && getDateOfBirth().length() == 6;
    }

    @Override
    public String toString() {
        return "Exp: " + getExpiryDate() + " Date: " + getDateOfBirth() + "Doc num" +
                getDocumentNumber() + ", valid: " + isValid();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {getDocumentNumber(),
                getExpiryDate(),
                getDateOfBirth()
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DocumentData createFromParcel(Parcel in) {
            return new DocumentData(in);
        }

        public DocumentData[] newArray(int size) {
            return new DocumentData[size];
        }
    };

}
