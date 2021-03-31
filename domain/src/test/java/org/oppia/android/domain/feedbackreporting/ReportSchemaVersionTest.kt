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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

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

  // The expected data can included in V1 of the report.
  private val gaeFeedbackReportParameterNamesV1 = listOf(
    "schemaVersion",
    "reportSubmissionTimestampSec",
    "userSuppliedFeedback",
    "systemContext",
    "deviceContext",
    "appContext"
  )
  private val gaeUserSuppliedFeedbackParameterNamesV1 = listOf(
    "reportType", "category", "userFeedbackSelectedItems", "userFeedbackOtherTextInput"
  )
  private val gaeFeedbackReportingSystemContextParameterNamesV1 = listOf(
    "packageVersionName", "packageVersionCode", "countryLocaleCode", "languageLocaleCode"
  )
  private val gaeFeedbackReportingDeviceContextParameterNamesV1 = listOf(
    "deviceMode,", "sdkVersion", "buildFingerprint", "networkType"
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
    val gaeFeedbackReportClassMembers = GaeFeedbackReport::class.members
    val feedbackReportFields = gaeFeedbackReportClassMembers.map { it.name }
    gaeFeedbackReportParameterNamesV1.forEach {
      assertThat(feedbackReportFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedUserSuppliedFeedbackMembers() {
    val gaeUserSuppliedFeedbackClassMembers = GaeUserSuppliedFeedback::class.members
    val userSuppliedFeedbackFields = gaeUserSuppliedFeedbackClassMembers.map { it.name }

    gaeUserSuppliedFeedbackParameterNamesV1.forEach {
      assertThat(userSuppliedFeedbackFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedSystemContextMembers() {
    val gaeFeedbackReportingSystemContextClassMembers =
      GaeFeedbackReportingSystemContext::class.members
    val feedbackReportingSystemContextFields = gaeFeedbackReportingSystemContextClassMembers.map {
      it.name
    }

    gaeFeedbackReportingSystemContextParameterNamesV1.forEach {
      assertThat(feedbackReportingSystemContextFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedDeviceContextMembers() {
    val gaeFeedbackReportingDeviceContextClassMembers =
      GaeFeedbackReportingDeviceContext::class.members
    val feedbackReportingDeviceContextFields = gaeFeedbackReportingDeviceContextClassMembers.map {
      it.name
    }

    gaeFeedbackReportingDeviceContextParameterNamesV1.forEach {
      assertThat(feedbackReportingDeviceContextFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedAppContextMembers() {
    val gaeFeedbackReportingAppContextClassMembers = GaeFeedbackReportingAppContext::class.members
    val feedbackReportingAppContextFields = gaeFeedbackReportingAppContextClassMembers.map {
      it.name
    }

    gaeFeedbackReportingAppContextParameterNamesV1.forEach {
      assertThat(feedbackReportingAppContextFields.contains(it)).isTrue()
    }
  }

  @Test
  fun testSchemaVersion_currentReportSchemaVersion_hasExpectedEntryPointMembers() {
    val gaeFeedbackReportingEntryPointClassMembers = GaeFeedbackReportingEntryPoint::class.members
    val feedbackReportingEntryPointFields = gaeFeedbackReportingEntryPointClassMembers.map {
      it.name
    }

    gaeFeedbackReportingEntryPointParameterNamesV1.forEach {
      assertThat(feedbackReportingEntryPointFields.contains(it)).isTrue()
    }
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
