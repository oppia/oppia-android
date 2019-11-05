package org.oppia.app.topic

import android.app.Application
import android.content.Context
import androidx.annotation.UiThread
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicActivity]. */
@RunWith(AndroidJUnit4::class)
class TopicFragmentTest {
  private val topicName = "First Test Topic"

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
    }
  }

  @Test
  fun testTopicFragment_overviewTopicTab_isDisplayedInTabLayout() {
    launch(TopicActivity::class.java).use {
      onView(withText(R.string.overview)).check(matches(isDescendantOfA(withId(R.id.topic_tabs_container))))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsOverview_isSuccessful() {
    launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsOverview_showsMatchingContent() {
    launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(topicName)
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabSelected() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.play),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
    }
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabWithContentMatched() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.play),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withText("First Story")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabSelected() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.train),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabWithContentMatched() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.train),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabSelected() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.review),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.review),
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
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.review),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        allOf(
          withText(R.string.overview),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            getResourceString(R.string.overview)
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenOverviewTab_showsOverviewTabWithContentMatched() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.review),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        allOf(
          withText(R.string.overview),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(topicName)
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_configurationChange_showsSameTabAndItsData() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.play),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            getResourceString(R.string.play)
          )
        )
      )
      onView(withText("First Story")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_configurationChange_showsSameTabAndItsData() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.train),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            getResourceString(R.string.train)
          )
        )
      )
      onView(withText("Master These Skills")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_configurationChange_showsSameTabAndItsData() {
    launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.review),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            getResourceString(R.string.review)
          )
        )
      )
      onView(
        atPositionOnView(R.id.review_skill_recycler_view, 0, R.id.skill_name)
      ).check(matches(withText(containsString("An important skill"))))
    }
  }

  @Test
  fun testTopicFragment_configurationChange_showsDefaultTabAndItsData() {
    launch(TopicActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            getResourceString(R.string.overview)
          )
        )
      )
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(
            containsString(topicName)
          )
        )
      )
    }
  }

  private fun getResourceString(id: Int): String {
    val resources =
      InstrumentationRegistry.getInstrumentation().targetContext.resources
    return resources.getString(id)
  }

  @Test
  fun testTopicActivity_clickOnSeeMore_isPlayTabIsSelectedAndContentMatched() {
    launch(TopicActivity::class.java).use {
      onView(
        withId(R.id.see_more_text_view)
      ).perform(scrollTo(), click())
      onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("PLAY")))
      onView(withText("First Story")).check(matches(isDisplayed()))
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
