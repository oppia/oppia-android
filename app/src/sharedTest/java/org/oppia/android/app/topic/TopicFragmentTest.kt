package org.oppia.android.app.topic

import android.app.Application
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
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
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @JvmField
  @field:[Inject EnablePracticeTab]
  var enablePracticeTab: Boolean = false

  private val internalProfileId = 0

  private val TOPIC_NAME = "Fractions"

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testTopicFragment_toolbarTitle_isDisplayedSuccessfully() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_toolbar_title)).check(matches(withText("Topic: Fractions")))
    }
  }

  @Test
  fun testTopicFragment_toolbarTitle_marqueeInRtl_isDisplayedCorrectly() {
    initializeApplicationComponent()
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    testCoroutineDispatchers.runCurrent()
    val topicToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.topic_toolbar_title)
    ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

    onView(withId(R.id.topic_toolbar_title)).perform(click())
    assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testTopicFragment_toolbarTitle_marqueeInLtr_isDisplayedCorrectly() {
    initializeApplicationComponent()
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    testCoroutineDispatchers.runCurrent()
    val topicToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.topic_toolbar_title)
    ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)
    onView(withId(R.id.topic_toolbar_title)).perform(click())
    assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testTopicFragment_clickOnToolbarNavigationButton_closeActivity() {
    initializeApplicationComponent()
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    onView(withContentDescription(R.string.navigate_up)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_infoTopicTab_isDisplayedInTabLayout() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withText(TopicTab.getTabForPosition(position = 0, enablePracticeTab).name)).check(
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
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      verifyTabTitleAtPosition(position = INFO_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_defaultTabIsInfo_showsMatchingContent() {
    initializeApplicationComponent()
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
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_showsPlayTabWithContentMatched() {
    initializeApplicationComponent()
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
  fun testTopicFragment_practiceTabEnabled_practiceTopicTabIsDisplayedInTabLayout() {
    initializeApplicationComponent(practiceTabIsEnabled = true)
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enablePracticeTab)
      onView(withText(practiceTab.name)).check(
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
  fun testTopicFragment_practiceTabDisabled_practiceTopicTabIsNotDisplayedInTabLayout() {
    initializeApplicationComponent(practiceTabIsEnabled = false)
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      // Unconditionally retrieve the practice tab name since this test is verifying that it's not
      // enabled.
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enablePracticeTab = true)
      onView(withText(practiceTab.name)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_practiceTabDisabled_configChange_practiceTopicTabIsNotDisplayed() {
    initializeApplicationComponent(practiceTabIsEnabled = false)
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      // Unconditionally retrieve the practice tab name since this test is verifying that it's not
      // enabled.
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enablePracticeTab = true)
      // The tab should still not be visible even after a configuration change.
      onView(withText(practiceTab.name)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabSelected() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_showsPracticeTabWithContentMatched() {
    initializeApplicationComponent()
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
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      verifyTabTitleAtPosition(position = REVISION_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    initializeApplicationComponent()
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
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      clickTabAtPosition(position = INFO_TAB_POSITION)
      verifyTabTitleAtPosition(position = INFO_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_thenInfoTab_showsInfoTabWithContentMatched() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = INFO_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(containsString(TOPIC_NAME))
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnPracticeTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent()
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
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = REVISION_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.revision_recycler_view,
        itemPosition = 0,
        targetViewId = R.id.subtopic_title,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicFragment_configChange_showsDefaultTabAndItsContent() {
    initializeApplicationComponent()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = INFO_TAB_POSITION)
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
  fun testTopicFragment_withStoryId_clickOnPracticeTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent()
    launchTopicPlayStoryActivityIntent(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0
    ).use {
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
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  /**
   * Creates TopicActivity Intent without a storyId
   */
  private fun createTopicActivityIntent(internalProfileId: Int, topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(), internalProfileId, topicId
    )
  }

  /**
   * Creates TopicActivity Intent with a storyId.
   * The intent returned from here can be used to
   * launch TopicActivity from Promoted stories.
   */
  private fun createTopicPlayStoryActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent {
    return TopicActivity.createTopicPlayStoryActivityIntent(
      ApplicationProvider.getApplicationContext(), internalProfileId, topicId, storyId
    )
  }

  /**
   * Launches TopicActivity without a storyId.
   * This simulates opening a topic from All topics list.
   */
  private fun launchTopicActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    return launch(createTopicActivityIntent(internalProfileId, topicId))
  }

  /**
   * Launches TopicActivity with a valid storyId.
   * This simulates opening a topic from Promoted stories.
   */
  private fun launchTopicPlayStoryActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): ActivityScenario<TopicActivity> {
    return launch(createTopicPlayStoryActivityIntent(internalProfileId, topicId, storyId))
  }

  private fun clickTabAtPosition(position: Int) {
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position, enablePracticeTab).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
  }

  private fun verifyTabTitleAtPosition(position: Int) {
    onView(withId(R.id.topic_tabs_container)).check(
      matches(
        matchCurrentTabTitle(
          TopicTab.getTabForPosition(position, enablePracticeTab).name
        )
      )
    )
  }

  // TODO(#2208): Create helper function in Test for RecyclerView
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

  private fun initializeApplicationComponent(practiceTabIsEnabled: Boolean = true) {
    TestModule.checkIfPracticeTabIsEnabled = { practiceTabIsEnabled }
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    companion object {
      // Note that a lambda is used here since primitive types (like booleans) can't be lateinit,
      // and defaulting the boolean can inadvertently enable actual state in the test (i.e. both
      // 'true' and 'false' mean something other than "not yet initialized").
      lateinit var checkIfPracticeTabIsEnabled: () -> Boolean
    }

    @Provides
    @EnablePracticeTab
    @Singleton
    fun provideEnablePracticeTab(): Boolean = checkIfPracticeTabIsEnabled()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, PlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
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
