package org.oppia.app.topic.review

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.text.style.CharacterStyle
import android.view.View
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicReviewFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicReviewFragmentTest {

  @get:Rule
  var topicActivityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testTopicReviewFragment_loadFragment_displayReviewSkills_isSuccessful() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.review_skill_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.skill_name))))
    }
  }

  @Test
  fun testTopicReviewFragment_loadFragment_selectReviewSkill_opensReviewActivity() {
    topicActivityTestRule.launchActivity(null)
    onView(atPosition(R.id.review_skill_recycler_view, 0)).perform(click())
    val conceptCardFragment: ConceptCardFragment? = topicActivityTestRule.activity.supportFragmentManager
      .findFragmentByTag(TopicActivity.TAG_CONCEPT_CARD_DIALOG) as ConceptCardFragment
    assertThat(conceptCardFragment).isNotNull()
  }

  @Test
  fun testTopicReviewFragment_loadFragment_selectReviewSkill_conceptCardDisplaysCorrectExplanation() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.review_skill_recycler_view, 1)).perform(click())
      onView(withId(R.id.concept_card_explanation_text)).check(matches(withText("Explanation with rich text.")))
      onView(withId(R.id.concept_card_explanation_text)).check(matches(containsRichText()))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicTrainFragment_loadFragment_configurationChange_skillsAreDisplayed() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      it.onActivity { activity ->
        activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
      }
      it.recreate()
      onView(atPosition(R.id.review_skill_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.skill_name))))
    }
  }

  private fun containsRichText() = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("Checks if view contains rich text")
    }

    override fun matchesSafely(view: View): Boolean {
      return view is TextView && view.text.toSpannable().getSpans(0, view.text.length, CharacterStyle::class.java).isNotEmpty()
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
