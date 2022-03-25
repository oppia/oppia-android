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
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingAppContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingDeviceContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingEntryPoint
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingSystemContext
import org.oppia.android.data.backends.gae.model.GaeUserSuppliedFeedback
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KCallable
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule

/**
 * Test for [ReportSchemaVersion] that validates the proper schema version is sent in feedback
 * report with the expected data.
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

  // The expected data that can be included in V1 of the report.
  private val gaeFeedbackReportParameterNamesV1 = listOf(
    "schemaVersion",
    "reportSubmissionTimestampSec",
    "userSuppliedFeedback",
    "systemContext",
    "deviceContext",
    "appContext"
  )

  private val gaeUserSuppliedFeedbackParameterNamesV1 = listOf(
    "reportType", "category", "feedbackList", "openTextUserInput"
  )

  private val gaeFeedbackReportingSystemContextParameterNamesV1 = listOf(
    "packageVersionName", "packageVersionCode", "countryLocaleCode", "languageLocaleCode"
  )

  private val gaeFeedbackReportingDeviceContextParameterNamesV1 = listOf(
    "deviceModel", "sdkVersion", "buildFingerprint", "networkType"
  )

  private val gaeFeedbackReportingAppContextParameterNamesV1 = listOf(
    "entryPoint",
    "textSize",
    "textLanguageCode",
    "audioLanguageCode",
    "downloadAndUpdateOnlyOnWifi",
    "automaticallyUpdateTopics",
    "isAdmin",
    "eventLogs",
    "logcatLogs"
  )

  private val gaeFeedbackReportingEntryPointParameterNamesV1 = listOf(
    "entryPointName", "topicId", "storyId", "explorationId", "subtopicId"
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
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedFeedbackReportMembers() {
    verifyDataFields(
      dataClassMembers = GaeFeedbackReport::class.members,
      expectedFields = gaeFeedbackReportParameterNamesV1
    )
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedUserSuppliedFeedbackMembers() {
    verifyDataFields(
      dataClassMembers = GaeUserSuppliedFeedback::class.members,
      expectedFields = gaeUserSuppliedFeedbackParameterNamesV1
    )
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedSystemContextMembers() {
    verifyDataFields(
      dataClassMembers = GaeFeedbackReportingSystemContext::class.members,
      expectedFields = gaeFeedbackReportingSystemContextParameterNamesV1
    )
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedDeviceContextMembers() {
    verifyDataFields(
      dataClassMembers = GaeFeedbackReportingDeviceContext::class.members,
      expectedFields = gaeFeedbackReportingDeviceContextParameterNamesV1
    )
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedAppContextMembers() {
    verifyDataFields(
      dataClassMembers = GaeFeedbackReportingAppContext::class.members,
      expectedFields = gaeFeedbackReportingAppContextParameterNamesV1
    )
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedEntryPointMembers() {
    verifyDataFields(
      dataClassMembers = GaeFeedbackReportingEntryPoint::class.members,
      expectedFields = gaeFeedbackReportingEntryPointParameterNamesV1
    )
  }

  private fun verifyDataFields(
    dataClassMembers: Collection<KCallable<*>>,
    expectedFields: List<String>
  ) {
    val dataClassFields = dataClassMembers.map { it.name }
    expectedFields.forEach { assertThat(dataClassFields.contains(it)).isTrue() }
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
      TestLogReportingModule::class, RobolectricModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
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
