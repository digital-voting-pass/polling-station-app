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

import com.digitalvotingpass.electionchoice.Election;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class TestElectionChoice {

    /**
     * Start up the splash activity for each test.
     */
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<ElectionChoiceActivity>(
            ElectionChoiceActivity.class) {
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
    }

    @After
    public void destroy() {
        Intents.release();
    }


    @Test
    public void atElectionActivity() throws Exception {
        onView(withId(R.id.app_bar)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertEquals(((Toolbar) view).getTitle(), "Choose election");
            }
        });
    }

    @Test
    public void clickOnElection() throws Exception {
        final Election e = ((ElectionChoiceActivity) mActivityRule.getActivity()).getAdapter().getItem(1);

        onData(instanceOf(Election.class)) // We are using the position so don't need to specify a data matcher
                .inAdapterView(withId(R.id.election_list)) // Specify the explicit id of the ListView
                .atPosition(1) // Explicitly specify the adapter item to use
                .perform(click());
        intended(hasComponent(MainActivity.class.getName()));
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
        List<Election> unfilteredList = ((ElectionChoiceActivity) mActivityRule.getActivity()).getAdapter().getList();

        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (unfilteredList.size())));
        onView(withId(R.id.search)).perform(click());
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("something"));

        List<Election> filteredList = ((ElectionChoiceActivity) mActivityRule.getActivity()).getAdapter().getList();
        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (filteredList.size())));
    }

    @Test
    public void searchCityAndClick() throws Exception {
        List<Election> unfilteredList = ((ElectionChoiceActivity) mActivityRule.getActivity()).getAdapter().getList();

        onView(withId(R.id.search)).perform(click());

        final Election toClick = unfilteredList.get(0);
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText(toClick.getPlace()));

        onView (withId (R.id.election_list)).check (ViewAssertions.matches (new Matchers().withListSize (1)));

        onData(instanceOf(Election.class))
                .inAdapterView(withId(R.id.election_list))
                .atPosition(0)
                .perform(click());
        intended(hasComponent(MainActivity.class.getName()));
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
}
