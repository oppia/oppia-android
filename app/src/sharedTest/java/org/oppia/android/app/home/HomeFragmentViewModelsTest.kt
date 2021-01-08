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
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.model.PromotedStory
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
import org.oppia.android.testing.TestCoroutineDispatchers
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

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

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

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private var morningClock = OppiaClock()
  private var eveningClock = OppiaClock()

  @Before
  fun setUp() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    setUpClocks()
    testCoroutineDispatchers.registerIdlingResource()
  }

  private fun setUpClocks() {
    morningClock.setCurrentTimeMs(MORNING_TIMESTAMP)
    eveningClock.setCurrentTimeMs(EVENING_TIMESTAMP)
  }

  private fun getTestFragment(activity: HomeFragmentTestActivity): HomeFragment {
    return activity.supportFragmentManager.findFragmentByTag(
      "home_fragment_test_activity"
    ) as HomeFragment
  }

  @Test
  fun testWelcomeViewModelEquals_reflexiveProfile1MorningAndProfile1Morning_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        assertThat(welcomeViewModelProfile1Morning.equals(welcomeViewModelProfile1Morning)).isTrue()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_symmetricProfile1MorningAndDifferentProfile1Morning_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val copyWelcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val isSymmetric =
          welcomeViewModelProfile1Morning.equals(copyWelcomeViewModelProfile1Morning) &&
            copyWelcomeViewModelProfile1Morning.equals(welcomeViewModelProfile1Morning)
        assertThat(isSymmetric).isTrue()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_transitiveProfile1MorningAndTwoDifferentProfile1Morning_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val copy1WelcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val copy2WelcomeVieModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val isTransitive =
          welcomeViewModelProfile1Morning.equals(copy1WelcomeViewModelProfile1Morning) &&
            copy1WelcomeViewModelProfile1Morning.equals(copy2WelcomeVieModelProfile1Morning) &&
            copy2WelcomeVieModelProfile1Morning.equals(welcomeViewModelProfile1Morning)
        assertThat(isTransitive).isTrue()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_consistentProfile1MorningAndDifferentProfile1Morning_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val copy1WelcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val isConsistent =
          welcomeViewModelProfile1Morning.equals(copy1WelcomeViewModelProfile1Morning) &&
            welcomeViewModelProfile1Morning.equals(copy1WelcomeViewModelProfile1Morning)
        assertThat(isConsistent).isTrue()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndNull_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        assertThat(welcomeViewModelProfile1Morning.equals(null)).isFalse()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile2Morning_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val welcomeViewModelProfile2Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 2"
        )
        assertThat(welcomeViewModelProfile1Morning.equals(welcomeViewModelProfile2Morning))
          .isFalse()
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile1Evening_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val welcomeViewModelProfile1Evening = WelcomeViewModel(
          fragment,
          eveningClock,
          "Profile 1"
        )
        val equal = welcomeViewModelProfile1Morning.equals(welcomeViewModelProfile1Evening)
        assertThat(equal).isFalse()
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_viewModelsEqualHashCodesEqual_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val fragment = getTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        val copyWelcomeViewModelProfile1Morning = WelcomeViewModel(
          fragment,
          morningClock,
          "Profile 1"
        )
        assertThat(welcomeViewModelProfile1Morning.equals(copyWelcomeViewModelProfile1Morning))
          .isTrue()

        assertThat(
          welcomeViewModelProfile1Morning.hashCode() ==
            copyWelcomeViewModelProfile1Morning.hashCode()
        ).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_reflexiveProfile1StoryCount3EntityTypeEmptyStory1_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        val isReflexive = promotedStoryViewModel.equals(promotedStoryViewModel)
        assertThat(isReflexive).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_symmetricProfile1StoryCount3EntityTypeEmptyStory1_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val copyPromotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        val isSymmetric = promotedStoryViewModel.equals(copyPromotedStoryViewModel) &&
          copyPromotedStoryViewModel.equals(promotedStoryViewModel)
        assertThat(isSymmetric).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_transitiveProfile1StoryCount3EntityTypeEmptyStory1_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val copy1PromotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val copy2PromotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        val isTransitive = promotedStoryViewModel.equals(copy1PromotedStoryViewModel) &&
          copy1PromotedStoryViewModel.equals(copy2PromotedStoryViewModel) &&
          copy2PromotedStoryViewModel.equals(promotedStoryViewModel)
        assertThat(isTransitive).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_consistentProfile1StoryCount3EntityTypeEmptyStory1_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val copyPromotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        val isConsistent = promotedStoryViewModel.equals(copyPromotedStoryViewModel) &&
          promotedStoryViewModel.equals(copyPromotedStoryViewModel)
        assertThat(isConsistent).isTrue()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_profile1StoryCount3EntityTypeEmptyStory1AndNull_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        assertThat(promotedStoryViewModel.equals(null)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_profileId1AndProfileId2_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelProfile1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val promotedStoryViewModelProfile2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */2,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        assertThat(promotedStoryViewModelProfile1.equals(promotedStoryViewModelProfile2)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_storyCount2AndStoryCount3_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelStoryCount2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */2,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val promotedStoryViewModelStoryCount3 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        assertThat(promotedStoryViewModelStoryCount2.equals(promotedStoryViewModelStoryCount3))
          .isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_entity1AndEntity2_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModelEntity1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"Entity 1",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val promotedStoryViewModelEntity2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"Entity 2",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )

        assertThat(promotedStoryViewModelEntity1.equals(promotedStoryViewModelEntity2)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelEquals_story1AndStory2_isFalse() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val story1 = PromotedStory.newBuilder()
          .setStoryId("id_1")
          .setStoryName("Story 1")
          .setTopicName("topic_name")
          .setTotalChapterCount(1)
          .build()
        val story2 = PromotedStory.newBuilder()
          .setStoryId("id_2")
          .setStoryName("Story 2")
          .setTopicName("topic_name")
          .setTotalChapterCount(1)
          .build()
        assertThat(story1.equals(story2)).isFalse()

        val promotedStoryViewModelStory1 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"Entity 1",
          story1
        )
        val promotedStoryViewModelStory2 = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"Entity 2",
          story2
        )

        assertThat(promotedStoryViewModelStory1.equals(promotedStoryViewModelStory2)).isFalse()
      }
    }
  }

  @Test
  fun testPromotedStoryViewModelHashCode_viewModelsEqualHashCodesEqual_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        val promotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        val copyPromotedStoryViewModel = PromotedStoryViewModel(
          /* activity = */ it,
          /* internalProfileId = */1,
          /* totalStoryCount = */3,
          /* entytType = */"",
          PromotedStory.newBuilder()
            .setStoryId("id_1")
            .setStoryName("Story 1")
            .setTopicName("topic_name")
            .setTotalChapterCount(1)
            .build()
        )
        assertThat(promotedStoryViewModel.equals(copyPromotedStoryViewModel)).isTrue()

        assertThat(promotedStoryViewModel.hashCode() == copyPromotedStoryViewModel.hashCode())
          .isTrue()
      }
    }
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
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
