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

private const val DEFAULT_TEST_APK_SIZE = Long.MAX_VALUE
private const val DEFAULT_TEST_STORAGE_USAGE = Long.MAX_VALUE
private const val DEFAULT_TEST_TOTAL_PSS = Long.MAX_VALUE
private const val DEFAULT_TEST_BYTES_SENT = Long.MAX_VALUE
private const val DEFAULT_TEST_BYTES_RECEIVED = Long.MAX_VALUE

/** Tests for [FakePerformanceMetricAssessor]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakePerformanceMetricAssessorTest {

  @Inject
  lateinit var fakePerformanceMetricUtils: FakePerformanceMetricAssessor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeMetricUtils_setApkSize_returnsCorrectApkSize() {
    fakePerformanceMetricUtils.setApkSize(DEFAULT_TEST_APK_SIZE)

    assertThat(fakePerformanceMetricUtils.getApkSize()).isEqualTo(DEFAULT_TEST_APK_SIZE)
  }

  @Test
  fun testFakeMetricUtils_setStorageUsage_returnsCorrectStorageSize() {
    fakePerformanceMetricUtils.setStorageUsage(DEFAULT_TEST_STORAGE_USAGE)

    assertThat(fakePerformanceMetricUtils.getUsedStorage()).isEqualTo(DEFAULT_TEST_STORAGE_USAGE)
  }

  @Test
  fun testFakeMetricUtils_setTotalPss_returnsCorrectTotalPss() {
    fakePerformanceMetricUtils.setTotalPss(DEFAULT_TEST_TOTAL_PSS)

    assertThat(fakePerformanceMetricUtils.getTotalPssUsed()).isEqualTo(DEFAULT_TEST_TOTAL_PSS)
  }

  @Test
  fun testFakeMetricUtils_setTotalSentBytes_returnsCorrectTotalSentBytes() {
    fakePerformanceMetricUtils.setTotalSentBytes(DEFAULT_TEST_BYTES_SENT)

    assertThat(fakePerformanceMetricUtils.getTotalSentBytes()).isEqualTo(DEFAULT_TEST_BYTES_SENT)
  }

  @Test
  fun testFakeMetricUtils_setTotalReceivedBytes_returnsCorrectTotalReceivedBytes() {
    fakePerformanceMetricUtils.setTotalReceivedBytes(DEFAULT_TEST_BYTES_RECEIVED)

    assertThat(fakePerformanceMetricUtils.getTotalReceivedBytes()).isEqualTo(DEFAULT_TEST_BYTES_RECEIVED)
  }

  @Test
  fun testFakeMetricUtils_setDeviceStorageTier_returnsCorrectTotalDeviceStorageTier() {
    val testDeviceStorageTier = OppiaMetricLog.StorageTier.LOW_STORAGE
    fakePerformanceMetricUtils.setDeviceStorageTier(testDeviceStorageTier)

    assertThat(fakePerformanceMetricUtils.getDeviceStorageTier()).isEqualTo(testDeviceStorageTier)
  }

  @Test
  fun testFakeMetricUtils_setDeviceMemoryTier_returnsCorrectTotalDeviceMemoryTier() {
    val testDeviceMemoryTier = OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER
    fakePerformanceMetricUtils.setDeviceMemoryTier(testDeviceMemoryTier)

    assertThat(fakePerformanceMetricUtils.getDeviceMemoryTier()).isEqualTo(testDeviceMemoryTier)
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
