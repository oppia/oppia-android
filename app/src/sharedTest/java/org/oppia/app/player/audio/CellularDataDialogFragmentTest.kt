package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [CellularDataDialogFragment]. */
@RunWith(AndroidJUnit4::class)
class CellularDataDialogFragmentTest {

  @Test
  fun testCellularDataDialogFragment_loadCellularDialogFragment_clickOkButton_audioFragmentIsDisplayed() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText(R.string.cellular_data_alert_dialog_title)).check(matches(isDisplayed()))
      onView(withText(R.string.cellular_data_alert_dialog_okay_button)).perform(click())
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
      onView(withText("hi-en")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testCellularDataDialogFragment_loadCellularDialogFragment_clickCancelButton_audioFragmentIsNotDisplayed() {
    ActivityScenario.launch(StateFragmentTestActivity::class.java).use {
      onView(withId(R.id.dummy_audio_button)).perform(click())
      onView(withText(R.string.cellular_data_alert_dialog_title)).check(matches(isDisplayed()))
      onView(withText(R.string.cellular_data_alert_dialog_cancel_button)).perform(click())
      onView(withId(R.id.ivPlayPauseAudio)).check(doesNotExist())
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
