package org.oppia.app.topic

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

private const val TOPIC_NAME = "First Test Topic"

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicFragmentTest {

  @Test
  fun testTopicFragment_toolbarTitle_isDisplayedSuccessfully() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.topic_toolbar))))
        .check(matches(withText("Topic: First Test Topic")))
    }
  }

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(1).name)))
    }
  }

  @Test
  fun testTopicFragment_infoTopicTab_isDisplayedInTabLayout() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(withText(TopicTab.getTabForPosition(0).name)).check(matches(isDescendantOfA(withId(R.id.topic_tabs_container))))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsInfo_isSuccessful() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(0).name)))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsInfo_showsMatchingContent() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(TOPIC_NAME)
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabSelected() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(1).name)))
    }
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabWithContentMatched() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        RecyclerViewMatcher.atPosition(
          R.id.story_summary_recycler_view,
          1
        )
      ).check(matches(ViewMatchers.hasDescendant(withText(containsString("First Story")))))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabSelected() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(2).name)))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabWithContentMatched() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabSelected() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(3).name)))
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.review_skill_recycler_view, 0, R.id.skill_name)).check(
        matches(
          withText(
            containsString(
              "An important skill"
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTab() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(0).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(0).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTabWithContentMatched() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(0).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(TOPIC_NAME)
          )
        )
      )
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicFragment_clickOnPlayTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(1).name
          )
        )
      )
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        RecyclerViewMatcher.atPosition(
          R.id.story_summary_recycler_view,
          1
        )
      ).check(matches(ViewMatchers.hasDescendant(withText(Matchers.containsString("First Story")))))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicFragment_clickOnTrainTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(2).name
          )
        )
      )
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicFragment_clickOnReviewTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(3).name
          )
        )
      )
      onView(
        atPositionOnView(R.id.review_skill_recycler_view, 0, R.id.skill_name)
      ).check(matches(withText(containsString("An important skill"))))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicFragment_configurationChange_showsDefaultTabAndItsContent() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(0).name
          )
        )
      )
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(TOPIC_NAME)
          )
        )
      )
    }
  }

  private fun launchTopicActivityIntent(topicId: String): ActivityScenario<TopicActivity> {
    val intent = TopicActivity.createTopicActivityIntent(ApplicationProvider.getApplicationContext(), topicId)
    return launch(intent)
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
