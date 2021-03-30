package org.oppia.android.domain.feedbackreporting

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [ReportSchemaVersion] that validates the proper schema version is passed in feedback
 * report network requests.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ReportSchemaVersionTest {

  @Inject
  lateinit var reportSchemaVersion: ReportSchemaVersion

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSchemaVersion_providerMatchesExpectedSchemaVersion() {
    val latestSchemaVersion = 1
    assertThat(reportSchemaVersion).isEqualTo(latestSchemaVersion)
  }

  @Test
  fun testSchemaVersion_forCurrentSchemaVersion_reportCollectsExpectedData() {
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Singleton
  @Component(modules = [FeedbackReportingModule::class])
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(reportSchemaVersionTest: ReportSchemaVersionTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerReportSchemaVersionTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(reportSchemaVersionTest: ReportSchemaVersionTest) {
      component.inject(reportSchemaVersionTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
