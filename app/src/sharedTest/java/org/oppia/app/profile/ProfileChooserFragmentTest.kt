package org.oppia.app.profile

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TIMEOUT = 1000L
private const val CONDITION_CHECK_INTERVAL = 100L

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileChooserFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileChooserFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
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
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("Admin")
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_is_admin_text
        )
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("Ben")
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1, R.id.profile_is_admin_text
        )
      ).check(
        matches(not(isDisplayed()))
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          3, R.id.add_profile_text
        )
      ).check(
        matches(
          withText(context.getString(R.string.profile_chooser_add))
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_initializeProfiles_checkProfilesLastVisitedTimeIsShown() {
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(atPosition(R.id.profile_recycler_view, 0)).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
      onView(withId(R.id.input_pin)).perform(typeText("12345"))
      intended(hasComponent(HomeActivity::class.java.name))
      onView(isRoot()).perform(pressBack())
      onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_last_visited
        )
      ).check(
        matches(
          isDisplayed()
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_last_visited
        )
      ).check(
        matches(
          withText(
            String.format(
              getResources().getString(R.string.profile_last_used) + " just now"
            )
          )
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_initializeProfiles_changeConfiguration_checkProfilesLastVisitedTimeIsShown() { // ktlint-disable max-length-line
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(
        atPosition(
          R.id.profile_recycler_view,
          0
        )
      ).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
      onView(withId(R.id.input_pin)).perform(typeText("12345"))
      intended(hasComponent(HomeActivity::class.java.name))
      onView(isRoot()).perform(pressBack())
      onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_last_visited
        )
      ).check(
        matches(
          isDisplayed()
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_last_visited
        )
      ).check(
        matches(
          withText(
            String.format(
              getResources().getString(R.string.profile_last_used) + " just now"
            )
          )
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_last_visited
        )
      ).check(
        matches(
          withText(
            String.format(
              getResources().getString(R.string.profile_last_used) + " just now"
            )
          )
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("Admin")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("A")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          2, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("B")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          3, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("Ben")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          4, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("C")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          5
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          5, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("D")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          6
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          6, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("E")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          7
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          7, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("F")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          8
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          8, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("G")
        )
      )
      onView(withId(R.id.profile_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          9
        )
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          9, R.id.profile_name_text
        )
      ).check(
        matches(
          withText("H")
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_clickProfile_checkOpensPinPasswordActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      onView(atPosition(R.id.profile_recycler_view, 0)).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_clickAddProfile_checkOpensAdminAuthActivity_onBackButton_opensProfileChooserFragment() { // ktlint-disable max-line-length
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(atPosition(R.id.profile_recycler_view, 3)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(AdminAuthActivity.getIntentKey(), 1))
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.admin_auth_toolbar))))
        .check(matches(withText(context.resources.getString(R.string.add_profile_title))))
      onView(withText(context.resources.getString(R.string.admin_auth_heading))).check(
        matches(
          isDisplayed()
        )
      )
      onView(withText(context.resources.getString(R.string.admin_auth_sub))).check(
        matches(
          isDisplayed()
        )
      )
      onView(isRoot()).perform(closeSoftKeyboard(), pressBack())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_clickAdminControls_checkOpensAdminAuthActivity_onBackButton_opensProfileChooserFragment() { // ktlint-disable max-line-length
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(AdminAuthActivity.getIntentKey(), 0))
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.admin_auth_toolbar))))
        .check(matches(withText(context.resources.getString(R.string.administrator_controls))))
      onView(withText(context.resources.getString(R.string.admin_auth_heading))).check(
        matches(
          isDisplayed()
        )
      )
      onView(withText(context.resources.getString(R.string.admin_auth_admin_controls_sub))).check(
        matches(isDisplayed())
      )
      onView(isRoot()).perform(closeSoftKeyboard(), pressBack())
      onView(withId(R.id.administrator_controls_linear_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_clickAdminProfileWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile(
      "Admin",
      "",
      null,
      true,
      -10710042,
      true
    )
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1,
          R.id.add_profile_item
        )
      ).perform(click())
      waitUntilActivityVisible<AdminPinActivity>()
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_clickAdminControlsWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile(
      "Admin",
      "",
      null,
      true,
      -10710042,
      true
    )
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_changeConfiguration_checkSpanCount_hasSpanCount2() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(isRoot()).perform(orientationLandscape())
      it.onActivity { activity ->
        val profileRecyclerView = activity.findViewById<RecyclerView>(
          R.id.profile_recycler_view
        )
        val layoutManager = profileRecyclerView.layoutManager as GridLayoutManager
        if (!activity.resources.getBoolean(R.bool.isTablet)) {
          assertThat(layoutManager.spanCount).isEqualTo(2)
        }
      }
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_checkLayoutManager_isLinearLayoutManager() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      it.onActivity { activity ->
        val profileRecyclerView = activity.findViewById<RecyclerView>(
          R.id.profile_recycler_view
        )
        val layoutManager = profileRecyclerView
          .layoutManager as LinearLayoutManager
        assertThat(layoutManager.orientation).isEqualTo(LinearLayoutManager.VERTICAL)
      }
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_onlyAdminProfile_checkText_setUpMultipleProfilesIsVisible() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.add_profile_text)).check(
        matches(withText(R.string.set_up_multiple_profiles))
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_onlyAdminProfile_checkDescriptionText_isDisplayed() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1, R.id.add_profile_description_text
        )
      )
        .check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_multipleProfiles_checkText_addProfileIsVisible() {
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(atPositionOnView(R.id.profile_recycler_view, 3, R.id.add_profile_text))
        .check(matches(withText(R.string.profile_chooser_add)))
    }
  }

  @Test
  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  fun testProfileChooserFragment_multipleProfiles_checkDescriptionText_isDisplayed() {
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          3, R.id.add_profile_description_text
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  private fun getCurrentActivity(): Activity? {
    var currentActivity: Activity? = null
    getInstrumentation().runOnMainSync {
      run {
        currentActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
          Stage.RESUMED
        ).elementAtOrNull(0)
      }
    }
    return currentActivity
  }

  private inline fun <reified T : Activity> isVisible(): Boolean {
    val am =
      InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
        ACTIVITY_SERVICE
      ) as ActivityManager
    val visibleActivityName = this.getCurrentActivity()!!::class.java.name
    return visibleActivityName == T::class.java.name
  }

  private inline fun <reified T : Activity> waitUntilActivityVisible() {
    val startTime = System.currentTimeMillis()
    while (!isVisible<T>()) {
      Thread.sleep(CONDITION_CHECK_INTERVAL)
      if (System.currentTimeMillis() - startTime >= TIMEOUT) {
        throw AssertionError(
          "Activity ${T::class.java.simpleName} not visible after $TIMEOUT milliseconds"
        )
      }
    }
  }

  private fun createProfileChooserActivityIntent(): Intent {
    return ProfileChooserActivity
      .createProfileChooserActivity(ApplicationProvider.getApplicationContext())
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest) {
      component.inject(profileChooserFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
