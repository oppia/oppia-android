package org.oppia.android.app.story

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
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
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.testing.FakeEventLogger
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
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

private const val TEST_TOPIC_ID = "GJ2rLXRKD5hw"
private const val TEST_STORY_ID = "GJ2rLXRKD5hw"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StoryActivityLocalTest.TestApplication::class,
  qualifiers = "sw600dp"
)
class StoryActivityLocalTest {

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val internalProfileId = 0
  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  @After
  fun tearDown() {
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testStoryActivity_onLaunch_logsEvent() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, TEST_TOPIC_ID, TEST_STORY_ID)
    ).use {
      val event = fakeEventLogger.getMostRecentEvent()

      assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
      assertThat(event.actionName).isEqualTo(EventLog.EventAction.OPEN_STORY_ACTIVITY)
      assertThat(event.context.activityContextCase).isEqualTo(STORY_CONTEXT)
      assertThat(event.context.storyContext.storyId).matches(TEST_STORY_ID)
      assertThat(event.context.storyContext.topicId).matches(TEST_TOPIC_ID)
    }
  }

  @Test
  fun testStoryFragment_completedChapter_checkProgressDrawableIsCorrect() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_solid_color_primary_32dp)))
    }
  }

  @Test
  fun testStoryFragment_notStartedChapter_checkProgressDrawableIsCorrect() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_stroke_2dp_color_primary_32dp)))
    }
  }

  @Test
  fun testStoryFragment_lockedChapter_checkProgressDrawableIsCorrect() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_stroke_1dp_grey_32dp)))
    }
  }

  @Test
  fun testStoryFragment_completedChapter_pawIconIsVisible() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.completed_chapter_image_view
        )
      ).check(matches(withDrawable(R.drawable.ic_lessons_icon_24dp)))
    }
  }

  @Test
  fun testStoryFragment_pendingChapter_pawIconIsGone() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.completed_chapter_image_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStoryFragment_completedChapter_verticalDashedLineIsVisible() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.verticalDashedLineView
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStoryFragment_lastChapter_verticalDashedLineIsGone() {
    launch<StoryActivity>(
      createStoryActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.verticalDashedLineView
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  private fun createStoryActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      storyId
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(storyActivityLocalTest: StoryActivityLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStoryActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(storyActivityLocalTest: StoryActivityLocalTest) {
      component.inject(storyActivityLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
