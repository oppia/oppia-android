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
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.OppiaShadowActivityManager
import org.oppia.android.testing.robolectric.OppiaShadowTrafficStats
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow.extract
import org.robolectric.shadow.api.Shadow.newInstanceOf

private const val TEST_APP_PATH = "TEST_APP_PATH"
private const val TEST_APP_PATH_CACHE = "TEST_APP_PATH_CACHE"
private const val TEST_APP_SUB_PATH = "TEST_APP_SUB_PATH"
private const val TEST_APP_SEMI_SUB_PATH = "TEST_APP_SEMI_SUB_PATH"
private const val TEST_FILE_NAME = "TEST_FILE_NAME"
private const val ONE_KILOBYTE = 1024
private const val ONE_MEGABYTE = ONE_KILOBYTE * 1024
private const val TWO_MEGABYTES = ONE_MEGABYTE * 2L
private const val TWO_AND_HALF_MEGABYTES = ONE_MEGABYTE * 2.5
private const val THREE_MEGABYTES = ONE_MEGABYTE * 3L
private const val ONE_GIGABYTE = ONE_MEGABYTE * 1024
private const val TWO_GIGABYTES = ONE_GIGABYTE * 2L
private const val THREE_GIGABYTES = ONE_GIGABYTE * 3L
private const val TEST_CURRENT_TIME = 1665790700L

/** Tests for [PerformanceMetricsAssessorImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(
  application = PerformanceMetricsAssessorImplTest.TestApplication::class,
  shadows = [OppiaShadowActivityManager::class, OppiaShadowTrafficStats::class]
)
class PerformanceMetricsAssessorImplTest {

  @Parameter var totalMemory: Long = Long.MIN_VALUE
  @Parameter var secondCpuValue: Long = 1200L
  @Parameter var secondAppTimeValue: Long = 1665790700L
  @Parameter var secondNumberOfOnlineCoresValue: Int = 2
  @Parameter var firstCpuValue: Long = 1000L
  @Parameter var firstAppTimeValue: Long = 1665790650L
  @Parameter var firstNumberOfOnlineCoresValue: Int = 6
  @Parameter var relativeCpuUsage: Double = Double.MIN_VALUE

  @Inject
  lateinit var performanceMetricsAssessorImpl: PerformanceMetricsAssessorImpl
  @Inject
  lateinit var context: Context
  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  private val shadowActivityManager by lazy {
    shadowOf(
      context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    ) as OppiaShadowActivityManager
  }

  private val shadowTrafficStats by lazy {
    extract(newInstanceOf(TrafficStats::class.java)) as OppiaShadowTrafficStats
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAssessor_createFilesWithContent_getTotalStorageUsed_retsCorrectStorageUsage() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_MEGABYTE))
    File.createTempFile(TEST_FILE_NAME, null, context.cacheDir)
      .writeBytes(ByteArray(ONE_MEGABYTE))

    assertThat(performanceMetricsAssessorImpl.getUsedStorage()).isEqualTo(ONE_MEGABYTE * 2)
  }

  @Test
  fun testAssessor_createFilesWithContent_withNesting_getTotStorageUsed_retsCorrectStorageUsage() {
    val mainFile = File(TEST_APP_PATH)
    val cacheFile = File.createTempFile(TEST_APP_PATH_CACHE, null, context.cacheDir)
    val subFile = File(mainFile, TEST_APP_SUB_PATH)
    val semiSubFile = File(subFile, TEST_APP_SEMI_SUB_PATH)

    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE).write(ByteArray(ONE_MEGABYTE))
    cacheFile.writeBytes(ByteArray(ONE_MEGABYTE))
    context.openFileOutput(subFile.name, Context.MODE_PRIVATE).write(ByteArray(ONE_MEGABYTE))
    context.openFileOutput(semiSubFile.name, Context.MODE_PRIVATE).write(ByteArray(ONE_MEGABYTE))

    assertThat(performanceMetricsAssessorImpl.getUsedStorage()).isEqualTo(ONE_MEGABYTE * 4)
  }

  @Test
  fun testAssessor_writeBytesLowerThanLowTierHigherBound_retsLowStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_KILOBYTE))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.LOW_STORAGE)
  }

  @Test
  fun testAssessor_writeBytesEqualToLowTierHigherBound_retsLowStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(ONE_MEGABYTE))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.LOW_STORAGE)
  }

  @Test
  fun testAssessor_writeBytesGreaterThanMediumTierLowerBound_retsMediumStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(TWO_AND_HALF_MEGABYTES.toInt()))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.MEDIUM_STORAGE)
  }

  @Test
  fun testAssessor_writeBytesEqualToMedTierHigherBound_retsMediumStorageUsageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(THREE_MEGABYTES.toInt()))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.MEDIUM_STORAGE)
  }

  @Test
  fun testAssessor_writeBytesGreaterThanHighTierLowerBound_retsHighStorageTier() {
    context.openFileOutput(TEST_APP_PATH, Context.MODE_PRIVATE)
      .write(ByteArray(TWO_MEGABYTES.toInt() * 2))
    assertThat(performanceMetricsAssessorImpl.getDeviceStorageTier())
      .isEqualTo(OppiaMetricLog.StorageTier.HIGH_STORAGE)
  }

  @Test
  fun testAssessor_getBytesSent_returnsCorrectAmountOfNetworkBytesSent() {
    shadowTrafficStats.setUidTxBytes(20L)

    assertThat(performanceMetricsAssessorImpl.getTotalSentBytes()).isEqualTo(20L)
  }

  @Test
  fun testAssessor_getBytesReceived_returnsCorrectAmountOfNetworkBytesReceived() {
    shadowTrafficStats.setUidRxBytes(20L)

    assertThat(performanceMetricsAssessorImpl.getTotalReceivedBytes()).isEqualTo(20L)
  }

  @Test
  fun testAssessor_setProcessMemoryInfo_setTotalPss_returnsCorrectMemoryUsage() {
    shadowActivityManager.setProcessMemoryInfo(
      Debug.MemoryInfo().apply {
        this.nativePss = 2
        this.dalvikPss = 1
        this.otherPss = 4
      }
    )

    assertThat(performanceMetricsAssessorImpl.getTotalPssUsed()).isEqualTo(7)
  }

  @Test
  fun testAssessor_removeCurrentApp_installTestApp_returnsCorrectApkSize() {
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
    Iteration(
      "zeroDoubleValue",
      "secondCpuValue=1000",
      "secondAppTimeValue=1665790700",
      "secondNumberOfOnlineCoresValue=2",
      "relativeCpuUsage=0.00"
    ),
    Iteration(
      "nonZeroDoubleValueTillTwoDecimalPoints",
      "secondCpuValue=1100",
      "secondAppTimeValue=1665790700",
      "secondNumberOfOnlineCoresValue=2",
      "relativeCpuUsage=0.40"
    ),
    Iteration(
      "nonZeroDoubleValueTillSevenDecimalPoints",
      "secondCpuValue=12100",
      "secondAppTimeValue=1869790700",
      "secondNumberOfOnlineCoresValue=8",
      "relativeCpuUsage=0.0000077"
    ),
    Iteration(
      "nonZeroDoubleValueTillElevenDecimalPoints",
      "secondCpuValue=2100",
      "secondAppTimeValue=186933790700",
      "secondNumberOfOnlineCoresValue=3",
      "relativeCpuUsage=0.00000000239"
    ),
  )
  fun testAssessor_setFirstAndSecondSnapshot_returnsCorrectRelativeCpuUsage() {
    val firstSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      firstAppTimeValue,
      firstCpuValue,
      firstNumberOfOnlineCoresValue
    )
    val secondSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      secondAppTimeValue,
      secondCpuValue,
      secondNumberOfOnlineCoresValue
    )
    val returnedRelativeCpuUsage =
      performanceMetricsAssessorImpl.getRelativeCpuUsage(firstSnapshot, secondSnapshot)
    assertThat(returnedRelativeCpuUsage).isWithin(1e-5).of(relativeCpuUsage)
  }

  @Test
  @RunParameterized(
    Iteration(
      "zeroDeltaOnlineCores",
      "firstNumberOfOnlineCoresValue=6",
      "secondNumberOfOnlineCoresValue=0"
    ),
    Iteration(
      "negativeOnlineCores",
      "firstNumberOfOnlineCoresValue=-1",
      "secondNumberOfOnlineCoresValue=6"
    )
  )
  fun testAssessor_inputInvalidOnlineCoresValues_calculateCpuUsage_returnsNull() {
    val firstSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      firstAppTimeValue,
      firstCpuValue,
      firstNumberOfOnlineCoresValue
    )
    val secondSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      secondAppTimeValue,
      secondCpuValue,
      secondNumberOfOnlineCoresValue
    )

    val relativeCpuUsage =
      performanceMetricsAssessorImpl.getRelativeCpuUsage(firstSnapshot, secondSnapshot)

    assertThat(relativeCpuUsage).isNull()
  }

  @Test
  @RunParameterized(
    Iteration("negativeDeltaCpuValue", "firstCpuValue=1000", "secondCpuValue=900"),
    Iteration("negativeCpuValue", "firstCpuValue=1000", "secondCpuValue=-900"),
    Iteration("outOfBoundsCpuValue", "firstCpuValue=1000", "secondCpuValue=9223372036854775807"),
  )
  fun testAssessor_inputInvalidCpuTimeValues_calculateCpuUsage_returnsNull() {
    val firstSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      firstAppTimeValue,
      firstCpuValue,
      firstNumberOfOnlineCoresValue
    )
    val secondSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      secondAppTimeValue,
      secondCpuValue,
      secondNumberOfOnlineCoresValue
    )

    val relativeCpuUsage =
      performanceMetricsAssessorImpl.getRelativeCpuUsage(firstSnapshot, secondSnapshot)

    assertThat(relativeCpuUsage).isNull()
  }

  @Test
  @RunParameterized(
    Iteration(
      "negativeDeltaAppTimeValue",
      "firstAppTimeValue=1665790650",
      "secondAppTimeValue=1665790050"
    ),
    Iteration(
      "negativeAppTimeValue",
      "firstAppTimeValue=1665790650",
      "secondAppTimeValue=-1665790050"
    ),
    Iteration(
      "zeroDeltaAppTimeValue",
      "firstAppTimeValue=1665790650",
      "secondAppTimeValue=1665790650"
    ),
  )
  fun testAssessor_inputInvalidAppTimeValues_calculateCpuUsage_returnsNull() {
    val firstSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      firstAppTimeValue,
      firstCpuValue,
      firstNumberOfOnlineCoresValue
    )
    val secondSnapshot = PerformanceMetricsAssessor.CpuSnapshot(
      secondAppTimeValue,
      secondCpuValue,
      secondNumberOfOnlineCoresValue
    )

    val relativeCpuUsage =
      performanceMetricsAssessorImpl.getRelativeCpuUsage(firstSnapshot, secondSnapshot)

    assertThat(relativeCpuUsage).isNull()
  }

  @Test
  fun testAssessor_getCurrentCpuSnapshot_snapshotHasCurrentAppTime() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_CURRENT_TIME)

    val currentSnapshot = performanceMetricsAssessorImpl.computeCpuSnapshotAtCurrentTime()

    assertThat(currentSnapshot.appTimeMillis).isEqualTo(TEST_CURRENT_TIME)
  }

  @Test
  @RunParameterized(
    Iteration("memoryEqualToLowerBound", "totalMemory=0"),
    Iteration("memoryInRange", "totalMemory=1147483648"),
    Iteration("memoryJustBelowUpperBound", "totalMemory=2147483647")
  )
  fun testAssessor_setTotalMemoryForLowMemoryRange_returnsCorrectLowMemoryTier() {
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = totalMemory
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsAssessorImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER)
  }

  @Test
  @RunParameterized(
    Iteration("memoryEqualToLowerBound", "totalMemory=2147483649"),
    Iteration("memoryInRange", "totalMemory=2684354560"),
    Iteration("memoryEqualToUpperBound", "totalMemory=3221225472")
  )
  fun testAssessor_setTotalMemoryForMediumMemoryRange_retsCorrectMediumMemoryTier() {
    val memoryInfo = ActivityManager.MemoryInfo()
    memoryInfo.totalMem = totalMemory
    shadowActivityManager.setMemoryInfo(memoryInfo)
    val memoryTier = performanceMetricsAssessorImpl.getDeviceMemoryTier()

    assertThat(memoryTier).isEqualTo(OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER)
  }

  @Test
  @RunParameterized(
    Iteration("memoryEqualToLowerBound", "totalMemory=3221225473"),
    Iteration("memoryInRange", "totalMemory=5221225472"),
    Iteration(
      "memoryEqualToMaxValue",
      "totalMemory=9223372036854775807"
    )
  )
  fun testAssessor_setTotalMemoryForHighMemoryRange_retsCorrectHighMemoryTier() {
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
