package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

// TODO(#239): AudioFragment implementation has been updated in PR #238 and because of which these audio-related test cases are failing.
/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {

  @Test
  fun testStateFragmentTestActivity_loadStateFragment_hasDummyButton() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).check(matches(withText("Dummy Audio Button")))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_clickPositive_showsAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.audio_fragment)).check(matches((isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_clickDummyButton_clickNegative_doesNotShowAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.audio_fragment)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_clickPositive_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickNegative_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(withText("Don\'t show this message again")))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndPositive_clickDummyButton_doesNotShowCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndNegative_clickDummyButton_doesNotShowCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_clickPositive_restartActivity_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickNegative_restartActivity_clickDummyButton_showsCellularDialog() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndPositive_restartActivity_clickDummyButton_doesNotShowCellularDialogAndShowsAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
      onView(withId(R.id.audio_fragment)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_clickCheckBoxAndNegative_restartActivity_clickDummyButton_doesNotShowCellularDialogAndAudioFragment() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText("CANCEL")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
    }
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withId(R.id.cellular_data_dialog_checkbox)).check(doesNotExist())
      onView(withId(R.id.audio_fragment)).check(matches(not(isDisplayed())))
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
