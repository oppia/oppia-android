package org.oppia.android.app.topic

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
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
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BlockingDispatcher
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

private const val TOPIC_NAME = "Fractions"

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicFragmentTest {

  private val internalProfileId = 0

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testTopicFragment_toolbarTitle_isDisplayedSuccessfully() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_toolbar_title)).check(matches(withText("Topic: Fractions")))
    }
  }

  @Test
  fun testTopicFragment_clickOnToolbarNavigationButton_closeActivity() {
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(1).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_infoTopicTab_isDisplayedInTabLayout() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withText(TopicTab.getTabForPosition(0).name)).check(
        matches(
          isDescendantOfA(
            withId(
              R.id.topic_tabs_container
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsInfo_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
  fun testTopicFragment_defaultTabIsInfo_showsMatchingContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(containsString(TOPIC_NAME))
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_showsPlayTabSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(1).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_showsPlayTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
        atPosition(R.id.story_summary_recycler_view, 1)
      ).check(
        matches(
          hasDescendant(
            withText(
              containsString(
                "Matthew Goes to the Bakery"
              )
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(2).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(3).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.revision_recycler_view, 0, R.id.subtopic_title)).check(
        matches(
          withText(
            containsString(
              "What is a Fraction?"
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTab() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
          matchCurrentTabTitle(TopicTab.getTabForPosition(0).name)
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
          withText(containsString(TOPIC_NAME))
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
        atPosition(
          R.id.story_summary_recycler_view,
          1
        )
      ).check(
        matches(
          hasDescendant(
            withText(
              containsString(
                "Matthew Goes to the Bakery"
              )
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
  fun testTopicFragment_clickOnReviewTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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
        atPositionOnView(R.id.revision_recycler_view, 0, R.id.subtopic_title)
      ).check(
        matches(
          withText(
            containsString(
              "What is a Fraction?"
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_configurationChange_showsDefaultTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
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

  private fun createTopicActivityIntent(internalProfileId: Int, topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(), internalProfileId, topicId
    )
  }

  private fun launchTopicActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    return launch(createTopicActivityIntent(internalProfileId, topicId))
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
