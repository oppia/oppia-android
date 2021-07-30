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

/** Test for [NetworkConnectionTestUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionTestUtilTest {

  private val NO_CONNECTION = -1

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var networkConnectionUtilProdImpl: NetworkConnectionUtilProdImpl

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionTestUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testSetNetworkInfo_wifiShadowNetwork_connected_connectionStatusIsWifi() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtilProdImpl.getCurrentConnectionStatus()).isEqualTo(LOCAL)
  }

  @Test
  fun testSetNetworkInfo_wifiShadowNetwork_notConnected_connectionStatusIsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_WIFI,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilProdImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testSetNetworkInfo_cellularShadowNetwork_connected_connectionStatusIsCellular() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.CONNECTED
    )
    assertThat(networkConnectionUtilProdImpl.getCurrentConnectionStatus()).isEqualTo(CELLULAR)
  }

  @Test
  fun testSetNetworkInfo_cellularShadowNetwork_notConnected_connectionStatusIsNone() {
    setNetworkConnectionStatus(
      status = ConnectivityManager.TYPE_MOBILE,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilProdImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  @Test
  fun testSetNetworkInfo_noActiveShadowNetwork_connectionStatusIsNone() {
    setNetworkConnectionStatus(
      status = NO_CONNECTION,
      networkState = NetworkInfo.State.DISCONNECTED
    )
    assertThat(networkConnectionUtilProdImpl.getCurrentConnectionStatus()).isEqualTo(NONE)
  }

  private fun setNetworkConnectionStatus(status: Int, networkState: NetworkInfo.State) {
    NetworkConnectionTestUtil.setNetworkInfo(context, status, networkState)
  }

  // TODO(#89): Move this to a common test application component.
  /** Test specific dagger module for [NetworkConnectionTestUtilTest]. */
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
  /** Test specific [ApplicationComponent] for [NetworkConnectionTestUtilTest]. */
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
     * Injects [TestApplicationComponent] to [NetworkConnectionTestUtilTest] providing the required
     * dagger modules.
     */
    fun inject(networkConnectionTestUtilTest: NetworkConnectionTestUtilTest)
  }
}
