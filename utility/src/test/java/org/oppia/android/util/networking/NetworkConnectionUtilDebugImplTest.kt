package org.oppia.android.util.networking

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowNetworkInfo
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
  fun testGetCurrentConnectionStatus_activeWifiConnection_noForcedConnection_returnsWifi() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, true)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifi_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifiConnection_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifi_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifi_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellularConnection_noForcedConnection_returnsCellular() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, true)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellular_forceNoConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.NONE)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellularConnection_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellular_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellular_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetworkConnection_noForcedConnection_returnsNone() {
    setNetworkConnectionStatus(NO_CONNECTION, false)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceWifiConnection_returnsNone() {
    setNetworkConnectionStatus(NO_CONNECTION, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.LOCAL)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetwork_forceCellularConnection_returnsNone() {
    setNetworkConnectionStatus(NO_CONNECTION, false)
    networkConnectionUtilDebugImpl.setCurrentConnectionStatus(ConnectionStatus.CELLULAR)
    assertThat(networkConnectionUtilDebugImpl.getCurrentConnectionStatus()).isEqualTo(
      ConnectionStatus.NONE
    )
  }

  private fun setNetworkConnectionStatus(status: Int, isConnected: Boolean) {
    shadowOf(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
      .setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
          null, status, 0, /* isAvailable= */ true, isConnected
        )
      )
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
