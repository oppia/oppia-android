package org.oppia.util.networking

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
import org.oppia.testing.TestDispatcherModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowNetworkInfo
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NetworkConnectionUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionUtilTest {

  private val NO_CONNECTION = -1

  @Inject lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifiConnection_returnsWifi() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, true)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifiConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIFI, false)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeEthernetConnection_returnsWifi() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_ETHERNET, true)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveEthernetConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_ETHERNET, false)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, true)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellularConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_MOBILE, false)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWimaxConnection_returnsCellular() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIMAX, true)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWimaxConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_WIMAX, false)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetworkConnection_returnsNone() {
    setNetworkConnectionStatus(NO_CONNECTION, false)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeBluetoothConnection_returnsNone() {
    setNetworkConnectionStatus(ConnectivityManager.TYPE_BLUETOOTH, true)
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
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
  @Component(modules = [TestModule::class, TestDispatcherModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(networkConnectionUtilTest: NetworkConnectionUtilTest)
  }
}
