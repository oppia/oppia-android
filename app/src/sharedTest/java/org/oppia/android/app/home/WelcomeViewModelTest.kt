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
import org.oppia.android.testing.time.FakeOppiaClock
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
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val testFragment by lazy { Fragment() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testWelcomeViewModelEquals_reflexiveBasicWelcomeViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(testFragment)

        // Verify the reflexive property of equals(): a == a.
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_symmetricBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(testFragment)
        val welcomeViewModelProfile1MorningCopy = createBasicWelcomeViewModel(testFragment)

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)
        assertThat(welcomeViewModelProfile1MorningCopy).isEqualTo(welcomeViewModelProfile1Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_transitiveBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1MorningCopy1 = createBasicWelcomeViewModel(testFragment)
        val welcomeViewModelProfile1MorningCopy2 = createBasicWelcomeViewModel(testFragment)
        val welcomeViewModelProfile1MorningCopy3 = createBasicWelcomeViewModel(testFragment)
        assertThat(welcomeViewModelProfile1MorningCopy1).isEqualTo(
          welcomeViewModelProfile1MorningCopy2
        )
        assertThat(welcomeViewModelProfile1MorningCopy2).isEqualTo(
          welcomeViewModelProfile1MorningCopy3
        )

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(welcomeViewModelProfile1MorningCopy1).isEqualTo(
          welcomeViewModelProfile1MorningCopy3
        )
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_consistentBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(testFragment)
        val welcomeViewModelProfile1MorningCopy = createBasicWelcomeViewModel(testFragment)
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_basicWelcomeViewModelAndNull_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(testFragment)

        // Verify the non-null property of equals(): for any non-null reference a, a != null
        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile2Morning_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )
        val welcomeViewModelProfile2Morning = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 2"
        )

        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(welcomeViewModelProfile2Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile1Evening_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )
        setTimeToEvening()
        val welcomeViewModelProfile1Evening = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )

        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(welcomeViewModelProfile1Evening)
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )
        val welcomeViewModelProfile1MorningCopy = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(welcomeViewModelProfile1Morning.hashCode())
          .isEqualTo(welcomeViewModelProfile1MorningCopy.hashCode())
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_sameViewModelHashCodeDoesNotChange_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use {
      it.onActivity {
        setUpTestFragment(it)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          testFragment,
          fakeOppiaClock,
          "Profile 1"
        )

        // Verify that hashCode consistently returns the same value.
        val firstHash = welcomeViewModelProfile1Morning.hashCode()
        val secondHash = welcomeViewModelProfile1Morning.hashCode()
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

  private fun setTimeToMorning() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
  }

  private fun setTimeToEvening() {
    fakeOppiaClock.setCurrentTimeMs(EVENING_TIMESTAMP)
  }

  private fun createBasicWelcomeViewModel(fragment: Fragment): WelcomeViewModel {
    setTimeToMorning()
    return WelcomeViewModel(
      fragment,
      fakeOppiaClock,
      "Profile 1"
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
