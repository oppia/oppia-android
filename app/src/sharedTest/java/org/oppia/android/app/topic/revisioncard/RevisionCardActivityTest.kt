package org.oppia.android.app.topic.revisioncard

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
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

private const val FRACTIONS_SUBTOPIC_LIST_SIZE = 4

/** Tests for [RevisionCardActivity]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RevisionCardActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RevisionCardActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var fakeAccessibilityService: FakeAccessibilityService

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val profileId = ProfileId.newBuilder().setInternalId(1).build()
    val currentScreenName = RevisionCardActivity.createRevisionCardActivityIntent(
      context,
      profileId,
      FRACTIONS_TOPIC_ID,
      1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ).extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.REVISION_CARD_ACTIVITY)
  }

  @Test
  fun testRevisionCardActivity_hasCorrectActivityLabel() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      lateinit var title: CharSequence
      scenario.onActivity { activity -> title = activity.title }

      // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
      // correct string when it's read out.
      assertThat(title).isEqualTo(context.getString(R.string.revision_card_activity_title))
    }
  }

  @Test
  fun testRevisionCardActivity_toolbarTitle_readerOff_marqueeInRtl_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(false)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->

        val revisionCardToolbarTitle: TextView =
          activity.findViewById(R.id.revision_card_toolbar_title)
        ViewCompat.setLayoutDirection(revisionCardToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

        onView(withId(R.id.revision_card_toolbar_title)).perform(click())
        assertThat(revisionCardToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(revisionCardToolbarTitle.isSelected).isEqualTo(true)
        assertThat(revisionCardToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testRevisionCardActivity_toolbarTitle_readerOn_marqueeInRtl_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(true)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->

        val revisionCardToolbarTitle: TextView =
          activity.findViewById(R.id.revision_card_toolbar_title)
        ViewCompat.setLayoutDirection(revisionCardToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

        onView(withId(R.id.revision_card_toolbar_title)).perform(click())
        assertThat(revisionCardToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(revisionCardToolbarTitle.isSelected).isEqualTo(false)
        assertThat(revisionCardToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testRevisionCardActivity_toolbarTitle_readerOff_marqueeInLtr_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(false)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->

        val revisionCardToolbarTitle: TextView =
          activity.findViewById(R.id.revision_card_toolbar_title)
        ViewCompat.setLayoutDirection(revisionCardToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

        onView(withId(R.id.revision_card_toolbar_title)).perform(click())
        assertThat(revisionCardToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(revisionCardToolbarTitle.isSelected).isEqualTo(true)
        assertThat(revisionCardToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testRevisionCardActivity_toolbarTitle_readerOn_marqueeInLtr_isDisplayedCorrectly() {
    fakeAccessibilityService.setScreenReaderEnabled(true)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->

        val revisionCardToolbarTitle: TextView =
          activity.findViewById(R.id.revision_card_toolbar_title)
        ViewCompat.setLayoutDirection(revisionCardToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

        onView(withId(R.id.revision_card_toolbar_title)).perform(click())
        assertThat(revisionCardToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(revisionCardToolbarTitle.isSelected).isEqualTo(false)
        assertThat(revisionCardToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testRevisionCardActivity_englishContentLang_pageContentsAreInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = "test_topic_id_0",
      subtopicId = 1
    ).use {
      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("sample subtopic with dummy content"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testRevisionCardActivity_profileWithArabicContentLang_pageContentsAreInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = "test_topic_id_0",
      subtopicId = 1
    ).use {
      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("محاكاة محتوى أكثر واقعية"))))
    }
  }

  @Test
  fun testActivity_requestReturnToTopic_finishesActivity() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->
        activity.onReturnToTopicRequested()
        testCoroutineDispatchers.runCurrent()

        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testActivity_requestReturnToTopic_logsCloseRevisionEvent() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      scenario.onActivity { activity ->
        activity.onReturnToTopicRequested()
        testCoroutineDispatchers.runCurrent()

        val latestEvent = fakeAnalyticsEventLogger.getMostRecentEvent()
        assertThat(latestEvent).hasCloseRevisionCardContextThat {
          hasTopicIdThat().isEqualTo(FRACTIONS_TOPIC_ID)
          hasSubtopicIndexThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testActivity_clickNavigationBack_finishesActivity() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      onView(withContentDescription("Navigate up")).perform(click())
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testActivity_clickNavigationBack_logsCloseRevisionEvent() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      onView(withContentDescription("Navigate up")).perform(click())
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity {
        val latestEvent = fakeAnalyticsEventLogger.getMostRecentEvent()
        assertThat(latestEvent).hasCloseRevisionCardContextThat {
          hasTopicIdThat().isEqualTo(FRACTIONS_TOPIC_ID)
          hasSubtopicIndexThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testActivity_pressBack_finishesActivity() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      pressBack()
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testActivity_pressBack_logsCloseRevisionEvent() {
    launchRevisionCardActivity(
      profileId = profileId,
      topicId = FRACTIONS_TOPIC_ID,
      subtopicId = 1
    ).use { scenario ->
      pressBack()
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity {
        val latestEvent = fakeAnalyticsEventLogger.getMostRecentEvent()
        assertThat(latestEvent).hasCloseRevisionCardContextThat {
          hasTopicIdThat().isEqualTo(FRACTIONS_TOPIC_ID)
          hasSubtopicIndexThat().isEqualTo(1)
        }
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun launchRevisionCardActivity(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int
  ): ActivityScenario<RevisionCardActivity> {
    val scenario = ActivityScenario.launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(profileId, topicId, subtopicId)
    )
    testCoroutineDispatchers.runCurrent()
    return scenario
  }

  private fun createRevisionCardActivityIntent(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int
  ) = RevisionCardActivity.createRevisionCardActivityIntent(
    context,
    profileId,
    topicId,
    subtopicId,
    FRACTIONS_SUBTOPIC_LIST_SIZE
  )

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
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
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
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
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(revisionCardActivityTest: RevisionCardActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRevisionCardActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(revisionCardActivityTest: RevisionCardActivityTest) {
      component.inject(revisionCardActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
