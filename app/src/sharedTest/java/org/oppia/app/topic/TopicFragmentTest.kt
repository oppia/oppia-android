package org.oppia.app.topic

import android.app.Application
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.testing.TopicTestActivity
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

private const val TOPIC_NAME = "First Test Topic"

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicFragmentTest {

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    launch(TopicTestActivity::class.java).use {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    launch(TopicTestActivity::class.java).use {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(1).name)))
    }
  }

  @Test
  fun testTopicFragment_overviewTopicTab_isDisplayedInTabLayout() {
    launch(TopicTestActivity::class.java).use {
      onView(withText(TopicTab.getTabForPosition(0).name)).check(matches(isDescendantOfA(withId(R.id.topic_tabs_container))))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsOverview_isSuccessful() {
    launch(TopicTestActivity::class.java).use {
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(TopicTab.getTabForPosition(0).name)))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsOverview_showsMatchingContent() {
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        RecyclerViewMatcher.atPosition(
          R.id.story_summary_recycler_view,
          0
        )
      ).check(matches(ViewMatchers.hasDescendant(withText(Matchers.containsString("First Story")))))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabSelected() {
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
  fun testTopicFragment_clickOnReviewTab_thenOverviewTab_showsOverviewTab() {
    launch(TopicTestActivity::class.java).use {
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
  fun testTopicFragment_clickOnReviewTab_thenOverviewTab_showsOverviewTabWithContentMatched() {
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        RecyclerViewMatcher.atPosition(
          R.id.story_summary_recycler_view,
          0
        )
      ).check(matches(ViewMatchers.hasDescendant(withText(Matchers.containsString("First Story")))))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicFragment_clickOnTrainTab_configurationChange_showsSameTabAndItsContent() {
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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
    launch(TopicTestActivity::class.java).use {
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

  @Test
  fun testTopicActivity_clickOnSeeMore_isPlayTabIsSelectedAndContentMatched() {
    launch(TopicTestActivity::class.java).use {
      onView(
        withId(R.id.see_more_text_view)
      ).perform(scrollTo(), click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("PLAY")))
      onView(withId(R.id.story_summary_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        RecyclerViewMatcher.atPosition(
          R.id.story_summary_recycler_view,
          0
        )
      ).check(matches(ViewMatchers.hasDescendant(withText(Matchers.containsString("First Story")))))
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
