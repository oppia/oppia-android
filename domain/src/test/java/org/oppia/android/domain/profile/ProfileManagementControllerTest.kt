package org.oppia.android.domain.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AudioLanguage.ARABIC_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.ENGLISH_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.HINDI_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileDatabase
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.ReadingTextSize.MEDIUM_TEXT_SIZE
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_2
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileManagementControllerTest]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileManagementControllerTest.TestApplication::class)
class ProfileManagementControllerTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale
  @field:[BackgroundDispatcher Inject] lateinit var backgroundDispatcher: CoroutineDispatcher
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var loggingIdentifierController: LoggingIdentifierController
  @Inject lateinit var oppiaClock: FakeOppiaClock

  private companion object {
    private val PROFILES_LIST = listOf<Profile>(
      Profile.newBuilder().setName("James").setPin("123").setAllowDownloadAccess(true).build(),
      Profile.newBuilder().setName("Sean").setPin("234").setAllowDownloadAccess(false).build(),
      Profile.newBuilder().setName("Ben").setPin("345").setAllowDownloadAccess(true).build(),
      Profile.newBuilder().setName("Rajat").setPin("456").setAllowDownloadAccess(false).build(),
      Profile.newBuilder().setName("Veena").setPin("567").setAllowDownloadAccess(true).build()
    )

    private val ADMIN_PROFILE_ID_0 = ProfileId.newBuilder().setInternalId(0).build()
    private val PROFILE_ID_0 = ProfileId.newBuilder().setInternalId(0).build()
    private val PROFILE_ID_1 = ProfileId.newBuilder().setInternalId(1).build()
    private val PROFILE_ID_2 = ProfileId.newBuilder().setInternalId(2).build()
    private val PROFILE_ID_3 = ProfileId.newBuilder().setInternalId(3).build()
    private val PROFILE_ID_4 = ProfileId.newBuilder().setInternalId(4).build()
    private val PROFILE_ID_6 = ProfileId.newBuilder().setInternalId(6).build()

    private const val DEFAULT_PIN = "12345"
    private const val DEFAULT_ALLOW_DOWNLOAD_ACCESS = true
    private const val DEFAULT_ALLOW_IN_LESSON_QUICK_LANGUAGE_SWITCHING = false
    private const val DEFAULT_AVATAR_COLOR_RGB = -10710042
    private const val DEFAULT_SURVEY_LAST_SHOWN_TIMESTAMP_MILLIS = 0L
    private const val CURRENT_TIMESTAMP = 1556094120000
  }

  @After
  fun tearDown() {
    TestModule.enableLearnerStudyAnalytics = false
  }

  @Test
  fun testAddProfile_addProfile_checkProfileIsAdded() {
    setUpTestApplicationComponent()
    val dataProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val profileDatabase = readProfileDatabase()
    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
    assertThat(profile.numberOfLogins).isEqualTo(0)
    assertThat(profile.isContinueButtonAnimationSeen).isEqualTo(false)
    assertThat(File(getAbsoluteDirPath("0")).isDirectory).isTrue()
    assertThat(profile.surveyLastShownTimestampMs).isEqualTo(0L)
    assertThat(profile.lastSelectedClassroomId).isEmpty()
  }

  @Test
  fun testAddProfile_addProfile_studyOff_checkProfileDoesNotIncludeLearnerId() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val dataProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    // The learner ID should not be generated if there's no ongoing study.
    val profileDatabase = readProfileDatabase()
    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.learnerId).isEmpty()
  }

  @Test
  fun testAddProfile_addProfile_studyOn_checkProfileDoesNotIncludeLearnerId() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val dataProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val profileDatabase = readProfileDatabase()
    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.learnerId).isEqualTo("26504347")
  }

  @Test
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val dataProvider = addAdminProfile(name = "JAMES", pin = "321")

    val failure = monitorFactory.waitForNextFailureResult(dataProvider)
    assertThat(failure).hasMessageThat().contains("JAMES is not unique to other profiles")
  }

  @Test
  fun testAddProfile_addProfileWithNumberInName_checkResultIsFailure() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val dataProvider = addAdminProfile(name = "James034", pin = "321")

    val failure = monitorFactory.waitForNextFailureResult(dataProvider)
    assertThat(failure).hasMessageThat().contains("James034 does not contain only letters")
  }

  @Test
  fun testGetProfile_addManyProfiles_checkGetProfileIsCorrect() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val dataProvider = profileManagementController.getProfile(PROFILE_ID_3)

    val profile = monitorFactory.waitForNextSuccessfulResult(dataProvider)
    assertThat(profile.name).isEqualTo("Rajat")
    assertThat(profile.pin).isEqualTo("456")
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
    assertThat(profile.id.internalId).isEqualTo(3)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
  }

  @Test
  fun testGetProfiles_addManyProfiles_checkAllProfilesAreAdded() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val dataProvider = profileManagementController.getProfiles()

    val profiles = monitorFactory.waitForNextSuccessfulResult(dataProvider).sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() {
    setUpTestApplicationComponent()
    addTestProfiles()

    setUpTestApplicationComponent()
    addNonAdminProfileAndWait(name = "Nikita", pin = "678", allowDownloadAccess = false)
    val dataProvider = profileManagementController.getProfiles()

    val profiles = monitorFactory.waitForNextSuccessfulResult(dataProvider).sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size + 1)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testUpdateLearnerId_addProfiles_updateLearnerIdWithSeed_withoutStudy_learnerIdIsUnchanged() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    val updateProvider = profileManagementController.initializeLearnerId(profileId)
    monitorFactory.ensureDataProviderExecutes(updateProvider)
    val profileProvider = profileManagementController.getProfile(profileId)

    // The learner ID shouldn't be updated if there's no ongoing study.
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.learnerId).isEmpty()
  }

  @Test
  fun testUpdateLearnerId_addProfiles_updateLearnerIdWithSeed_withStudy_learnerIdIsUnchanged() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    val updateProvider = profileManagementController.initializeLearnerId(profileId)
    monitorFactory.ensureDataProviderExecutes(updateProvider)
    val profileProvider = profileManagementController.getProfile(profileId)

    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.learnerId).isEqualTo("a625db55")
  }

  @Test
  fun testGetCurrentProfileId_noProfileLoggedIn_returnsNull() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()

    val currentProfileId = profileManagementController.getCurrentProfileId()

    // If no profile is logged in, then no current ID can be reported.
    assertThat(currentProfileId).isNull()
  }

  @Test
  fun testGetCurrentProfileId_withProfileLoggedIn_returnsLoggedInProfileId() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val currentProfileId = profileManagementController.getCurrentProfileId()

    // The reported current profile ID should be the one that was logged into most recently.
    assertThat(currentProfileId).isEqualTo(PROFILE_ID_1)
  }

  @Test
  fun testGetCurrentProfileId_withProfileLoggedIn_thenAnother_returnsLatestLoggedInProfileId() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_2)
    )

    val currentProfileId = profileManagementController.getCurrentProfileId()

    // The reported current profile ID should be the one that was logged into most recently.
    assertThat(currentProfileId).isEqualTo(PROFILE_ID_2)
  }

  @Test
  fun testFetchCurrentLearnerId_noLoggedInProfile_returnsNull() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val learnerId = fetchSuccessfulAsyncValue(profileManagementController::fetchCurrentLearnerId)

    assertThat(learnerId).isNull()
  }

  @Test
  fun testFetchContinueButtonAnimationStatus_logInProfile1_checkStatusForProfile2IsFalse() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val continueButtonSeenStatus = fetchSuccessfulAsyncValue(
      profileManagementController::fetchContinueAnimationSeenStatus,
      PROFILE_ID_2
    )
    assertThat(continueButtonSeenStatus).isFalse()
  }

  @Test
  fun testFetchContinueButtonAnimationStatus_realProfile_notSeen_returnsFalse() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val continueButtonSeenStatus = fetchSuccessfulAsyncValue(
      profileManagementController::fetchContinueAnimationSeenStatus, PROFILE_ID_1
    )
    assertThat(continueButtonSeenStatus).isFalse()
  }

  @Test
  fun testFetchContinueButtonAnimationStatus_realProfile_markedAsSeen_returnsTrue() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    fetchSuccessfulAsyncValue(
      profileManagementController::markContinueButtonAnimationSeen,
      PROFILE_ID_1
    )

    val continueButtonSeenStatus = fetchSuccessfulAsyncValue(
      profileManagementController::fetchContinueAnimationSeenStatus,
      PROFILE_ID_1
    )
    assertThat(continueButtonSeenStatus).isTrue()
  }

  @Test
  fun testFetchContinueButtonAnimationStatus_realProfile_markedAsSeenTwice_returnsTrue() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    fetchSuccessfulAsyncValue(
      profileManagementController::markContinueButtonAnimationSeen,
      PROFILE_ID_1
    )
    fetchSuccessfulAsyncValue(
      profileManagementController::markContinueButtonAnimationSeen,
      PROFILE_ID_1
    )

    val continueButtonSeenStatus = fetchSuccessfulAsyncValue(
      profileManagementController::fetchContinueAnimationSeenStatus,
      PROFILE_ID_1
    )
    assertThat(continueButtonSeenStatus).isTrue()
  }

  @Test
  fun testFetchContinueButtonAnimationStatus_realProfile_markedAsSeen_inDiffProfile_returnsFalse() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    fetchSuccessfulAsyncValue(
      profileManagementController::markContinueButtonAnimationSeen,
      PROFILE_ID_1
    )

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_2)
    )

    val continueButtonSeenStatus = fetchSuccessfulAsyncValue(
      profileManagementController::fetchContinueAnimationSeenStatus,
      PROFILE_ID_2
    )
    assertThat(continueButtonSeenStatus).isFalse()
  }

  @Test
  fun testFetchCurrentLearnerId_loggedInProfile_createdWithStudyOff_returnsEmptyString() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val learnerId = fetchSuccessfulAsyncValue(profileManagementController::fetchCurrentLearnerId)

    assertThat(learnerId).isEmpty()
  }

  @Test
  fun testFetchCurrentLearnerId_loggedInProfile_createdWithStudyOn_returnsEmptyString() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val learnerId = fetchSuccessfulAsyncValue(profileManagementController::fetchCurrentLearnerId)

    assertThat(learnerId).isEqualTo("02308fa0")
  }

  @Test
  fun testFetchLearnerId_nonExistentProfile_returnsNull() {
    setUpTestApplicationComponent()

    val learnerId = fetchSuccessfulAsyncValue {
      profileManagementController.fetchLearnerId(PROFILE_ID_2)
    }

    assertThat(learnerId).isNull()
  }

  @Test
  fun testFetchLearnerId_createdProfileWithStudyOff_returnsEmptyString() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    addTestProfiles()

    val learnerId = fetchSuccessfulAsyncValue {
      profileManagementController.fetchLearnerId(PROFILE_ID_2)
    }

    assertThat(learnerId).isEmpty()
  }

  @Test
  fun testFetchLearnerId_createdProfileWithStudyOn_returnsEmptyString() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()

    val learnerId = fetchSuccessfulAsyncValue {
      profileManagementController.fetchLearnerId(PROFILE_ID_2)
    }

    assertThat(learnerId).isEqualTo("a625db55")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithUniqueName_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_2, "John")
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.name).isEqualTo("John")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkUpdatedFailed() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_2, "James")

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("James is not unique to other profiles")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_6, "John")

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateName_addProfiles_updateProfileAvatar_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController
      .updateProfileAvatar(
        PROFILE_ID_2,
        /* avatarImagePath = */ null,
        colorRgb = DEFAULT_AVATAR_COLOR_RGB
      )
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.avatar.avatarColorRgb).isEqualTo(DEFAULT_AVATAR_COLOR_RGB)
  }

  @Test
  fun testUpdatePin_addProfiles_updatePin_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updatePin(PROFILE_ID_2, "321")
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.pin).isEqualTo("321")
  }

  @Test
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkUpdateFailed() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updatePin(PROFILE_ID_6, "321")
    testCoroutineDispatchers.runCurrent()

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateDownloadAccess_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateAllowDownloadAccess(PROFILE_ID_2, false)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateAllowDownloadAccess(PROFILE_ID_6, false)
    testCoroutineDispatchers.runCurrent()

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_badProfileId_updateFails() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_6, allowInLessonQuickLanguageSwitching = true
    )

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_noPerm_enablePermission_updateSucceeds() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = false)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = true
    )
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_noPerm_enablePermission_profileCanNowSwitchLangs() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = false)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = true
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    // The permission has been enabled.
    val profile = retrieveProfile(PROFILE_ID_0)
    assertThat(profile.allowInLessonQuickLanguageSwitching).isTrue()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_noPerm_disablePermission_updateSucceeds() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = false)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = false
    )
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_noPerm_disablePermission_profileCannotSwitchLangs() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = false)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = false
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val profile = retrieveProfile(PROFILE_ID_0)
    assertThat(profile.allowInLessonQuickLanguageSwitching).isFalse()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_withPerm_enablePermission_updateSucceeds() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = true)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = true
    )
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_withPerm_enablePermission_profileCanSwitchLangs() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = true)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = true
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val profile = retrieveProfile(PROFILE_ID_0)
    assertThat(profile.allowInLessonQuickLanguageSwitching).isTrue()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_withPerm_disablePermission_updateSucceeds() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = true)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = false
    )
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateEnableInLessonLangSwitching_withPerm_disablePerm_profileCannotNowSwitchLangs() {
    setUpTestApplicationComponent()
    addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching = true)

    val updateProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = PROFILE_ID_0, allowInLessonQuickLanguageSwitching = false
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    // The permission has been disabled.
    val profile = retrieveProfile(PROFILE_ID_0)
    assertThat(profile.allowInLessonQuickLanguageSwitching).isFalse()
  }

  @Test
  fun testUpdateReadingTextSize_addProfiles_updateWithFontSize18_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateReadingTextSize(PROFILE_ID_2, MEDIUM_TEXT_SIZE)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testGetAudioLanguage_initialProfileCreation_defaultsToEnglish() {
    setUpTestApplicationComponent()

    addTestProfiles()

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(ENGLISH_AUDIO_LANGUAGE)
  }

  @Test
  fun testUpdateAudioLanguage_updateToHindi_updateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, HINDI_AUDIO_LANGUAGE)
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateAudioLanguage_updateToBrazilianPortuguese_updateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, BRAZILIAN_PORTUGUESE_LANGUAGE)
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateAudioLanguage_updateToArabic_updateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, ARABIC_LANGUAGE)
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateAudioLanguage_updateToNigerianPidgin_updateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, NIGERIAN_PIDGIN_LANGUAGE)
    val monitor = monitorFactory.createMonitor(updateProvider)
    testCoroutineDispatchers.runCurrent()

    monitor.ensureNextResultIsSuccess()
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateToHindi_updateChangesAudioLanguage() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, HINDI_AUDIO_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(HINDI_AUDIO_LANGUAGE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateToBrazilianPortuguese_updateChangesAudioLanguage() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, BRAZILIAN_PORTUGUESE_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(BRAZILIAN_PORTUGUESE_LANGUAGE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateToArabic_updateChangesAudioLanguage() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, ARABIC_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(ARABIC_LANGUAGE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateToNigerianPidgin_updateChangesAudioLanguage() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, NIGERIAN_PIDGIN_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(NIGERIAN_PIDGIN_LANGUAGE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateToArabicThenEnglish_updateChangesAudioLanguageToEnglish() {
    setUpTestApplicationComponent()
    addTestProfiles()
    val updateProvider1 =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, NIGERIAN_PIDGIN_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider1)

    val updateProvider2 =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, ENGLISH_AUDIO_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider2)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(ENGLISH_AUDIO_LANGUAGE)
  }

  // Requires language configurations.
  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testUpdateAudioLanguage_updateProfile1ToArabic_profile2IsUnchanged() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_1, ARABIC_LANGUAGE)
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val audioLanguageProvider = profileManagementController.getAudioLanguage(PROFILE_ID_2)
    val audioLanguage = monitorFactory.waitForNextSuccessfulResult(audioLanguageProvider)
    assertThat(audioLanguage).isEqualTo(ENGLISH_AUDIO_LANGUAGE)
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfile_checkDeletionIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val deleteProvider = profileManagementController.deleteProfile(PROFILE_ID_2)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(deleteProvider)
    monitorFactory.waitForNextFailureResult(profileProvider)
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() {
    setUpTestApplicationComponent()
    addTestProfiles()

    profileManagementController.deleteProfile(PROFILE_ID_3).ensureExecutes()
    profileManagementController.deleteProfile(PROFILE_ID_4).ensureExecutes()
    addAdminProfileAndWait(name = "John", pin = "321")

    val profilesProvider = profileManagementController.getProfiles()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider).sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(4)
    assertThat(profiles[profiles.size - 2].name).isEqualTo("Ben")
    assertThat(profiles.last().name).isEqualTo("John")
    assertThat(profiles.last().id.internalId).isEqualTo(5)
    assertThat(File(getAbsoluteDirPath("3")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("4")).isDirectory).isFalse()
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfiles_restartApplication_checkDeletionIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    profileManagementController.deleteProfile(PROFILE_ID_1).ensureExecutes()
    profileManagementController.deleteProfile(PROFILE_ID_2).ensureExecutes()
    profileManagementController.deleteProfile(PROFILE_ID_3).ensureExecutes()
    testCoroutineDispatchers.runCurrent()
    setUpTestApplicationComponent()

    val profilesProvider = profileManagementController.getProfiles()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider)
    assertThat(profiles.size).isEqualTo(2)
    assertThat(profiles.first().name).isEqualTo("James")
    assertThat(profiles.last().name).isEqualTo("Veena")
    assertThat(File(getAbsoluteDirPath("1")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("3")).isDirectory).isFalse()
  }

  @Test
  fun testLoginProfile_addedProfile_profileIdTimestampAndNumberOfLoginsIsCorrectlyUpdated() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val loginProvider = profileManagementController.loginToProfile(PROFILE_ID_2)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(loginProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profileManagementController.getCurrentProfileId()?.internalId).isEqualTo(2)
    assertThat(profile.lastLoggedInTimestampMs).isNotEqualTo(0)
    assertThat(profile.numberOfLogins).isEqualTo(1)
  }

  @Test
  fun testLoginToProfile_addProfile_loginToProfileTwice_checkNumberOfLoginsIsTwo() {
    setUpTestApplicationComponent()
    addTestProfiles()
    var loginProvider = profileManagementController.loginToProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(loginProvider)

    // log out of profile 2
    loginProvider = profileManagementController.loginToProfile(PROFILE_ID_3)
    monitorFactory.waitForNextSuccessfulResult(loginProvider)

    loginProvider = profileManagementController.loginToProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(loginProvider)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.numberOfLogins).isEqualTo(2)
  }

  @Test
  fun testLoginToProfile_addProfiles_loginToProfileWithBadProfileId_checkLoginFailed() {
    setUpTestApplicationComponent()
    addTestProfiles()

    val loginProvider = profileManagementController.loginToProfile(PROFILE_ID_6)

    val error = monitorFactory.waitForNextFailureResult(loginProvider)
    assertThat(error)
      .hasMessageThat()
      .contains(
        "org.oppia.android.domain.profile.ProfileManagementController\$ProfileNotFoundException: " +
          "ProfileId 6 is not associated with an existing profile"
      )
  }

  @Test
  fun testLogInToProfile_sessionIdHasChanged() {
    setUpTestApplicationComponent()
    addTestProfiles()
    val previousSessionId = retrieveCurrentSessionId()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    // Logging into a new profile should regenerate the session ID.
    val latestSessionId = retrieveCurrentSessionId()
    assertThat(latestSessionId).isNotEqualTo(previousSessionId)
  }

  @Test
  fun testLogInToProfile_thenToSameProfileAgain_sessionIdHasChangedAgain() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )
    val previousSessionId = retrieveCurrentSessionId()

    // Log into the same profile twice (e.g. the case where the user logs out, then back in).
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    // Logging into the same profile a second time should also regenerate the session ID.
    val latestSessionId = retrieveCurrentSessionId()
    assertThat(latestSessionId).isNotEqualTo(previousSessionId)
  }

  @Test
  fun testLogInToProfile_thenToAnotherProfile_sessionIdHasChangedAgain() {
    setUpTestApplicationComponent()
    addTestProfiles()
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )
    val sessionIdForProfile1 = retrieveCurrentSessionId()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_2)
    )

    // Logging into a different profile a should regenerate the session ID.
    val sessionIdForProfile2 = retrieveCurrentSessionId()
    assertThat(sessionIdForProfile2).isNotEqualTo(sessionIdForProfile1)
  }

  @Test
  fun testLogInToProfile_invalidProfile_sessionIdDoesNotChange() {
    setUpTestApplicationComponent()
    addTestProfiles()
    val previousSessionId = retrieveCurrentSessionId()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_6)
    )

    // The session ID shouldn't change if the attempt to log in failed.
    val latestSessionId = retrieveCurrentSessionId()
    assertThat(latestSessionId).isEqualTo(previousSessionId)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_checkIfProfileEverAdded() {
    setUpTestApplicationComponent()
    val addProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(addProvider)

    val profileDatabase = readProfileDatabase()
    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(false)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_getWasProfileEverAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isFalse()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_checkIfProfileEverAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val profileDatabase = readProfileDatabase()

    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(true)
    assertThat(profileDatabase.profilesMap.size).isEqualTo(2)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_getWasProfileEverAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()
    testCoroutineDispatchers.runCurrent()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileIsAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    profileManagementController.deleteProfile(PROFILE_ID_1).ensureExecutes()
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()
    assertThat(profileDatabase.profilesMap.size).isEqualTo(1)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileWasAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")
    profileManagementController.deleteProfile(PROFILE_ID_1).ensureExecutes()
    testCoroutineDispatchers.runCurrent()

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()
    testCoroutineDispatchers.runCurrent()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testDeleteProfile_logsDeleteProfileEvent() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.deleteProfile(PROFILE_ID_2)
    )

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(eventLog).hasDeleteProfileContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testAddAdminProfile_addAnotherAdminProfile_checkSecondAdminProfileWasNotAdded() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "Rohit")

    val addProfile2 = addAdminProfile(name = "Ben")

    val error = monitorFactory.waitForNextFailureResult(addProfile2)
    assertThat(error).hasMessageThat().contains("Profile cannot be an admin")
  }

  @Test
  fun testDeviceSettings_addAdminProfile_getDefaultDeviceSettings_isSuccessful() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()

    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_getDeviceSettings_isSuccessful() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")

    val updateProvider = profileManagementController.updateWifiPermissionDeviceSettings(
      ADMIN_PROFILE_ID_0,
      downloadAndUpdateOnWifiOnly = true
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateTopicsAutoDeviceSettings_isSuccessful() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")

    val updateProvider =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, automaticallyUpdateTopics = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_andTopicDevSettings_succeeds() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")

    val updateProvider1 =
      profileManagementController.updateWifiPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, downloadAndUpdateOnWifiOnly = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider1)
    val updateProvider2 =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, automaticallyUpdateTopics = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider2)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_updateDeviceWifiSettings_fromUserProfile_isFailure() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val updateProvider =
      profileManagementController.updateWifiPermissionDeviceSettings(
        PROFILE_ID_1, downloadAndUpdateOnWifiOnly = true
      )

    monitorFactory.waitForNextFailureResult(updateProvider)
  }

  @Test
  fun testDeviceSettings_updateTopicsAutomaticallyDeviceSettings_fromUserProfile_isFailure() {
    setUpTestApplicationComponent()
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val updateProvider =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        PROFILE_ID_1, automaticallyUpdateTopics = true
      )

    monitorFactory.waitForNextFailureResult(updateProvider)
  }

  @Test
  fun testFetchSurveyLastShownTime_realProfile_beforeFirstSurveyShown_returnsDefaultTimestamp() {
    setUpTestApplicationComponent()
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    val lastShownTimeMs = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveSurveyLastShownTimestamp(
        PROFILE_ID_1
      )
    )

    assertThat(lastShownTimeMs).isEqualTo(DEFAULT_SURVEY_LAST_SHOWN_TIMESTAMP_MILLIS)
  }

  @Test
  fun testFetchSurveyLastShownTime_updateLastShownTimeFunctionCalled_returnsCurrentTime() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(CURRENT_TIMESTAMP)
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    fetchSuccessfulAsyncValue(
      profileManagementController::updateSurveyLastShownTimestamp,
      PROFILE_ID_1
    )

    val lastShownTimeMs = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveSurveyLastShownTimestamp(
        PROFILE_ID_1
      )
    )

    assertThat(lastShownTimeMs).isEqualTo(CURRENT_TIMESTAMP)
  }

  @Test
  fun testFetchSurveyLastShownTime_updateLastShownTime_inOneProfile_doesNotUpdateOtherProfiles() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(CURRENT_TIMESTAMP)
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )

    fetchSuccessfulAsyncValue(
      profileManagementController::updateSurveyLastShownTimestamp,
      PROFILE_ID_1
    )

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_2)
    )

    val lastShownTimeMs = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveSurveyLastShownTimestamp(
        PROFILE_ID_2
      )
    )

    assertThat(lastShownTimeMs).isEqualTo(DEFAULT_SURVEY_LAST_SHOWN_TIMESTAMP_MILLIS)
  }

  @Test
  fun testFetchLastSelectedClassroomId_updateClassroomId_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_0)
    )

    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.updateLastSelectedClassroomId(
        PROFILE_ID_0,
        TEST_CLASSROOM_ID_1
      )
    )

    val lastSelectedClassroomId = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveLastSelectedClassroomId(PROFILE_ID_0)
    )

    assertThat(lastSelectedClassroomId).isEqualTo(TEST_CLASSROOM_ID_1)
  }

  @Test
  fun testFetchLastSelectedClassroomId_updateClassroomIdTwice_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_0)
    )

    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.updateLastSelectedClassroomId(
        PROFILE_ID_0,
        TEST_CLASSROOM_ID_1
      )
    )

    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.updateLastSelectedClassroomId(
        PROFILE_ID_0,
        TEST_CLASSROOM_ID_2
      )
    )

    val lastSelectedClassroomId = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveLastSelectedClassroomId(PROFILE_ID_0)
    )

    assertThat(lastSelectedClassroomId).isEqualTo(TEST_CLASSROOM_ID_2)
  }

  @Test
  fun testFetchLastSelectedClassroomId_updateClassroomIds_checkUpdateIsSuccessfulPerProfile() {
    setUpTestApplicationComponent()
    addTestProfiles()

    // Login to profile 0 and update the last selected classroom to classroom 1.
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_0)
    )
    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.updateLastSelectedClassroomId(
        PROFILE_ID_0,
        TEST_CLASSROOM_ID_1
      )
    )

    // Login to profile 1 and update the last selected classroom to classroom 2.
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )
    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.updateLastSelectedClassroomId(
        PROFILE_ID_1,
        TEST_CLASSROOM_ID_2
      )
    )

    // Verify that last selected classroom of profile 0 is classroom 1.
    val profile0SelectedClassroomId = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveLastSelectedClassroomId(PROFILE_ID_0)
    )
    assertThat(profile0SelectedClassroomId).isEqualTo(TEST_CLASSROOM_ID_1)

    // Verify that last selected classroom of profile 1 is classroom 2.
    val classroomIdProfile1 = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveLastSelectedClassroomId(PROFILE_ID_1)
    )
    assertThat(classroomIdProfile1).isEqualTo(TEST_CLASSROOM_ID_2)
  }

  @Test
  fun testFetchLastSelectedClassroomId_withoutUpdatingClassroomId_returnEmptyClassroomId() {
    setUpTestApplicationComponent()
    addTestProfiles()

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_0)
    )
    val lastSelectedClassroomId = monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.retrieveLastSelectedClassroomId(PROFILE_ID_0)
    )
    assertThat(lastSelectedClassroomId).isEmpty()
  }

  @Test
  fun testUpdateProfile_updateMultipleFields_checkUpdateIsSuccessful() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateNewProfileDetails(
      PROFILE_ID_0,
      ProfileType.SOLE_LEARNER,
      null,
      -1,
      "John",
      isAdmin = true
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_0)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)

    assertThat(profile.name).isEqualTo("John")
    assertThat(profile.profileType).isEqualTo(ProfileType.SOLE_LEARNER)
    assertThat(profile.isAdmin).isEqualTo(true)
    assertThat(profile.avatar.avatarImageUri).isEmpty()
    assertThat(profile.avatar.avatarColorRgb).isEqualTo(-1)
  }

  @Test
  fun testUpdateProfile_updateMultipleFields_invalidName_checkNameUpdateFailed() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateNewProfileDetails(
      PROFILE_ID_0,
      ProfileType.SOLE_LEARNER,
      null,
      -1,
      "John123",
      isAdmin = true
    )
    val failure = monitorFactory.waitForNextFailureResult(updateProvider)

    assertThat(failure).hasMessageThat().contains("John123 does not contain only letters")
  }

  @Test
  fun testUpdateProfile_updateMultipleFields_nullAvatarUri_setsAvatarColorSuccessfully() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateNewProfileDetails(
      PROFILE_ID_0,
      ProfileType.SOLE_LEARNER,
      null,
      -11235672,
      "John",
      isAdmin = true
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_0)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)

    assertThat(profile.avatar.avatarImageUri).isEmpty()
    assertThat(profile.avatar.avatarColorRgb).isEqualTo(-11235672)
    assertThat(profile.name).isEqualTo("John")
    assertThat(profile.profileType).isEqualTo(ProfileType.SOLE_LEARNER)
    assertThat(profile.isAdmin).isEqualTo(true)
  }

  @Test
  fun testUpdateProfile_updateMultipleFields_unspecifiedProfileType_returnsProfileTypeError() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateNewProfileDetails(
      PROFILE_ID_0,
      ProfileType.PROFILE_TYPE_UNSPECIFIED,
      null,
      -11235672,
      "John",
      isAdmin = true
    )

    val failure = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(failure).hasMessageThat().isEqualTo("ProfileType must be set.")
  }

  @Test
  fun testUpdateProfile_updateMultipleFields_invalidProfileId_checkUpdateFailed() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateNewProfileDetails(
      PROFILE_ID_3,
      ProfileType.SOLE_LEARNER,
      null,
      -1,
      "John",
      isAdmin = true
    )
    val failure = monitorFactory.waitForNextFailureResult(updateProvider)

    assertThat(failure).hasMessageThat()
      .contains("ProfileId ${PROFILE_ID_3?.internalId} does not match an existing Profile")
  }

  @Test
  fun testUpdateExistingAdminProfile_updateProfileTypeToSupervisor_checkProfileTypeSupervisor() {
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()

    val updateProvider = profileManagementController.updateProfileType(
      PROFILE_ID_0,
      ProfileType.SUPERVISOR
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val updatedProfileProvider = profileManagementController.getProfile(PROFILE_ID_0)
    val updatedProfile = monitorFactory.waitForNextSuccessfulResult(updatedProfileProvider)
    assertThat(updatedProfile.profileType).isEqualTo(ProfileType.SUPERVISOR)
  }

  @Test
  fun testUpdateExistingPinlessAdmin_updateProfileTypeToSoleLearner_checkProfileTypeSoleLearner() {
    setUpTestApplicationComponent()
    addAdminProfile(name = "Admin", pin = "")

    val updateProvider = profileManagementController.updateProfileType(
      PROFILE_ID_0,
      ProfileType.SOLE_LEARNER
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val updatedProfileProvider = profileManagementController.getProfile(PROFILE_ID_0)
    val updatedProfile = monitorFactory.waitForNextSuccessfulResult(updatedProfileProvider)
    assertThat(updatedProfile.profileType).isEqualTo(ProfileType.SOLE_LEARNER)
  }

  @Test
  fun testUpdateExistingNonAdminProfile_updateProfileTypeToLearner_checkProfileTypeAddLearner() {
    setUpTestApplicationComponent()
    addAdminProfile("Admin")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val updateProvider = profileManagementController.updateProfileType(
      PROFILE_ID_1,
      ProfileType.ADDITIONAL_LEARNER
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val updatedProfileProvider = profileManagementController.getProfile(PROFILE_ID_1)
    val updatedProfile = monitorFactory.waitForNextSuccessfulResult(updatedProfileProvider)
    assertThat(updatedProfile.profileType).isEqualTo(ProfileType.ADDITIONAL_LEARNER)
  }

  @Test
  fun testUpdateDefaultProfile_profileTypeToSoleLearner_checkProfileTypeSoleLearner() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateProfileType(
      PROFILE_ID_0,
      ProfileType.SOLE_LEARNER
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    val updatedProfileProvider = profileManagementController.getProfile(PROFILE_ID_0)
    val updatedProfile = monitorFactory.waitForNextSuccessfulResult(updatedProfileProvider)
    assertThat(updatedProfile.profileType).isEqualTo(ProfileType.SOLE_LEARNER)
  }

  @Test
  fun testUpdateDefaultProfile_profileTypeUnspecified_returnsProfileTypeError() {
    setUpTestApplicationComponent()
    profileTestHelper.createDefaultAdminProfile()

    val updateProvider = profileManagementController.updateProfileType(
      PROFILE_ID_0,
      ProfileType.PROFILE_TYPE_UNSPECIFIED
    )

    val failure = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(failure).hasMessageThat().isEqualTo("ProfileType must be set.")
  }

  private fun addTestProfiles() {
    val profileAdditionProviders = PROFILES_LIST.map {
      addNonAdminProfile(it.name, pin = it.pin, allowDownloadAccess = it.allowDownloadAccess)
    }
    profileAdditionProviders.forEach(monitorFactory::ensureDataProviderExecutes)
  }

  private fun checkTestProfilesArePresent(resultList: List<Profile>) {
    PROFILES_LIST.forEachIndexed { idx, profile ->
      assertThat(resultList[idx].name).isEqualTo(profile.name)
      assertThat(resultList[idx].pin).isEqualTo(profile.pin)
      assertThat(resultList[idx].allowDownloadAccess).isEqualTo(profile.allowDownloadAccess)
      assertThat(resultList[idx].id.internalId).isEqualTo(idx)
      assertThat(File(getAbsoluteDirPath(idx.toString())).isDirectory).isTrue()
    }
  }

  private fun addProfileForLanguageSwitching(allowInLessonQuickLanguageSwitching: Boolean) {
    addNonAdminProfileAndWait(
      name = "Test Profile",
      allowInLessonQuickLanguageSwitching = allowInLessonQuickLanguageSwitching
    )
  }

  private fun retrieveProfile(profileId: ProfileId) =
    monitorFactory.waitForNextSuccessfulResult(profileManagementController.getProfile(profileId))

  private fun retrieveCurrentSessionId() =
    monitorFactory.waitForNextSuccessfulResult(loggingIdentifierController.getSessionId())

  private fun getAbsoluteDirPath(path: String): String {
    /**
     * context.filesDir.toString() looks like /tmp/robolectric-Method_test_name/org.oppia.android.util.test-dataDir/files
     * dropLast(5) removes files from the path and then it appends the real path with "app_" as a prefix
     */
    return context.filesDir.toString().dropLast(5) + "app_" + path
  }

  private fun readProfileDatabase(): ProfileDatabase {
    return FileInputStream(
      File(context.filesDir, "profile_database.cache")
    ).use(ProfileDatabase::parseFrom)
  }

  private fun addAdminProfile(name: String, pin: String = DEFAULT_PIN): DataProvider<Any?> =
    addProfile(name, pin, isAdmin = true)

  private fun addAdminProfileAndWait(name: String, pin: String = DEFAULT_PIN) {
    monitorFactory.ensureDataProviderExecutes(addAdminProfile(name, pin))
  }

  private fun addNonAdminProfile(
    name: String,
    pin: String = DEFAULT_PIN,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    allowInLessonQuickLanguageSwitching: Boolean = DEFAULT_ALLOW_IN_LESSON_QUICK_LANGUAGE_SWITCHING,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB
  ): DataProvider<Any?> {
    return addProfile(
      name,
      pin,
      avatarImagePath = null,
      allowDownloadAccess,
      allowInLessonQuickLanguageSwitching,
      colorRgb,
      isAdmin = false
    )
  }

  private fun addNonAdminProfileAndWait(
    name: String,
    pin: String = DEFAULT_PIN,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    allowInLessonQuickLanguageSwitching: Boolean = DEFAULT_ALLOW_IN_LESSON_QUICK_LANGUAGE_SWITCHING,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB
  ) {
    monitorFactory.ensureDataProviderExecutes(
      addNonAdminProfile(
        name, pin, allowDownloadAccess, allowInLessonQuickLanguageSwitching, colorRgb
      )
    )
  }

  private fun addProfile(
    name: String,
    pin: String = DEFAULT_PIN,
    avatarImagePath: Uri? = null,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    allowInLessonQuickLanguageSwitching: Boolean = DEFAULT_ALLOW_IN_LESSON_QUICK_LANGUAGE_SWITCHING,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB,
    isAdmin: Boolean
  ): DataProvider<Any?> {
    return profileManagementController.addProfile(
      name,
      pin,
      avatarImagePath,
      allowDownloadAccess,
      colorRgb,
      isAdmin,
      allowInLessonQuickLanguageSwitching = allowInLessonQuickLanguageSwitching
    )
  }

  private fun <T> fetchSuccessfulAsyncValue(block: suspend () -> T) =
    CoroutineScope(backgroundDispatcher).async { block() }.waitForSuccessfulResult()

  private fun <T> fetchSuccessfulAsyncValue(
    block: suspend (profileId: ProfileId) -> T,
    profileId: ProfileId
  ) = CoroutineScope(backgroundDispatcher).async { block(profileId) }.waitForSuccessfulResult()

  private fun <T> Deferred<T>.waitForSuccessfulResult(): T {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> result.value
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private fun <T> DataProvider<T>.ensureExecutes() = monitorFactory.ensureDataProviderExecutes(this)

  private fun setUpTestApplicationComponentWithoutLearnerAnalyticsStudy() {
    TestModule.enableLearnerStudyAnalytics = false
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponentWithLearnerAnalyticsStudy() {
    TestModule.enableLearnerStudyAnalytics = true
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    internal companion object {
      // This is expected to be off by default, so this helps the tests above confirm that the
      // feature's default value is, indeed, off.
      var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    }

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

    // The scoping here is to ensure changes to the module value above don't change the parameter
    // within the same application instance.
    @Provides
    @Singleton
    @EnableLearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return PlatformParameterValue.createDefaultParameter(
        defaultValue = enableFeature
      )
    }

    @Provides
    @Singleton
    @EnableLoggingLearnerStudyIds
    fun provideLoggingLearnerStudyIds(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return PlatformParameterValue.createDefaultParameter(
        defaultValue = enableFeature
      )
    }
  }

  @Module
  class TestLoggingIdentifierModule {
    companion object {
      const val applicationIdSeed = 1L
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestLoggingIdentifierModule::class, SyncStatusModule::class, AssetModule::class,
      ApplicationLifecycleModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileManagementControllerTest: ProfileManagementControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileManagementControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(profileManagementControllerTest: ProfileManagementControllerTest) {
      component.inject(profileManagementControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
