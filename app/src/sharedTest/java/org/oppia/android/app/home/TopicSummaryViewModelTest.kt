package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HomeFragmentTestActivity
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
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
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

private const val TEST_FRAGMENT_TAG = "topic_summary_view_model_test_fragment"

/** Tests for [TopicSummaryViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicSummaryViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class TopicSummaryViewModelTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var translationController: TranslationController

  private val testFragment by lazy { HomeFragment() }

  private val topicSummary1 = EphemeralTopicSummary.newBuilder().apply {
    topicSummary = TopicSummary.newBuilder()
      .setTopicId("id_1")
      .setTitle(SubtitledHtml.newBuilder().setContentId("title").setHtml("topic_name"))
      .setTotalChapterCount(2)
      .build()
  }.build()
  private val topicSummary2 = EphemeralTopicSummary.newBuilder().apply {
    topicSummary = TopicSummary.newBuilder()
      .setTopicId("id_2")
      .setTitle(SubtitledHtml.newBuilder().setContentId("title").setHtml("topic_name"))
      .setTotalChapterCount(2)
      .build()
  }.build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testTopicSummaryViewModelEquals_reflexiveBasicTopicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)

        // Verify the reflexive property of equals(): a == a.
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModel)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_symmetricBasicTopicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(homeFragmentTestActivity)

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModelCopy)
        assertThat(topicSummaryViewModelCopy).isEqualTo(topicSummaryViewModel)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_transitiveBasicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModelCopy1 = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        val topicSummaryViewModelCopy2 = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        val topicSummaryViewModelCopy3 = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        assertThat(topicSummaryViewModelCopy1).isEqualTo(topicSummaryViewModelCopy2)
        assertThat(topicSummaryViewModelCopy2).isEqualTo(topicSummaryViewModelCopy3)

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(topicSummaryViewModelCopy1).isEqualTo(topicSummaryViewModelCopy3)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_consistentBasicTopicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModelCopy)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModelCopy)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_basicTopicSummaryViewModelAndNull_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)

        // Verify the non-null property of equals(): for any non-null reference a, a != null
        assertThat(topicSummaryViewModel).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_topicSummary1AndTopicSummary2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModelTopicSummary1 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )
        val topicSummaryViewModelTopicSummary2 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary2,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )

        assertThat(topicSummaryViewModelTopicSummary1)
          .isNotEqualTo(topicSummaryViewModelTopicSummary2)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_entity1AndEntity2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModelEntity1 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )
        val topicSummaryViewModelEntity2 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary1,
          entityType = "entity_2",
          topicSummaryClickListener = testFragment,
          position = 5,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )

        assertThat(topicSummaryViewModelEntity1).isNotEqualTo(topicSummaryViewModelEntity2)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_position4AndPosition5_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModelPosition4 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 4,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )
        val topicSummaryViewModelPosition5 = TopicSummaryViewModel(
          activity = homeFragmentTestActivity,
          ephemeralTopicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5,
          homeFragmentTestActivity.appLanguageResourceHandler,
          translationController
        )

        assertThat(topicSummaryViewModelPosition4).isNotEqualTo(topicSummaryViewModelPosition5)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(homeFragmentTestActivity)
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModelCopy)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(topicSummaryViewModel.hashCode()).isEqualTo(topicSummaryViewModelCopy.hashCode())
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelHashCode_sameViewModelHashCodeDoesNotChange_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        setUpTestFragment(homeFragmentTestActivity)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(homeFragmentTestActivity)

        // Verify that hashCode consistently returns the same value.
        val firstHash = topicSummaryViewModel.hashCode()
        val secondHash = topicSummaryViewModel.hashCode()
        assertThat(firstHash).isEqualTo(secondHash)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpTestFragment(activity: HomeFragmentTestActivity) {
    activity.supportFragmentManager.beginTransaction().add(testFragment, TEST_FRAGMENT_TAG)
      .commitNow()
  }

  private fun createBasicTopicSummaryViewModel(
    activity: HomeFragmentTestActivity
  ): TopicSummaryViewModel {
    return TopicSummaryViewModel(
      activity = activity,
      ephemeralTopicSummary = topicSummary1,
      entityType = "entity",
      topicSummaryClickListener = testFragment,
      position = 5,
      activity.appLanguageResourceHandler,
      translationController
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, PlatformParameterModule::class, ApplicationModule::class,
      RobolectricModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(topicSummaryViewModelTest: TopicSummaryViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicSummaryViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicSummaryViewModelTest: TopicSummaryViewModelTest) {
      component.inject(topicSummaryViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
