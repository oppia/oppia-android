package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
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
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = RecentlyPlayedSpanTest.TestApplication::class)
class RecentlyPlayedSpanTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  private val internalProfileId = 0
  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Config(qualifiers = "port")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem0_port_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 2,
          position = 0
        )
      )
    }
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem0_tablet_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 3,
          position = 0
        )
      )
    }
  }

  @Config(qualifiers = "land")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem0_landscape_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 3,
          position = 0
        )
      )
    }
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem0_landscape_tablet_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 4,
          position = 0
        )
      )
    }
  }

  @Config(qualifiers = "port")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem2_port_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 2,
          position = 2
        )
      )
    }
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem2_tablet_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 3,
          position = 2
        )
      )
    }
  }

  @Config(qualifiers = "land")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem2_landscape_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 3,
          position = 2
        )
      )
    }
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testRecentlyPlayedSpanTest_checkSpanForItem2_landscape_tablet_hasCorrectSpanCount() {
    launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 4,
          position = 2
        )
      )
    }
  }

  private fun createRecentlyPlayedActivityIntent(profileId: Int): Intent {
    return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
      context,
      profileId
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
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(recentlyPlayedSpanTest: RecentlyPlayedSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRecentlyPlayedSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(recentlyPlayedSpanTest: RecentlyPlayedSpanTest) {
      component.inject(recentlyPlayedSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
