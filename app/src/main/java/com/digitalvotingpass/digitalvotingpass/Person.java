package com.digitalvotingpass.digitalvotingpass;

import android.os.Parcel;
import android.os.Parcelable;

import net.sf.scuba.data.Gender;

/**
 * Represents a person.
 * Created by rico on 1-6-17.
 */

public class Person implements Parcelable {


    private String firstName;
    private String lastName;
    private Gender gender;
    private String stringFemale, stringMale, stringUnspecified, stringUnknown;

    /**
     * Create a person without info.
     */
    public Person() {}

    /**
     * Create a person with info.
     * @param firstName First name.
     * @param lastName Last name.
     * @param gender Gender.
     */
    public Person(String firstName, String lastName, Gender gender) {
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
    }


    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name and capitalize it.
     * @param firstName
     */
    public void setFirstName(String firstName) {
        if (firstName != null && firstName.length() >= 1) {
            firstName = firstName.toLowerCase();
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
        }
        this.firstName = firstName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender =gender;
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name and capitalize the last word.
     * @param lastName The last name.
     */
    public void setLastName(String lastName) {
        if(lastName != null && lastName.length() >= 1) {
            String[] names = lastName.toLowerCase().split(" ");

            int last = names.length -1;
            names[last]= names[last].substring(0,1).toUpperCase() +
                    names[last].substring(1);
            lastName = "";
            for(int i=0; i < names.length; i++) {
                lastName += names[i] + " ";
            }
            lastName = lastName.trim();
        }
        this.lastName = lastName;
    }

    /**
     * Set the gender strings, this is necessary because we can't get
     * the strings in the person class and passing a {@link Context} object might
     * cause memory leaks.
     * @param male Male string.
     * @param female Female string.
     * @param unspecified Unspecified string.
     * @param unknown Unknown string.
     */
    public void setGenderStrings(String male, String female, String unspecified, String unknown) {
        stringMale = male;
        stringFemale = female;
        stringUnspecified = unspecified;
        stringUnknown = unknown;
    }


    /**
     * Return the gender in string format. Note that first setGenderString should be called, see
     * the documentation for reasons why.
     * @return The gender.
     */
    public String genderToString() {
        switch (getGender()) {
            case FEMALE:
                return stringFemale;
            case MALE:
                return stringMale;
            case UNKNOWN:
                return stringUnknown;
            case UNSPECIFIED:
                return stringUnspecified;
        }
        return null;
    }


    /**
     * Create a Person object via a parcel
     * @param in The parcel.
     */
    protected Person(Parcel in) {
        String data[] = new String[2];
        in.readStringArray(data);
        setFirstName(data[0]);
        setLastName(data[1]);
        setGender(Gender.getInstance(in.readInt()));
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] { getFirstName(), getLastName() });
        parcel.writeInt(getGender().toInt());
    }
}
