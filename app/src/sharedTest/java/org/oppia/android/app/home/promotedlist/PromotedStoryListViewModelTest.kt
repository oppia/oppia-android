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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.model.PromotedStory
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

/** Tests for [PromotedStoryListViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PromotedStoryListViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class PromotedStoryListViewModelTest {
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

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPromotedStoryListViewModelEquals_reflexiveStoryListOf2_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it, listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListViewModelCopy = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModelCopy1 = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListViewModelCopy2 = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListViewModelCopy3 = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListViewModelCopy = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModelOf2 = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListViewModelOf3 = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2, promotedStory3)
        )

        assertThat(promotedStoryListViewModelOf2).isNotEqualTo(promotedStoryListViewModelOf3)
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
        )
        val promotedStoryListCopy = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = createPromotedStoryListViewModel(
          it,
          listOf(promotedStory1, promotedStory2)
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
    return promotedStoryList.map { promotedStory ->
      PromotedStoryViewModel(
        activity = activity,
        internalProfileId = 1,
        totalStoryCount = promotedStoryList.size,
        entityType = "entity",
        promotedStory = promotedStory
      )
    }
  }

  private fun createPromotedStoryListViewModel(
    activity: AppCompatActivity,
    promotedStoryList: List<PromotedStory>
  ): PromotedStoryListViewModel {
    return PromotedStoryListViewModel(
      activity,
      createPromotedStoryViewModelList(activity, promotedStoryList)
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
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
