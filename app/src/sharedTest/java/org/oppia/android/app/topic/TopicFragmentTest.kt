package org.oppia.android.app.topic

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
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
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val INFO_TAB_POSITION = 0
private const val LESSON_TAB_POSITION = 1
private const val PRACTICE_TAB_POSITION = 2
private const val REVISION_TAB_POSITION = 3

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicFragmentTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0

  private val TOPIC_NAME = "Fractions"

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testTopicFragment_toolbarTitle_isDisplayedSuccessfully() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
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
      verifyTabTitleAtPosition(position = 1)
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
      verifyTabTitleAtPosition(position = 0)
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsInfo_showsMatchingContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
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
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      verifyTabTitleAtPosition(position = 1)
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_showsPlayTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      verifyTabTitleAtPosition(position = 2)
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      verifyTabTitleAtPosition(position = 3)
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.revision_recycler_view,
        itemPosition = 0,
        targetViewId = R.id.subtopic_title,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTab() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      clickTabAtPosition(position = INFO_TAB_POSITION)
      verifyTabTitleAtPosition(position = 0)
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTabWithContentMatched() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = 0)
      testCoroutineDispatchers.runCurrent()
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
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyTabTitleAtPosition(position = 1)
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = 2)
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_configurationChange_showsSameTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = 3)
      matchStringOnListItem(
        recyclerView = R.id.revision_recycler_view,
        itemPosition = 0,
        targetViewId = R.id.subtopic_title,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicFragment_configurationChange_showsDefaultTabAndItsContent() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyTabTitleAtPosition(position = 0)
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

  private fun clickTabAtPosition(position: Int) {
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
  }

  private fun verifyTabTitleAtPosition(position: Int) {
    onView(withId(R.id.topic_tabs_container)).check(
      matches(
        matchCurrentTabTitle(
          TopicTab.getTabForPosition(position).name
        )
      )
    )
  }

  private fun matchStringOnListItem(
    recyclerView: Int,
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerView,
        itemPosition,
        targetViewId
      )
    ).check(
      matches(
        withText(
          containsString(
            stringToMatch
          )
        )
      )
    )
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
  interface TestApplicationComponent : ApplicationComponent {
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
