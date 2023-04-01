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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val ONE_MEGABYTE = 1024L * 1024L
private const val TEST_APK_SIZE = ONE_MEGABYTE * 3L
private const val TEST_STORAGE_USAGE = ONE_MEGABYTE * 7L
private const val TEST_TOTAL_PSS = ONE_MEGABYTE * 2L
private const val TEST_BYTES_SENT = ONE_MEGABYTE
private const val TEST_BYTES_RECEIVED = ONE_MEGABYTE
private const val DEFAULT_TEST_APK_SIZE = 0L
private const val DEFAULT_TEST_STORAGE_USAGE = 0L
private const val DEFAULT_TEST_TOTAL_PSS = 0L
private const val DEFAULT_TEST_BYTES_SENT = 0L
private const val DEFAULT_TEST_BYTES_RECEIVED = 0L

/** Tests for [FakePerformanceMetricAssessor]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakePerformanceMetricAssessorTest {

  @Inject
  lateinit var fakePerformanceMetricAssessor: FakePerformanceMetricAssessor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeMetricAssessor_setApkSize_returnsCorrectApkSize() {
    fakePerformanceMetricAssessor.setApkSize(TEST_APK_SIZE)

    assertThat(fakePerformanceMetricAssessor.getApkSize()).isEqualTo(TEST_APK_SIZE)
  }

  @Test
  fun testFakeMetricAssessor_getApkSize_returnsDefaultApkSize() {
    assertThat(fakePerformanceMetricAssessor.getApkSize()).isEqualTo(DEFAULT_TEST_APK_SIZE)
  }

  @Test
  fun testFakeMetricAssessor_setStorageUsage_returnsCorrectStorageSize() {
    fakePerformanceMetricAssessor.setStorageUsage(TEST_STORAGE_USAGE)

    assertThat(fakePerformanceMetricAssessor.getUsedStorage()).isEqualTo(TEST_STORAGE_USAGE)
  }

  @Test
  fun testFakeMetricAssessor_getStorageUsage_returnsDefaultStorageUsage() {
    assertThat(fakePerformanceMetricAssessor.getUsedStorage()).isEqualTo(DEFAULT_TEST_STORAGE_USAGE)
  }

  @Test
  fun testFakeMetricAssessor_setTotalPss_returnsCorrectTotalPss() {
    fakePerformanceMetricAssessor.setTotalPss(TEST_TOTAL_PSS)

    assertThat(fakePerformanceMetricAssessor.getTotalPssUsed()).isEqualTo(TEST_TOTAL_PSS)
  }

  @Test
  fun testFakeMetricAssessor_getTotalPss_returnsDefaultTotalPss() {
    assertThat(fakePerformanceMetricAssessor.getUsedStorage()).isEqualTo(DEFAULT_TEST_TOTAL_PSS)
  }

  @Test
  fun testFakeMetricAssessor_setTotalSentBytes_returnsCorrectTotalSentBytes() {
    fakePerformanceMetricAssessor.setTotalSentBytes(TEST_BYTES_SENT)

    assertThat(fakePerformanceMetricAssessor.getTotalSentBytes()).isEqualTo(TEST_BYTES_SENT)
  }

  @Test
  fun testFakeMetricAssessor_getTotalSentBytes_returnsDefaultTotalSentBytes() {
    assertThat(fakePerformanceMetricAssessor.getTotalSentBytes()).isEqualTo(DEFAULT_TEST_BYTES_SENT)
  }

  @Test
  fun testFakeMetricAssessor_setTotalReceivedBytes_returnsCorrectTotalReceivedBytes() {
    fakePerformanceMetricAssessor.setTotalReceivedBytes(TEST_BYTES_RECEIVED)

    assertThat(fakePerformanceMetricAssessor.getTotalReceivedBytes())
      .isEqualTo(TEST_BYTES_RECEIVED)
  }

  @Test
  fun testFakeMetricAssessor_getTotalReceivedBytes_returnsDefaultTotalReceivedBytes() {
    assertThat(fakePerformanceMetricAssessor.getTotalReceivedBytes())
      .isEqualTo(DEFAULT_TEST_BYTES_RECEIVED)
  }

  @Test
  fun testFakeMetricAssessor_setDeviceStorageTier_returnsCorrectTotalDeviceStorageTier() {
    val testDeviceStorageTier = OppiaMetricLog.StorageTier.LOW_STORAGE
    fakePerformanceMetricAssessor.setDeviceStorageTier(testDeviceStorageTier)

    assertThat(fakePerformanceMetricAssessor.getDeviceStorageTier())
      .isEqualTo(testDeviceStorageTier)
  }

  @Test
  fun testFakeMetricAssessor_getDeviceStorageTier_returnsDefaultTotalDeviceStorageTier() {
    val defaultTestDeviceStorageTier = OppiaMetricLog.StorageTier.MEDIUM_STORAGE

    assertThat(fakePerformanceMetricAssessor.getDeviceStorageTier()).isEqualTo(
      defaultTestDeviceStorageTier
    )
  }

  @Test
  fun testFakeMetricAssessor_setDeviceMemoryTier_returnsCorrectTotalDeviceMemoryTier() {
    val testDeviceMemoryTier = OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
    fakePerformanceMetricAssessor.setDeviceMemoryTier(testDeviceMemoryTier)

    assertThat(fakePerformanceMetricAssessor.getDeviceMemoryTier()).isEqualTo(testDeviceMemoryTier)
  }

  @Test
  fun testFakeMetricAssessor_getDeviceMemoryTier_returnsDefaultTotalDeviceMemoryTier() {
    val defaultTestDeviceMemoryTier = OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER

    assertThat(fakePerformanceMetricAssessor.getDeviceMemoryTier()).isEqualTo(
      defaultTestDeviceMemoryTier
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakePerformanceMetricAssessorTest_TestApplicationComponent.builder()
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

    fun inject(fakePerformanceMetricAssessorTest: FakePerformanceMetricAssessorTest)
  }
}
