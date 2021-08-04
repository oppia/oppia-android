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
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NetworkConnectionUtilDebugImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionUtilDebugImplTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var networkConnectionUtilDebugImpl: NetworkConnectionUtilDebugImpl

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var networkConnectionTestUtil: NetworkConnectionTestUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionUtilDebugImplTest_TestApplicationComponent.builder()
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
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_noForcedConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  private fun setNetworkConnectionStatus(status: Int, networkState: NetworkInfo.State) {
    networkConnectionTestUtil.setNetworkInfo(status, networkState)
  }

  // TODO(#89): Move this to a common test application component.
  /** Test specific dagger module for [NetworkConnectionUtilDebugImplTest]. */
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
  /** Test specific [ApplicationComponent] for [NetworkConnectionUtilDebugImplTest]. */
  interface TestApplicationComponent {
    /** Test specific [Component.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder {
      /** Binds [Application] to [TestApplicationComponent]. */
      @BindsInstance
      fun setApplication(application: Application): Builder

      /** Builds [TestApplicationComponent]. */
      fun build(): TestApplicationComponent
    }

    /**
     * Injects [TestApplicationComponent] to [NetworkConnectionUtilDebugImplTest] providing the required
     * dagger modules.
     */
    fun inject(networkConnectionUtilDebugImplTest: NetworkConnectionUtilDebugImplTest)
  }
}
