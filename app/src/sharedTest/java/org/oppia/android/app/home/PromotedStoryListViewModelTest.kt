package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
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
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
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

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testPromotedStoryListViewModelEquals_reflexiveStoryListOf2_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        val isReflexive = promotedStoryListViewModel.equals(promotedStoryListViewModel)
        assertThat(isReflexive).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_symmetricStoryListOf2_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val copyPromotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        val isSymmetric = promotedStoryListViewModel.equals(copyPromotedStoryListViewModel) &&
          copyPromotedStoryListViewModel.equals(promotedStoryListViewModel)
        assertThat(isSymmetric).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_transitiveStoryListOf2_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val copy1PromotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val copy2PromotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        val isTransitive = promotedStoryListViewModel.equals(copy1PromotedStoryListViewModel) &&
          copy1PromotedStoryListViewModel.equals(copy2PromotedStoryListViewModel) &&
          copy2PromotedStoryListViewModel.equals(promotedStoryListViewModel)
        assertThat(isTransitive).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_consistentStoryListOf2_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val copyPromotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        val isConsistent = promotedStoryListViewModel.equals(copyPromotedStoryListViewModel) &&
          promotedStoryListViewModel.equals(copyPromotedStoryListViewModel)
        assertThat(isConsistent).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_storyListOf2AndNull_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        assertThat(promotedStoryListViewModel.equals(null)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelEquals_storyListOf2AndStoryListOf3_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModelOf2 = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val promotedStoryListViewModelOf3 = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory3
            )
          )
        )

        assertThat(promotedStoryListViewModelOf2.equals(promotedStoryListViewModelOf3)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelHashCode_viewModelsEqualHashCodesEqual_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        val copyPromotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )
        assertThat(promotedStoryListViewModel.equals(copyPromotedStoryListViewModel)).isTrue()

        assertThat(promotedStoryListViewModel.hashCode())
          .isEqualTo(copyPromotedStoryListViewModel.hashCode())
      }
    }
  }

  @Test
  fun testPromotedStoryListViewModelHashCode_sameViewModelHashCodeDoesNotChange_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryListViewModel = PromotedStoryListViewModel(
          it,
          listOf(
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory1
            ),
            PromotedStoryViewModel(
              /* activity = */ it,
              /* internalProfileId = */1,
              /* totalStoryCount = */3,
              /* entytType = */"",
              promotedStory2
            )
          )
        )

        val firstHash = promotedStoryListViewModel.hashCode()
        val secondHash = promotedStoryListViewModel.hashCode()
        assertThat(firstHash).isEqualTo(secondHash)
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