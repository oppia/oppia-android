package org.oppia.app.topic.conceptcard

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
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
import org.junit.Before
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

  private lateinit var activityScenario: ActivityScenario<ConceptCardFragmentTestActivity>

  @Before
  fun setUp() {
    activityScenario = ActivityScenario.launch(ConceptCardFragmentTestActivity::class.java)
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill1_explanationIsDisplayed() {
    onView(withId(R.id.open_dialog_1)).perform(click())
    onView(withId(R.id.explanation)).check(matches(withText("Explanation with <b>rich text</b>.")))
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill1_workedExamplesAreDisplayed() {
    onView(withId(R.id.open_dialog_1)).perform(click())
    onView(withText("Worked example with <i>rich text</i>.")).check(matches(isDisplayed()))
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_explanationIsDisplayed() {
    onView(withId(R.id.open_dialog_2)).perform(click())
    onView(withId(R.id.explanation)).check(matches(withText("Explanation without rich text.")))
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_workedExamplesAreDisplayed() {
    onView(withId(R.id.open_dialog_2)).perform(click())
    onView(withText("Worked example without rich text.")).check(matches(isDisplayed()))
    onView(withText("Second worked example.")).check(matches(isDisplayed()))
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_afterConfigurationChange_workedExamplesAreDisplayed() {
    onView(withId(R.id.open_dialog_2)).perform(click())
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(withText("Worked example without rich text.")).check(matches(isDisplayed()))
    onView(withText("Second worked example.")).check(matches(isDisplayed()))
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
