package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HomeFragmentTestActivity
import org.oppia.android.app.testing.HomeFragmentTestActivity.Companion.createHomeFragmentTestActivity
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.util.system.OppiaClock
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [HomeViewModel]s data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = HomeFragmentViewModelsTest.TestApplication::class,
  manifest = Config.NONE
)
class HomeFragmentViewModelsTest {
  @Inject
  lateinit var context: Context

  private val promotedStory1 = PromotedStory.newBuilder()
    .setStoryId("id_1")
    .setStoryName("Story 1")
    .setTopicName("topic_name")
    .setTotalChapterCount(1)
    .build()
  private val promotedStory2 = PromotedStory.newBuilder()
    .setStoryId("id_2")
    .setStoryName("Story 2")
    .setTopicName("topic_name")
    .setTotalChapterCount(1)
    .build()
  private val promotedStory3 = PromotedStory.newBuilder()
    .setStoryId("id_3")
    .setStoryName("Story 3")
    .setTopicName("topic_name")
    .setTotalChapterCount(1)
    .build()
  private val topicSummary1 = TopicSummary.newBuilder()
    .setTopicId("id_1")
    .setName("topic_name")
    .setTotalChapterCount(2)
    .build()
  private val topicSummary2 = TopicSummary.newBuilder()
    .setTopicId("id_2")
    .setName("topic_name")
    .setTotalChapterCount(2)
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun getTestFragment(activity: HomeFragmentTestActivity): HomeFragment {
    return activity.supportFragmentManager.findFragmentByTag(
      "home_fragment_test_activity"
    ) as HomeFragment
  }

  @Test
  fun testTopicSummaryViewModelEquals_reflexiveTopicSummary1Entity1Position5_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        assertThat(topicSummaryViewModel.equals(topicSummaryViewModel)).isTrue()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_symmetricTopicSummary1Entity1Position5_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val copyTopicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        val isSymmetric = topicSummaryViewModel.equals(copyTopicSummaryViewModel) &&
          copyTopicSummaryViewModel.equals(topicSummaryViewModel)
        assertThat(isSymmetric).isTrue()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_transitiveTopicSummary1Entity1Position5_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val copy1TopicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val copy2TopicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        val isTransitive = topicSummaryViewModel.equals(copy1TopicSummaryViewModel) &&
          copy1TopicSummaryViewModel.equals(copy2TopicSummaryViewModel) &&
          copy2TopicSummaryViewModel.equals(topicSummaryViewModel)
        assertThat(isTransitive).isTrue()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_consistentTopicSummary1Entity1Position5_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val copyTopicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        val isConsistent = topicSummaryViewModel.equals(copyTopicSummaryViewModel) &&
          topicSummaryViewModel.equals(copyTopicSummaryViewModel)
        assertThat(isConsistent).isTrue()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_topicSummary1Entity1Position5AndNull_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        assertThat(topicSummaryViewModel.equals(null)).isFalse()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_topicSummary1AndTopicSummary2_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel1 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val topicSummaryViewModel2 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary2,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        assertThat(topicSummaryViewModel1.equals(topicSummaryViewModel2)).isFalse()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_entity1AndEntity2_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel1 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val topicSummaryViewModel2 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_2",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        assertThat(topicSummaryViewModel1.equals(topicSummaryViewModel2)).isFalse()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_position4AndPosition5_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel1 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 4
        )
        val topicSummaryViewModel2 = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )

        assertThat(topicSummaryViewModel1.equals(topicSummaryViewModel2)).isFalse()
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelHashCode_viewModelsEqualHashCodesEqual_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val topicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        val copyTopicSummaryViewModel = TopicSummaryViewModel(
          /* activity = */ it,
          topicSummary1,
          /* entityType = */ "entity_1",
          /* topicSummaryCLickListener = */ fragment,
          /* position = */ 5
        )
        assertThat(topicSummaryViewModel.equals(copyTopicSummaryViewModel)).isTrue()

        assertThat(topicSummaryViewModel.hashCode())
          .isEqualTo(copyTopicSummaryViewModel.hashCode())
      }
    }
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

    fun inject(homeFragmentViewModelsTest: HomeFragmentViewModelsTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHomeFragmentViewModelsTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(homeViewModelTest: HomeFragmentViewModelsTest) {
      component.inject(homeViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
