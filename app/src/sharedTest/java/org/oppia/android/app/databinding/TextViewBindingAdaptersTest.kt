package org.oppia.android.app.databinding

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
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
import org.oppia.android.app.databinding.TextViewBindingAdapters.setDrawableEndCompat
import org.oppia.android.app.databinding.TextViewBindingAdapters.setProfileDataText
import org.oppia.android.app.databinding.TextViewBindingAdapters.setProfileLastVisitedText
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.TextViewBindingAdaptersTestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time: Wed Apr 24 2019 08:22:00
private const val TIMESTAMP = 1556094120000

/** Tests for [TextViewBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TextViewBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TextViewBindingAdaptersTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testTextViewBindingAdapters_profileDataTextIsCorrect() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileDataText(textView, TIMESTAMP)
        assertThat(textView.text.toString()).isEqualTo("Created on April 24, 2019")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeLessThenAMinute_setsLastUsedJustNowText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs)
        assertThat(textView.text.toString()).isEqualTo("Last used just now")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeAMinuteAgo_setsLastUsedAMinuteAgoText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 60000)
        assertThat(textView.text.toString()).isEqualTo("Last used a minute ago")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeTwoMinutesAgo_setsLastUsedTwoMinutesAgoText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 120000)
        assertThat(textView.text.toString()).isEqualTo("Last used 2 minutes ago")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeAnHourAgo_setsLastUsedAnHourAgoText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 3600000)
        assertThat(textView.text.toString()).isEqualTo("Last used an hour ago")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeTwoHoursAgo_setsLastUsedTwoHoursAgoText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 7200000)
        assertThat(textView.text.toString()).isEqualTo("Last used 2 hours ago")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeADayAgo_setsLastUsedYesterdayText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 86400000)
        assertThat(textView.text.toString()).isEqualTo("Last used yesterday")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeTwoDaysAgo_setsLastUsedTwoDaysAgoText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs - 172800000)
        assertThat(textView.text.toString()).isEqualTo("Last used 2 days ago")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeInFuture_setsLastUsedRecentlyText() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      val getCurrentTimeMs = fakeOppiaClock.getCurrentTimeMs()
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(textView, getCurrentTimeMs + 172800000)
        assertThat(textView.text.toString()).isEqualTo("Last used recently")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forTimeZero_setsLastUsedRecently() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(
          textView,
          /* timestamp = */ 0
        )
        assertThat(textView.text.toString()).isEqualTo("Last used recently")
      }
    }
  }

  @Test
  fun testSetProfileLastVisitedText_forNegativeTime_setsLastUsedRecently() {
    runWithLaunchedActivity {
      fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
      fakeOppiaClock.setCurrentTimeMs(TIMESTAMP)
      onActivity {
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setProfileLastVisitedText(
          textView,
          /* timestamp = */ -1
        )
        assertThat(textView.text.toString()).isEqualTo("Last used recently")
      }
    }
  }

  @Test
  fun testTextViewBindingAdapters_drawableEndCompactIsCorrect() {
    runWithLaunchedActivity {
      onActivity {
        val drawable = ContextCompat.getDrawable(
          it,
          R.drawable.ic_add_profile
        )
        val textView: TextView = it.findViewById(R.id.test_text_view)
        setDrawableEndCompat(
          textView,
          /* drawable = */ drawable
        )
        assertThat(textView.compoundDrawablesRelative[2]).isEqualTo(drawable)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun runWithLaunchedActivity(
    testBlock: ActivityScenario<TextViewBindingAdaptersTestActivity>.() -> Unit
  ) {
    val intent = Intent(context, TextViewBindingAdaptersTestActivity::class.java)
    ActivityScenario.launch<TextViewBindingAdaptersTestActivity>(intent).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterTestModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestImageLoaderModule::class,
      TestLogReportingModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(textViewBindingAdaptersTest: TextViewBindingAdaptersTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTextViewBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(textViewBindingAdaptersTest: TextViewBindingAdaptersTest) {
      component.inject(textViewBindingAdaptersTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
