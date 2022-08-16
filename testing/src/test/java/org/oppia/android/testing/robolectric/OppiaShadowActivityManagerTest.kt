package org.oppia.android.testing.robolectric

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Debug
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OppiaShadowActivityManager]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  manifest = Config.NONE,
  sdk = [Build.VERSION_CODES.P],
  shadows = [OppiaShadowActivityManager::class]
)
class OppiaShadowActivityManagerTest {
  @Inject
  lateinit var context: Context

  private val oppiaShadowActivityManager: OppiaShadowActivityManager by lazy {
    shadowOf(
      context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    ) as OppiaShadowActivityManager
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCustomShadow_initialState_debugMemoryInfo_returnsDefaultState() {
    val defaultDebugMemoryShadow = oppiaShadowActivityManager.getProcessMemoryInfo(intArrayOf(0))
    assertThat(defaultDebugMemoryShadow).isNotNull()
    assertThat(defaultDebugMemoryShadow?.size).isNotEqualTo(0)
    assertThat(defaultDebugMemoryShadow?.get(0)?.totalPss).isEqualTo(0)
  }

  @Test
  fun testCustomShadow_initialState_memoryInfo_returnsDefaultState() {
    val outInfo = ActivityManager.MemoryInfo()
    oppiaShadowActivityManager.getMemoryInfo(outInfo)
    assertThat(outInfo.totalMem).isEqualTo(0)
    assertThat(outInfo.availMem).isEqualTo(0)
    assertThat(outInfo.lowMemory).isEqualTo(false)
    assertThat(outInfo.threshold).isEqualTo(0)
  }

  @Test
  fun testCustomShadow_setDebugMemoryInfo_returnsCorrectValue() {
    val debugMemoryInfo = Debug.MemoryInfo().apply {
      this.nativePss = 2
      this.dalvikPss = 1
      this.otherPss = 3
    }
    oppiaShadowActivityManager.setProcessMemoryInfo(debugMemoryInfo)

    val returnedDebugMemoryInfo = oppiaShadowActivityManager.getProcessMemoryInfo(intArrayOf(0))
    assertThat(returnedDebugMemoryInfo?.get(0)?.nativePss).isEqualTo(2)
    assertThat(returnedDebugMemoryInfo?.get(0)?.dalvikPss).isEqualTo(1)
    assertThat(returnedDebugMemoryInfo?.get(0)?.otherPss).isEqualTo(3)
    // Since totalPss = nativePss + dalvikPss + otherPss.
    assertThat(returnedDebugMemoryInfo?.get(0)?.totalPss).isEqualTo(6)
  }

  @Test
  fun testCustomShadow_setMemoryInfo_returnsCorrectOutInfo() {
    val memoryInfo = ActivityManager.MemoryInfo().apply {
      this.availMem = 1
      this.totalMem = 2
      this.lowMemory = true
      this.threshold = 10
    }
    oppiaShadowActivityManager.setMemoryInfo(memoryInfo)

    val outInfo = ActivityManager.MemoryInfo()
    oppiaShadowActivityManager.getMemoryInfo(outInfo)
    assertThat(outInfo.totalMem).isEqualTo(2)
    assertThat(outInfo.availMem).isEqualTo(1)
    assertThat(outInfo.lowMemory).isEqualTo(true)
    assertThat(outInfo.threshold).isEqualTo(10)
  }

  @Test
  fun testCustomShadow_setMemoryInfo_setDataInOutInfo_returnsCorrectDataInOutInfo() {
    val memoryInfo = ActivityManager.MemoryInfo().apply {
      this.availMem = 1
      this.totalMem = 2
      this.lowMemory = true
      this.threshold = 10
    }
    oppiaShadowActivityManager.setMemoryInfo(memoryInfo)

    val outInfo = ActivityManager.MemoryInfo().apply {
      this.availMem = 5
      this.totalMem = 3
      this.lowMemory = false
      this.threshold = 43
    }

    oppiaShadowActivityManager.getMemoryInfo(outInfo)
    assertThat(outInfo.totalMem).isEqualTo(2)
    assertThat(outInfo.availMem).isEqualTo(1)
    assertThat(outInfo.lowMemory).isEqualTo(true)
    assertThat(outInfo.threshold).isEqualTo(10)
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaShadowActivityManagerTest_TestApplicationComponent.builder()
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
  @Component(
    modules = [
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(oppiaShadowActivityManagerTest: OppiaShadowActivityManagerTest)
  }
}
