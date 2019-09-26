package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {

  // This method is dependent on {@link org.oppia.app.player.state.StateFragment#createDummyDataForButtonCheck}
  // Dummy States:
  // 0 -> Continue
  // 1 -> MultipleChoiceInput
  // 2 -> TextInput
  // 3 -> Continue
  // 4 -> ItemSelectionInput
  // 5 -> EndExploration
  @Test
  fun testStateFragment_stateButtons_continueToEndExploration_endExplorationButtonIsDisplayedInFinal() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      // 0 -> Continue
      onView(withId(R.id.continue_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.continue_state_button)).perform(click())

      // 1 -> MultipleChoiceInput
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.submit_state_button)).check(matches(not(isClickable())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("MultipleChoiceInput"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_state_button)).perform(click())

      // 2 -> TextInput
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.submit_state_button)).check(matches(not(isClickable())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("TextInput"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_state_button)).perform(click())

      // 3 -> Continue
      onView(withId(R.id.continue_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.continue_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.continue_state_button)).perform(click())

      // 4 -> ItemSelectionInput
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.submit_state_button)).check(matches(not(isClickable())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("ItemSelectionInput"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_state_button)).perform(click())

      // 5 -> EndExploration
      // Moving backward to state-4 from state-5 using "Previous"
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.previous_state_image_view)).perform(click())

      // 4 -> ItemSelectionInput
      // State 4 should be visible again with next and previous buttons.
      // Moving backward to state-3 from state-4 using "Previous"
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.previous_state_image_view)).perform(click())

      // 3 -> Continue
      // State 3 should be visible again with next and previous buttons.
      // Moving forward to state-4 from state-3 using "Next"
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.next_state_image_view)).perform(click())

      // 4 -> ItemSelectionInput
      // State 4 should be visible again with next and previous buttons.
      // Moving forward to state-5 from state-4 using "Next"
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.next_state_image_view)).perform(click())

      // 5 -> EndExploration
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.end_exploration_state_button)).perform(click())
    }
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
