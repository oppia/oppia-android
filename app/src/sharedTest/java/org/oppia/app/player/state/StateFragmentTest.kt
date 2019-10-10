package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testStateFragmentTestActivity_loadStateFragment_hasDummyButton() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).check(matches(withText("Dummy Audio Button")))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_clickPositive_showsAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.audio_fragment)).check(matches((isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_clickNegative_doesNotShowAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.audio_fragment)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_clickPositive_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickNegative_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndPositive_clickDummyButton_doesNotShowCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      Espresso.onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndNegative_clickDummyButton_doesNotShowCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      Espresso.onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_clickPositive_restartActivity_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickNegative_restartActivity_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndPositive_restartActivity_clickDummyButton_doesNotShowCellularDialogAndShowsAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      Espresso.onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
      Espresso.onView(withId(R.id.audio_fragment)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndNegative_restartActivity_clickDummyButton_doesNotShowCellularDialogAndAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      Espresso.onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.dummy_audio_button)).perform(click())
      Espresso.onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
      Espresso.onView(withId(R.id.audio_fragment)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragmentButtons_loadExplorationTest5_startState_submitButtonIsInactive() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      onView(withId(R.id.continue_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.end_exploration_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.learn_again_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.previous_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.submit_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_state_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragmentButtons_loadExplorationTest5_startState_typeAnswer_submitButtonIsActive() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragmentButtons_loadExplorationTest5_startState_typeAnswer_clearAnswer_submitButtonIsInactive() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text"), closeSoftKeyboard())
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.submit_state_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragmentButtons_loadExplorationTest5_secondState_previousButtonWorks_nextButtonWorks() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))

      // State 0
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).perform(click())

      // State 1
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.previous_state_image_view)).perform(click())

      // State 0
      onView(withId(R.id.previous_state_image_view)).check(matches(not(isDisplayed())))
      onView(withId(R.id.submit_state_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_image_view)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_image_view)).perform(click())

      //State 1
      onView(withId(R.id.previous_state_image_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragmentButtons_loadExplorationTest5_startToEndTraversal_isSuccessful() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))

      // State 0
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text 1"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).perform(click())

      // State 1
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text 2"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).perform(click())

      // State 2
      onView(withId(R.id.dummy_interaction_edit_text)).perform(clearText())
      onView(withId(R.id.dummy_interaction_edit_text)).perform(typeText("Sample text 3"), closeSoftKeyboard())
      onView(withId(R.id.submit_state_button)).perform(click())

      //State 4
      onView(withId(R.id.continue_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.continue_state_button)).perform(click())

      //State 5
      onView(withId(R.id.end_exploration_state_button)).check(matches(isDisplayed()))
      onView(withId(R.id.end_exploration_state_button)).perform(click())

      assertTrue(activityTestRule.activity == null)
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
