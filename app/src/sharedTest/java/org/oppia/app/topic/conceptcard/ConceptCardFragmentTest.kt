package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.topic.conceptcard.testing.ConceptCardFragmentTestActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [ConceptCardFragment]. */
@RunWith(AndroidJUnit4::class)
class ConceptCardFragmentTest {

  @Test
  fun testConceptCardFragment_openDialogFragment_explanationIsDisplayed() {
    ActivityScenario.launch(ConceptCardFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.open_dialog)).perform(click())
      Espresso.onView(withId(R.id.explanation)).check(matches(withText("Explanation without rich text.")))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment_workedExamplesIsDisplayed() {
    ActivityScenario.launch(ConceptCardFragmentTestActivity::class.java).use {
      Espresso.onView(withId(R.id.open_dialog)).perform(click())
      Espresso.onView(withText("Worked example without rich text.")).check(matches(isDisplayed()))
      Espresso.onView(withText("Second worked example.")).check(matches(isDisplayed()))
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
