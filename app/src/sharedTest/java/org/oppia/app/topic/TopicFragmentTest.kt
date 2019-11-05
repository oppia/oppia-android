package org.oppia.app.topic

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.annotation.UiThread
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicActivity]. */
@RunWith(AndroidJUnit4::class)
class TopicFragmentTest {
  private val topicName = "First Test Topic"

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    activityTestRule.launchActivity(intent)
  }

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
    onView(withId(R.id.topic_tabs_viewpager)).perform(ViewActions.swipeLeft())
  }

  @Test
  fun testTopicFragment_overviewTopicIsDisplayedInTabLayout_worksAsExpected() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
  }

  @Test
  fun testTopicFragment_defaultTabIsOverview_isSuccessful() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
  }

  @Test
  fun testTopicFragment_showsOverviewTabSelectedAndContentMatched() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    )
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabSelected() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_showsPlayTabWithContentMatched() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
      .perform(click())
    onView(withText("First Story")).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabSelected() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_showsTrainTabWithContentMatched() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
      .perform(click())
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabSelected() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
      .perform(click())
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

  @Test
  fun testTopicFragment_clickOnOverviewTab_showsOverviewTabSelected() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
  }

  @Test
  fun testTopicFragment_clickOnOverviewTab_showsOverviewTabWithContentMatched() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
      .perform(click())
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
      .perform(click())
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    )
  }

  @Test
  fun testTopicFragment_clickOnPlayTab_configurationChange_showsSameTabAndItsData() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
      .perform(click())
    onView(withText("First Story")).check(matches(isDisplayed()))
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.play))))
    onView(withText("First Story")).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicFragment_clickOnTrainTab_configurationChange_showsSameTabAndItsData() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
      .perform(click())
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.train))))
    onView(withText("Master These Skills")).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_configurationChange_showsSameTabAndItsData() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
      .perform(click())
    onView(
      atPositionOnView(R.id.review_skill_recycler_view, 0, R.id.skill_name)
    ).check(matches(withText(containsString("An important skill"))))
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.review))))
    onView(
      atPositionOnView(R.id.review_skill_recycler_view, 0, R.id.skill_name)
    ).check(matches(withText(containsString("An important skill"))))
  }

  @Test
  fun testTopicFragment_clickOnOverviewTab_configurationChange_showsSameTabAndItsData() {
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
      .perform(click())
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    )
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_tabs_container)).check(matches(matchCurrentTabTitle(getResourceString(R.string.overview))))
    onView(withId(R.id.topic_name_text_view)).check(
      matches(
        withText(
          Matchers.containsString(topicName)
        )
      )
    )
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getResourceString(id: Int): String {
    val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    return resources.getString(id)
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
