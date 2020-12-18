package org.oppia.android.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
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
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AdministratorControlsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdministratorControlsActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdministratorControlsActivityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
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
  fun testAdministratorControlsFragment_loadFragment_displayGeneralAndProfileManagement() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 0,
        targetView = R.id.general_text_view
      )
      verifyTextOnAdministratorListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.edit_account_text_view,
        stringIdToMatch = R.string.administrator_controls_edit_account
      )
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 1,
        targetView = R.id.profile_management_text_view
      )
      verifyTextOnAdministratorListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.edit_profiles_text_view,
        stringIdToMatch = R.string.administrator_controls_edit_profiles
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayDownloadPermissionsAndSettings() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnAdministratorListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.download_permissions_text_view,
        stringIdToMatch = R.string.administrator_controls_download_permissions_label
      )
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 2,
        targetView = R.id.topic_update_on_wifi_constraint_layout
      )
      scrollToPosition(position = 2)
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 2,
        targetView = R.id.auto_update_topic_constraint_layout
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayApplicationSettings() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 3,
        targetView = R.id.app_information_text_view
      )
      verifyTextOnAdministratorListItemAtPosition(
        itemPosition = 3,
        targetViewId = R.id.app_version_text_view,
        stringIdToMatch = R.string.administrator_controls_app_version
      )
      verifyItemDisplayedOnAdministratorControlListItem(
        itemPosition = 4,
        targetView = R.id.account_actions_text_view
      )
      verifyTextOnAdministratorListItemAtPosition(
        itemPosition = 4,
        targetViewId = R.id.log_out_text_view,
        stringIdToMatch = R.string.administrator_controls_log_out
      )
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_topicUpdateOnWifiSwitchIsNotChecked_autoUpdateTopicSwitchIsNotChecked() { // ktlint-disable max-line-length
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      ).check(matches(not(isChecked())))
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testAdministratorControlsFragment_topicUpdateOnWifiSwitchIsChecked_configurationChange_checkIfSwitchIsChecked() { // ktlint-disable max-line-length
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      ).check(matches(not(isChecked())))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_switch
        )
      ).check(matches(not(isChecked())))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      ).check(matches(isChecked()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_switch
        )
      ).check(matches(not(isChecked())))
      onView(isRoot()).perform(orientationPortrait())
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      ).check(matches(isChecked()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_clickEditProfile_checkOpensProfileListActivity() { // ktlint-disable max-line-length
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.edit_profiles_text_view)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickLogoutButton_displaysLogoutDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_okay_button)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_cancel_button)
    }
  }

  @Test
  fun testAdministratorControlsFragment_changeConfiguration_clickLogout_displaysLogoutDialog() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_okay_button)
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_cancel_button)
    }
  }

  // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
  @Test
  fun testAdministratorControlsFragment_clickOkButtonInLogoutDialog_opensProfileChooserActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      onView(withText(R.string.log_out_dialog_okay_button)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickCancelButtonInLogoutDialog_dialogDismissed() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      onView(withId(R.id.log_out_text_view)).perform(click())
      verifyTextInDialog(textInDialogId = R.string.log_out_dialog_message)
      onView(withText(R.string.log_out_dialog_cancel_button)).perform(click())
      onView(withId(R.id.log_out_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickAppVersion_opensAppVersionActivity() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      onView(withId(R.id.app_version_text_view)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
    }
  }

  private fun createAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      context,
      profileId
    )
  }

  private fun verifyItemDisplayedOnAdministratorControlListItem(
    itemPosition: Int,
    targetView: Int
  ) {
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        itemPosition,
        targetView
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyTextOnAdministratorListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    @StringRes stringIdToMatch: Int
  ) {
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(context.getString(stringIdToMatch))))
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.administrator_controls_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun verifyTextInDialog(@StringRes textInDialogId: Int) {
    onView(withText(context.getString(textInDialogId)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
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

    fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdministratorControlsActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest) {
      component.inject(administratorControlsActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
