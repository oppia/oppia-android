package org.oppia.android.app.onboarding

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.MessageLite
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
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProtoExtra
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
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [CreateProfileFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = CreateProfileFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class CreateProfileFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var editTextInputAction: EditTextInputAction
  @Inject lateinit var testGlideImageLoader: TestGlideImageLoader

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

  @Test
  fun testFragment_nicknameLabelIsDisplayed() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.create_profile_nickname_label))
        .check(
          matches(
            allOf(
              isDisplayed(),
              withText(
                context.getString(
                  R.string.create_profile_activity_nickname_label
                )
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_stepCountText_isDisplayed() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.onboarding_steps_count))
        .check(
          matches(
            allOf(
              isDisplayed(),
              withText(
                context.getString(
                  R.string.onboarding_step_count_three
                )
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_continueButtonClicked_filledNickname_launchesLearnerIntroScreen() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = IntroActivityParams.newBuilder().setProfileNickname("John").build()
      intended(
        allOf(
          hasComponent(IntroActivity::class.java.name),
          hasProtoExtra("OnboardingIntroActivity.params", expectedParams)
        )
      )
    }
  }

  @Test
  fun testFragment_continueButtonClicked_filledNickname_doesNotShowErrorText() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  @Test
  fun testFragment_continueButtonClicked_emptyNickname_showNicknameErrorText() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_continueButtonClicked_filledNickname_afterError_launchesLearnerIntroScreen() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(isDisplayed()))

      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = IntroActivityParams.newBuilder().setProfileNickname("John").build()
      intended(
        allOf(
          hasComponent(IntroActivity::class.java.name),
          hasProtoExtra("OnboardingIntroActivity.params", expectedParams)
        )
      )
    }
  }

  @Test
  fun testFragment_onTextChanged_afterError_hidesErrorMessage() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(isDisplayed()))

      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFragment_landscapeMode_filledNickname_continueButtonClicked_launchesLearnerIntroScreen() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = IntroActivityParams.newBuilder().setProfileNickname("John").build()
      intended(
        allOf(
          hasComponent(IntroActivity::class.java.name),
          hasProtoExtra("OnboardingIntroActivity.params", expectedParams)
        )
      )
    }
  }

  @Test
  fun testFragment_landscapeMode_filledNickname_continueButtonClicked_doesNotShowErrorText() {
    launchNewLearnerProfileActivity().use {
      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  @Test
  fun testFragment_landscapeMode_continueButtonClicked_emptyNickname_showNicknameErrorText() {
    launchNewLearnerProfileActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_landscape_continueButtonClicked_afterErrorShown_launchesLearnerIntroScreen() {
    launchNewLearnerProfileActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_nickname_error))
        .check(matches(isDisplayed()))

      onView(withId(R.id.create_profile_nickname_edittext))
        .perform(
          editTextInputAction.appendText("John"),
          closeSoftKeyboard()
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = IntroActivityParams.newBuilder().setProfileNickname("John").build()
      intended(
        allOf(
          hasComponent(IntroActivity::class.java.name),
          hasProtoExtra("OnboardingIntroActivity.params", expectedParams)
        )
      )
    }
  }

  @Test
  fun testFragment_backButtonPressed_currentScreenIsDestroyed() {
    launchNewLearnerProfileActivity().use { scenario ->
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario?.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_landscapeMode_backButtonPressed_currentScreenIsDestroyed() {
    launchNewLearnerProfileActivity().use { scenario ->
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario?.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_tapToAddPictureClicked_hasGalleryIntent() {
    launchNewLearnerProfileActivity().use {
      onView(withText(R.string.create_profile_activity_profile_picture_prompt))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasAction(Intent.ACTION_PICK))
    }
  }

  @Test
  fun testFragment_landscapeMode_tapToAddPictureClicked_hasGalleryIntent() {
    launchNewLearnerProfileActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.create_profile_activity_profile_picture_prompt))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasAction(Intent.ACTION_PICK))
    }
  }

  @Test
  fun testFragment_tapToAddPictureClicked_loadsTheImageFromGallery() {
    val expectedIntent: Matcher<Intent> = hasAction(Intent.ACTION_PICK)

    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)

    launchNewLearnerProfileActivity().use {
      onView(withText(R.string.create_profile_activity_profile_picture_prompt))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val loadedImageUri = activityResult.resultData.data.toString()
      assertThat(loadedImageUri).contains("launcher_icon")
    }
  }

  @Test
  fun testFragment_uploadProfilePicture_displaysImageInTarget() {
    val expectedIntent: Matcher<Intent> = hasAction(Intent.ACTION_PICK)

    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)

    launchNewLearnerProfileActivity().use {
      onView(withText(R.string.create_profile_activity_profile_picture_prompt))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      val expectedImage = activityResult.resultData.data.toString()
      val loadedImages = testGlideImageLoader.getLoadedBitmaps()
      assertThat(loadedImages.first()).isEqualTo(expectedImage)
    }
  }

  private fun createGalleryPickActivityResultStub(): Instrumentation.ActivityResult {
    val resources: Resources = context.resources
    val imageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
        resources.getResourcePackageName(R.mipmap.launcher_icon) + '/' +
        resources.getResourceTypeName(R.mipmap.launcher_icon) + '/' +
        resources.getResourceEntryName(R.mipmap.launcher_icon)
    )
    val resultIntent = Intent()
    resultIntent.data = imageUri
    return Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
  }

  private fun launchNewLearnerProfileActivity():
    ActivityScenario<CreateProfileActivity>? {
      val scenario = ActivityScenario.launch<CreateProfileActivity>(
        CreateProfileActivity.createProfileActivityIntent(context)
      )
      testCoroutineDispatchers.runCurrent()
      return scenario
    }

  private fun <T : MessageLite> hasProtoExtra(keyName: String, expectedProto: T): Matcher<Intent> {
    val defaultProto = expectedProto.newBuilderForType().build()
    return object : TypeSafeMatcher<Intent>() {
      override fun describeTo(description: Description) {
        description.appendText("Intent with extra: $keyName and proto value: $expectedProto")
      }

      override fun matchesSafely(intent: Intent): Boolean {
        return intent.hasExtra(keyName) &&
          intent.getProtoExtra(keyName, defaultProto) == expectedProto
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestPlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class, TestImageLoaderModule::class,
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(newLearnerProfileFragmentTest: CreateProfileFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCreateProfileFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(newLearnerProfileFragmentTest: CreateProfileFragmentTest) {
      component.inject(newLearnerProfileFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
