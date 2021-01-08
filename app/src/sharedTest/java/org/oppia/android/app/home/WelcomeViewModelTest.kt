package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

private const val TEST_FRAGMENT_TAG = "welcome_view_model_test_fragment"

/** Tests for [WelcomeViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WelcomeViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class WelcomeViewModelTest {
  @Inject
  lateinit var context: Context

  @Inject
  lateinit var morningClock: OppiaClock

  @Inject
  lateinit var eveningClock: OppiaClock

  private val testFragment by lazy { Fragment() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpTestFragment(activity: HomeFragmentTestActivity) {
    activity.supportFragmentManager.beginTransaction().add(testFragment, TEST_FRAGMENT_TAG).commitNow()
  }

  private fun setUpDifferentClockTimes() {
    morningClock = OppiaClock()
    morningClock.setCurrentTimeMs(MORNING_TIMESTAMP)
    eveningClock = OppiaClock()
    eveningClock.setCurrentTimeMs(EVENING_TIMESTAMP)
  }

  @Test
  fun testWelcomeViewModelEquals_reflexiveProfile1MorningAndProfile1Morning_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val copyWelcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val copy1WelcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val copy2WelcomeVieModelProfile1Morning = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val copy1WelcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val welcomeViewModelProfile2Morning = WelcomeViewModel(
          testFragment,
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
        setUpDifferentClockTimes()
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val welcomeViewModelProfile1Evening = WelcomeViewModel(
          testFragment,
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
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val copyWelcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        assertThat(welcomeViewModelProfile1Morning.equals(copyWelcomeViewModelProfile1Morning))
          .isTrue()

        assertThat(welcomeViewModelProfile1Morning.hashCode())
          .isEqualTo(copyWelcomeViewModelProfile1Morning.hashCode())
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_sameViewModelHashCodeDoesNotChange_isTrue() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          morningClock,
          "Profile 1"
        )
        val firstHash = welcomeViewModelProfile1Morning.hashCode()
        val secondHash = welcomeViewModelProfile1Morning.hashCode()

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

    fun inject(welcomeViewModelTest: WelcomeViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWelcomeViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(welcomeViewModelTest: WelcomeViewModelTest) {
      component.inject(welcomeViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
