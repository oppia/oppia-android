package org.oppia.android.app.preview

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.containsString
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topicdownloaded.TopicDownloadedActivity
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TOPIC_ID = "GJ2rLXRKD5hw"
private const val TOPIC_NAME = "Fractions"

private const val TOPIC_DESCRIPTION =
  "You'll often need to talk about part of an object or group. For example, " +
    "a jar of milk might be half-full, or some of the eggs in a box might have broken. " +
    "In these lessons, you'll learn to use fractions to describe situations like these."

/** Tests for [TopicPreviewFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicPreviewFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicPreviewFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0
  private val topicThumbnail = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testTopicPreviewFragment_toolbarTitleIsDisplayed() {
    launchTopicPreviewActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_preview_toolbar_title)).check(
        matches(
          withText(
            String.format(
              context.resources.getString(R.string.topic_preview_toolbar_title),
              TOPIC_NAME
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicPreviewFragment_thumbnailIsDisplayed() {
    launchTopicPreviewActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_preview_thumbnail_image_view)).check(
        matches(
          withDrawable(
            topicThumbnail
          )
        )
      )
    }
  }

  @Test
  fun testTopicPreviewFragment_topicNameIsCorrect() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_preview_name_text_view)).check(matches(withText(TOPIC_NAME)))
    }
  }

  @Test
  fun testTopicPreviewFragment_checkTopicDescription_isCorrect() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_preview_description_text_view)).check(
        matches(
          withText(
            containsString(
              TOPIC_DESCRIPTION
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicPreviewFragment_storyHeadingIsDisplayed() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.stories_heading)).perform(scrollTo())
      onView(withId(R.id.stories_heading)).check(matches(withText("Story You Can Play")))
    }
  }

  @Test
  fun testTopicPreviewFragment_storySummaryListIsDisplayed() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.story_summary_recycler_view)).perform(scrollTo())
      onView(withId(R.id.story_summary_recycler_view)).check(matches(isDisplayed()))
      verifyTextOnStorySummaryListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicPreviewFragment_skillsHeadingIsDisplayed() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skills_heading)).perform(scrollTo())
      onView(withId(R.id.skills_heading)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicPreviewFragment_skillsListIsDisplayed() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skills_recycler_view)).perform(scrollTo())
      onView(withId(R.id.skills_recycler_view)).check(matches(isDisplayed()))
      verifyTextOnSkillsListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicPreviewFragment_clickDownloadTopic_opensTopicDownloadedActivity() {
    launchTopicPreviewActivityIntent(
      internalProfileId = internalProfileId,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_preview_download_image_view)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(TopicDownloadedActivity::class.java.name))
      intended(hasExtra(TopicDownloadedActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicDownloadedActivity.getTopicIdKey(), TEST_TOPIC_ID))
    }
  }

  private fun verifyTextOnStorySummaryListItemAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      RecyclerViewMatcher.atPosition(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition
      )
    ).check(matches(hasDescendant(withText(containsString(stringToMatch)))))
  }

  private fun verifyTextOnSkillsListItemAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      RecyclerViewMatcher.atPosition(
        recyclerViewId = R.id.skills_recycler_view,
        position = itemPosition
      )
    ).check(matches(hasDescendant(withText(containsString(stringToMatch)))))
  }

  private fun launchTopicPreviewActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicPreviewActivity.createTopicPreviewActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        topicId
      )
    return ActivityScenario.launch(intent)
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
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(topicPreviewFragmentTest: TopicPreviewFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicPreviewFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicPreviewFragmentTest: TopicPreviewFragmentTest) {
      component.inject(topicPreviewFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
