package org.oppia.android.domain.feedbackreporting

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
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
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

  @JvmField
  @field:[Inject ReportSchemaVersion]
  var reportSchemaVersion: Int = 0

  // The latest schema version of the feedback report used. This should be updated each time the
  // feedback report format is changed.
  private val latestSchemaVersion = 1

  private val gaeFeedbackReportParameterNamesV1 = listOf(
    "schemaVersion",
    "reportSubmissionTimestampSec",
    "userSuppliedFeedback",
    "systemContext",
    "deviceContext",
    "appContext"
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSchemaVersion_providerMatchesExpectedSchemaVersion() {
    assertThat(reportSchemaVersion).isEqualTo(latestSchemaVersion)
  }

  @Test
  fun testSchemaVersion_forCurrentSchemaVersion_hasExpectedFeedbackReportMembers() {
    val gaeFeedbackReportClassMembers = GaeFeedbackReport::class.members
    val feedbackReportFields = gaeFeedbackReportClassMembers.map { it.name }
    gaeFeedbackReportParameterNamesV1.forEach {
      assertThat(feedbackReportFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_forCurrentSchemaVersion_hasExpectedUserSuppliedFeedbackMembers() {
    // For report schema V1, the information in the GaeUserSuppliedFeedback should include:
    //      a user-selected report-type
    //      a report category
    //      a list of checkbox options a user can select
    //      a short open-text input
    // SystemContext: the package version name of this Oppia instance, the package version code of
    //                this Oppia instance, the country locale code set on the user's device, and the
    //                language locale code set on the user's device.
    // DeviceContext: the user's device model, the Android SDK version on the user's device, the
    //                build fingerprint of the device, and the network type when the reports was
    //                submitted by the user.
    // ReportContext: the entry point to feedback reporting used by the learner, the text size of
    //                the  app as set in the Oppia settings, the text language code set in the
    //                Oppia settings, the audio language code set in the Oppia settings, whether
    //                the learner can download and update topics only on wifi, whether the app can
    //                can automatically update topics, whether the account sending the report is
    //                a profile admin, a list of the event logs collected in the app, and a list of
    //                the logcat logs collected in the app.

  }

  private fun setUpTestApplicationComponent() {
    DaggerReportSchemaVersionTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

  @Singleton
  @Component(
    modules = [
      TestModule::class, FeedbackReportingModule::class, TestDispatcherModule::class,
      TestLogReportingModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(reportSchemaVersionTest: ReportSchemaVersionTest)
  }
}
