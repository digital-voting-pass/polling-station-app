package com.digitalvotingpass.blockchain;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Sha256Hash;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by wkmeijer on 6-6-17.
 */
public class BlockChainTest {
    private ArrayList<Asset> assetList;
    private Sha256Hash mockHash  = new Sha256Hash("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    @Mock
    private BlockChain mockedBlockchain;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void init() {
        assetList = new ArrayList<>();
        assetList.add(new Asset("T_2017", mockHash));
        assetList.add(new Asset("G_Delft", mockHash));
        assetList.add(new Asset("P_Utrecht", mockHash));

        when(mockedBlockchain.getAssets()).thenReturn(assetList);
    }

    @Test
    public void testAssetExists() throws Exception {
        Asset testAsset = new Asset("W_Zeeland", mockHash);
        assetList.add(testAsset);
        assertTrue(mockedBlockchain.assetExists(testAsset));
    }

    @Test
    public void testAssetExistsSimilarInput() throws Exception {
        Asset testAsset = new Asset("G_Delft", mockHash);
        assertTrue(mockedBlockchain.assetExists(testAsset));
    }

    @Test
    public void testAssetExistsWrongInput() throws Exception {
        Asset testAsset = new Asset("W_Zeeland", mockHash);
        assertFalse(mockedBlockchain.assetExists(testAsset));
    }

}
