package org.oppia.app.completedstorylist

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
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
import org.oppia.app.model.ProfileId
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.topic.TopicActivity
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
import org.oppia.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
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
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [CompletedStoryListActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = CompletedStoryListActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class CompletedStoryListActivityTest {

  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var storyProfileTestHelper: StoryProgressTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    FirebaseApp.initializeApp(context)

    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProfileTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProfileTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
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
  fun testCompletedStoryList_checkItem0_storyThumbnailDescriptionIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list))
        .perform(
          scrollToPosition<RecyclerView.ViewHolder>(
            0
          )
        )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          0,
          R.id.completed_story_lesson_thumbnail
        )
      ).check(
        matches(
          withContentDescription(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem0_storyNameIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          0,
          R.id.completed_story_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_clickOnItem0_intentIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          0,
          R.id.completed_story_name_text_view
        )
      ).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicActivity.getTopicIdKey(), FRACTIONS_TOPIC_ID))
      intended(hasExtra(TopicActivity.getStoryIdKey(), FRACTIONS_STORY_ID_0))
    }
  }

  @Test
  fun testCompletedStoryList_configurationChange_clickOnItem0_intentIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          0,
          R.id.completed_story_name_text_view
        )
      ).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicActivity.getTopicIdKey(), FRACTIONS_TOPIC_ID))
      intended(hasExtra(TopicActivity.getStoryIdKey(), FRACTIONS_STORY_ID_0))
    }
  }

  @Test
  fun testCompletedStoryList_checkItem0_titleIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          0,
          R.id.completed_story_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Fractions"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem1_storyNameIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios: Part 1"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_changeOrientation_checkItem1_storyNameIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios: Part 1"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem1_storyThumbnailDescriptionIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_lesson_thumbnail
        )
      ).check(
        matches(
          withContentDescription(containsString("Ratios: Part 1"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_changeOrientation_checkItem1_storyThumbnailDescriptionIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_lesson_thumbnail
        )
      ).check(
        matches(
          withContentDescription(containsString("Ratios: Part 1"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem1_titleIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios and Proportional Reasoning"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_changeOrientation_checkItem1_titleIsCorrect() {
    launch<CompletedStoryListActivity>(
      createCompletedStoryListActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.completed_story_list,
          1,
          R.id.completed_story_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios and Proportional Reasoning"))
        )
      )
    }
  }

  private fun createCompletedStoryListActivityIntent(internalProfileId: Int): Intent {
    return CompletedStoryListActivity.createCompletedStoryListActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(completedStoryListActivityTest: CompletedStoryListActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCompletedStoryListActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(completedStoryListActivityTest: CompletedStoryListActivityTest) {
      component.inject(completedStoryListActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
