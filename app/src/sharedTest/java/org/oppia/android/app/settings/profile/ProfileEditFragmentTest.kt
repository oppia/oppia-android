package org.oppia.android.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ProfileEditFragmentTestActivity
import org.oppia.android.app.testing.ProfileEditFragmentTestActivity.Companion.createProfileEditFragmentTestActivity
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
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
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
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileEditFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileEditFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class ProfileEditFragmentTest {

  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    TestPlatformParameterModule.reset()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProfileEdit_startWithUserProfile_clickProfileDeletionButton_checkOpensDeletionDialog() {
    launchFragmentTestActivity(internalProfileId = 1).use {
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileEdit_configChange_startWithUserProfile_clickDelete_checkOpensDeletionDialog() {
    launchFragmentTestActivity(internalProfileId = 1).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileEdit_startWithUserProfile_clickDelete_configChange_checkDeletionDialogIsVisible() {
    launchFragmentTestActivity(internalProfileId = 1).use {
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .inRoot(isDialog()).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testProfileEdit_startWithUserHasDownloadAccess_downloadsDisabled_switchIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileEdit_userDoesNotHaveDownloadAccess_downloadDisabled_switchIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileEdit_startWithUserHasDownloadAccess_downloadsEnabled_checkSwitchIsChecked() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileEdit_configChange_userHasDownloadAccess_downloadsEnabled_checkSwitchIsChecked() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    val addProfileProvider = profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )
    monitorFactory.waitForNextSuccessfulResult(addProfileProvider)
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }

  @Test
  fun testProfileEdit_userHasDownloadAccess_downloadsEnabled_clickAllowDownloads_checkChanged() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
      onView(withId(R.id.profile_edit_allow_download_container)).perform(click())
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testProfileEdit_userDoesNotHaveDownloadAccess_downloadsEnabled_switchIsNotClickable() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileEdit_userHasDownloadAccess_downloadsEnabled_switchContainerIsFocusable() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(isFocusable()))
    }
  }

  @Test
  fun testProfileEdit_startWithUserHasDownloadAccess_downloadsEnabled_switchContainerIsDisplayed() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    launchFragmentTestActivity(internalProfileId = 4).use {
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileEdit_userDoesNotHaveDownloadAccess_downloadsEnabled_switchIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(true)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileEdit_studyOff_doesNotHaveMarkChaptersCompletedButton() {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(false)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_mark_chapters_for_completion_button))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileEdit_studyOn_hasMarkChaptersCompletedButton() {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(true)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_mark_chapters_for_completion_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileEdit_studyOn_landscape_hasMarkChaptersCompletedButton() {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(true)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_mark_chapters_for_completion_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileEdit_studyOn_clickMarkChapsCompleted_opensMarkCompleteActivityForProfile() {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(true)
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_mark_chapters_for_completion_button)).perform(click())

      intended(hasComponent(MarkChaptersCompletedActivity::class.java.name))
      intended(hasExtra("MarkChaptersCompletedActivity.profile_id", 0))
      intended(hasExtra("MarkChaptersCompletedActivity.show_confirmation_notice", true))
    }
  }

  @Test
  fun testProfileEdit_featureOff_doesNotHaveEnableQuickSwitchingSwitch() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(false)

    // Without the study feature enabled, the switch should not be visible.
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_container))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileEdit_featureOn_hasEnableQuickSwitchingSwitch() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)

    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_container))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileEdit_featureOn_landscape_hasEnableQuickSwitchingSwitch() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)

    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_container))
        .perform(scrollTo())

      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_container))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileEdit_featureOn_doNotHaveSwitchingPermission_enableLanguageSwitchingIsOff() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)

    // Without the permission to switch languages, the setting should be off by default.
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_switch))
        .check(matches(not(isChecked())))
    }
  }

  @Test
  fun testProfileEdit_featureOn_hasSwitchingPermission_enableLanguageSwitchingIsOn() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)

    val updateLangProvider = profileManagementController.updateEnableInLessonQuickLanguageSwitching(
      profileId = ProfileId.newBuilder().apply { internalId = 0 }.build(),
      allowInLessonQuickLanguageSwitching = true
    )
    monitorFactory.waitForNextSuccessfulResult(updateLangProvider)

    // With the permission to switch languages, the setting should be on by default.
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_switch))
        .check(matches(isChecked()))
    }
  }

  @Test
  fun testProfileEdit_featureOn_doNotClickEnableLanguageSwitching_doesNotHaveSwitchingPermission() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)
    // Open the UI, but don't interact with it.
    launchFragmentTestActivity(internalProfileId = 0).use {}

    // The user should not have permission to switch languages (since the switch wasn't toggled).
    val profileProvider =
      profileManagementController.getProfile(
        ProfileId.newBuilder().apply { internalId = 0 }.build()
      )
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)

    assertThat(profile.allowInLessonQuickLanguageSwitching).isFalse()
  }

  @Test
  fun testProfileEdit_studyOn_clickEnableLanguageSwitching_hasSwitchingPermission() {
    TestPlatformParameterModule.forceEnableFastLanguageSwitchingInLesson(true)

    // Enable language switching in the UI.
    launchFragmentTestActivity(internalProfileId = 0).use {
      onView(withId(R.id.profile_edit_enable_in_lesson_language_switching_container))
        .perform(click())
    }

    // The user should have permission to switch languages (since the switch was toggled).
    val profileProvider =
      profileManagementController.getProfile(
        ProfileId.newBuilder().apply { internalId = 0 }.build()
      )
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.allowInLessonQuickLanguageSwitching).isTrue()
  }

  private fun launchFragmentTestActivity(internalProfileId: Int) =
    launch<ProfileEditFragmentTestActivity>(
      createProfileEditFragmentTestActivity(context, internalProfileId)
    ).also { testCoroutineDispatchers.runCurrent() }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileEditFragmentTest: ProfileEditFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileEditFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileEditFragmentTest: ProfileEditFragmentTest) =
      component.inject(profileEditFragmentTest)

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
