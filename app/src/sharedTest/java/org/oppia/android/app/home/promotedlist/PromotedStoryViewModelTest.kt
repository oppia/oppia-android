package org.oppia.android.app.home.promotedlist

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

/** Tests for [PromotedStoryViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PromotedStoryViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class PromotedStoryViewModelTest {
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

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createBasicPromotedStoryViewModel(
    activity: AppCompatActivity
  ): PromotedStoryViewModel {
     return PromotedStoryViewModel(
      activity =  activity,
      internalProfileId =  1,
      totalStoryCount =  3,
      entityType =  "entity",
      promotedStory = promotedStory1
    )
  }

  @Test
  fun testPromotedStoryViewModelEquals_reflexiveBasicPromotedStoryViewModel_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)

        // Verify the reflexive property of equals(): a == a.
        assertThat(promotedStoryViewModel).isEqualTo(promotedStoryViewModel)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_symmetricBasicPromotedStoryViewModels_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        val copyPromotedStoryViewModel = createBasicPromotedStoryViewModel(it)

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(promotedStoryViewModel).isEqualTo(copyPromotedStoryViewModel)
        assertThat(copyPromotedStoryViewModel).isEqualTo(promotedStoryViewModel)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_transitiveBasicPromotedStoryViewModels_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelCopy1 = createBasicPromotedStoryViewModel(it)
        val promotedStoryViewModelCopy2 = createBasicPromotedStoryViewModel(it)
        val promotedStoryViewModelCopy3 = createBasicPromotedStoryViewModel(it)
        assertThat(promotedStoryViewModelCopy1).isEqualTo(promotedStoryViewModelCopy2)
        assertThat(promotedStoryViewModelCopy2).isEqualTo(promotedStoryViewModelCopy3)

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(promotedStoryViewModelCopy1).isEqualTo(promotedStoryViewModelCopy3)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_consistentBasicPromotedStoryViewModels_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        val copyPromotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        assertThat(promotedStoryViewModel).isEqualTo(copyPromotedStoryViewModel)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(promotedStoryViewModel).isEqualTo(copyPromotedStoryViewModel)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_basicPromotedStoryViewModelAndNull_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)

        assertThat(promotedStoryViewModel).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_profileId1AndProfileId2_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelProfile1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity",
          promotedStory1
        )
        val promotedStoryViewModelProfile2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */2,
          /* totalStoryCount = */3,
          /* entytType = */"entity",
          promotedStory1
        )

        assertThat(promotedStoryViewModelProfile1).isNotEqualTo(promotedStoryViewModelProfile2)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_storyCount2AndStoryCount3_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelStoryCount2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */2,
          /* entytType = */"entity",
          promotedStory1
        )
        val promotedStoryViewModelStoryCount3 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity",
          promotedStory1
        )

        assertThat(promotedStoryViewModelStoryCount2).isNotEqualTo(promotedStoryViewModelStoryCount3)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_entity1AndEntity2_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelEntity1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity_1",
          promotedStory1
        )
        val promotedStoryViewModelEntity2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity_2",
          promotedStory1
        )

        assertThat(promotedStoryViewModelEntity1).isNotEqualTo(promotedStoryViewModelEntity2)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_story1AndStory2_isFalse() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        assertThat(promotedStory1.equals(promotedStory2)).isFalse()

        val promotedStoryViewModelStory1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity_1",
          promotedStory1
        )
        val promotedStoryViewModelStory2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"entity_2",
          promotedStory2
        )

        assertThat(promotedStoryViewModelStory1).isNotEqualTo(promotedStoryViewModelStory2)
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelHashCode_viewModelsEqualHashCodesEqual_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        val copyPromotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        assertThat(promotedStoryViewModel).isEqualTo(copyPromotedStoryViewModel)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(promotedStoryViewModel.hashCode())
          .isEqualTo(copyPromotedStoryViewModel.hashCode())
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelHashCode_sameViewModelHashCodeDoesNotChange_isTrue() {
    ActivityScenario.launch<HomeFragmentTestActivity>(
      HomeFragmentTestActivity.createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = createBasicPromotedStoryViewModel(it)
        val firstHash = promotedStoryViewModel.hashCode()
        val secondHash = promotedStoryViewModel.hashCode()

        // Verify that hashCode consistently returns the same value.
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

    fun inject(promotedStoryViewModelTest: PromotedStoryViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPromotedStoryViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(promotedStoryViewModelTest: PromotedStoryViewModelTest) {
      component.inject(promotedStoryViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
