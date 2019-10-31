package org.oppia.app.topic

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.UiThread
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicActivity]. */
@RunWith(AndroidJUnit4::class)
class TopicFragmentTest {
  private val topicName = "Second Test Topic"

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testTopicActivity_clickDummyButton_showsTopicActivityWithMultipleTabs_isTabLayoutDisplayed() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))

  }

  @Test
  fun testTopicActivity_swipePage_hasSwipedPage() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
    onView(withId(R.id.topic_tabs_viewpager)).perform(ViewActions.swipeLeft())
  }

  @Test
  @UiThread
  fun testTopicActivity_showsTopicActivityWithMultipleTabs_isTabLayoutDisplayedWithOverviewTabSelectedWhenOpenedFirstTime() {
    activityTestRule.launchActivity(null)

    onView(
      allOf(
        withText("OVERVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    )
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("OVERVIEW")))
  }

  @Test
  @UiThread
  fun testTopicActivity_isOverviewTabSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("OVERVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    )
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("OVERVIEW")))
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    ).perform(scrollTo())
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnPlayTab_isTabSwitchedToPlayTabAndTabIsSelected() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("PLAY"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("PLAY")))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnPlayTab_isPlayTabIsSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("PLAY"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withText("First Story")).check(matches(isDisplayed()))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnTrainTab_isSwitchToTrainTabAndTrainTabIsSelected() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("TRAIN"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("TRAIN")))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnTrainTab_isTrainTabIsSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("TRAIN"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnReviewTab_isSwitchToReviewTabAndReviewTabIsSelected() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("REVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("REVIEW")))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnReviewTab_isReviewTabIsSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("REVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      allOf(
        atPosition(R.id.review_skill_recycler_view, 0).also { withText("An important skill") },
        isDescendantOfA(withId(R.id.review_skill_recycler_view))
      ).also { withId(R.id.skill_name) })
  }

  @Test
  @UiThread
  fun testTopicActivity_showsTopicActivityWithMultipleTabs_clickOnOverviewTab_isTabLayoutDisplayedWithOverviewTabSelected() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("REVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      allOf(
        withText("OVERVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())

    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("OVERVIEW")))

  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnOverviewTab_isOverviewTabSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("REVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      allOf(
        withText("OVERVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    ).perform(scrollTo())
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnPlayTab_configurationChange_isSameTabAndItsDataDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("PLAY"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withText("First Story")).check(matches(isDisplayed()))
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("PLAY")))
    onView(withText("First Story")).check(matches(isDisplayed()))
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnTrainTab_configurationChange_isSameTabAndItsDataDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("TRAIN"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(
      allOf(
        withText("TRAIN"),
        isDisplayed()
      )
    )
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
  }

  fun testTopicActivity_clickOnReviewTab_configurationChange_isSameTabAndItsDataDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("REVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(allOf(
      atPosition(R.id.review_skill_recycler_view, 0).also { withText("An important skill") },
      isDescendantOfA(withId(R.id.review_skill_recycler_view))
    ).also { withId(R.id.skill_name) })
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(
      allOf(
        withText("REVIEW"),
        isDisplayed()
      )
    )
    onView(allOf(
      atPosition(R.id.review_skill_recycler_view, 0).also { withText("An important skill") },
      isDescendantOfA(withId(R.id.review_skill_recycler_view))
    ).also { withId(R.id.skill_name) })
  }

  @Test
  @UiThread
  fun testTopicActivity_clickOnOverviewTab_configurationChange_isSameTabAndItsDataDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      allOf(
        withText("OVERVIEW"),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    ).perform(scrollTo())
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(
      allOf(
        withText("OVERVIEW"),
        isDisplayed()
      )
    )
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    ).perform(scrollTo())
  }
  @Test
  @UiThread
  fun testTopicActivity_clickOnSeeMore_isPlayTabIsSelectedAndContentMatched() {
    activityTestRule.launchActivity(null)
    onView(
      withId(R.id.see_more_text_view)
    ).perform(scrollTo(),click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle("PLAY")))
    onView(withText("First Story")).check(matches(isDisplayed()))
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
