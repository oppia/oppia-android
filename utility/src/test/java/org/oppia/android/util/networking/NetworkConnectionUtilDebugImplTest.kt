@file:Suppress("DEPRECATION") // Deprecated Android SDK components are needed in this test suite.

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
import org.oppia.android.testing.networking.NetworkConnectionTestUtil
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
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
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceNoConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_noForcedConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_forceWifiConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifi_forceCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_noForcedConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceWifiConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceNoConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_noForcedConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_forceWifiConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellular_forceCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_noForcedConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceWifiConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
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

    fun inject(networkConnectionUtilDebugImplTest: NetworkConnectionUtilDebugImplTest)
  }
}
