package org.oppia.android.app.administratorcontrols.learneranalytics

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.getIntents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerFactory
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.EventLogSubject.LearnerDetailsContextSubject
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.logging.TestSyncStatusManager
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileAndDeviceIdFragment]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileAndDeviceIdFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileAndDeviceIdFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var logUploadWorkerFactory: LogUploadWorkerFactory
  @Inject lateinit var syncStatusManager: TestSyncStatusManager
  @Inject lateinit var learnerAnalyticsLogger: LearnerAnalyticsLogger
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale

  private val clipboardManager by lazy {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableEditAccountsOptionsUi(true)
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(true)
    TestPlatformParameterModule.forceEnableLoggingLearnerStudyIds(true)
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.addOnlyAdminProfile()

    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasThreeItems() {
    runWithLaunchedActivityAndAddedFragment {
      // There should be three items: a header, a profile, and the sync status.
      onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 4))
    }
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasDeviceIdHeader() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdLabelAt(position = 0).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_hasDeviceId() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdLabelAt(position = 0).check(matches(withText(containsString("113e04cc09a3"))))
    }
  }

  @Test
  fun testFragment_deviceId_hasCopyButton() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdCopyButtonAt(position = 0).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_deviceId_clickCopyButton_copiesDeviceIdToClipboard() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdCopyButtonAt(position = 0).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Oppia installation ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("113e04cc09a3")
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasOneProfileListed() {
    runWithLaunchedActivityAndAddedFragment {
      onProfileNameAt(position = 1).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_profileEntry_hasProfileName() {
    runWithLaunchedActivityAndAddedFragment {
      onProfileNameAt(position = 1).check(matches(withText("Admin")))
    }
  }

  @Test
  fun testFragment_profileEntry_hasLearnerId() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdAt(position = 1).check(matches(withText("8dcbbd21")))
    }
  }

  @Test
  fun testFragment_profileEntry_hasCopyButton() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_profileEntry_clickFirstCopyButton_copiesAdminLearnerIdToClipboard() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Admin's learner ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("8dcbbd21")
  }

  @Test
  fun testFragment_multipleProfiles_listsAllProfiles() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      // Header + admin + 5 new profiles + sync status = 8 items.
      onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 9))
    }
  }

  @Test
  fun testFragment_multipleProfiles_adminIsFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      onProfileNameAt(position = 1).check(matches(withText("Admin")))
    }
  }

  @Test
  fun testFragment_multipleProfiles_secondEntryHasDifferentName() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      // The second entry is not the admin.
      onProfileNameAt(position = 2).check(matches(withText("A")))
    }
  }

  @Test
  fun testFragment_multipleProfiles_secondEntryHasDifferentLearnerIdThanFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      // The second profile has a different learner ID.
      onLearnerIdAt(position = 1).check(matches(withText("8dcbbd21")))
      onLearnerIdAt(position = 2).check(matches(withText("208663b0")))
    }
  }

  @Test
  fun testFragment_multipleProfiles_copySecondEntry_copiesDifferentLearnerIdThanFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 2).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("A's learner ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("208663b0")
  }

  @Test
  fun testFragment_initialState_deviceIdCopyButtonHasCopyLabel() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdCopyButtonAt(position = 0).check(matches(withText("Copy")))
    }
  }

  @Test
  fun testFragment_adminProfile_initialState_learnerIdCopyButtonHasCopyLabel() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).check(matches(withText("Copy")))
    }
  }

  @Test
  fun testFragment_adminProfile_clickDeviceIdCopyButton_deviceIdIsCopiedButNotLearnerId() {
    runWithLaunchedActivityAndAddedFragment {
      onDeviceIdCopyButtonAt(position = 0).perform(click())
      testCoroutineDispatchers.runCurrent()

      onDeviceIdCopyButtonAt(position = 0).check(matches(withText("Copied")))
      onLearnerIdCopyButtonAt(position = 1).check(matches(withText("Copy")))
    }
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_learnerIdIsCopiedButNotDeviceId() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).perform(click())
      testCoroutineDispatchers.runCurrent()

      onDeviceIdCopyButtonAt(position = 0).check(matches(withText("Copy")))
      onLearnerIdCopyButtonAt(position = 1).check(matches(withText("Copied")))
    }
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_copyInOtherApp_nothingIsCopied() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).perform(click())
      testCoroutineDispatchers.runCurrent()

      updateClipDataAsThoughFromAnotherApp()

      // Changing the clipboard in a different app should reset the labels.
      onDeviceIdCopyButtonAt(position = 0).check(matches(withText("Copy")))
      onLearnerIdCopyButtonAt(position = 1).check(matches(withText("Copy")))
    }
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_rotate_learnerIdStillCopied() {
    runWithLaunchedActivityAndAddedFragment {
      onLearnerIdCopyButtonAt(position = 1).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      // The button label should be restored after a rotation.
      onDeviceIdCopyButtonAt(position = 0).check(matches(withText("Copy")))
      onLearnerIdCopyButtonAt(position = 1).check(matches(withText("Copied")))
    }
  }

  @Test
  fun testFragment_multipleProfiles_rotate_profilesStillPresent() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    runWithLaunchedActivityAndAddedFragment {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 9))
    }
  }

  @Test
  fun testFragment_firstEntry_noAdminEvents_hasZeroAdminEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)

    runWithLaunchedActivityAndAddedFragment {
      onAwaitingUploadLearnerEventsCountAt(position = 1).check(matches(withText("0")))
      onUploadedLearnerEventsCountAt(position = 1).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_firstEntry_adminEvents_notUploaded_hasSomeAdminEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events.
      logTwoAnalyticsEvents(profileId = ADMIN_PROFILE_ID)

      // Two are awaiting upload, but neither have been uploaded yet.
      onAwaitingUploadLearnerEventsCountAt(position = 1).check(matches(withText("2")))
      onUploadedLearnerEventsCountAt(position = 1).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_firstEntry_adminEvents_uploaded_hasSomeAdminEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events, upload them, then log one more event.
      logTwoAnalyticsEvents(profileId = ADMIN_PROFILE_ID)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)

      // Two should be uploaded, and one waiting upload.
      onAwaitingUploadLearnerEventsCountAt(position = 1).check(matches(withText("1")))
      onUploadedLearnerEventsCountAt(position = 1).check(matches(withText("2")))
    }
  }

  @Test
  fun testFragment_firstEntry_noGenericEvents_hasZeroGenericEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)

    runWithLaunchedActivityAndAddedFragment {
      onAwaitingUploadUncategorizedEventsCountAt(position = 1).check(matches(withText("0")))
      onUploadedUncategorizedEventsCountAt(position = 1).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_firstEntry_genericEvents_notUploaded_hasSomeGenericEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events off of the admin profile.
      logTwoAnalyticsEvents(profileId = null)

      onAwaitingUploadUncategorizedEventsCountAt(position = 1).check(matches(withText("2")))
      onUploadedUncategorizedEventsCountAt(position = 1).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_firstEntry_genericEvents_uploaded_hasSomeGenericEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events, upload them, then log one more event, off of the admin profile.
      logTwoAnalyticsEvents(profileId = null)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = null)

      onAwaitingUploadUncategorizedEventsCountAt(position = 1).check(matches(withText("1")))
      onUploadedUncategorizedEventsCountAt(position = 1).check(matches(withText("2")))
    }
  }

  @Test
  fun testFragment_firstEntry_mixOfAdminAndGenericEvents_someUploaded_reportsAllEvents() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log & upload a mix of events with and without the admin profile.
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logThreeAnalyticsEvents(profileId = null)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = null)
      logTwoAnalyticsEvents(profileId = ADMIN_PROFILE_ID)

      // Event counts should be represented in the correct places.
      onAwaitingUploadLearnerEventsCountAt(position = 1).check(matches(withText("2")))
      onUploadedLearnerEventsCountAt(position = 1).check(matches(withText("1")))
      onAwaitingUploadUncategorizedEventsCountAt(position = 1).check(matches(withText("1")))
      onUploadedUncategorizedEventsCountAt(position = 1).check(matches(withText("3")))
    }
  }

  @Test
  fun testFragment_secondEntry_noLearnerEvents_hasZeroLearnerEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)

    runWithLaunchedActivityAndAddedFragment {
      onAwaitingUploadLearnerEventsCountAt(position = 2).check(matches(withText("0")))
      onUploadedLearnerEventsCountAt(position = 2).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_secondEntry_learnerEvents_notUploaded_hasSomeLearnerEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events.
      logTwoAnalyticsEvents(profileId = LEARNER_PROFILE_ID_0)

      // Two are awaiting upload, but neither have been uploaded yet.
      onAwaitingUploadLearnerEventsCountAt(position = 2).check(matches(withText("2")))
      onUploadedLearnerEventsCountAt(position = 2).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_secondEntry_learnerEvents_uploaded_hasSomeLearnerEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events, upload them, then log one more event.
      logTwoAnalyticsEvents(profileId = LEARNER_PROFILE_ID_0)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)

      // Two should be uploaded, and one waiting upload.
      onAwaitingUploadLearnerEventsCountAt(position = 2).check(matches(withText("1")))
      onUploadedLearnerEventsCountAt(position = 2).check(matches(withText("2")))
    }
  }

  @Test
  fun testFragment_secondEntry_learnerEvents_hasZeroAdminOrGenericEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events, upload them, then log one more event, for a learner profile.
      logTwoAnalyticsEvents(profileId = LEARNER_PROFILE_ID_0)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)

      // The admin profile's event counts shouldn't change since the only logged events were for a
      // specific learner profile.
      onAwaitingUploadLearnerEventsCountAt(position = 1).check(matches(withText("0")))
      onUploadedLearnerEventsCountAt(position = 1).check(matches(withText("0")))
      onAwaitingUploadUncategorizedEventsCountAt(position = 1).check(matches(withText("0")))
      onUploadedUncategorizedEventsCountAt(position = 1).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_secondEntry_adminAndGenericEvents_uploaded_hasZeroLearnerEventsReported() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log a couple of events generically and for the admin profile.
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = null)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = null)

      // No events should be reported for the learner since it didn't have any events uploaded.
      onAwaitingUploadLearnerEventsCountAt(position = 2).check(matches(withText("0")))
      onUploadedLearnerEventsCountAt(position = 2).check(matches(withText("0")))
    }
  }

  @Test
  fun testFragment_initialState_profileDataHasYetToBeCollected() {
    runWithLaunchedActivityAndAddedFragment {
      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("No data has been collected yet to upload"))))
    }
  }

  @Test
  fun testFragment_initialState_wait_profileDataHasYetToBeCollected() {
    runWithLaunchedActivityAndAddedFragment {
      testCoroutineDispatchers.advanceTimeBy(delayTimeMillis = TimeUnit.SECONDS.toMillis(1))

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("No data has been collected yet to upload"))))
    }
  }

  @Test
  fun testFragment_eventLogged_waitingForUpload_indicatorTextMentionsWaiting() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Enqueue the event, but don't actually begin uploading it, then reconnect to the network.
      // Note that the extra event log is a slight hack to force a refresh of the status indicator
      // (since network changes are polled when there are other changes to represent rather than
      // being actively "pushed" by the system).
      logAnalyticsEvent()
      connectNetwork()
      logAnalyticsEvent()

      // The status indicator is suggesting that events can be uploaded (and there are some
      // available to upload), they just haven't been scheduled yet.
      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("Waiting to schedule data uploading worker…"))))
    }
  }

  @Test
  fun testFragment_eventLogged_waitingForUpload_uploadStarted_profileDataIsCurrentlyUploading() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Unfortunately, it's tricky to pause the actual upload worker so this is a hacky way to
      // produce the same situation to ensure the label is correct.
      logAnalyticsEvent()
      syncStatusManager.forceSyncStatus(SyncStatus.DATA_UPLOADING)
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("Profile data is currently uploading"))))
    }
  }

  @Test
  fun testFragment_noConnectivity_indicatorTextMentionsDataCannotBeUploaded() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Enqueue an event but don't reconnect to the network.
      logAnalyticsEvent()

      // The status indicator is suggesting that internet connectivity needs to resume in order to
      // upload events.
      onSyncStatusAt(position = 2)
        .check(
          matches(
            withText(
              containsString(
                "Please connect to a WiFi or Cellular network in order to upload profile data"
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_eventLogged_waitForUpload_profileDataIsUploaded() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      logAnalyticsEvent()
      connectNetwork()
      flushEventWorkerQueue()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("All profile data has been uploaded"))))
    }
  }

  @Test
  fun testFragment_eventLogged_uploadError_profileDataHasError() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent()

      // An upload error can currently only be simulated by directly influencing the sync manager.
      syncStatusManager.forceSyncStatus(SyncStatus.UPLOAD_ERROR)
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(
          matches(withText(containsString("Something went wrong when trying to upload events")))
        )
    }
  }

  @Test
  fun testFragment_eventLogged_uploadError_anotherLogged_wait_profileDataIsUploading() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent()
      syncStatusManager.reportUploadError()
      testCoroutineDispatchers.runCurrent()

      logAnalyticsEvent()
      connectNetwork()
      flushEventWorkerQueue()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("All profile data has been uploaded"))))
    }
  }

  @Test
  fun testFragment_rotate_profileDataHasYetToBeCollected() {
    runWithLaunchedActivityAndAddedFragment {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("No data has been collected yet to upload"))))
    }
  }

  @Test
  fun testFragment_eventLogged_waitingForUpload_rotate_profileDataIsCurrentlyUploading() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent()
      syncStatusManager.forceSyncStatus(SyncStatus.DATA_UPLOADING)
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("Profile data is currently uploading"))))
    }
  }

  @Test
  fun testFragment_eventLogged_waitForUpload_rotate_profileDataIsUploaded() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent()
      connectNetwork()
      flushEventWorkerQueue()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(matches(withText(containsString("All profile data has been uploaded"))))
    }
  }

  @Test
  fun testFragment_eventsLogged_uploadError_rotate_profileDataHasError() {
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent()
      syncStatusManager.forceSyncStatus(SyncStatus.UPLOAD_ERROR)
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onSyncStatusAt(position = 2)
        .check(
          matches(withText(containsString("Something went wrong when trying to upload events")))
        )
    }
  }

  @Test
  fun testFragment_multipleProfiles_clickShareIdsAndLogs_sendsIntentWithIdsAndLogsText() {
    // Use fake time so that the generated event logs are consistent across runs.
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    profileTestHelper.addMoreProfiles(numProfiles = 2)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // Log & upload some events, then enqueue others.
      logThreeAnalyticsEvents(profileId = null)
      logTwoAnalyticsEvents(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)
      logTwoAnalyticsEvents(profileId = LEARNER_PROFILE_ID_1)
      connectOnlyToFlushWorkerQueue()
      logAnalyticsEvent(profileId = null)
      logThreeAnalyticsEvents(profileId = ADMIN_PROFILE_ID)
      logTwoAnalyticsEvents(profileId = LEARNER_PROFILE_ID_0)
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_1)
      connectNetwork()
      logAnalyticsEvent(profileId = null) // Log an event to trigger tracking the network change.

      onShareIdsAndEventsButtonAt(position = 5).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedShareTextPattern =
        """
        Oppia app installation ID: 113e04cc09a3
        - Profile name: Admin, learner ID: 8dcbbd21
          - Uploading learner events: 3
          - Uploaded learner events: 2
          - Uploading uncategorized events: 1
          - Uploaded uncategorized events: 4
        - Profile name: A, learner ID: 208663b0
          - Uploading learner events: 2
          - Uploaded learner events: 1
        - Profile name: B, learner ID: 92d0c6e2
          - Uploading learner events: 1
          - Uploaded learner events: 2
        Current sync status: Waiting to schedule data uploading worker….
        Event log encoding integrity checks:
        - First 40 chars of encoded string: ([\p{Alnum}/\\+=]+)
        - Last 40 chars of encoded string: ([\p{Alnum}/\\+=]+)
        - SHA-1 hash \(unwrapped event string\): (\p{XDigit}+)
        - Total event string length \(unwrapped\): (\p{Digit}+)
        Encoded event logs:([\p{Alnum}/+=\p{Space}]+)
        """.trimIndent().toRegex()
      val intents = getIntents()
      val extraText = intents.singleOrNull()?.getStringExtra(Intent.EXTRA_TEXT)
      assertThat(intents).hasSize(1)
      assertThat(intents.single()).hasAction(Intent.ACTION_SEND)
      assertThat(intents.single()).hasType("text/plain")
      assertThat(intents.single()).extras().containsKey(Intent.EXTRA_TEXT)
      assertThat(extraText).matches(expectedShareTextPattern.toPattern())
      val (encodingPrefix, encodingSuffix, shaHash, encodingLength, rawEncodedLogs) =
        extraText?.let { expectedShareTextPattern.matchEntire(it) }?.destructured!!
      val unwrappedEncodedLogs = rawEncodedLogs.trim().replace(" ", "").replace("\n", "")
      // Verify that the correct _values_ are being outputted, even if the specific values might
      // differ slightly (depending on the running platform).
      assertThat(encodingPrefix).isEqualTo(unwrappedEncodedLogs.take(40))
      assertThat(encodingSuffix).isEqualTo(unwrappedEncodedLogs.takeLast(40))
      assertThat(shaHash).isEqualTo(unwrappedEncodedLogs.computeSha1Hash())
      assertThat(encodingLength.toInt()).isEqualTo(unwrappedEncodedLogs.length)
      // Verify the encoded events themselves are correct by decoding them and analyzing the loaded
      // proto (since the string can vary somewhat).
      val eventLogs = decodeEventLogString(unwrappedEncodedLogs)
      assertThat(eventLogs.eventLogsToUploadCount).isEqualTo(7)
      assertThat(eventLogs.uploadedEventLogsCount).isEqualTo(9)
      assertThat(eventLogs.eventLogsToUploadList[0]).hasCommonPropsWithNoProfileId()
      assertThat(eventLogs.eventLogsToUploadList[1]).hasCommonPropsWithProfile(ADMIN_PROFILE_ID)
      assertThat(eventLogs.eventLogsToUploadList[2]).hasCommonPropsWithProfile(ADMIN_PROFILE_ID)
      assertThat(eventLogs.eventLogsToUploadList[3]).hasCommonPropsWithProfile(ADMIN_PROFILE_ID)
      assertThat(eventLogs.eventLogsToUploadList[4]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_0)
      assertThat(eventLogs.eventLogsToUploadList[5]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_0)
      assertThat(eventLogs.eventLogsToUploadList[6]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_1)
      assertThat(eventLogs.uploadedEventLogsList[0]).hasCommonPropsWithNoProfileId()
      assertThat(eventLogs.uploadedEventLogsList[1]).hasCommonPropsWithNoProfileId()
      assertThat(eventLogs.uploadedEventLogsList[2]).hasCommonPropsWithNoProfileId()
      assertThat(eventLogs.uploadedEventLogsList[3]).hasCommonPropsWithProfile(ADMIN_PROFILE_ID)
      assertThat(eventLogs.uploadedEventLogsList[4]).hasCommonPropsWithProfile(ADMIN_PROFILE_ID)
      assertThat(eventLogs.uploadedEventLogsList[5]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_0)
      assertThat(eventLogs.uploadedEventLogsList[6]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_1)
      assertThat(eventLogs.uploadedEventLogsList[7]).hasCommonPropsWithProfile(LEARNER_PROFILE_ID_1)
      assertThat(eventLogs.uploadedEventLogsList[8]).hasCommonPropsWithNoProfileId()
    }
  }

  @Test
  fun testFragment_noEventsPending_uploadLogsButtonDisabled() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      // The upload button should be disabled when there are no events to upload.
      onUploadLogsButtonAt(position = 4).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testFragment_multipleEventsPending_noConnection_uploadLogsButtonDisabled() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      logAnalyticsEvent(profileId = null)
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)

      // The button is still disabled since there's no internet connection.
      onUploadLogsButtonAt(position = 4).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testFragment_multipleEventsPending_uploadLogsButtonEnabled() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.

      logAnalyticsEvent(profileId = null)
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)
      connectNetwork()
      logAnalyticsEvent(profileId = null) // Log an event to trigger tracking the network change.

      // With events pending & connectivity, the upload button should now be available to press.
      onUploadLogsButtonAt(position = 4).check(matches(isEnabled()))
    }
  }

  @Test
  fun testFragment_multipleEventsPending_clickUploadLogs_wait_uploadsEventLogs() {
    profileTestHelper.addMoreProfiles(numProfiles = 1)
    runWithLaunchedActivityAndAddedFragment {
      disconnectNetwork() // Ensure events are cached.
      logAnalyticsEvent(profileId = null)
      logAnalyticsEvent(profileId = ADMIN_PROFILE_ID)
      logAnalyticsEvent(profileId = LEARNER_PROFILE_ID_0)
      connectNetwork()
      logAnalyticsEvent(profileId = null) // Log an event to trigger tracking the network change.
      fakeAnalyticsEventLogger.clearAllEvents()

      // Click the 'upload logs' button and wait.
      onUploadLogsButtonAt(position = 4).perform(click())
      testCoroutineDispatchers.runCurrent()

      // The events should be uploaded.
      assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(3)
    }
  }

  private fun runWithLaunchedActivityAndAddedFragment(
    testBlock: ActivityScenario<TestActivity>.() -> Unit
  ) {
    ActivityScenario.launch<TestActivity>(TestActivity.createIntent(context)).use { scenario ->
      scenario.onActivity { activity ->
        activity.setContentView(R.layout.test_activity)

        activity.supportFragmentManager.beginTransaction()
          .add(R.id.test_fragment_placeholder, ProfileAndDeviceIdFragment())
          .commitNow()
      }
      connectNetwork() // Start with internet connectivity.
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  private fun scrollTo(position: Int) {
    onView(withId(R.id.profile_and_device_id_recycler_view))
      .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(position, scrollTo()))
    testCoroutineDispatchers.runCurrent()
  }

  private fun onDeviceIdLabelAt(position: Int) = onProfileListItemAt(position, R.id.device_id_label)

  private fun onDeviceIdCopyButtonAt(position: Int) =
    onProfileListItemAt(position, R.id.device_id_copy_button)

  private fun onProfileNameAt(position: Int) =
    onProfileListItemAt(position, R.id.profile_id_view_profile_name)

  private fun onLearnerIdAt(position: Int) =
    onProfileListItemAt(position, R.id.profile_id_view_learner_id)

  private fun onLearnerIdCopyButtonAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_id_copy_button)

  private fun onAwaitingUploadLearnerEventsCountAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_events_waiting_upload_count)

  private fun onUploadedLearnerEventsCountAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_events_uploaded_count)

  private fun onAwaitingUploadUncategorizedEventsCountAt(position: Int) =
    onProfileListItemAt(position, R.id.uncategorized_events_waiting_upload_count)

  private fun onUploadedUncategorizedEventsCountAt(position: Int) =
    onProfileListItemAt(position, R.id.uncategorized_events_uploaded_count)

  private fun onSyncStatusAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_analytics_sync_status_text_view)

  private fun onShareIdsAndEventsButtonAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_analytics_share_ids_and_events_button)

  private fun onUploadLogsButtonAt(position: Int) =
    onProfileListItemAt(position, R.id.learner_analytics_upload_logs_now_button)

  private fun onProfileListItemAt(position: Int, @IdRes viewId: Int): ViewInteraction {
    scrollTo(position)
    return onView(profileListItemAt(position, viewId))
  }

  private fun profileListItemAt(position: Int, @IdRes viewId: Int): Matcher<View> {
    return atPositionOnView(
      recyclerViewId = R.id.profile_and_device_id_recycler_view, position, targetViewId = viewId
    )
  }

  private fun getCurrentClipData(): ClipData? = clipboardManager.primaryClip

  private fun updateClipDataAsThoughFromAnotherApp() {
    // This must use the setter since property syntax seems to break on SDK 30.
    @Suppress("UsePropertyAccessSyntax")
    clipboardManager.setPrimaryClip(
      ClipData.newPlainText(
        /* label = */ "Label of text from another app", /* text = */ "Text copied from another app"
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun logAnalyticsEvent(profileId: ProfileId? = null) {
    learnerAnalyticsLogger.logAppInForeground(
      installationId = TEST_INSTALLATION_ID, profileId, learnerId = TEST_LEARNER_ID
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun logTwoAnalyticsEvents(profileId: ProfileId? = null) {
    logAnalyticsEvent(profileId)
    logAnalyticsEvent(profileId)
  }

  private fun logThreeAnalyticsEvents(profileId: ProfileId? = null) {
    logTwoAnalyticsEvents(profileId)
    logAnalyticsEvent(profileId)
  }

  private fun flushEventWorkerQueue() {
    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY, LogUploadWorker.EVENT_WORKER
    ).build()

    val request = OneTimeWorkRequestBuilder<LogUploadWorker>().setInputData(inputData).build()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
  }

  private fun disconnectNetwork() {
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.NONE)
  }

  private fun connectNetwork() {
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
  }

  private fun connectOnlyToFlushWorkerQueue() {
    connectNetwork()
    flushEventWorkerQueue()
    disconnectNetwork()
  }

  private fun String.computeSha1Hash(): String {
    return machineLocale.run {
      MessageDigest.getInstance("SHA-1")
        .digest(this@computeSha1Hash.toByteArray())
        .joinToString("") { "%02x".formatForMachines(it) }
    }
  }

  private fun decodeEventLogString(encodedEventLogs: String): OppiaEventLogs {
    return GZIPInputStream(Base64.getDecoder().decode(encodedEventLogs).inputStream()).use { inps ->
      OppiaEventLogs.newBuilder().mergeFrom(inps).build()
    }
  }

  private fun EventLogSubject.hasCommonProperties() {
    hasNoLanguageInformation()
    hasTimestampThat().isEqualTo(0)
    isEssentialPriority()
    hasAppInForegroundContextThat().hasDefaultIds()
  }

  private fun EventLogSubject.hasCommonPropsWithNoProfileId() {
    hasCommonProperties()
    hasNoProfileId()
  }

  private fun EventLogSubject.hasCommonPropsWithProfile(profileId: ProfileId) {
    hasCommonProperties()
    hasProfileIdThat().isEqualTo(profileId)
  }

  private fun EventLogSubject.hasNoLanguageInformation() {
    hasAppLanguageSelectionThat().isEqualToDefaultInstance()
    hasWrittenTranslationLanguageSelectionThat().isEqualToDefaultInstance()
    hasAudioTranslationLanguageSelectionThat().isEqualToDefaultInstance()
  }

  private fun LearnerDetailsContextSubject.hasDefaultIds() {
    hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
    hasInstallationIdThat().isEqualTo(TEST_INSTALLATION_ID)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    companion object {
      // Use a fixed application ID to ensure deterministic generation of learner IDs.
      var fixedApplicationId = DEFAULT_APPLICATION_ID
    }

    @Provides
    @ApplicationIdSeed
    fun provideFakeApplicationIdSeed(): Long = fixedApplicationId
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestPlatformParameterModule::class,
      TestModule::class, RobolectricModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class, LoggerModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      SyncStatusTestModule::class, SplitScreenInteractionModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, MetricLogSchedulerModule::class,
      TestingBuildFlavorModule::class,
      ActivityRouterModule::class, CpuPerformanceSnapshotterModule::class,
      ApplicationLifecycleModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(test: ProfileAndDeviceIdFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileAndDeviceIdFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(test: ProfileAndDeviceIdFragmentTest) {
      component.inject(test)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private companion object {
    private const val DEFAULT_APPLICATION_ID = 123456789L
    private const val TEST_LEARNER_ID = "test_learner_id"
    private const val TEST_INSTALLATION_ID = "test_install_id"

    private val ADMIN_PROFILE_ID = createProfileId(internalProfileId = 0)
    private val LEARNER_PROFILE_ID_0 = createProfileId(internalProfileId = 1)
    private val LEARNER_PROFILE_ID_1 = createProfileId(internalProfileId = 2)

    private fun createProfileId(internalProfileId: Int) = ProfileId.newBuilder().apply {
      internalId = internalProfileId
    }.build()
  }
}
