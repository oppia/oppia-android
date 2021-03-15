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
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
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

/** Tests for [PromotedListItemViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PromotedListItemViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class PromotedListItemViewModelTest {

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

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPromotedListItemViewModelEquals_reflexiveBasicPromotedListItemViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)

        // Verify the reflexive property of equals(): a == a.
        assertThat(promotedListItemViewModel).isEqualTo(promotedListItemViewModel)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_symmetricBasicPromotedListItemViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)
        val promotedListItemViewModelCopy = createBasicPromotedListItemViewModel(it)

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(promotedListItemViewModel).isEqualTo(promotedListItemViewModelCopy)
        assertThat(promotedListItemViewModelCopy).isEqualTo(promotedListItemViewModel)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_transitiveBasicPromotedListItemViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModelCopy1 = createBasicPromotedListItemViewModel(it)
        val promotedListItemViewModelCopy2 = createBasicPromotedListItemViewModel(it)
        val promotedListItemViewModelCopy3 = createBasicPromotedListItemViewModel(it)
        assertThat(promotedListItemViewModelCopy1).isEqualTo(promotedListItemViewModelCopy2)
        assertThat(promotedListItemViewModelCopy2).isEqualTo(promotedListItemViewModelCopy3)

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(promotedListItemViewModelCopy1).isEqualTo(promotedListItemViewModelCopy3)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_consistentBasicPromotedListItemViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)
        val promotedListItemViewModelCopy = createBasicPromotedListItemViewModel(it)
        assertThat(promotedListItemViewModel).isEqualTo(promotedListItemViewModelCopy)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(promotedListItemViewModel).isEqualTo(promotedListItemViewModelCopy)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_basicPromotedListItemViewModelAndNull_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)

        assertThat(promotedListItemViewModel).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_profileId1AndProfileId2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModelProfile1 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity",
          promotedStory = promotedStory1
        )
        val promotedListItemViewModelProfile2 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 2,
          totalStoryCount = 3,
          entityType = "entity",
          promotedStory = promotedStory1
        )

        assertThat(promotedListItemViewModelProfile1).
        isNotEqualTo(promotedListItemViewModelProfile2)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_storyCount2AndStoryCount3_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModelStoryCount2 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 2,
          entityType = "entity",
          promotedStory = promotedStory1
        )
        val promotedListItemViewModelStoryCount3 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity",
          promotedStory = promotedStory1
        )

        assertThat(promotedListItemViewModelStoryCount2)
          .isNotEqualTo(promotedListItemViewModelStoryCount3)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_entity1AndEntity2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModelEntity1 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity_1",
          promotedStory = promotedStory1
        )
        val promotedListItemViewModelEntity2 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity_2",
          promotedStory = promotedStory1
        )

        assertThat(promotedListItemViewModelEntity1).isNotEqualTo(promotedListItemViewModelEntity2)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelEquals_story1AndStory2_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        assertThat(promotedStory1.equals(promotedStory2)).isFalse()

        val promotedListItemViewModelStory1 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity",
          promotedStory = promotedStory1
        )
        val promotedListItemViewModelStory2 = PromotedListItemViewModel(
          activity = it,
          internalProfileId = 1,
          totalStoryCount = 3,
          entityType = "entity",
          promotedStory = promotedStory2
        )

        assertThat(promotedListItemViewModelStory1).isNotEqualTo(promotedListItemViewModelStory2)
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)
        val promotedListItemViewModelCopy = createBasicPromotedListItemViewModel(it)
        assertThat(promotedListItemViewModel).isEqualTo(promotedListItemViewModelCopy)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(promotedListItemViewModel.hashCode())
          .isEqualTo(promotedListItemViewModelCopy.hashCode())
      }
    }
  }

  @Test
  fun testPromotedListItemViewModelHashCode_sameViewModelHashCodeDoesNotChange_isEqual() {
    launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedListItemViewModel = createBasicPromotedListItemViewModel(it)
        val firstHash = promotedListItemViewModel.hashCode()
        val secondHash = promotedListItemViewModel.hashCode()

        // Verify that hashCode consistently returns the same value.
        assertThat(firstHash).isEqualTo(secondHash)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createBasicPromotedListItemViewModel(
    activity: AppCompatActivity
  ): PromotedListItemViewModel {
    return PromotedListItemViewModel(
      activity = activity,
      internalProfileId = 1,
      totalStoryCount = 3,
      entityType = "entity",
      promotedStory = promotedStory1
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
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
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

    fun inject(promotedListItemViewModelTest: PromotedListItemViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPromotedListItemViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(promotedListItemViewModelTest: PromotedListItemViewModelTest) {
      component.inject(promotedListItemViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
