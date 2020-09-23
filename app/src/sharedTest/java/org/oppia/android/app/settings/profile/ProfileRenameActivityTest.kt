package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
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
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileRenameActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileRenameActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

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
  // TODO(#973): Fix ProfileRenameActivityTest
  @Ignore
  fun testProfileRenameActivity_inputNewName_clickSave_checkNameIsSaved() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
      onView(withId(R.id.profile_edit_name)).check(matches(withText("James")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNewName_configurationChange_checkSaveIsEnabled() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNewName_configurationChange_inputTextExists() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("James"))
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).check(
        matches(
          withText("James")
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileRenameActivityTest
  @Ignore
  fun testProfileRenameActivity_inputOldName_clickSave_checkNameNotUniqueError() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("Admin"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  // TODO(#973): Fix ProfileRenameActivityTest
  @Ignore
  fun testProfileRenameActivity_inputOldName_clickSave_inputName_checkErrorIsCleared() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("Admin"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText(" "))
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("123"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_only_letters))))
    }
  }

  @Test
  // TODO(#973): Fix ProfileRenameActivityTest
  @Ignore
  fun testProfileRenameActivity_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText("123"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(
        allOf(
          withId(R.id.input),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).perform(typeText(" "))
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testProfileRenameActivity_inputName_changeConfiguration_checkNameIsDisplayed() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("test"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).check(
        matches(
          withText("test")
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileRenameActivityTest
  @Ignore
  fun testProfileRenameActivity_inputOldName_clickSave_changeConfiguration_errorIsVisible() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_name)))).perform(
        typeText("Admin"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_name))
        )
      ).check(matches(withText(context.getString(R.string.add_profile_error_name_not_unique))))
    }
  }

  @Test
  fun testProfileRenameActivity_clickSave_changeConfiguration_saveButtonIsNotClickable() {
    launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context,
        1
      )
    ).use {
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileRenameActivityTest: ProfileRenameActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileRenameActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileRenameActivityTest: ProfileRenameActivityTest) {
      component.inject(profileRenameActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
