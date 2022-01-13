package org.oppia.android.app.accessibility

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeAccessibilityService]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeAccessibilityServiceTest {
  @Inject
  lateinit var accessibilityService: FakeAccessibilityService

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeAccessibilityService_initialState_isScreenReaderEnabled_isFalse() {
    assertThat(accessibilityService.isScreenReaderEnabled()).isFalse()
  }

  @Test
  fun testFakeAccessibilityService_initialState_getLatestAnnouncement_isNull() {
    assertThat(accessibilityService.getLatestAnnouncement()).isNull()
  }

  @Test
  fun testFakeAccessibilityService_setScreenReaderEnabledTrue_isTrue() {
    accessibilityService.setScreenReaderEnabled(true)
    assertThat(accessibilityService.isScreenReaderEnabled()).isTrue()
  }

  @Test
  fun testFakeAccessibilityService_setScreenReaderEnabledFalse_isFalse() {
    accessibilityService.setScreenReaderEnabled(false)
    assertThat(accessibilityService.isScreenReaderEnabled()).isFalse()
  }

  @Test
  fun testFakeAccessibilityService_announceForAccessibilityForView_latestAnnouncementIsSet() {
    accessibilityService.announceForAccessibilityForView(mock(View::class.java), "test")
    assertThat(accessibilityService.getLatestAnnouncement()).isEqualTo("test")
  }

  @Test
  fun testFakeAccessibilityService_resetLatestAnnouncement_latestAnnouncementIsNull() {
    accessibilityService.resetLatestAnnouncement()
    assertThat(accessibilityService.getLatestAnnouncement()).isNull()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeAccessibilityServiceTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Singleton
  @Component(modules = [AccessibilityTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(fakeAccessibilityServiceTest: FakeAccessibilityServiceTest)
  }
}
