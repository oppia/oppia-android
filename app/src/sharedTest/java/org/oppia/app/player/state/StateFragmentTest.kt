package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.testing.ContentCardTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {

  @get:Rule
  var homeActivityTestRule: ActivityTestRule<HomeActivity> = ActivityTestRule(
    HomeActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  // TODO(#163): Add more test-cases
  //  1. Actually going through each of the exploration states with typing text/clicking the correct answers for each of the interactions.
  //  2. Verifying the button visibility state based on whether text is missing, then present/missing for text input or numeric input.
  //  3. Testing providing the wrong answer and showing feedback and the same question again.
  //  4. Configuration change with typed text (e.g. for numeric or text input) retains that temporary text and you can continue with the exploration after rotating.
  //  5. Configuration change after submitting the wrong answer to show that the old answer & re-ask of the question stay the same.
  //  6. Backward/forward navigation along with configuration changes to verify that you stay on the navigated state.
  //  7. Verifying that old answers were present when navigation backward/forward.
  //  8. Testing providing the wrong answer and showing hints.

  @Test
  fun testStateFragment_clickPlayExploration_explorationLoadsSuccessfully() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      onView(atPosition(R.id.state_recycler_view, 0)).check(matches(hasDescendant(withId(R.id.interaction_button))))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_submitAnswer_submitChangesToContinueButton() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.interaction_button
        )
      ).check(matches(withText(R.string.state_submit_button)))
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.interaction_button
        )
      ).check(matches(withText(R.string.state_continue_button)))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_previousAndNextButtonIsNotDisplayed() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.previous_state_image_view)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.next_state_image_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.previous_state_image_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_submitAnswer_clickContinueButton_clickPreviousButton_previousButtonIsHiddenAndNextButtonIsDisplayed() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.previous_state_image_view)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.previous_state_image_view)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.next_state_image_view)).check(matches(isDisplayed()))
    }
  }

  // TODO(#257): This test case corresponds to the special test-case mentioned in #257 and discussed in #251.
  @Test
  fun testStateFragment_loadExplorationTest5_submitAnswer_clickContinueButton_submitAnswer_clickPreviousButton_clickNextButton_continueButtonIsDisplayed() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      // State 0: Welcome! - MultipleChoiceInout Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      // State 1: What language - TextInput Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.previous_state_image_view)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.next_state_image_view)).perform(click())
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.interaction_button
        )
      ).check(matches(withText(R.string.state_continue_button)))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testStateFragment_loadExplorationTest5_submitAnswer_clickContinueButton_configurationChange_previousAndInteractionButtonIsDisplayed() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      // State 0: MultipleChoiceInput
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      it.onActivity { activity ->
        activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
      }
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.previous_state_image_view
        )
      ).check(matches(isDisplayed()))
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_continueToEndExploration_endExplorationButtonIsDisplayedInFinal() {
    launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      // State 0: Welcome! - MultipleChoiceInout Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      // State 1: What language - TextInput Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      // State 2: Things you can do - Continue Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      // State 3: Numeric input - NumericInput Interaction
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
      // State 4: END -> EndExploration
      onView(
        atPositionOnView(
          R.id.state_recycler_view,
          0,
          R.id.interaction_button
        )
      ).check(matches(withText(R.string.state_end_exploration_button)))
    }
  }

  @Test
  fun testStateFragment_loadExplorationTest5_continueToEndExploration_clickEndExplorationButton_destroysExplorationActivity() {
    homeActivityTestRule.launchActivity(null)
    onView(withId(R.id.play_exploration_button)).perform(click())
    // State 0: Welcome! - MultipleChoiceInout Interaction
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    // State 1: What language - TextInput Interaction
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    // State 2: Things you can do - Continue Interaction
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    // State 3: Numeric input - NumericInput Interaction
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    // State 4: END - EndExploration
    onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.interaction_button)).perform(click())
    intended(hasComponent(HomeActivity::class.java.name))
  }

  @Test
  fun testContentCard_forDemoExploration_withCustomOppiaTags_displaysParsedHtml() {
    launch(ContentCardTestActivity::class.java).use {
      val htmlResult =
        "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n\n" +
            "Incidentally, do you know where the name 'Oppia' comes from?"
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.content_text_view)).check(matches(withText(htmlResult)))
    }
  }

  @Test
  fun testMultipleChoiceInput_showsRadioButtons_forDemoExploration_withCustomOppiaTags_userSelectsDesiredOption() {
    launch(ContentCardTestActivity::class.java).use {
      onView(withId(R.id.play_exploration_button_1)).perform(click())
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
      )
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click())
      )
    }
  }

  @Test
  fun testItemSelectionInput_showsCheckBox_forDemoExploration_withCustomOppiaTags_userSelectsDesiredOptions() {
    launch(ContentCardTestActivity::class.java).use {
      onView(withId(R.id.play_exploration_button_2)).perform(click())
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
      )
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click())
      )
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testItemSelectionInput_showsCheckBox_withMaxSelectionAllowed_userSelectsDesiredOptions_correctError() {
    launch(ContentCardTestActivity::class.java).use {
      onView(withId(R.id.play_exploration_button_2)).perform(click())
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
      )
      it.onActivity { activity ->
        activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
      }
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click())
      )
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click())
      )
      onView(withId(R.id.selection_interaction_recyclerview)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click())
      )
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }
  }
}
