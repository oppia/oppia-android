package org.oppia.android.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.testing.NavigationDrawerTestActivity
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AdministratorControlsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AdministratorControlsActivityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

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
    DaggerAdministratorControlsActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdminControls_withAdminProfile_openAdminControlsFromNavDrawer_onBackPressed_showsHome() {
    ActivityScenario
      .launch<NavigationDrawerTestActivity>(
        createNavigationDrawerActivityIntent(0)
      )
      .use {
        onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
        onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
          .perform(nestedScrollTo()).perform(click())
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
        intended(hasExtra(AdministratorControlsActivity.getIntentKey(), 0))
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.home_recycler_view)).check(matches(isDisplayed()))
      }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_loadFragment_displayGeneralAndProfileManagement() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          0, R.id.general_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          0, R.id.edit_account_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_edit_account
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          1,
          R.id.profile_management_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          1, R.id.edit_profiles_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_edit_profiles
              )
            )
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_loadFragment_displayDownloadPermissionsAndSettings() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.download_permissions_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_download_permissions_label
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_constraint_layout
        )
      )
        .check(matches(isDisplayed()))
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.auto_update_topic_constraint_layout
        )
      )
        .check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_loadFragment_displayApplicationSettings() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          3, R.id.app_information_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          3, R.id.app_version_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_app_version
              )
            )
          )
        )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          4, R.id.account_actions_text_view
        )
      )
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          4, R.id.log_out_text_view
        )
      )
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.administrator_controls_log_out
              )
            )
          )
        )
    }
  }

  @Test
  fun testAdminControls_topicUpdateOnWifiSwitchIsNotChecked_autoUpdateTopicSwitchIsNotChecked() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2,
          R.id.topic_update_on_wifi_switch
        )
      )
        .check(matches(not(isChecked())))
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.administrator_controls_list,
          2, R.id.auto_update_topic_switch
        )
      )
        .check(matches(not(isChecked())))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdminControls_topicUpdateOnWifiSwitchIsChecked_configChange_checkIfSwitchIsChecked() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    )
    onView(withId(R.id.administrator_controls_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        2
      )
    )
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.topic_update_on_wifi_switch
      )
    )
      .check(matches(not(isChecked())))
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.auto_update_topic_switch
      )
    )
      .check(matches(not(isChecked())))
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.topic_update_on_wifi_switch
      )
    )
      .perform(click())
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.administrator_controls_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        2
      )
    )
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.topic_update_on_wifi_switch
      )
    )
      .check(matches(isChecked()))
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.auto_update_topic_switch
      )
    )
      .check(matches(not(isChecked())))
    onView(isRoot()).perform(orientationPortrait())
    onView(withId(R.id.administrator_controls_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        2
      )
    )
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.topic_update_on_wifi_switch
      )
    )
      .check(matches(isChecked()))
    onView(
      atPositionOnView(
        R.id.administrator_controls_list,
        2, R.id.auto_update_topic_switch
      )
    )
      .check(matches(not(isChecked())))
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdminControls_onClickTopicUpdateOnWifiSwitch_reopen_switchRemainsChecked() {
    ActivityScenario
      .launch<NavigationDrawerTestActivity>(
        createNavigationDrawerActivityIntent(profileId = 0)
      )
      .use {
        onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
        onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
          .perform(nestedScrollTo())
          .perform(click())
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
        onView(
          atPositionOnView(
            R.id.administrator_controls_list,
            2,
            R.id.topic_update_on_wifi_switch
          )
        )
          .check(matches(isNotChecked()))
        onView(
          atPositionOnView(
            R.id.administrator_controls_list,
            2,
            R.id.topic_update_on_wifi_switch
          )
        )
          .perform(click())
        onView(isRoot()).perform(pressBack())
        onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
        onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
          .perform(click())
        onView(
          atPositionOnView(
            R.id.administrator_controls_list,
            2,
            R.id.topic_update_on_wifi_switch
          )
        )
          .check(matches(isChecked()))
      }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdminControls_clickEditProfile_opensProfileListActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.edit_profiles_text_view)).perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_clickLogoutButton_displaysLogoutDialog() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_changeConfiguration_clickLogout_displaysLogoutDialog() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  // TODO(#762): Replace [ProfileChooserActivity] to [LoginActivity] once it is added.
  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_clickOkButtonInLogoutDialog_opensProfileChooserActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_okay_button)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_clickCancelButtonInLogoutDialog_dialogDismissed() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        profileId = 0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(withId(R.id.log_out_text_view)).perform(click())
      onView(withText(R.string.log_out_dialog_message)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.log_out_dialog_cancel_button)).perform(click())
      onView(withId(R.id.log_out_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix AdministratorControlsActivityTest
  @Ignore
  fun testAdministratorControlsFragment_clickAppVersion_opensAppVersionActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.app_version_text_view)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
    }
  }

  private fun createAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  private fun createNavigationDrawerActivityIntent(profileId: Int): Intent {
    return NavigationDrawerTestActivity.createNavigationDrawerTestActivity(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return Matchers.allOf(
          ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(NestedScrollView::class.java))
        )
      }

      override fun perform(uiController: UiController, view: View) {
        try {
          val nestedScrollView =
            findFirstParentLayoutOfClass(view, NestedScrollView::class.java) as NestedScrollView
          nestedScrollView.scrollTo(0, view.getTop())
        } catch (e: Exception) {
          throw PerformException.Builder()
            .withActionDescription(this.description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(e)
            .build()
        }
        uiController.loopMainThreadUntilIdle()
      }
    }
  }

  private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View {
    var parent: ViewParent = FrameLayout(view.getContext())
    lateinit var incrementView: ViewParent
    var i = 0
    while (!(parent.javaClass === parentClass)) {
      if (i == 0) {
        parent = findParent(view)
      } else {
        parent = findParent(incrementView)
      }
      incrementView = parent
      i++
    }
    return parent as View
  }

  private fun findParent(view: View): ViewParent {
    return view.getParent()
  }

  private fun findParent(view: ViewParent): ViewParent {
    return view.getParent()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
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
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest)
  }
}
