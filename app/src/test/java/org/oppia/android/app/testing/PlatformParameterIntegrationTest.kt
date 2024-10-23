package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.base.Optional
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
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
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.RemoteAuthNetworkInterceptor
import org.oppia.android.data.backends.gae.api.PlatformParameterService
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
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorkerFactory
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.network.MockPlatformParameterService
import org.oppia.android.testing.network.RetrofitTestModule
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
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Tests to verify the working of Platform Parameter Architecture. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterIntegrationTest.TestApplication::class)
class PlatformParameterIntegrationTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var platformParameterController: PlatformParameterController

  @Inject
  lateinit var platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory

  @Inject
  lateinit var context: Context

  private val mockPlatformParameterListWithToastEnabled by lazy {
    val mockSplashScreenWelcomeMsgParam = PlatformParameter.newBuilder()
      .setName(SPLASH_SCREEN_WELCOME_MSG)
      .setBoolean(SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE)
      .build()

    listOf<PlatformParameter>(mockSplashScreenWelcomeMsgParam)
  }

  private val mockPlatformParameterListWithToastDisabled by lazy {
    val mockSplashScreenWelcomeMsgParam = PlatformParameter.newBuilder()
      .setName(SPLASH_SCREEN_WELCOME_MSG)
      .setBoolean(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
      .build()

    listOf<PlatformParameter>(mockSplashScreenWelcomeMsgParam)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(platformParameterSyncUpWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testIntegration_readEmptyDatabase_checkWelcomeMsgIsInvisibleByDefault() {
    launch(SplashTestActivity::class.java).use { scenario ->
      // Fetch the latest platform parameter from cache store after execution of work request to
      // imitate the loading process at the start of splash test activity.
      scenario.onActivity { activity ->
        activity.splashTestActivityPresenter.loadPlatformParameters()
      }
      testCoroutineDispatchers.runCurrent()

      assertThat(ShadowToast.getLatestToast()).isNull()
    }
  }

  @Test
  fun testIntegration_updateEmptyDatabase_readDatabase_checkWelcomeMsgIsVisible() {
    platformParameterController.updatePlatformParameterDatabase(
      mockPlatformParameterListWithToastEnabled
    )
    testCoroutineDispatchers.runCurrent()

    launch(SplashTestActivity::class.java).use { scenario ->
      // Fetch the latest platform parameter from cache store after execution of work request to
      // imitate the loading process at the start of splash test activity.
      scenario.onActivity { activity ->
        activity.splashTestActivityPresenter.loadPlatformParameters()
      }
      testCoroutineDispatchers.runCurrent()

      assertThat(ShadowToast.getLatestToast()).isNotNull()
      assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(SplashTestActivity.WELCOME_MSG)
    }
  }

  @Test
  fun testIntegration_executeSyncUpWorkerCorrectly_readDatabase_checkWelcomeMsgIsVisible() {
    launch(SplashTestActivity::class.java).use { scenario ->
      // Set up versionName to get correct network response from mock platform parameter service.
      setUpApplicationForVersionName(MockPlatformParameterService.appVersionForCorrectResponse)
      platformParameterController.updatePlatformParameterDatabase(
        mockPlatformParameterListWithToastDisabled
      )

      val workManager = WorkManager.getInstance(context)
      val requestId = setUpAndEnqueueSyncUpWorkerRequest(workManager)
      testCoroutineDispatchers.runCurrent()

      val workInfo = workManager.getWorkInfoById(requestId)
      // Check the work request succeeded which means the local database was updated with new values.
      assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)

      // Fetch the latest platform parameter from cache store after execution of work request to
      // imitate the loading process at the start of splash test activity.
      scenario.onActivity { activity ->
        activity.splashTestActivityPresenter.loadPlatformParameters()
      }
      testCoroutineDispatchers.runCurrent()

      // As the local database was updated correctly the app will use the server values, and the
      // server value for the splash screen welcome msg param is true.
      assertThat(ShadowToast.getLatestToast()).isNotNull()
      assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(SplashTestActivity.WELCOME_MSG)
    }
  }

  @Test
  fun testIntegration_executeSyncUpWorkerIncorrectly_readDatabase_checkWelcomeMsgIsInvisible() {
    launch(SplashTestActivity::class.java).use { scenario ->
      // Set up versionName to get incorrect network response from mock platform parameter service.
      setUpApplicationForVersionName(MockPlatformParameterService.appVersionForWrongResponse)
      platformParameterController.updatePlatformParameterDatabase(
        mockPlatformParameterListWithToastDisabled
      )

      val workManager = WorkManager.getInstance(context)
      val requestId = setUpAndEnqueueSyncUpWorkerRequest(workManager)
      testCoroutineDispatchers.runCurrent()

      val workInfo = workManager.getWorkInfoById(requestId)
      // Check the work request fails because of incorrect network response. This means that the
      // local database is not updated with new values.
      assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)

      // Fetch the latest platform parameter from cache store after execution of work request to
      // imitate the loading process at the start of splash test activity.
      scenario.onActivity { activity ->
        activity.splashTestActivityPresenter.loadPlatformParameters()
      }
      testCoroutineDispatchers.runCurrent()

      // As the local database was not updated due to work request failure the app will use default
      // values, and the default value for the splash screen welcome msg param is false.
      assertThat(ShadowToast.getLatestToast()).isNull()
    }
  }

  private fun setUpApplicationForVersionName(testAppVersionName: String) {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = testAppVersionName
    packageManager.installPackage(packageInfo)
  }

  private fun setUpAndEnqueueSyncUpWorkerRequest(workManager: WorkManager): UUID {
    val inputData = Data.Builder().putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    ).build()

    val request = OneTimeWorkRequestBuilder<PlatformParameterSyncUpWorker>()
      .setInputData(inputData)
      .build()

    // Enqueue the work request to fetch and cache the platform parameters from backend service.
    workManager.enqueue(request)
    return request.id
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestNetworkModule {

    @OppiaRetrofit
    @Provides
    @Singleton
    fun provideRetrofitInstance(
      jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
      remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor,
      @BaseUrl baseUrl: String
    ): Optional<Retrofit> {
      val client = OkHttpClient.Builder()
        .addInterceptor(jsonPrefixNetworkInterceptor)
        .addInterceptor(remoteAuthNetworkInterceptor)
        .build()

      return Optional.of(
        Retrofit.Builder()
          .baseUrl(baseUrl)
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client)
          .build()
      )
    }

    @Provides
    @NetworkApiKey
    fun provideNetworkApiKey(): String = ""

    @Provides
    fun provideMockPlatformParameterService(
      mockRetrofit: MockRetrofit
    ): Optional<PlatformParameterService> {
      val delegate = mockRetrofit.create(PlatformParameterService::class.java)
      return Optional.of(MockPlatformParameterService(delegate))
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      PlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, TestNetworkModule::class, RetrofitTestModule::class,
      NetworkConfigProdModule::class, NetworkConnectionUtilDebugModule::class,
      NetworkConnectionDebugUtilModule::class, AssetModule::class, LocaleProdModule::class,
      ActivityRecreatorTestModule::class, PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
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

    fun inject(platformParameterIntegrationTest: PlatformParameterIntegrationTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterIntegrationTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(platformParameterIntegrationTest: PlatformParameterIntegrationTest) {
      component.inject(platformParameterIntegrationTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
