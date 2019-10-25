package org.oppia.app.topic.overview

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicOverviewFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicOverviewFragmentTest {

  private val topicThumbnail = R.drawable.lesson_thumbnail_graphic_child_with_cupcakes

  private val topicName = "Second Test Topic"

  private val topicDescription =
    "A topic considering the various implications of having especially long topic descriptions. " +
        "These descriptions almost certainly need to wrap, which should be interesting in the UI (especially on " +
        "small screens). Consider also that there may even be multiple points pertaining to a topic, some of which " +
        "may require expanding the description section in order to read the whole topic description."

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testTopicOverviewFragment_loadFragment_checkTopicName_isCorrect() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(topicName))))
    }
  }

  // TODO(#135): Update this test case to check on click of See More play tab is shown.
  @Test
  fun testTopicOverviewFragment_loadFragment_seeMoreIsClickable() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.see_more_text_view)).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicOverviewFragment_loadFragmentWithTestTopicId1_checkTopicDescription_isCorrect() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_description_text_view)).check(matches(withText(containsString(topicDescription))))
    }
  }

  @Test
  fun testTopicOverviewFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_thumbnail_image_view)).check(matches(withDrawable(topicThumbnail)))
    }
  }

  @Test
  fun testTopicOverviewFragment_loadFragment_configurationChange_checkTopicName_isCorrect() {
    activityTestRule.launchActivity(null)
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(topicName))))
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
