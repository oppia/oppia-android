package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.audio.testing.AudioFragmentTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

// TODO(#116): Write test-cases when the user enables/disables on cellular with/without saving the setting.
// TODO: https://github.com/robolectric/robolectric/issues/5235 Need ShadowMediaPlayer to write tests, can import robolectric beccause of duplicated classes
/** Tests for [CellularDataDialogFragment]. */
@RunWith(AndroidJUnit4::class)
class AudioFragmentTest {

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    ActivityScenario.launch(AudioFragmentTestActivity::class.java).use {
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withDrawable(R.drawable.ic_play_circle_filled_black_24dp)))
    }
  }

  private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("ImageView with drawable same as drawable with id $id")
    }

    override fun matchesSafely(view: View): Boolean {
      val context = view.context
      val expectedBitmap = context?.getDrawable(id)?.toBitmap()
      return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
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
