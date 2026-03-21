package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.onboarding.IntroActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.AdminAuthActivity.Companion.ADMIN_AUTH_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.profile.AdminPinActivity.Companion.ADMIN_PIN_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
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
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.oppia.android.util.properties.CustomPropertyRetrieverProdModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileChooserFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileChooserFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileChooserFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  private val testProfileId = LegacyProfileId.newBuilder().setInternalId(0).build()
  private val testProfileId1 = LegacyProfileId.newBuilder().setInternalId(1).build()

  @Before
  fun setUp() {
    Intents.init()
    // TODO(#5835): Call setUpTestApplicationComponent() here once flag overrides init earlier.
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testProfileChooserActivity_hasCorrectLabel() {
    setUpTestApplicationComponent()
    launch(ProfileChooserActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
        // correct string when it's read out.
        assertThat(title).isEqualTo(context.getString(R.string.profile_chooser_activity_label))
      }
    }
  }

  @Test
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin",
        recyclerViewId = R.id.profile_recycler_view,
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_is_admin_text,
        stringToMatch = context.getString(R.string.profile_chooser_admin),
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 1, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben",
        recyclerViewId = R.id.profile_recycler_view
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.profile_is_admin_text,
        )
      ).check(matches(not(isDisplayed())))
      scrollToPosition(position = 3, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add),
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_showsJustNowText() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now",
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_changeConfiguration_showsJustNowText() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now",
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 1, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 2, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 3, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 4, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "C",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 5, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "D",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 6, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "E",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 7, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "F",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 8, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "G",
        recyclerViewId = R.id.profile_recycler_view
      )
      scrollToPosition(position = 9, recyclerViewId = R.id.profile_recycler_view)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 9,
        targetView = R.id.profile_name_text,
        stringToMatch = "H",
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_onboardingV1_clickAdminProfile_checkOpensPinPasswordActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
    }
  }

  @Test
  fun testMigrateProfiles_onboardingV2_clickAdminProfile_checkOpensProfileLoginActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = true)
    profileTestHelper.updateProfileType(testProfileId, ProfileType.SUPERVISOR)

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(ProfileLoginActivity::class.java.name))
    }
  }

  @Test
  fun testMigrateProfiles_onboardingV2_clickLearnerWithPin_checkOpensProfileLoginActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = true)

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 1
        )
      ).perform(click())
      intended(hasComponent(ProfileLoginActivity::class.java.name))
    }
  }

  @Test
  fun testMigrateProfiles_onboardingV2_clickLearnerWithoutPin_checkOpensIntroActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileManagementController.addProfile(
      name = "Learner",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )
    profileTestHelper.markProfileOnboardingStarted(testProfileId1)

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 1
        )
      ).perform(click())
      intended(hasComponent(IntroActivity::class.java.name))
    }
  }

  @Test
  fun testMigrateProfiles_onboardingV2_clickLearnerWithoutPin_checkIntroActivityHasNoStepCount() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileManagementController.addProfile(
      name = "Learner",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 1
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.onboarding_step_count_four)).check(doesNotExist())
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminProfileWithNoPin_checkOpensAdminPinActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.add_profile_item
        )
      ).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
      intended(hasExtraWithKey(ADMIN_PIN_ACTIVITY_PARAMS_KEY))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControlsWithNoPin_checkOpensAdminControlsActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
      it.onActivity { activity ->
        assertThat(
          activity.intent.extractCurrentUserProfileId().internalId
        ).isEqualTo(0)
      }
    }
  }

  @Test
  fun testProfileChooserFragment_checkLayoutManager_isLinearLayoutManager() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
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
  fun testProfileChooserFragment_onlyAdminProfile_checkText_setUpMultipleProfilesIsVisible() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.set_up_multiple_profiles),
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_onlyAdminProfile_checkDescriptionText_isDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.add_profile_description_text
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkText_addProfileIsVisible() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add),
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkDescriptionText_isDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 4,
          targetViewId = R.id.add_profile_description_text
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControls_opensAdminAuthActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtraWithKey(ADMIN_AUTH_ACTIVITY_PARAMS_KEY))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAddProfile_opensAdminAuthActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 4
        )
      ).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtraWithKey(ADMIN_AUTH_ACTIVITY_PARAMS_KEY))
      intended(hasProtoExtra(PROFILE_ID_INTENT_DECORATOR, testProfileId))
    }
  }

  @Test
  fun testProfileChooserFragment_clickProfile_opensHomeActivity() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_chooser_item
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(HomeActivity::class.java.name))
      hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR)
    }
  }

  @Test
  fun testProfileChooserFragment_enableClassrooms_clickProfile_opensClassroomListActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_chooser_item
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(ClassroomListActivity::class.java.name))
      hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR)
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_checkAddProfileTextIsDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_selection_add_profile_text)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_configChange_checkAddProfileTextIsDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      orientationLandscape()
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.profile_selection_add_profile_text)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_landscapeMode_checkScrollArrowsAreDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_list_scroll_left)).check(matches(isDisplayed()))
      onView(withId(R.id.profile_list_scroll_right)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_landscape_shortList_checkScrollArrowsAreNotDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_list_scroll_left)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.profile_list_scroll_right)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_ltr_checkListIsSortedAlphabetically() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      it.onActivity {
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 0)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 0,
          targetView = R.id.profile_name_text,
          stringToMatch = "Admin",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 1)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 1,
          targetView = R.id.profile_name_text,
          stringToMatch = "A",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 2)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 2,
          targetView = R.id.profile_name_text,
          stringToMatch = "B",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 3)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 3,
          targetView = R.id.profile_name_text,
          stringToMatch = "C",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 4)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 4,
          targetView = R.id.profile_name_text,
          stringToMatch = "D",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 5)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 5,
          targetView = R.id.profile_name_text,
          stringToMatch = "E",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 6)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 6,
          targetView = R.id.profile_name_text,
          stringToMatch = "F",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 7)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 7,
          targetView = R.id.profile_name_text,
          stringToMatch = "G",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 8)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 8,
          targetView = R.id.profile_name_text,
          stringToMatch = "H",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 9)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 9,
          targetView = R.id.profile_name_text,
          stringToMatch = "I",
          recyclerViewId = R.id.profiles_list_landscape
        )
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_ltr_checkRightArrowScrollsListToTheRight() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Click twice to scroll to the end of the list.
      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      it.onActivity {
        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 8,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("H"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 9,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("I"), isDisplayed())
          )
        )
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_ltr_checkLeftArrowScrollsListToTheLeft() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Click twice to scroll to the end of the list.
      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Click twice to scroll to the beginning of the list.
      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      it.onActivity {
        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 0,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("Admin"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 1,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("A"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 2,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("B"), isDisplayed())
          )
        )
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  fun testFragment_enableOnboardingV2_rtl_checkListIsSortedAlphabetically() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)

    launch(ProfileChooserActivity::class.java).use {
      it.onActivity {
        testCoroutineDispatchers.runCurrent()

        // Verify that the display locale is set up correctly (for string formatting).
        val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
        val localeContext = displayLocale.localeContext
        assertThat(localeContext.languageDefinition.language)
          .isEqualTo(OppiaLanguage.ARABIC)

        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 0)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 0,
          targetView = R.id.profile_name_text,
          stringToMatch = "Admin",
          recyclerViewId = R.id.profiles_list_landscape
        )
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 1,
          targetView = R.id.profile_name_text,
          stringToMatch = "A",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 2)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 2,
          targetView = R.id.profile_name_text,
          stringToMatch = "B",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 3)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 3,
          targetView = R.id.profile_name_text,
          stringToMatch = "C",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 4)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 4,
          targetView = R.id.profile_name_text,
          stringToMatch = "D",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 5)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 5,
          targetView = R.id.profile_name_text,
          stringToMatch = "E",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 6)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 6,
          targetView = R.id.profile_name_text,
          stringToMatch = "F",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 7)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 7,
          targetView = R.id.profile_name_text,
          stringToMatch = "G",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 8)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 8,
          targetView = R.id.profile_name_text,
          stringToMatch = "H",
          recyclerViewId = R.id.profiles_list_landscape
        )
        scrollToPosition(recyclerViewId = R.id.profiles_list_landscape, position = 9)
        verifyTextOnProfileListItemAtPosition(
          itemPosition = 9,
          targetView = R.id.profile_name_text,
          stringToMatch = "I",
          recyclerViewId = R.id.profiles_list_landscape
        )
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  fun testFragment_enableOnboardingV2_rtl_checkLeftArrowScrollsListToTheRight() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language)
        .isEqualTo(OppiaLanguage.ARABIC)

      // Click twice to scroll to the end of the list.
      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      it.onActivity {
        // Verify that the list has scrolled away from the beginning.
        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 0,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("Admin"), not(isDisplayed()))
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 8,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("H"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 9,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("I"), isDisplayed())
          )
        )
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  fun testFragment_enableOnboardingV2_rtl_checkRightArrowScrollsListToTheLeft() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language)
        .isEqualTo(OppiaLanguage.ARABIC)

      // Click twice to scroll to the end of the list.
      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_left)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Click twice to scroll to the beginning of the list.
      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_list_scroll_right)).perform(click())
      testCoroutineDispatchers.runCurrent()

      it.onActivity {
        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 0,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("Admin"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 1,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("A"), isDisplayed())
          )
        )

        onView(
          withViewAtPosition(
            recyclerViewId = R.id.profiles_list_landscape,
            position = 2,
            targetViewId = R.id.profile_name_text,
            itemMatcher = allOf(withText("B"), isDisplayed())
          )
        )
      }
    }
  }

  @Test
  fun testProfileChooserFragment_enableOnboardingV2_clickAddProfileButton_opensAdminAuthActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_button)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasProtoExtra(PROFILE_ID_INTENT_DECORATOR, testProfileId))
    }
  }

  @Test
  fun testProfileChooserFragment_enableOnboardingV2_clickAddProfilePrompt_opensAdminAuthActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.add_profile_prompt)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_enableOnboardingV2_initializeProfiles_checkProfilesAreShown() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_is_admin_text,
        stringToMatch = context.getString(R.string.profile_chooser_admin)
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_enableOnboardingV2_afterVisitingHomeActivity_showsJustNowText() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profiles_list,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now"
      )
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_enableOnboardingV2_landscapeMode_afterVisitingHome_showsJustNowText() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profiles_list_landscape,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now",
        recyclerViewId = R.id.profiles_list_landscape,
      )
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_addManyProfiles_checkAddProfileButtonIsNotVisible() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(9)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A"
      )
      scrollToPosition(position = 2)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B"
      )
      scrollToPosition(position = 3)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "C"
      )
      scrollToPosition(position = 4)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "D"
      )
      scrollToPosition(position = 5)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "E"
      )
      scrollToPosition(position = 6)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "F"
      )
      scrollToPosition(position = 7)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "G"
      )
      scrollToPosition(position = 8)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "H"
      )
      scrollToPosition(position = 9)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 9,
        targetView = R.id.profile_name_text,
        stringToMatch = "I"
      )
      onView(withId(R.id.add_profile_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.add_profile_prompt)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_addLessThan10Profiles_checkAddProfileButtonIsVisible() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A"
      )
      scrollToPosition(position = 2)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B"
      )
      scrollToPosition(position = 3)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "C"
      )
      scrollToPosition(position = 4)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "D"
      )
      scrollToPosition(position = 5)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "E"
      )
      scrollToPosition(position = 6)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "F"
      )
      scrollToPosition(position = 7)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "G"
      )
      scrollToPosition(position = 8)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "H"
      )
      onView(withId(R.id.add_profile_button)).check(matches(isDisplayed()))
      onView(withId(R.id.add_profile_prompt)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_addManyProfiles_checkProfilesSortedAlphabetically() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A"
      )
      scrollToPosition(position = 2)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B"
      )
      scrollToPosition(position = 3)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
      scrollToPosition(position = 4)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "C"
      )
      scrollToPosition(position = 5)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "D"
      )
      scrollToPosition(position = 6)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "E"
      )
      scrollToPosition(position = 7)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "F"
      )
      scrollToPosition(position = 8)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "G"
      )
      scrollToPosition(position = 9)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 9,
        targetView = R.id.profile_name_text,
        stringToMatch = "H"
      )
    }
  }

  @Test
  fun testFragment_enableOnboardingV2_clickAdminProfileWithPin_checkOpensProfileLoginActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileTestHelper.updateProfileType(testProfileId, ProfileType.SUPERVISOR)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(ProfileLoginActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_clickLearnerWithoutPin_alreadyOnboarded_checkOpensHomeActivity() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfile()
    profileManagementController.addProfile(
      name = "Learner",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    )
    profileTestHelper.markProfileOnboardingEnded(testProfileId1)

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profiles_list,
          position = 1
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooser_afterDataReset_showsAdminProfile() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    testCoroutineDispatchers.runCurrent()

    // Perform data reset by deleting all profiles.
    profileManagementController.deleteAllProfiles()
    testCoroutineDispatchers.runCurrent()

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      // In v1 onboarding flow, deleteAllProfiles() does not update the in-memory cache
      // (the app is expected to be forcibly closed after deletion), so the admin profile
      // is still shown in the recycler view.
      onView(withId(R.id.profile_recycler_view))
        .check(matches(isDisplayed()))
      scrollToPosition(
        recyclerViewId = R.id.profile_recycler_view,
        position = 0
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin",
        recyclerViewId = R.id.profile_recycler_view
      )
    }
  }

  @Test
  fun testProfileChooser_enableOnboardingV2_afterDataReset_showsEmptyProfileList() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    testCoroutineDispatchers.runCurrent()

    // Perform data reset by deleting all profiles.
    profileManagementController.deleteAllProfiles()
    testCoroutineDispatchers.runCurrent()

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      // In v2 onboarding flow, the profile list is empty after data reset.
      onView(withId(R.id.profiles_list))
        .check(matches(isDisplayed()))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profiles_list,
          position = 0,
          targetViewId = R.id.profile_name_text
        )
      ).check(doesNotExist())
    }
  }

  @Test
  fun testProfileChooser_afterDataReset_selectingAdminProfile_doesNotCrash() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    testCoroutineDispatchers.runCurrent()

    // Perform data reset by deleting all profiles.
    profileManagementController.deleteAllProfiles()
    testCoroutineDispatchers.runCurrent()

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      // In v1 onboarding flow, the admin profile is still displayed due to in-memory
      // cache. Verify that the admin profile is present and clickable without crashing.
      scrollToPosition(
        recyclerViewId = R.id.profile_recycler_view,
        position = 0
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin",
        recyclerViewId = R.id.profile_recycler_view
      )
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooser_afterDataReset_classroomsEnabled_selectingAdminProfile_doesNotCrash() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = false)
    testCoroutineDispatchers.runCurrent()

    // Perform data reset by deleting all profiles.
    profileManagementController.deleteAllProfiles()
    testCoroutineDispatchers.runCurrent()

    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      // In v1 onboarding flow with classrooms enabled, the admin profile is still
      // displayed due to in-memory cache. Verify it is clickable and navigates
      // to ClassroomListActivity.
      scrollToPosition(
        recyclerViewId = R.id.profile_recycler_view,
        position = 0
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin",
        recyclerViewId = R.id.profile_recycler_view
      )
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun withViewAtPosition(
    recyclerViewId: Int,
    position: Int,
    targetViewId: Int,
    itemMatcher: Matcher<View?>
  ): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description) {
        description.appendText("View at position $position in RecyclerView with ID $recyclerViewId")
        itemMatcher.describeTo(description)
      }

      override fun matchesSafely(view: View): Boolean {
        val recyclerView = view.rootView.findViewById<RecyclerView>(recyclerViewId) ?: return false
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return false
        val targetView = viewHolder.itemView.findViewById<View>(targetViewId) ?: return false

        return itemMatcher.matches(targetView)
      }
    }
  }

  private fun verifyTextOnProfileListItemAtPosition(
    itemPosition: Int,
    targetView: Int,
    stringToMatch: String,
    recyclerViewId: Int = R.id.profiles_list
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = recyclerViewId,
        position = itemPosition,
        targetViewId = targetView
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun createProfileChooserActivityIntent(): Intent {
    return ProfileChooserActivity
      .createProfileChooserActivity(ApplicationProvider.getApplicationContext())
  }

  private fun scrollToPosition(recyclerViewId: Int = R.id.profiles_list, position: Int) {
    onView(withId(recyclerViewId)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      CustomPropertyRetrieverProdModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestPlatformParameterModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
  }
}
