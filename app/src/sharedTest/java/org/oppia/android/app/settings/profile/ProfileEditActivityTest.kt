package org.oppia.android.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileEditActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileEditActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_startActivityWithAdminProfile_checkAdminInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        0
      )
    ).use {
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Admin"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithAdminProfile_checkAdminInfoIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Admin"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Admin")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches(not(isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_startActivityWithUserProfile_checkUserInfoIsDisplayed() {
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Ben"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_allow_download_container)).check(matches((isDisplayed())))
      onView(withId(R.id.profile_delete_button)).check(matches((isDisplayed())))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_checkUserInfoIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_bar)).check(matches(hasDescendant(withText("Ben"))))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("Ben")))
      onView(withId(R.id.profile_edit_allow_download_container)).perform(scrollTo())
        .check(matches((isDisplayed())))
      onView(withId(R.id.profile_delete_button))
        .perform(scrollTo())
        .check(
          matches(
            (
              isDisplayed()
              )
          )
        )
    }
  }

  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickRenameButton_checkOpensProfileRenameActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_rename_button)).perform(click())
      intended(hasComponent(ProfileRenameActivity::class.java.name))
    }
  }

  @Test
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickRenameButton_checkOpensProfileRenameActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_button)).perform(click())
      intended(hasComponent(ProfileRenameActivity::class.java.name))
    }
  }

  @Test
  fun testProfileEditActivity_startActivityWithUserProfile_clickResetPin_checkOpensProfileResetPinActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_reset_button)).perform(click())
      intended(hasComponent(ProfileResetPinActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickResetPin_checkOpensProfileResetPinActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_reset_button)).perform(scrollTo()).perform(click())
      intended(hasComponent(ProfileResetPinActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_startActivityWithUserProfile_clickProfileDeletionButton_checkOpensDeletionDialog() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickProfileDeletionButton_checkOpensDeletionDialog() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_message))
        .check(
          matches(
            isDisplayed()
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_startActivityWithUserProfile_clickProfileDeletionButton_clickDelete_checkReturnsToProfileListActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_delete_button)).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_positive)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithUserProfile_clickProfileDeletionButton_clickDelete_checkReturnsToProfileListActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_delete_button)).perform(scrollTo()).perform(click())
      onView(withText(R.string.profile_edit_delete_dialog_positive)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_startActivityWithUserHasDownloadAccess_checkSwitchIsChecked() {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        3
      )
    ).use {
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }

  @Test
  // TODO(#973): Fix ProfileEditActivityTest
  @Ignore
  fun testProfileEditActivity_configurationChange_startActivityWithUserHasDownloadAccess_checkSwitchIsChecked() { // ktlint-disable max-line-length
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
    ActivityScenario.launch<ProfileEditActivity>(
      ProfileEditActivity.createProfileEditActivity(
        context,
        3
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_edit_allow_download_switch)).check(matches(isChecked()))
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileEditActivityTest: ProfileEditActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileEditActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileEditActivityTest: ProfileEditActivityTest) {
      component.inject(profileEditActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
