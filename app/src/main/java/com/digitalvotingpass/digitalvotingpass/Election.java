package com.digitalvotingpass.digitalvotingpass;

/**
 * Created by wkmeijer on 29-5-17.
 */

public class Election {
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
}
