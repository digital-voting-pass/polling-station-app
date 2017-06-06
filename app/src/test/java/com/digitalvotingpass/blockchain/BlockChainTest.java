package com.digitalvotingpass.blockchain;

import org.bitcoinj.core.Asset;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by wkmeijer on 6-6-17.
 */
public class BlockChainTest {
    private ArrayList<Asset> assetList;
    @Mock
    BlockChain mockedBlockchain;

    @Before
    public void init() {
        assetList = new ArrayList<Asset>();
        assetList.add(new Asset("T_2017", null));
        assetList.add(new Asset("G_Delft", null));
        assetList.add(new Asset("P_Utrecht", null));

        when(mockedBlockchain.getAssets()).thenReturn(assetList);

    }

    @Test
    public void testAssetExists() throws Exception {
        Asset testAsset = new Asset("W_Zeeland", null);
        assetList.add(testAsset);
        assertTrue(BlockChain.getInstance().assetExists(testAsset));
    }

    @Test
    public void testAssetExistsSimilarInput() throws Exception {
        Asset testAsset = new Asset("G_Delft", null);
        assertTrue(BlockChain.getInstance().assetExists(testAsset));
    }

    @Test
    public void testAssetExistsWrongInput() throws Exception {
        Asset testAsset = new Asset("W_Zeeland", null);
        assertFalse(BlockChain.getInstance().assetExists(testAsset));
    }

}
