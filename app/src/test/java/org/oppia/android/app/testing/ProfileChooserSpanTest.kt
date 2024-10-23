package org.oppia.android.app.testing

import android.app.Application
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
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
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW = "profile_recycler_view"

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * Reference document :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileChooserSpanTest.TestApplication::class)
class ProfileChooserSpanTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.runCurrent()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProfileChooserFragmentRecyclerView_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-ldpi")
  fun testProfileChooserFragmentRecyclerView_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-mdpi")
  fun testProfileChooserFragmentRecyclerView_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-hdpi")
  fun testProfileChooserFragmentRecyclerView_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xhdpi")
  fun testProfileChooserFragmentRecyclerView_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  fun testProfileChooserFragmentRecyclerView_landscape_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testProfileChooserFragmentRecyclerView_tablet_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-hdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  private fun getProfileRecyclerViewGridLayoutManager(
    activity: ProfileChooserFragmentTestActivity
  ): GridLayoutManager {
    return getProfileRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getProfileRecyclerView(activity: ProfileChooserFragmentTestActivity): RecyclerView {
    return getProfileChooserFragment(activity).view?.findViewWithTag<View>(
      TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW
    )!! as RecyclerView
  }

  private fun getProfileChooserFragment(
    activity: ProfileChooserFragmentTestActivity
  ): ProfileChooserFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ) as ProfileChooserFragment
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(profileChooserSpanTest: ProfileChooserSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileChooserSpanTest: ProfileChooserSpanTest) {
      component.inject(profileChooserSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
