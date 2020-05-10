package org.oppia.testing

import android.app.Application
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowSystemClock
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeSystemClock]. */
@RunWith(AndroidJUnit4::class)
class FakeSystemClockTest {

  @Inject lateinit var fakeSystemClock: FakeSystemClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetTimeMillis_fromFake_isInitiallyZero() {
    assertThat(fakeSystemClock.getTimeMillis()).isEqualTo(0)
  }

  @Test
  fun testGetTimeMillis_fromSystem_isInitiallyZero() {
    assertThat(ShadowSystemClock.currentTimeMillis()).isEqualTo(0)
  }

//  @Test
//  fun testGetTimeMillis_fromSystem_afterDelay_isStillZero() {
//    Thread.sleep(1000)
//
    // Verify that the system clock remains 0.
//    assertThat(System.currentTimeMillis()).isEqualTo(0)
//  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeSystemClockTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(fakeSystemClockTest: FakeSystemClockTest)
  }
}
