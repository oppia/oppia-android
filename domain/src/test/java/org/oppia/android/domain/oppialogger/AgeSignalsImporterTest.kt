package org.oppia.android.domain.oppialogger

import android.app.Application
import android.content.Context
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
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton
import com.google.android.gms.common.api.Status
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel

/** Tests for [AgeSignalsImporter]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AgeSignalsImporterTest {

  @Inject
  lateinit var ageSignalsImporter: AgeSignalsImporter

  @Inject
  lateinit var fakeAgeSignalsManager: FakeAgeSignalsManager

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    ShadowLog.reset()
  }

  @Test
  fun testImporter_success_logsSuccess() {
    val result = AgeSignalsResult.builder()
      .setUserStatus(AgeSignalsVerificationStatus.VERIFIED)
      .build()
    fakeAgeSignalsManager.setNextAgeSignalsResult(result)

    ageSignalsImporter.onCreate()
    testCoroutineDispatchers.runCurrent()

    val logs = ShadowLog.getLogs()
    val log = logs.find { it.tag == "AgeSignalsImporter" && it.msg.contains("Successfully ingested age signals") }
    assertThat(log).isNotNull()
  }

  @Test
  fun testImporter_failure_logsFailure() {
    val exception = AgeSignalsException(Status(1, "API error"))
    fakeAgeSignalsManager.setNextAgeSignalsException(exception)

    ageSignalsImporter.onCreate()
    testCoroutineDispatchers.runCurrent()

    val logs = ShadowLog.getLogs()
    val log = logs.find { it.tag == "AgeSignalsImporter" && it.msg.contains("Failed to ingest age signals") }
    assertThat(log).isNotNull()
    assertThat(log?.msg).contains("API error")
  }

  private fun setUpTestApplicationComponent() {
    DaggerAgeSignalsImporterTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideFakeAgeSignalsManager(): FakeAgeSignalsManager {
      return FakeAgeSignalsManager()
    }

    @Provides
    @Singleton
    fun provideAgeSignalsManager(fake: FakeAgeSignalsManager): AgeSignalsManager {
      return fake
    }

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

  @Singleton
  @Component(
    modules = [
      TestModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      LocaleProdModule::class,
      FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: AgeSignalsImporterTest)
  }
}
