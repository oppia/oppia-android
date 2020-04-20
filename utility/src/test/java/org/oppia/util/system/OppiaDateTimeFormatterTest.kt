package org.oppia.util.system

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000
// Time: Formatted time for 1556094120000 timestamp
private const val MORNING_FORMATTED_TIME = "24 April 2019"
private const val MILLISECONDS = 1586774460000
private const val TIMESTAMP_IN_SECONDS = 1586774460L

/** Tests for [OppiaDateTimeFormatter]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class OppiaDateTimeFormatterTest {

  @Inject
  lateinit var oppiaDateTimeFormatter: OppiaDateTimeFormatter

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaDateTimeFormatterTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testFormatDateFromDateString_successFormatToString() {
    assertThat(
      oppiaDateTimeFormatter.formatDateFromDateString(
        OppiaDateTimeFormatter.DD_MMM_YYYY,
        MORNING_TIMESTAMP
      )
    ).isEqualTo(MORNING_FORMATTED_TIME)
  }

  @Test
  fun testCheckAndConvertTimestampToMilliseconds_successConvertedToMilliseconds() {
    assertThat(
      oppiaDateTimeFormatter.checkAndConvertTimestampToMilliseconds(
        TIMESTAMP_IN_SECONDS
      )
    ).isEqualTo(MILLISECONDS)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(oppiaTimeFormatterTest: OppiaDateTimeFormatterTest)
  }
}
