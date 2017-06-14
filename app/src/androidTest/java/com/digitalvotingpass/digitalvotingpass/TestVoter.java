package com.digitalvotingpass.digitalvotingpass;

import android.os.Parcel;

import net.sf.scuba.data.Gender;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by rico on 13-6-17.
 */

public class TestVoter {

    private String firstName;
    private String lastName;

    @Before
    public void setUp() {
        firstName = "Ocir";
        lastName = "Gnibbut";
    }


    @Test
    public void testVoterParcelMale() {
        Voter voter = new Voter(firstName, lastName, Gender.MALE);


        Parcel parcel = Parcel.obtain();
        voter.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        Voter createdFromParcel = Voter.CREATOR.createFromParcel(parcel);

        assertEquals(voter.getFirstName(), createdFromParcel.getFirstName());
        assertEquals(voter.getLastName(), createdFromParcel.getLastName());
        assertEquals(voter.getGender(), createdFromParcel.getGender());
    }

    @Test
    public void testVoterParcelFemale() {
        Voter voter = new Voter(firstName, lastName, Gender.FEMALE);


        Parcel parcel = Parcel.obtain();
        voter.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        Voter createdFromParcel = Voter.CREATOR.createFromParcel(parcel);

        assertEquals(voter.getFirstName(), createdFromParcel.getFirstName());
        assertEquals(voter.getLastName(), createdFromParcel.getLastName());
        assertEquals(voter.getGender(), createdFromParcel.getGender());
    }
}
