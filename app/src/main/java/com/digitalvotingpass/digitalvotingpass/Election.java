package com.digitalvotingpass.digitalvotingpass;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wkmeijer on 29-5-17.
 */

public class Election implements Parcelable{
    public String kind;
    public String place;

    /**
     * Creates a election object with two attributes.
     * @param kind - The kind of election. (e.g. municipal, parlement)
     * @param place - The location of the electino. (e.g. Amsterdam, Den Haag, Noord-Brabant)
     */
    public Election(String kind, String place) {
        this.kind = kind;
        this.place = place;
    }

    /**
     * Constructor for parcel
     * @param in - The parcel containing the data for the election object
     */
    public Election(Parcel in){
        String[] data = new String[2];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.kind = data[0];
        this.place = data[1];
    }

    /**
     * Getter function for kind attribute of Election object
     * @return kind - A string depicting the kind of election
     */
    public String getKind() {
        return kind;
    }

    /**
     * Getter function for place attribute of Election object
     * @return place - A string depicting the place of an election
     */
    public String getPlace() {
        return place;
    }

    /**
     * Must be overridden, can be ignored.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write the data of the election object to the parcel
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.kind,
                this.place});
    }

    /**
     * Used to regenerate the election object.
     */
    public static final Parcelable.Creator<Election> CREATOR = new Parcelable.Creator<Election>() {
        public Election createFromParcel(Parcel in) {
            return new Election(in);
        }

        public Election[] newArray(int size) {
            return new Election[size];
        }
    };
}
