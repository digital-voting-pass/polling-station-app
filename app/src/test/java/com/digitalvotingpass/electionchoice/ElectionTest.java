package com.digitalvotingpass.electionchoice;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Sha256Hash;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ElectionTest {
    private Sha256Hash mockHash  = new Sha256Hash("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    @Test
    public void equalsActualEqual() throws Exception {
        Election e1 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        Election e2 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        assertTrue(e1.equals(e2));
    }

    @Test
    public void equalsNonEqualPlace() throws Exception {
        Election e1 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        Election e2 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        assertTrue(e1.equals(e2));
    }

    @Test
    public void equalsNonEqualKind() throws Exception {
        Election e1 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        Election e2 = new Election("Delft", "Provincial election", new Asset("G_Delft", mockHash));
        assertFalse(e1.equals(e2));
    }

    @Test
    public void equalsNonEqualAssetName() throws Exception {
        Election e1 = new Election("Delft", "Municipal election", new Asset("G_Delft", mockHash));
        Election e2 = new Election("Delft", "Municipal election", new Asset("T_Delft", mockHash));
        assertFalse(e1.equals(e2));
    }

}