package org.oppia.app.topic.play

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.topic.TopicActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicPlayFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicPlayFragmentTest {

  // TODO(#137): Add following test-cases once story-progress function is implemented and expandable list is introduced.
  //  Story progress is is displayed correctly.
  //  Click on arrow to show and hide expandable list is working correctly.
  //  Expandable list is showing correct chapter names.
  //  Upon configuration change expanded list should remain expanded.
  //  Click on story-title or entire item should open [StoryActivity].
  //  Click on chapter in expandable list should start exploration.

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_storyName_isCorrect() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.story_summary_recycler_view, 0)).check(matches(hasDescendant(withText("First Story"))))
    }
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterCount_isCorrect() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.story_summary_recycler_view, 0)).check(matches(hasDescendant(withText("1 Chapter"))))
    }
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_configurationChange_storyName_isCorrect() {
    activityTestRule.launchActivity(null)
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(atPosition(R.id.story_summary_recycler_view, 0)).check(matches(hasDescendant(withText("1 Chapter"))))
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
