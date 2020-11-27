package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
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

/** Tests for [ProfileChooserFragment]. */
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

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
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
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      matchStringOnProfileListItem(
        position = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      matchStringOnProfileListItem(
        position = 0,
        targetView = R.id.profile_is_admin_text,
        stringToMatch = context.getString(R.string.profile_chooser_admin)
      )
      scrollToPosition(position = 1)
      matchStringOnProfileListItem(
        position = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1,
          R.id.profile_is_admin_text
        )
      ).check(matches(not(isDisplayed())))
      scrollToPosition(position = 3)
      matchStringOnProfileListItem(
        position = 3,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add)
      )
    }
  }

  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_showsJustNowText() {
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0,
          R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      matchStringOnProfileListItem(
        position = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now"
      )
    }
  }

  // TODO(#973): Fix ProfileChooserFragmentTest
  @Ignore
  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_changeConfiguration_showsJustNowText() {
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          0,
          R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      matchStringOnProfileListItem(
        position = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      matchStringOnProfileListItem(
        position = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      scrollToPosition(position = 1)
      matchStringOnProfileListItem(
        position = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A"
      )
      scrollToPosition(position = 2)
      matchStringOnProfileListItem(
        position = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B"
      )
      scrollToPosition(position = 3)
      matchStringOnProfileListItem(
        position = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
      scrollToPosition(position = 4)
      matchStringOnProfileListItem(
        position = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "C"
      )
      scrollToPosition(position = 5)
      matchStringOnProfileListItem(
        position = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "D"
      )
      scrollToPosition(position = 6)
      matchStringOnProfileListItem(
        position = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "E"
      )
      scrollToPosition(position = 7)
      matchStringOnProfileListItem(
        position = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "F"
      )
      scrollToPosition(position = 8)
      matchStringOnProfileListItem(
        position = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "G"
      )
      scrollToPosition(position = 9)
      matchStringOnProfileListItem(
        position = 9,
        targetView = R.id.profile_name_text,
        stringToMatch = "H"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_clickProfile_checkOpensPinPasswordActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(atPosition(R.id.profile_recycler_view, 0)).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminProfileWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile(
      "Admin",
      "",
      null,
      true,
      -10710042,
      true
    ).toLiveData()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1,
          R.id.add_profile_item
        )
      ).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControlsWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile(
      "Admin",
      "",
      null,
      true,
      -10710042,
      true
    ).toLiveData()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_checkLayoutManager_isLinearLayoutManager() {
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
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      matchStringOnProfileListItem(
        position = 1,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.set_up_multiple_profiles)
      )
    }
  }

  @Test
  fun testProfileChooserFragment_onlyAdminProfile_checkDescriptionText_isDisplayed() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          1,
          R.id.add_profile_description_text
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkText_addProfileIsVisible() {
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      matchStringOnProfileListItem(
        position = 3,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add)
      )
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkDescriptionText_isDisplayed() {
    profileTestHelper.initializeProfiles()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_recycler_view,
          3,
          R.id.add_profile_description_text
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  private fun createProfileChooserActivityIntent(): Intent {
    return ProfileChooserActivity
      .createProfileChooserActivity(ApplicationProvider.getApplicationContext())
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.profile_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun matchStringOnProfileListItem(position: Int, targetView: Int, stringToMatch: String) {
    onView(
      atPositionOnView(
        R.id.profile_recycler_view,
        position,
        targetView
      )
    ).check(matches(withText(stringToMatch)))
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
