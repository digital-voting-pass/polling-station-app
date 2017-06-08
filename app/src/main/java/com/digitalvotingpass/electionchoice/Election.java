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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Election) {
            Election that = (Election) obj;
            if(!this.getPlace().equals(that.getPlace()) ||
                    !this.getKind().equals(that.getKind()) ||
                    !this.getAsset().getName().equals(that.getAsset().getName())) {
                return false;
            } else {
                return true;
            }
        } else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + place.hashCode();
        result = 31 * result + kind.hashCode();
        result = 31 * result + asset.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String res = "<Election: ";
        res += "Kind: \'" + this.kind + "\' ";
        res += "Place: \'" + this.place + "\' ";
        res += "Asset: [name: " + asset.getName() + "] ";
        res += ">";
        return res;
    }


}
