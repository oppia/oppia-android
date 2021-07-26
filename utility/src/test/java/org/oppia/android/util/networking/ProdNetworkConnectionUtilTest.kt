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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowNetworkInfo
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProdNetworkConnectionUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProdNetworkConnectionUtilTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProdNetworkConnectionUtilTest_TestApplicationComponent.builder()
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
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWifiConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeEthernetConnection_returnsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.LOCAL
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveEthernetConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_ETHERNET,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeCellularConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveCellularConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeWimaxConnection_returnsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.CELLULAR
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_nonActiveWimaxConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIMAX,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_noActiveNetworkConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  @Test
  fun testGetCurrentConnectionStatus_activeBluetoothConnection_returnsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_BLUETOOTH,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtil.getCurrentConnectionStatus()).isEqualTo(
      NetworkConnectionUtil.ConnectionStatus.NONE
    )
  }

  private fun setNetworkConnectionStatus(status: Int, networkState: NetworkInfo.State) {
    shadowOf(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
      .setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
          /* detailedState = */ null,
          /* type = */ status,
          /* subType = */ 0,
          /* isAvailable = */ true,
          /* state = */ networkState
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

    fun inject(networkConnectionUtilProdImplTest: ProdNetworkConnectionUtilTest)
  }
}
