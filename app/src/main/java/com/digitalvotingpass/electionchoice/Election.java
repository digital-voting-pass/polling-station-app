package com.digitalvotingpass.electionchoice;

import org.bitcoinj.core.Asset;

public class Election {
    public String kind;
    public String place;
    public Asset asset;

    /**
     * Creates an election object with two attributes.
     * @param kind - The kind of election. (e.g. municipal, parlement)
     * @param place - The location of the election. (e.g. Amsterdam, Den Haag, Noord-Brabant)
     * @param asset - The asset that corresponds to this Election.
     */
    public Election(String kind, String place, Asset asset) {
        this.kind = kind;
        this.place = place;
        this.asset = asset;
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
     * Getter function for the asset attribute of Election object
     * @return asset - An Asset object, the token on the blockchain
     */
    public Asset getAsset() { return asset; }
}
