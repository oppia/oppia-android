package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.StoryProgressTestHelper
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
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

/** Tests for [RecentlyPlayedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RecentlyPlayedFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RecentlyPlayedFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = true
    )
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createRecentlyPlayedActivityIntent(profileId: Int): Intent {
    return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
      context,
      profileId
    )
  }

  @Test
  fun testRecentlyPlayedTestActivity_clickOnToolbarNavigationButton_closeActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.recently_played_toolbar)).perform(click())
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.recently_played_toolbar))
        )
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.divider_view)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.story_name_text_view)
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.topic_name_text_view)
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_clickStory_intentsToExplorationActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).perform(click())
      intended(
        allOf(
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
            FRACTIONS_EXPLORATION_ID_0
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY,
            FRACTIONS_STORY_ID_0
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY,
            FRACTIONS_TOPIC_ID
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
            internalProfileId
          ),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsLastMonthSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 2, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          2,
          R.id.divider_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(instanceOf(TextView::class.java), withParent(withId(R.id.recently_played_toolbar)))
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_doesNotShowSectionDivider() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.divider_view)
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_showsLastWeekSectionTitle() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.story_name_text_view)
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.topic_name_text_view)
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_lessonThumbnailIsCorrect() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem2_showsLastMonthSectionTitle() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 2, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem1_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem3_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 3
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem1_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem3_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 3
        )
      )
    }
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

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRecentlyPlayedFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest) {
      component.inject(recentlyPlayedFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
