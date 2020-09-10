package org.oppia.app.topic

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

private const val TOPIC_NAME = "Fractions"

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicFragmentTest {

  private val internalProfileId = 0

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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
  // TODO(#973): Fix TopicFragmentTest
  @Ignore
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(topicFragmentTest: TopicFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicFragmentTest: TopicFragmentTest) {
      component.inject(topicFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
