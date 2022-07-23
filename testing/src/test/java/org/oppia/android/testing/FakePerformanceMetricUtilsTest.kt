package org.oppia.android.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

private const val TEST_APK_SIZE = Long.MAX_VALUE
private const val TEST_STORAGE_USAGE = Long.MAX_VALUE
private const val TEST_TOTAL_PSS = Long.MAX_VALUE
private const val TEST_BYTES_SENT = Long.MAX_VALUE
private const val TEST_BYTES_RECEIVED = Long.MAX_VALUE

/** Tests for [FakePerformanceMetricUtils]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakePerformanceMetricUtilsTest {

  @Inject
  lateinit var fakePerformanceMetricUtils: FakePerformanceMetricUtils

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeMetricUtils_setApkSize_returnsCorrectApkSize() {
    fakePerformanceMetricUtils.setApkSize(TEST_APK_SIZE)

    assertThat(fakePerformanceMetricUtils.getApkSize()).isEqualTo(TEST_APK_SIZE)
  }

  @Test
  fun testFakeMetricUtils_doNotSetApkSize_returnsDefaultApkSize() {
    assertThat(fakePerformanceMetricUtils.getApkSize())
      .isEqualTo(fakePerformanceMetricUtils.testApkSize)
  }

  @Test
  fun testFakeMetricUtils_setStorageUsage_returnsCorrectStorageSize() {
    fakePerformanceMetricUtils.setStorageUsage(TEST_STORAGE_USAGE)

    assertThat(fakePerformanceMetricUtils.getUsedStorage()).isEqualTo(TEST_STORAGE_USAGE)
  }

  @Test
  fun testFakeMetricUtils_doNotSetStorageUsage_returnsDefaultStorageUsage() {
    assertThat(fakePerformanceMetricUtils.getUsedStorage())
      .isEqualTo(fakePerformanceMetricUtils.testStorageUsage)
  }

  @Test
  fun testFakeMetricUtils_setTotalPss_returnsCorrectTotalPss() {
    fakePerformanceMetricUtils.setTotalPss(TEST_TOTAL_PSS)

    assertThat(fakePerformanceMetricUtils.getTotalPssUsed()).isEqualTo(TEST_TOTAL_PSS)
  }

  @Test
  fun testFakeMetricUtils_doNotSetTotalPss_returnsDefaultTotalPss() {
    assertThat(fakePerformanceMetricUtils.getTotalPssUsed())
      .isEqualTo(fakePerformanceMetricUtils.testTotalPss)
  }

  @Test
  fun testFakeMetricUtils_setTotalSentBytes_returnsCorrectTotalSentBytes() {
    fakePerformanceMetricUtils.setTotalSentBytes(TEST_BYTES_SENT)

    assertThat(fakePerformanceMetricUtils.getTotalSentBytes()).isEqualTo(TEST_BYTES_SENT)
  }

  @Test
  fun testFakeMetricUtils_doNotSetTotalSentBytes_returnsDefaultTotalSentBytes() {
    assertThat(fakePerformanceMetricUtils.getTotalSentBytes())
      .isEqualTo(fakePerformanceMetricUtils.testTotalBytesSent)
  }

  @Test
  fun testFakeMetricUtils_setTotalReceivedBytes_returnsCorrectTotalReceivedBytes() {
    fakePerformanceMetricUtils.setTotalReceivedBytes(TEST_BYTES_RECEIVED)

    assertThat(fakePerformanceMetricUtils.getTotalReceivedBytes()).isEqualTo(TEST_BYTES_RECEIVED)
  }

  @Test
  fun testFakeMetricUtils_doNotSetTotalReceivedBytes_returnsDefaultTotalReceivedBytes() {
    assertThat(fakePerformanceMetricUtils.getTotalReceivedBytes())
      .isEqualTo(fakePerformanceMetricUtils.testTotalReceivedBytes)
  }

  @Test
  fun testFakeMetricUtils_setDeviceStorageTier_returnsCorrectTotalDeviceStorageTier() {
    val testDeviceStorageTier = OppiaMetricLog.StorageTier.LOW_STORAGE
    fakePerformanceMetricUtils.setDeviceStorageTier(testDeviceStorageTier)

    assertThat(fakePerformanceMetricUtils.getDeviceStorageTier()).isEqualTo(testDeviceStorageTier)
  }

  @Test
  fun testFakeMetricUtils_doNotSetDeviceStorageTier_returnsDefaultTotalDeviceStorageTier() {
    assertThat(fakePerformanceMetricUtils.getDeviceStorageTier())
      .isEqualTo(fakePerformanceMetricUtils.testDeviceStorageTier)
  }

  @Test
  fun testFakeMetricUtils_setDeviceMemoryTier_returnsCorrectTotalDeviceMemoryTier() {
    val testDeviceMemoryTier = OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
    fakePerformanceMetricUtils.setDeviceMemoryTier(testDeviceMemoryTier)

    assertThat(fakePerformanceMetricUtils.getDeviceMemoryTier()).isEqualTo(testDeviceMemoryTier)
  }

  @Test
  fun testFakeMetricUtils_doNotSetDeviceMemoryTier_returnsDefaultTotalDeviceMemoryTier() {
    assertThat(fakePerformanceMetricUtils.getDeviceMemoryTier())
      .isEqualTo(fakePerformanceMetricUtils.testDeviceMemoryTier)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakePerformanceMetricUtilsTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(fakePerformanceMetricUtilsTest: FakePerformanceMetricUtilsTest)
  }
}