package org.oppia.android.app.home.promotedlist

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
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.PromotedStoryList
import org.oppia.android.app.model.SubtitledHtml
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

/** Tests for [PromotedStoryListViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PromotedStoryListViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class PromotedStoryListViewModelTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var translationController: TranslationController

  private val promotedStory1 = PromotedStory.newBuilder()
    .setStoryId("id_1")
    .setStoryTitle(SubtitledHtml.newBuilder().setContentId("story_title").setHtml("Story 1"))
    .setTopicTitle(SubtitledHtml.newBuilder().setContentId("topic_title").setHtml("topic_name"))
    .setTotalChapterCount(1)
    .build()
  private val promotedStory2 = PromotedStory.newBuilder()
    .setStoryId("id_2")
    .setStoryTitle(SubtitledHtml.newBuilder().setContentId("story_title").setHtml("Story 2"))
    .setTopicTitle(SubtitledHtml.newBuilder().setContentId("topic_title").setHtml("topic_name"))
    .setTotalChapterCount(1)
    .build()
  private val promotedStory3 = PromotedStory.newBuilder()
    .setStoryId("id_3")
    .setStoryTitle(SubtitledHtml.newBuilder().setContentId("story_title").setHtml("Story 3"))
    .setTopicTitle(SubtitledHtml.newBuilder().setContentId("topic_title").setHtml("topic_name"))
    .setTotalChapterCount(1)
    .build()

  private val promotedActivityList1 = PromotedActivityList.newBuilder()
    .setPromotedStoryList(
      PromotedStoryList.newBuilder()
        .addRecentlyPlayedStory(promotedStory1)
        .addOlderPlayedStory(promotedStory2).build()
    ).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPromotedStoryListViewModelEquals_reflexiveStoryListOf2_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity, listOf(promotedStory1, promotedStory2), promotedActivityList1
        )

        // Verify the reflexive property of equals(): a == a.
        assertThat(promotedStoryListViewModel).isEqualTo(promotedStoryListViewModel)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_symmetricStoryListOf2_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListViewModelCopy = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(promotedStoryListViewModel).isEqualTo(promotedStoryListViewModelCopy)
        assertThat(promotedStoryListViewModelCopy).isEqualTo(promotedStoryListViewModel)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_transitiveStoryListOf2_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModelCopy1 = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListViewModelCopy2 = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListViewModelCopy3 = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        assertThat(promotedStoryListViewModelCopy1).isEqualTo(promotedStoryListViewModelCopy2)
        assertThat(promotedStoryListViewModelCopy2).isEqualTo(promotedStoryListViewModelCopy3)

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(promotedStoryListViewModelCopy1).isEqualTo(promotedStoryListViewModelCopy3)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_consistentStoryListOf2_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListViewModelCopy = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        assertThat(promotedStoryListViewModel).isEqualTo(promotedStoryListViewModelCopy)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(promotedStoryListViewModel).isEqualTo(promotedStoryListViewModelCopy)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_storyListOf2AndNull_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )

        // Verify the non-null property of equals(): for any non-null reference a, a != null
        assertThat(promotedStoryListViewModel).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_storyListOf2AndStoryListOf3_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModelOf2 = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListViewModelOf3 = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2, promotedStory3),
          promotedActivityList1
        )

        assertThat(promotedStoryListViewModelOf2).isNotEqualTo(promotedStoryListViewModelOf3)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        val promotedStoryListCopy = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )
        assertThat(promotedStoryListViewModel).isEqualTo(promotedStoryListCopy)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(promotedStoryListViewModel.hashCode())
          .isEqualTo(promotedStoryListCopy.hashCode())
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelHashCode_sameViewModelHashCodeDoesNotChange_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { homeFragmentTestActivity ->
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          homeFragmentTestActivity,
          listOf(promotedStory1, promotedStory2),
          promotedActivityList1
        )

        // Verify that hashCode consistently returns the same value.
        val firstHash = promotedStoryListViewModel.hashCode()
        val secondHash = promotedStoryListViewModel.hashCode()
        assertThat(firstHash).isEqualTo(secondHash)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createPromotedStoryViewModelList(
    activity: AppCompatActivity,
    promotedStoryList: List<PromotedStory>
  ): List<PromotedStoryViewModel> {
    return promotedStoryList.mapIndexed { index, promotedStory ->
      PromotedStoryViewModel(
        activity = activity,
        internalProfileId = 1,
        totalStoryCount = promotedStoryList.size,
        entityType = "entity",
        promotedStory = promotedStory,
        translationController,
        index
      )
    }
  }

  private fun createPromotedStoryListViewModel(
    activity: HomeFragmentTestActivity,
    promotedStoryList: List<PromotedStory>,
    promotedActivityList: PromotedActivityList
  ): PromotedStoryListViewModel {
    return PromotedStoryListViewModel(
      activity,
      createPromotedStoryViewModelList(activity, promotedStoryList),
      promotedActivityList,
      activity.appLanguageResourceHandler
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

    fun inject(promotedStoryListViewModelTest: PromotedStoryListViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPromotedStoryListViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(promotedStoryListViewModelTest: PromotedStoryListViewModelTest) {
      component.inject(promotedStoryListViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
