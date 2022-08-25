package org.oppia.android.testing.robolectric

import android.app.Application
import android.content.Context
import android.net.TrafficStats
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OppiaShadowTrafficStats]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  manifest = Config.NONE,
  sdk = [Build.VERSION_CODES.P],
  shadows = [OppiaShadowTrafficStats::class]
)
class OppiaShadowTrafficStatsTest {

  @Inject
  lateinit var context: Context

  private val oppiaShadowTrafficStats: OppiaShadowTrafficStats
    get() =
      Shadow.extract(Shadow.newInstanceOf(TrafficStats::class.java)) as OppiaShadowTrafficStats

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    // Make sure this is reset between tests.
    OppiaShadowTrafficStats.reset()
  }

  @Test
  fun testCustomShadow_initialState_returnsDefaultValuesForTxAndRxBytes() {
    assertThat(OppiaShadowTrafficStats.getUidRxBytes(0)).isEqualTo(0L)
    assertThat(OppiaShadowTrafficStats.getUidTxBytes(0)).isEqualTo(0L)
  }

  @Test
  fun testCustomShadow_setUidTxBytes_returnsCorrectTxBytesValue() {
    oppiaShadowTrafficStats.setUidTxBytes(9)
    assertThat(OppiaShadowTrafficStats.getUidTxBytes(0)).isEqualTo(9L)
  }

  @Test
  fun testCustomShadow_setUidRxBytes_returnsCorrectTxBytesValue() {
    oppiaShadowTrafficStats.setUidRxBytes(9)
    assertThat(OppiaShadowTrafficStats.getUidRxBytes(0)).isEqualTo(9L)
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaShadowTrafficStatsTest_TestApplicationComponent.builder()
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

    fun inject(oppiaShadowTrafficStatsTest: OppiaShadowTrafficStatsTest)
  }
}
