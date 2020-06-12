package org.oppia.app.topic.conceptcard

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.parser.RichTextViewMatcher.Companion.containsRichText
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
  fun testConceptCardFragment_clickOnToolbarNavigationButton_closeActivity() {
    onView(withId(R.id.open_dialog_0)).perform(click())
    onView(withId(R.id.concept_card_toolbar)).perform(click())
  }

  @Test
  fun testConceptCardFragment_toolbarTitle_isDisplayedSuccessfully() {
    onView(withId(R.id.open_dialog_0)).perform(click())
    onView(
      allOf(
        instanceOf(TextView::class.java),
        withParent(withId(R.id.concept_card_toolbar))
      )
    ).check(matches(withText(R.string.concept_card_toolbar_title)))
  }

  @Test
  fun testConceptCardFragment_configurationChange_toolbarTitle_isDisplayedSuccessfully() {
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.open_dialog_0)).perform(click())
    onView(
      allOf(
        instanceOf(TextView::class.java),
        withParent(withId(R.id.concept_card_toolbar))
      )
    ).check(matches(withText(R.string.concept_card_toolbar_title)))
  }

  @Test
  fun testConceptCardFragment_configurationChange_conceptCardIsDisplayedCorrectly() {
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.open_dialog_0)).perform(click())
    onView(withId(R.id.concept_card_explanation_text))
      .check(
        matches(
          withText(
            "Hello. Welcome to Oppia."
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text)).check(matches(not(containsRichText())))
  }

  @Test
  fun testConceptCardFragment_openDialogFragment0_checkSkillAndExplanationAreDisplayedWithoutRichText() { // ktlint-disable max-line-length
    onView(withId(R.id.open_dialog_0)).perform(click())
    onView(withId(R.id.concept_card_heading_text))
      .check(
        matches(
          withText(
            "An important skill"
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text))
      .check(
        matches(
          withText(
            "Hello. Welcome to Oppia."
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text)).check(matches(not(containsRichText())))
  }

  @Test
  fun testConceptCardFragment_openDialogFragment1_checkSkillAndExplanationAreDisplayedWithRichText() { // ktlint-disable max-line-length
    onView(withId(R.id.open_dialog_1)).perform(click())
    onView(withId(R.id.concept_card_heading_text))
      .check(
        matches(
          withText(
            "Another important skill"
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text))
      .check(
        matches(
          withText(
            "Explanation with rich text."
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text)).check(matches(containsRichText()))
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_afterConfigurationChange_workedExamplesAreDisplayed() { // ktlint-disable max-line-length
    onView(withId(R.id.open_dialog_1)).perform(click())
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.concept_card_heading_text))
      .check(
        matches(
          withText(
            "Another important skill"
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text))
      .check(
        matches(
          withText(
            "Explanation with rich text."
          )
        )
      )
    onView(withId(R.id.concept_card_explanation_text)).check(matches(containsRichText()))
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
    fun provideBackgroundDispatcher(
      @BlockingDispatcher blockingDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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
