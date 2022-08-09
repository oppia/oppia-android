package org.oppia.android.util.logging.performancemetrics

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.net.TrafficStats
import android.os.Debug
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowActivityManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.testing.robolectric.OppiaShadowActivityManager
import org.robolectric.shadow.api.Shadow

private const val TEST_APP_PATH = "TEST_APP_PATH"
private const val TEST_FILE_NAME = "TEST_FILE_NAME"
private const val ONE_KILOBYTE = 1024
private const val ONE_MEGABYTE = ONE_KILOBYTE * 1024
private const val TWO_MEGABYTES = ONE_MEGABYTE * 2L
private const val THREE_MEGABYTES = ONE_MEGABYTE * 3L
private const val ONE_GIGABYTE = ONE_MEGABYTE * 1024
private const val TWO_GIGABYTES = ONE_GIGABYTE * 2L
private const val THREE_GIGABYTES = ONE_GIGABYTE * 3L

/** Tests for [PerformanceMetricsAssessorImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(
  application = PerformanceMetricsAssessorImplTest.TestApplication::class,
  shadows = [OppiaShadowActivityManager::class]
  )
class PerformanceMetricsAssessorImplTest {

  @Parameter
  var totalMemory: Long = Long.MIN_VALUE // Inited because primitives can't be lateinit.

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var performanceMetricsAssessorImpl: PerformanceMetricsAssessorImpl

  @Inject
  lateinit var context: Context

  private val activityManager by lazy {
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPerformanceMetricsUtils_getTotalStorageUsed_returnsCorrectStorageUsage() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_MEGABYTE))
    File.createTempFile(TEST_FILE_NAME, null, context.cacheDir)
      .writeBytes(ByteArray(ONE_MEGABYTE))

    assertThat(performanceMetricsAssessorImpl.getUsedStorage()).isEqualTo(ONE_MEGABYTE * 2)
  }

  @Test
  fun testPerformanceMetricsUtils_writeBytesLowerThanLowTierHigherBound_retsLowStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_KILOBYTE))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.LOW_STORAGE)
  }

  @Test
  fun testPerformanceMetricsUtils_writeBytesEqualToLowTierHigherBound_retsLowStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_MEGABYTE))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.LOW_STORAGE)
  }

  @Test
  fun testPerformanceMetricsUtils_writeBytesEqualToMedTierLowerBound_retsMediumStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(TWO_MEGABYTES.toInt()))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.MEDIUM_STORAGE)
  }

  @Test
  fun testPerformanceMetricsUtils_writeBytesEqualToMedTierHigherBound_retsMediumStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(THREE_MEGABYTES.toInt()))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.MEDIUM_STORAGE)
  }

  @Test
  fun testPerformanceMetricsUtils_writeBytesGreaterThanHighTierLowerBound_retsHighStorageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(TWO_MEGABYTES.toInt() * 2))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.HIGH_STORAGE)
  }

  @Test
  fun testPerformanceMetricsUtils_getBytesSent_returnsCorrectAmountOfNetworkBytesSent() {
    val expectedNetworkBytesSent = TrafficStats.getUidTxBytes(context.applicationInfo.uid)

    assertThat(performanceMetricsAssessorImpl.getTotalSentBytes())
      .isEqualTo(expectedNetworkBytesSent)
  }

  @Test
  fun testPerformanceMetricsUtils_getBytesReceived_returnsCorrectAmountOfNetworkBytesReceived() {
    val expectedNetworkBytesReceived = TrafficStats.getUidRxBytes(context.applicationInfo.uid)

    assertThat(performanceMetricsAssessorImpl.getTotalReceivedBytes())
      .isEqualTo(expectedNetworkBytesReceived)
  }

  @Test
  fun testAssessor_setProcessMemoryInfo_setTotalPss_returnsCorrectMemoryUsage() {
    val shadow = Shadow.extract(activityManager) as OppiaShadowActivityManager
    shadow.setProcessMemoryInfo(
      Debug.MemoryInfo().apply {
        this.nativePss = 2
        this.dalvikPss = 1
        this.otherPss = 4
      }
    )

    assertThat(performanceMetricsAssessorImpl.getTotalPssUsed()).isEqualTo(7)
  }

  @Test
  fun testPerformanceMetricsUtils_removeCurrentApp_installTestApp_returnsCorrectApkSize() {
    val shadowPackageManager = shadowOf(context.packageManager)
    File(TEST_APP_PATH).writeBytes(ByteArray(ONE_MEGABYTE))
    val applicationInfo = context.applicationInfo.apply {
      this.sourceDir = TEST_APP_PATH
    }
    shadowPackageManager.removePackage(context.packageName)
    shadowPackageManager.installPackage(
      PackageInfo().apply {
        this.packageName = context.packageName
        this.applicationInfo = applicationInfo
      }
    )
    val apkSize = performanceMetricsAssessorImpl.getApkSize()
    assertThat(apkSize).isEqualTo(ONE_MEGABYTE)
  }

  @Test
  @RunParameterized(
    OppiaParameterizedTestRunner.Iteration("memoryEqualToLowerBound", "totalMemory=0"),
    OppiaParameterizedTestRunner.Iteration("memoryInRange", "totalMemory=1147483648"),
    OppiaParameterizedTestRunner.Iteration("memoryJustBelowUpperBound", "totalMemory=2147483647")
  )
  fun testPerformanceMetricsUtils_setTotalMemoryForLowMemoryRange_returnsCorrectLowMemoryTier() {
    val shadowActivityManager: ShadowActivityManager = shadowOf(activityManager)
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = totalMemory
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsAssessorImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER)
  }

  @Test
  @RunParameterized(
    OppiaParameterizedTestRunner.Iteration("memoryEqualToLowerBound", "totalMemory=2147483648"),
    OppiaParameterizedTestRunner.Iteration("memoryInRange", "totalMemory=2684354560"),
    OppiaParameterizedTestRunner.Iteration("memoryEqualToUpperBound", "totalMemory=3221225472")
  )
  fun testPerformanceMetricsUtils_setTotalMemoryForMediumMemoryRange_retsCorrectMediumMemoryTier() {
    val shadowActivityManager: ShadowActivityManager = shadowOf(activityManager)
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = totalMemory
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsAssessorImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER)
  }

  @Test
  @RunParameterized(
    OppiaParameterizedTestRunner.Iteration("memoryEqualToLowerBound", "totalMemory=3221225473"),
    OppiaParameterizedTestRunner.Iteration("memoryInRange", "totalMemory=5221225472"),
    OppiaParameterizedTestRunner.Iteration(
      "memoryEqualToMaxValue",
      "totalMemory=9223372036854775807"
    )
  )
  fun testPerformanceMetricsUtils_setTotalMemoryForHighMemoryRange_retsCorrectHighMemoryTier() {
    val shadowActivityManager: ShadowActivityManager = shadowOf(activityManager)
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = totalMemory
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsAssessorImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {

    @Provides
    @Singleton
    fun providesContext(application: Application): Context = application

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  class TestPlatformParameterModule {

    companion object {
      var forceLearnerAnalyticsStudy: Boolean = false
    }

    @Provides
    @SplashScreenWelcomeMsg
    fun provideSplashScreenWelcomeMsgParam(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
    }

    @Provides
    @SyncUpWorkerTimePeriodHours
    fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
      )
    }

    @Provides
    @EnableLanguageSelectionUi
    fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(
        ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      )
    }

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(forceLearnerAnalyticsStudy)
    }
  }

  @Module
  class TestPerformanceMetricsAssessorModule {
    @Provides
    fun providePerformanceMetricsAssessor(
      performanceMetricsAssessorImpl: PerformanceMetricsAssessorImpl
    ): PerformanceMetricsAssessor = performanceMetricsAssessorImpl

    @Provides
    @LowStorageTierUpperBound
    fun provideLowStorageTierUpperBound(): Long = TWO_MEGABYTES

    @Provides
    @MediumStorageTierUpperBound
    fun provideMediumStorageTierUpperBound(): Long = THREE_MEGABYTES

    @Provides
    @LowMemoryTierUpperBound
    fun provideLowMemoryTierUpperBound(): Long = TWO_GIGABYTES

    @Provides
    @MediumMemoryTierUpperBound
    fun provideMediumMemoryTierUpperBound(): Long = THREE_GIGABYTES
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, SyncStatusModule::class,
      TestPerformanceMetricsAssessorModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(performanceMetricsUtilsTest: PerformanceMetricsAssessorImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsAssessorImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(performanceMetricsUtilsTest: PerformanceMetricsAssessorImplTest) {
      component.inject(performanceMetricsUtilsTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
