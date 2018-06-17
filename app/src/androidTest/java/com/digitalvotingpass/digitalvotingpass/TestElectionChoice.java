package com.digitalvotingpass.digitalvotingpass;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import org.bitcoinj.core.Asset;
import org.bitcoinj.core.Sha256Hash;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class TestElectionChoice {
    private ElectionChoiceActivity electionActivity;
    private Sha256Hash mockHash  = new Sha256Hash("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    /**
     * Start up the splash activity for each test.
     */
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
        mActivityRule.launchActivity(new Intent(targetContext, SplashActivity.class));
        while (!mActivityRule.getActivity().isFinishing()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        electionActivity = (ElectionChoiceActivity) mElectionActivityRule.launchActivity(new Intent(targetContext, ElectionChoiceActivity.class));
    }

    @After
    public void destroy() {
        Intents.release();
        try {
            BlockChain.getInstance(null).disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        electionActivity.finish();
    }

    @Test
    public void atElectionActivity() throws Exception {
        onView(withId(R.id.app_bar)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertEquals(((Toolbar) view).getTitle(), electionActivity.getString(R.string.election_choice));
            }
        });
    }

    @Test
    public void clickOnElection() throws Exception {
        final Election e = electionActivity.getAdapter().getItem(1);

        onData(instanceOf(Election.class)) // We are using the position so don't need to specify a data matcher
                .inAdapterView(withId(R.id.election_list)) // Specify the explicit id of the ListView
                .atPosition(1) // Explicitly specify the adapter item to use
                .perform(click());
//        intended(hasComponent(MainActivity.class.getName()));
        onView(withId(R.id.app_bar)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertEquals(((Toolbar) view).getTitle(), e.getKind());
                assertEquals(((Toolbar) view).getSubtitle(), e.getPlace());
            }
        });
    }

    @Test
    public void performSearch() throws Exception {
        List<Election> unfilteredList = electionActivity.getAdapter().getList();

        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (unfilteredList.size())));
        onView(withId(R.id.search)).perform(click());
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("something"));

        List<Election> filteredList = electionActivity.getAdapter().getList();
        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (filteredList.size())));
    }

    @Test
    public void searchCityAndClick() throws Exception {
        List<Election> unfilteredList = electionActivity.getAdapter().getList();

        onView(withId(R.id.search)).perform(click());

        final Election toClick = unfilteredList.get(0);
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText(toClick.getPlace()));

        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (1)));

        onData(instanceOf(Election.class))
                .inAdapterView(withId(R.id.election_list))
                .atPosition(0)
                .perform(click());
//        intended(hasComponent(MainActivity.class.getName()));
        onView(withId(R.id.app_bar)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertEquals(((Toolbar) view).getTitle(), toClick.getKind());
                assertEquals(((Toolbar) view).getSubtitle(), toClick.getPlace());
            }
        });
    }

    private class Matchers {
        private Matcher<View> withListSize (final int size) {
            return new TypeSafeMatcher<View> () {
                @Override public boolean matchesSafely (final View view) {
                    return ((ListView) view).getCount () == size;
                }

                @Override public void describeTo (final Description description) {
                    description.appendText ("ListView should have " + size + " items");
                }
            };
        }
    }

    @Test
    public void testLoadElectionCorrectFormat() throws Exception {
        ArrayList<Asset> assetList = new ArrayList<>();
        Asset asset1 = new Asset("G_Delft", mockHash);
        Asset asset2 = new Asset("T_Nederland", mockHash);
        Asset asset3 = new Asset("P_Zuid-Holland", mockHash);
        Asset asset4 = new Asset("W_Utrecht", mockHash);
        assetList.add(asset1);
        assetList.add(asset2);
        assetList.add(asset3);
        assetList.add(asset4);

        ArrayList<Election> expected = new ArrayList<>();
        expected.add(new Election(electionActivity.getString(R.string.gemeente), "Delft", asset1));
        expected.add(new Election(electionActivity.getString(R.string.tweedekamer), "Nederland", asset2));
        expected.add(new Election(electionActivity.getString(R.string.provinciaal), "Zuid-Holland", asset3));
        expected.add(new Election(electionActivity.getString(R.string.waterschap), "Utrecht", asset4));

        assertEquals(expected, electionActivity.loadElections(assetList));
    }

}
