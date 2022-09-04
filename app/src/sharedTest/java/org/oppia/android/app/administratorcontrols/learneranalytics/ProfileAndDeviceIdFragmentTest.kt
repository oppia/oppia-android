package org.oppia.android.app.administratorcontrols.learneranalytics

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerFactory
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.system.OppiaClock
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
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
  private companion object {
    private const val DEFAULT_APPLICATION_ID = 123456789L
  }

  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var oppiaClock: OppiaClock
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var logUploadWorkerFactory: LogUploadWorkerFactory
  @Inject lateinit var syncStatusManager: SyncStatusManager
  @Inject lateinit var learnerAnalyticsLogger: LearnerAnalyticsLogger

  private val clipboardManager by lazy {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableEditAccountsOptionsUi(true)
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(true)
    setUpTestApplicationComponent()
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
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasThreeItems() {
    initializeActivityAndAddFragment()

    // There should be three items: a header, a profile, and the sync status.
    onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 3))
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasDeviceIdHeader() {
    initializeActivityAndAddFragment()

    onView(deviceIdLabelAt(position = 0)).check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_hasDeviceId() {
    initializeActivityAndAddFragment()

    onView(deviceIdLabelAt(position = 0)).check(matches(withText(containsString("c85606ca6390"))))
  }

  @Test
  fun testFragment_deviceId_hasCopyButton() {
    initializeActivityAndAddFragment()

    onView(deviceIdCopyButtonAt(position = 0)).check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_deviceId_clickCopyButton_copiesDeviceIdToClipboard() {
    initializeActivityAndAddFragment()

    onView(deviceIdCopyButtonAt(position = 0)).perform(click())
    testCoroutineDispatchers.runCurrent()

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Oppia installation ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("c85606ca6390")
  }

  @Test
  fun testFragment_withOnlyAdminProfile_hasOneProfileListed() {
    initializeActivityAndAddFragment()

    onView(profileNameAt(position = 1)).check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_profileEntry_hasProfileName() {
    initializeActivityAndAddFragment()

    onView(profileNameAt(position = 1)).check(matches(withText("Admin")))
  }

  @Test
  fun testFragment_profileEntry_hasLearnerId() {
    initializeActivityAndAddFragment()

    onView(learnerIdAt(position = 1)).check(matches(withText("a9fe66ab")))
  }

  @Test
  fun testFragment_profileEntry_hasCopyButton() {
    initializeActivityAndAddFragment()

    onView(learnerIdCopyButtonAt(position = 1)).check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_profileEntry_clickFirstCopyButton_copiesAdminLearnerIdToClipboard() {
    initializeActivityAndAddFragment()

    onView(learnerIdCopyButtonAt(position = 1)).perform(click())
    testCoroutineDispatchers.runCurrent()

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Admin's learner ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("a9fe66ab")
  }

  @Test
  fun testFragment_multipleProfiles_listsAllProfiles() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    initializeActivityAndAddFragment()

    // Header + admin + 5 new profiles + sync status = 8 items.
    onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 8))
  }

  @Test
  fun testFragment_multipleProfiles_adminIsFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    initializeActivityAndAddFragment()

    onView(profileNameAt(position = 1)).check(matches(withText("Admin")))
  }

  @Test
  fun testFragment_multipleProfiles_secondEntryHasDifferentName() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    initializeActivityAndAddFragment()

    // The second entry is not the admin.
    onView(profileNameAt(position = 2)).check(matches(withText("A")))
  }

  @Test
  fun testFragment_multipleProfiles_secondEntryHasDifferentLearnerIdThanFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    initializeActivityAndAddFragment()

    // The second profile has a different learner ID.
    onView(learnerIdAt(position = 1)).check(matches(withText("a9fe66ab")))
    onView(learnerIdAt(position = 2)).check(matches(withText("c368b501")))
  }

  @Test
  fun testFragment_multipleProfiles_copySecondEntry_copiesDifferentLearnerIdThanFirst() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)

    initializeActivityAndAddFragment()

    onView(learnerIdCopyButtonAt(position = 2)).perform(click())
    testCoroutineDispatchers.runCurrent()

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("A's learner ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo("c368b501")
  }

  @Test
  fun testFragment_initialState_deviceIdCopyButtonHasCopyLabel() {
    initializeActivityAndAddFragment()

    onView(deviceIdCopyButtonAt(position = 0)).check(matches(withText("Copy")))
  }

  @Test
  fun testFragment_adminProfile_initialState_learnerIdCopyButtonHasCopyLabel() {
    initializeActivityAndAddFragment()

    onView(learnerIdCopyButtonAt(position = 1)).check(matches(withText("Copy")))
  }

  @Test
  fun testFragment_adminProfile_clickDeviceIdCopyButton_deviceIdIsCopiedButNotLearnerId() {
    initializeActivityAndAddFragment()

    onView(deviceIdCopyButtonAt(position = 0)).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(deviceIdCopyButtonAt(position = 0)).check(matches(withText("Copied")))
    onView(learnerIdCopyButtonAt(position = 1)).check(matches(withText("Copy")))
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_learnerIdIsCopiedButNotDeviceId() {
    initializeActivityAndAddFragment()

    onView(learnerIdCopyButtonAt(position = 1)).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(deviceIdCopyButtonAt(position = 0)).check(matches(withText("Copy")))
    onView(learnerIdCopyButtonAt(position = 1)).check(matches(withText("Copied")))
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_copyInOtherApp_nothingIsCopied() {
    initializeActivityAndAddFragment()
    onView(learnerIdCopyButtonAt(position = 1)).perform(click())
    testCoroutineDispatchers.runCurrent()

    updateClipDataAsThoughFromAnotherApp()

    // Changing the clipboard in a different app should reset the labels.
    onView(deviceIdCopyButtonAt(position = 0)).check(matches(withText("Copy")))
    onView(learnerIdCopyButtonAt(position = 1)).check(matches(withText("Copy")))
  }

  @Test
  fun testFragment_adminProfile_clickLearnerIdCopyButton_rotate_learnerIdStillCopied() {
    initializeActivityAndAddFragment()
    onView(learnerIdCopyButtonAt(position = 1)).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    // The button label should be restored after a rotation.
    onView(deviceIdCopyButtonAt(position = 0)).check(matches(withText("Copy")))
    onView(learnerIdCopyButtonAt(position = 1)).check(matches(withText("Copied")))
  }

  @Test
  fun testFragment_multipleProfiles_rotate_profilesStillPresent() {
    profileTestHelper.addMoreProfiles(numProfiles = 5)
    initializeActivityAndAddFragment()

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.profile_and_device_id_recycler_view)).check(hasItemCount(count = 8))
  }

  @Test
  fun testFragment_initialState_profileDataHasYetToBeCollected() {
    initializeActivityAndAddFragment()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("No data has been collected yet to upload"))))
  }

  @Test
  fun testFragment_initialState_wait_profileDataHasYetToBeCollected() {
    initializeActivityAndAddFragment()

    testCoroutineDispatchers.advanceTimeBy(delayTimeMillis = TimeUnit.SECONDS.toMillis(1))

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("No data has been collected yet to upload"))))
  }

  @Test
  fun testFragment_eventLogged_waitingForUpload_profileDataIsCurrentlyUploading() {
    initializeActivityAndAddFragment()

    // Unfortunately, it's tricky to pause the actual upload worker so this is a hacky way to
    // produce the same situation to ensure the label is correct.
    queueAnalyticsEvent()
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADING)
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("Profile data is currently uploading"))))
  }

  @Test
  fun testFragment_eventLogged_waitForUpload_profileDataIsUploaded() {
    initializeActivityAndAddFragment()

    queueAnalyticsEvent()
    flushEventWorkerQueue()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("All profile data has been uploaded"))))
  }

  @Test
  fun testFragment_eventLogged_networkError_profileDataHasError() {
    initializeActivityAndAddFragment()
    queueAnalyticsEvent()

    // A network error can currently only be simulated by directly influencing the sync manager.
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.NETWORK_ERROR)
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
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

  @Test
  fun testFragment_eventLogged_networkError_anotherLogged_wait_profileDataIsUploading() {
    initializeActivityAndAddFragment()
    queueAnalyticsEvent()
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.NETWORK_ERROR)

    queueAnalyticsEvent()
    flushEventWorkerQueue()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("All profile data has been uploaded"))))
  }

  @Test
  fun testFragment_rotate_profileDataHasYetToBeCollected() {
    initializeActivityAndAddFragment()

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("No data has been collected yet to upload"))))
  }

  @Test
  fun testFragment_eventLogged_waitingForUpload_rotate_profileDataIsCurrentlyUploading() {
    initializeActivityAndAddFragment()
    queueAnalyticsEvent()
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADING)

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("Profile data is currently uploading"))))
  }

  @Test
  fun testFragment_eventLogged_waitForUpload_rotate_profileDataIsUploaded() {
    initializeActivityAndAddFragment()
    queueAnalyticsEvent()
    flushEventWorkerQueue()

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
      .check(matches(withText(containsString("All profile data has been uploaded"))))
  }

  @Test
  fun testFragment_eventsLogged_networkError_rotate_profileDataHasError() {
    initializeActivityAndAddFragment()
    queueAnalyticsEvent()
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.NETWORK_ERROR)

    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    onView(syncStatusAt(position = 2))
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

  private fun initializeActivityAndAddFragment() {
    activityRule.scenario.onActivity { activity ->
      activity.setContentView(R.layout.test_activity)

      activity.supportFragmentManager.beginTransaction()
        .add(R.id.test_fragment_placeholder, ProfileAndDeviceIdFragment())
        .commitNow()
    }
    testCoroutineDispatchers.runCurrent()
  }

  private fun deviceIdLabelAt(position: Int) = profileListItemAt(position, R.id.device_id_label)

  private fun deviceIdCopyButtonAt(position: Int) =
    profileListItemAt(position, R.id.device_id_copy_button)

  private fun profileNameAt(position: Int) =
    profileListItemAt(position, R.id.profile_id_view_profile_name)

  private fun learnerIdAt(position: Int) =
    profileListItemAt(position, R.id.profile_id_view_learner_id)

  private fun learnerIdCopyButtonAt(position: Int) =
    profileListItemAt(position, R.id.learner_id_copy_button)

  private fun syncStatusAt(position: Int) =
    profileListItemAt(position, R.id.learner_analytics_sync_status_text_view)

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
        /* label= */ "Label of text from another app", /* text= */ "Text copied from another app"
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun queueAnalyticsEvent() {
    learnerAnalyticsLogger.logAppInForeground(
      installationId = "test_install_id", learnerId = "test_learner_id"
    )
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
      LogStorageModule::class, CachingTestModule::class, PrimeTopicAssetsControllerModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      SyncStatusModule::class, SplitScreenInteractionModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
}
