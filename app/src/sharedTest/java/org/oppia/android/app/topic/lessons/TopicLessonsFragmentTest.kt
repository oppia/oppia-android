package org.oppia.android.app.topic.lessons

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.EnablePracticeTab
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicLessonsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicLessonsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicLessonsFragmentTest {
  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @JvmField
  @field:[Inject EnablePracticeTab]
  var enablePracticeTab: Boolean = false

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
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
  fun testLessonsPlayFragment_loadRatiosTopic_storyName_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterCountTextMultiple_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 2, stringToMatch = "2 Chapters")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_completeStoryProgress_isDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "100%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_completeStoryProgress_contentDescriptionIsCorrect() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(itemPosition = 1, stringToMatch = "100%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_noStoryProgress_contentDescriptionIsCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(itemPosition = 1, stringToMatch = "0%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadFractionsTopic_storyChapterTextsContentDescriptionIsCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID)).use {
      clickLessonTab()
      verifyStoryAndChapterCountContentDescriptionAtPosition(
        itemPosition = 1,
        stringToMatch = "2 Chapters in Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_partialStoryProgress_isDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 2, stringToMatch = "50%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configurationChange_storyName_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      onView(isRoot()).perform(orientationLandscape())
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testLessonsPlayFragment_loadRatiosTopic_configurationLandscape_storyName_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.story_name_text_view)
      intended(hasComponent(StoryActivity::class.java.name))
      intended(hasExtra(StoryActivity.STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, RATIOS_STORY_ID_0))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterListIsNotVisible() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(withId(R.id.chapter_recycler_view)).check(doesNotExist())
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_default_arrowDown() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_list_drop_down_icon
        )
      ).check(
        matches(
          withDrawable(R.drawable.ic_arrow_drop_down_black_24dp)
        )
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIcon_chapterListIsVisible() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickChapter_opensExplorationActivity() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.chapter_container)))).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
          internalProfileId
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY,
          RATIOS_TOPIC_ID
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY,
          RATIOS_STORY_ID_0
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
          RATIOS_EXPLORATION_ID_0
        )
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_clickExpandListIconIndex2_chapterListForIndex1IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(withId(R.id.story_summary_recycler_view)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(2, scrollTo())
      )
      clickStoryItem(position = 2, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex2_clickExpandListIconIndex1_chapterListForIndex2IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      scrollToPosition(position = 2)
      clickStoryItem(position = 2, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 2,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_configurationChange_chapterListIsVisible() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      onView(isRoot()).perform(orientationLandscape())
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_configurationLandscape_chapterListIsVisible() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_partialProg_partialProgIconIsDisplayed() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      verifyChapterPlayStateIconIsVisible(itemPosition = 0)
      verifyPartialProgressIconIsDisplayed(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_partialProg_configChange_partialProgIconIsDisplayed() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(isRoot()).perform(orientationLandscape())
      verifyChapterPlayStateIconIsVisible(itemPosition = 0)
      verifyPartialProgressIconIsDisplayed(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_chapterCompleted_completedIconIsDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      verifyChapterPlayStateIconIsVisible(itemPosition = 0)
      verifyChapterCompletedIconIsDisplayed(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_chapterCompleted_configChange_completedIconIsDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(isRoot()).perform(orientationLandscape())
      verifyChapterPlayStateIconIsVisible(itemPosition = 0)
      verifyChapterCompletedIconIsDisplayed(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_startedNotCompleted_chapterPlayStateIconIsNotVisible() {
    storyProgressTestHelper.markStartedNotCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      verifyChapterPlayStateIconIsNotVisible(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_unsavedPartialProg_chapterPlayStateIconIsNotVisible() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      verifyChapterPlayStateIconIsNotVisible(itemPosition = 0)
    }
  }

  private fun createTopicActivityIntent(internalProfileId: Int, topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId
    )
  }

  private fun clickLessonTab() {
    testCoroutineDispatchers.runCurrent()
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position = 1, enablePracticeTab).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickStoryItem(position: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.story_summary_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyTextOnStorySummaryListItemAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      atPosition(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition
      )
    ).check(matches(hasDescendant(withText(containsString(stringToMatch)))))
  }

  private fun verifyProgressContentDescriptionAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition,
        targetViewId = R.id.story_progress_container
      )
    ).check(matches(withContentDescription(stringToMatch)))
  }

  private fun verifyStoryAndChapterCountContentDescriptionAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition,
        targetViewId = R.id.story_name_chapter_count_container
      )
    ).check(matches(withContentDescription(stringToMatch)))
  }

  private fun verifyChapterPlayStateIconIsVisible(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyChapterPlayStateIconIsNotVisible(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(not(isDisplayed())))
  }

  private fun verifyPartialProgressIconIsDisplayed(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(withDrawable(R.drawable.ic_pending_24dp)))
  }

  private fun verifyChapterCompletedIconIsDisplayed(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(withDrawable(R.drawable.ic_check_24dp)))
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(topicLessonsFragmentTest: TopicLessonsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicLessonsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicLessonsFragmentTest: TopicLessonsFragmentTest) {
      component.inject(topicLessonsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
