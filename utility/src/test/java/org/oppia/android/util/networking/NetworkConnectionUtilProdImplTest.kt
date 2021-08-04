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
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWifiConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeEthernetConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveEthernetConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveCellularConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWimaxConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testGetCurrentConnectionStatus_inactiveWimaxConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetworkConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testGetCurrentConnectionStatus_activeBluetoothConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_BLUETOOTH,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  private fun setNetworkConnectionStatus(status: Int, networkState: NetworkInfo.State) {
    networkConnectionTestUtil.setNetworkInfo(status, networkState)
  }

  // TODO(#89): Move this to a common test application component.
  /** Test specific dagger module for [NetworkConnectionUtilProdImplTest]. */
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
  /** Test specific [ApplicationComponent] for [NetworkConnectionUtilProdImplTest]. */
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
     * Injects [TestApplicationComponent] to [NetworkConnectionUtilProdImplTest] providing the required
     * dagger modules.
     */
    fun inject(networkConnectionUtilProdImplTest: NetworkConnectionUtilProdImplTest)
  }
}
