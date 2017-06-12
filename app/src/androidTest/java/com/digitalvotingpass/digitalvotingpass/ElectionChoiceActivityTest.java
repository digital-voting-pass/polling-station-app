package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Sha256Hash;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            SplashActivity.class, true, false);

    @Rule
    public ActivityTestRule mElectionActivityRule = new ActivityTestRule<ElectionChoiceActivity>(
            ElectionChoiceActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, ElectionChoiceActivity.class);
            return result;
        }
    };

    @Before
    public void setUp() {
        Intents.init();

        // Wait till the splashactivity is closed, indicating blockchain was instantiated has started
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mActivityRule.launchActivity(new Intent(targetContext, ElectionChoiceActivity.class));
        while (!mActivityRule.getActivity().isFinishing()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        activity = (ElectionChoiceActivity) mElectionActivityRule.launchActivity(new Intent(targetContext, ElectionChoiceActivity.class));
    }

    @After
    public void destroy() {
        Intents.release();
        try {
            BlockChain.getInstance(null).disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        activity.finish();
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