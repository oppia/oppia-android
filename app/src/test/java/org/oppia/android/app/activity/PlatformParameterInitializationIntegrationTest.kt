package org.oppia.android.app.activity

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
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
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.FeatureFlagBindingModule
import org.oppia.android.domain.platformparameter.FeatureFlagsMapBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetrieverProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.properties.CustomPropertyRetrieverProdModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Tests for validating that platform parameter initialization happens correctly for
 * `InjectableAppCompatActivity`s.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterInitializationIntegrationTest.TestApplication::class)
class PlatformParameterInitializationIntegrationTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var platformParameterController: PlatformParameterController

  @field:[Inject LowestSupportedApiLevel]
  lateinit var lowestSupportedApiLevelProvider: Provider<PlatformParameterValue<Int>>

  @Test
  fun testInjectPlatformParameter_withoutParameterLoading_throwsException() {
    setUpTestApplicationComponent(skipParameterLoading = true)
    waitForParametersToLoad()

    // Injecting a parameter without allowing platform parameters to load should throw an exception.
    // This acts as a sanity baseline for verification later in the test suite.
    val exception: IllegalStateException = assertThrows<IllegalStateException>() {
      lowestSupportedApiLevelProvider.get()
    }
    assertThat(exception)
      .hasMessageThat()
      .containsMatch("Attempting to access (.+?) before initialization")
  }

  @Test
  fun testInjectPlatformParameter_withParameterLoading_doesNotThrowException() {
    setUpTestApplicationComponent(skipParameterLoading = false)
    waitForParametersToLoad()

    // The parameter injection should work if loading is enabled. This demonstrates that the test's
    // machinery itself can work to validate InjectableAppCompatActivity's initialization behavior.
    lowestSupportedApiLevelProvider.get()
  }

  @Test
  fun testLaunchInjectableAppCompatActivity_withoutParameterLoading_throwsException() {
    setUpTestApplicationComponent(skipParameterLoading = true)

    // Trying to launch the activity when parameter loading is disabled will throw an exception.
    // This is a hack to try and simulate InjectableAppCompatActivity missing its extra
    // initialization logic to demonstrate that the failure still happens without it.
    val exception: IllegalStateException = assertThrows<IllegalStateException>() {
      // See testLaunchInjectableAppCompatActivity_withParameterLoading_doesNotThrowException for why
      // the parameter fetching is happening here.
      runWithLaunchedActivity { lowestSupportedApiLevelProvider.get() }
    }
    assertThat(exception)
      .hasMessageThat()
      .containsMatch("Attempting to access (.+?) before initialization")
  }

  @Test
  fun testLaunchInjectableAppCompatActivity_withParameterLoading_doesNotThrowException() {
    setUpTestApplicationComponent(skipParameterLoading = false)

    // This doesn't throw an exception because InjectableAppCompatActivity's loading logic kicks in
    // and ensures that parameters are successfully loaded prior to try to fetch things. Note that
    // the test doesn't explicitly load parameters since it's relying on the activity to do that.
    // Note also the extra parameter fetch is for redundancy. As of the time this test was written
    // there was at least 1 feature flag that would be fetched during activity initialization which
    // would trigger the failure. If that changes in the future and there are no flags along the
    // default happy path, this extra parameter injection should trigger the same failure (since a
    // regression could then be missed). It's for this reason that the parameter itself is validated
    // as failing on its own in earlier tests in this suite.
    runWithLaunchedActivity { lowestSupportedApiLevelProvider.get() }
  }

  private fun setUpTestApplicationComponent(skipParameterLoading: Boolean) {
    TestModule.skipParameterLoading = skipParameterLoading
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun waitForParametersToLoad() {
    // Ensure platform parameters are loaded (unless the test explicitly disables that). This mimics
    // how other tests are arranged.
    val loadDeferred = platformParameterController.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    check(loadDeferred.isCompleted) { "Expected parameter loading to have finished." }
  }

  private fun runWithLaunchedActivity(testBlock: ActivityScenario<TestActivity>.() -> Unit) {
    ActivityScenario.launch<TestActivity>(TestActivity.createIntent(context)).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  @Module
  class TestModule {
    companion object {
      var skipParameterLoading: Boolean? = null
    }

    @Provides
    @Singleton
    fun bindPlatformParameterController(
      impl: PlatformParameterControllerProdImpl,
      testCoroutineDispatchers: TestCoroutineDispatchers
    ): PlatformParameterController {
      return object : PlatformParameterController {
        override fun loadParametersAsync(): Deferred<Unit> {
          val skipParameterLoading = checkNotNull(skipParameterLoading) {
            "Error: The test isn't set up correctly."
          }
          // Calling code can be blocking which means the returned deferred must run immediately,
          // hence the use of the Unconfined dispatcher.
          return CoroutineScope(Dispatchers.Unconfined).async {
            if (!skipParameterLoading) {
              val loadResult = impl.loadParametersAsync()
              testCoroutineDispatchers.runCurrent()
              check(loadResult.isCompleted) { "Expected parameter loading to have finished." }
            }
            // Otherwise, do nothing to load parameters.
          }
        }

        override fun getParameterInitializationStatus(): DataProvider<Boolean> {
          return impl.getParameterInitializationStatus()
        }

        override fun downloadRemoteParameters(): DataProvider<Unit> {
          return impl.downloadRemoteParameters()
        }
      }
    }

    @Provides
    fun bindPlatformParameterConfigRetriever(
      impl: PlatformParameterConfigRetrieverProdImpl
    ): PlatformParameterConfigRetriever = impl
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      AccessibilityTestModule::class,
      ActivityIntentFactoriesModule::class,
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
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
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
      FeatureFlagsMapBindingModule::class,
      FeatureFlagBindingModule::class,
      PlatformParameterBindingModule::class,
      PlatformParameterModule.PlatformParameterProcessStateModule::class,
      PlatformParameterModule.PlatformParameterControllerProdImplModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      CustomPropertyRetrieverProdModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: PlatformParameterInitializationIntegrationTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterInitializationIntegrationTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: PlatformParameterInitializationIntegrationTest) {
      component.inject(test)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
