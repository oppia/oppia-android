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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HomeFragmentTestActivity
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
import org.oppia.android.testing.time.FakeOppiaClockModule
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

private const val TEST_FRAGMENT_TAG = "topic_summary_view_model_test_fragment"

/** Tests for [TopicSummaryViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicSummaryViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class TopicSummaryViewModelTest {

  @Inject
  lateinit var context: Context

  private val testFragment by lazy { HomeFragment() }

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

  @Test
  fun testTopicSummaryViewModelEquals_reflexiveBasicTopicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)

        // Verify the reflexive property of equals(): a == a.
        assertThat(topicSummaryViewModel).isEqualTo(topicSummaryViewModel)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_symmetricBasicTopicSummaryViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(it)

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
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModelCopy1 = createBasicTopicSummaryViewModel(it)
        val topicSummaryViewModelCopy2 = createBasicTopicSummaryViewModel(it)
        val topicSummaryViewModelCopy3 = createBasicTopicSummaryViewModel(it)
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
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(it)
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
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)

        // Verify the non-null property of equals(): for any non-null reference a, a != null
        assertThat(topicSummaryViewModel).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_topicSummary1AndTopicSummary2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModelTopicSummary1 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5
        )
        val topicSummaryViewModelTopicSummary2 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary2,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5
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
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModelEntity1 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5
        )
        val topicSummaryViewModelEntity2 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary1,
          entityType = "entity_2",
          topicSummaryClickListener = testFragment,
          position = 5
        )

        assertThat(topicSummaryViewModelEntity1).isNotEqualTo(topicSummaryViewModelEntity2)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelEquals_position4AndPosition5_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModelPosition4 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 4
        )
        val topicSummaryViewModelPosition5 = TopicSummaryViewModel(
          activity = it,
          topicSummary = topicSummary1,
          entityType = "entity_1",
          topicSummaryClickListener = testFragment,
          position = 5
        )

        assertThat(topicSummaryViewModelPosition4).isNotEqualTo(topicSummaryViewModelPosition5)
      }
    }
  }

  @Test
  fun testTopicSummaryViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)
        val topicSummaryViewModelCopy = createBasicTopicSummaryViewModel(it)
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
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val topicSummaryViewModel = createBasicTopicSummaryViewModel(it)

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

  private fun createBasicTopicSummaryViewModel(activity: AppCompatActivity): TopicSummaryViewModel {
    return TopicSummaryViewModel(
      activity = activity,
      topicSummary = topicSummary1,
      entityType = "entity",
      topicSummaryClickListener = testFragment,
      position = 5
    )
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
