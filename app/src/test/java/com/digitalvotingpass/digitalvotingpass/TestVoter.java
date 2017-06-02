package com.digitalvotingpass.digitalvotingpass;

import net.sf.scuba.data.Gender;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for Voter.
 * Created by rico on 2-6-17.
 */

public class TestVoter {

    private String firstName = "Tom";
    private String lastName = "de Vries";

    private Gender[] genders  = new Gender[] { Gender.getInstance(Gender.MALE.toInt()),
            Gender.getInstance(Gender.FEMALE.toInt()),
            Gender.getInstance(Gender.UNSPECIFIED.toInt()),
            Gender.getInstance(Gender.UNKNOWN.toInt()) };
    private String[] genderStrings = new String[] { "Mr", "Ms", "unspecified", "unknown"} ;

    private Voter voter;

    @Test
    public void testGetters() {
        voter = new Voter(firstName, lastName, genders[0]);
        assertEquals(firstName, voter.getFirstName());
        assertEquals(lastName, voter.getLastName());
        assertEquals(genders[0].toInt(), voter.getGender().toInt());
    }

    @Test
    public void testSetters() {
        voter = new Voter();
        voter.setFirstName(firstName);
        voter.setLastName(lastName);
        voter.setGender(genders[0]);
        assertEquals(firstName, voter.getFirstName());
        assertEquals(lastName, voter.getLastName());
        assertEquals(genders[0].toInt(), voter.getGender().toInt());
    }

    @Test
    public void testGenderStrings() {
        voter = new Voter();
        voter.setGenderStrings(genderStrings[0], genderStrings[1], genderStrings[2], genderStrings[3]);
        for(int i=0; i< genders.length; i++) {
            voter.setGender(genders[i]);
            assertEquals(genderStrings[i], voter.genderToString());
        }
    }

    @Test
    public void testLastName1() {
        String lName = "VAN DE VORst";
        String expectedLName = "van de Vorst";
        voter = new Voter(firstName, lName, genders[0]);
        assertEquals(expectedLName, voter.getLastName());
    }

    @Test
    public void testLastName2() {
        String lName = "VorsT";
        String expectedLName = "Vorst";
        voter = new Voter(firstName, lName, genders[0]);
        assertEquals(expectedLName, voter.getLastName());
    }

}
