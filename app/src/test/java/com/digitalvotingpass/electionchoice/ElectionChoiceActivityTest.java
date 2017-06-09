package com.digitalvotingpass.electionchoice;

import android.content.Context;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Sha256Hash;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by wkmeijer on 7-6-17.
 */
public class ElectionChoiceActivityTest {
    private ElectionChoiceActivity activity;
    private Sha256Hash mockHash  = new Sha256Hash("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    @Mock
    Context mMockContext;

    @Before
    public void initTests() {
        activity = new ElectionChoiceActivity();
    }


    @Test
    public void testLoadElectionsEmpty() throws Exception {
        ArrayList<Asset> assetList = new ArrayList<>();
        ArrayList<Election> expected = new ArrayList<>();

        assertEquals(expected, activity.loadElections(assetList));
    }

    @Test
    public void testLoadElectionsSingleInput() throws Exception {
        ArrayList<Asset> assetList = new ArrayList<>();
        Asset asset1 = new Asset("votingpass", mockHash);
        assetList.add(asset1);
        ArrayList<Election> expected = new ArrayList<>();
        expected.add(new Election("", "votingpass", asset1));

        assertEquals(expected, activity.loadElections(assetList));
    }

    @Test
    public void testLoadElectionsMultipleInputs() throws Exception {
        ArrayList<Asset> assetList = new ArrayList<>();
        Asset asset1 = new Asset("votingpass", mockHash);
        Asset asset2 = new Asset("votingpass1", mockHash);
        Asset asset3 = new Asset("votingpass2", mockHash);
        assetList.add(asset1);
        assetList.add(asset2);
        assetList.add(asset3);

        ArrayList<Election> expected = new ArrayList<>();
        expected.add(new Election("", "votingpass", asset1));
        expected.add(new Election("", "votingpass1", asset2));
        expected.add(new Election("", "votingpass2", asset3));

        assertEquals(expected, activity.loadElections(assetList));
    }

}