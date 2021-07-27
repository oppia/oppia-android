package org.oppia.android.util.networking

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
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import org.oppia.android.util.utility.NetworkConnectionTestUtil
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DebugNetworkConnectionUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DebugNetworkConnectionUtilTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var debugNetworkConnectionUtil: DebugNetworkConnectionUtil

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var networkConnectionTestUtil: NetworkConnectionTestUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDebugNetworkConnectionUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_noForcedConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.NONE)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifi_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifi_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifi_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_noForcedConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.NONE)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellular_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellular_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellular_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    debugNetworkConnectionUtil.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(debugNetworkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  private fun setNetworkConnectionStatus(status: Int, networkState: NetworkInfo.State) {
    networkConnectionTestUtil.setNetworkInfo(context, status, networkState)
  }

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
      TestModule::class, NetworkConnectionUtilDebugModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(networkConnectionUtilDebugImplTest: DebugNetworkConnectionUtilTest)
  }
}
