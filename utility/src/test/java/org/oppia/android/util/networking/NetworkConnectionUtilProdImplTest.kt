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

/** Tests for [NetworkConnectionUtilProdImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionUtilProdImplTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var networkConnectionTestUtil: NetworkConnectionTestUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionUtilProdImplTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWifiConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifiConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeEthernetConnection_returnsWifi() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveEthernetConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellularConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWimaxConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWimaxConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetworkConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeBluetoothConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_BLUETOOTH,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testLocalLogName_activeWifiConnection_returnsLocal() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(LOCAL.logName).isEqualTo("Local")
  }

  @Test
  fun testCellularLogName_activeCellularConnection_returnsCellular() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(CELLULAR.logName).isEqualTo("Cellular")
  }

  @Test
  fun testNoneLogName_inactiveWifiConnection_returnsNone() {
    networkConnectionTestUtil.setNetworkInfo(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(NONE.logName).isEqualTo("None")
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
      TestModule::class, NetworkConnectionUtilProdModule::class,
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

    fun inject(networkConnectionUtilProdImplTest: NetworkConnectionUtilProdImplTest)
  }
}
