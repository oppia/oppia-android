package org.oppia.app.topic.review

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
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
  fun testTopicReviewFragment_loadFragment_displayReviewSkills_isSuccessfully() {
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
      .findFragmentByTag(TopicActivity.getConceptCardFragmentTag()) as ConceptCardFragment
    assertTrue(conceptCardFragment != null)
  }

  @Test
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
