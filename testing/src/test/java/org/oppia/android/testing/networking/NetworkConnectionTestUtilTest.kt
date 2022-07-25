@file:Suppress("DEPRECATION") // Deprecated Android SDK components are needed in this test suite.

package org.oppia.android.testing.networking

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Test for [NetworkConnectionTestUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionTestUtilTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var networkConnectionTestUtil: NetworkConnectionTestUtil

  lateinit var connectivityManager: ConnectivityManager

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    connectivityManager = context.getSystemService(
      Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionTestUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testSetNetworkInfo_wifiShadowNetwork_connected_networkInfoIsWifiConnected() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(connectivityManager.getActiveType()).isEqualTo(ConnectivityManager.TYPE_WIFI)
    assertThat(connectivityManager.getActiveState()).isEqualTo(NetworkInfo.State.CONNECTED)
  }

  @Test
  fun testSetNetworkInfo_wifiShadowNetwork_notConnected_networkInfoIsWifiDisconnected() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(connectivityManager.getActiveType()).isEqualTo(ConnectivityManager.TYPE_WIFI)
    assertThat(connectivityManager.getActiveState()).isEqualTo(NetworkInfo.State.DISCONNECTED)
  }

  @Test
  fun testSetNetworkInfo_cellularShadowNetwork_connected_networkInfoIsCellularConnected() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(connectivityManager.getActiveType()).isEqualTo(ConnectivityManager.TYPE_MOBILE)
    assertThat(connectivityManager.getActiveState()).isEqualTo(NetworkInfo.State.CONNECTED)
  }

  @Test
  fun testSetNetworkInfo_cellularShadowNetwork_notConnected_networkInfoIsCellularDisconnected() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(connectivityManager.getActiveType()).isEqualTo(ConnectivityManager.TYPE_MOBILE)
    assertThat(connectivityManager.getActiveState()).isEqualTo(NetworkInfo.State.DISCONNECTED)
  }

  @Test
  fun testSetNetworkInfo_noActiveShadowNetwork_networkInfoIsNoNetwork() {
    networkConnectionTestUtil.setNetworkInfo(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(connectivityManager.getActiveType()).isEqualTo(NO_CONNECTION)
    assertThat(connectivityManager.getActiveState()).isEqualTo(NetworkInfo.State.DISCONNECTED)
  }

  private fun ConnectivityManager.getActiveType() = activeNetworkInfo?.type

  private fun ConnectivityManager.getActiveState() = activeNetworkInfo?.state

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(networkConnectionTestUtilTest: NetworkConnectionTestUtilTest)
  }
}
