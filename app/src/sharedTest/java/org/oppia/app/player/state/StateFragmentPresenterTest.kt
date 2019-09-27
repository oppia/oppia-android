package org.oppia.app.player.state;
import android.app.Application
import android.content.Context
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher

@RunWith(AndroidJUnit4::class)
class StateFragmentPresenterTest {
  //to test text,number,and fraction input interaction views
  @Test
  fun addTextInputContentCard() {
    ActivityScenario.launch(HomeActivity::class.java).use {
//      onView(ViewMatchers.withId(R.id.dummy_audio_button)).perform(click())
//      onView(ViewMatchers.withHint("text"))
//          .check(matches(isDisplayed()))
//      onView(ViewMatchers.withHint("text"))
//          .perform(click())
//      onView(ViewMatchers.withHint("text"))
//          .perform(clearText(), typeText("Some Text"));
//      pauseTestFor(500)
//      onView(ViewMatchers.withHint("number 1"))
//          .check(matches(isDisplayed()))
//      onView(ViewMatchers.withHint("number 1"))
//          .perform(click())
//      onView(ViewMatchers.withHint("text"))
//          .perform(clearText(), typeText("123"));
//      pauseTestFor(500)
//      onView(ViewMatchers.withHint("fraction 1/1"))
//          .check(matches(isDisplayed()))
//      onView(ViewMatchers.withHint("fraction 1/1"))
//          .perform(click())
//      onView(ViewMatchers.withHint("text"))
//          .perform(clearText(), typeText("1/3"));
//      pauseTestFor(500)

    }
  }

  fun pauseTestFor(milliseconds: Long) {
    try {
      Thread.sleep(milliseconds)
    } catch (e: InterruptedException) {
      e.printStackTrace();
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
