package org.oppia.app.topic.revision

import android.app.Application
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicRevisionFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicRevisionFragmentTest {
  private val subtopicThumbnail = R.drawable.topic_fractions_01
  private val internalProfileId = 0

  @get:Rule
  var topicActivityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Test
  fun testTopicRevisionFragment_loadFragment_displayReviewTopics_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_selectReviewTopics_opensReviewActivity() {
    topicActivityTestRule.launchActivity(
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(3).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPosition(R.id.revision_recycler_view, 0)).perform(click())
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_selectReviewTopics_reviewCardDisplaysCorrectExplanation() { // ktlint-disable max-line-length
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 0)).perform(click())
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            withText(
              "Description of subtopic is here."
            )
          )
        )
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view)).check(
        matches(
          hasDescendant(
            withDrawable(
              subtopicThumbnail
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_checkSpanCoun_isTwo() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view))
        .check(
          GridLayoutManagerColumnCountAssertion(
            2
          )
        )
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_configurationChange_reviewSubtopicsAreDisplayed() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view)).check(
        matches(
          hasDescendant(
            withDrawable(
              subtopicThumbnail
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_configurationChange_checkSpanCount_isThree() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view))
        .check(
          GridLayoutManagerColumnCountAssertion(
            3
          )
        )
    }
  }

  private fun launchTopicActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        topicId
      )
    return ActivityScenario.launch(intent)
  }

  class GridLayoutManagerColumnCountAssertion(expectedColumnCount: Int) : ViewAssertion {
    private var expectedColumnCount: Int = 0

    init {
      this.expectedColumnCount = expectedColumnCount
    }

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      val recyclerView = view as RecyclerView
      if (recyclerView.layoutManager is GridLayoutManager) {
        val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
        val spanCount = gridLayoutManager.spanCount
        if (spanCount != expectedColumnCount) {
          val errorMessage =
            ("expected column count " + expectedColumnCount + " but was " + spanCount)
          throw AssertionError(errorMessage)
        }
      } else {
        throw IllegalStateException("no grid layout manager")
      }
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
