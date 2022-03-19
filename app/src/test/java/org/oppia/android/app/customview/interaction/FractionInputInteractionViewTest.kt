package org.oppia.android.app.customview.interaction

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.player.state.StateFragmentTest
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.FractionInputInteractionViewTestActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigFastShowTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FractionInputInteractionView]. */
@RunWith(AndroidJUnit4::class)
@Config(
  application = FractionInputInteractionViewTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
@LooperMode(LooperMode.Mode.PAUSED)

class FractionInputInteractionViewTest {

  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testFractionInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(0)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withNegativeNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(editTextInputAction.appendText("-9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withWholeNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(editTextInputAction.appendText("9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withFraction_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withNegativeFraction_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "-9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withMixedNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(5)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withNegativeMixedNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      FractionInputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "-55 59/9"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(55)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(59)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(9)
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withNegativeSignOtherThanAt0_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "55-"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withNegativeSignAt0MoreThan1_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "--55"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withDividerMoreThanOnce_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5/5/"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withDividerAtStart_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "/5"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withPartialMixedNumber_numberFormatErrorIsNotDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 5/"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withPartialMixedNumberSubmit_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 5/"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button))
      .check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withMixedNumber_submit_noErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "3 1/2"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button))
      .check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error))
      .check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withDivideByZero_errorIsNotDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1/0"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withDivideByZero_submit_divideByZeroErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1/0"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button))
      .check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_divide_by_zero
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withInvalidCharacter_invalidCharacterErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "."
          )
        )
      onView(withId(R.id.fraction_input_error))
        .check(
          matches(
            withText(
              R.string.fraction_error_invalid_chars
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks
  fun testFractionInput_withLong_submit_numberTooLongErrorIsDisplayed() {
    ActivityScenario.launch(FractionInputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "12345678"
          )
        )
      closeSoftKeyboard()
      onView(withId(R.id.submit_button))
        .check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.fraction_input_error))
        .check(
          matches(
            withText(
              R.string.fraction_error_larger_than_seven_digits
            )
          )
        )
    }

    @Test
    @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
    fun testFractionInput_withFraction_configChange_hasCorrectPendingAnswer() {
      val activityScenario = ActivityScenario.launch(
        FractionInputInteractionViewTestActivity::class.java
      )
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "9/5"
          )
        )
      activityScenario.onActivity { activity ->
        activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
      }
      onView(withId(R.id.test_fraction_input_interaction_view)).check(matches(isDisplayed()))
        .check(matches(withText("9/5")))
    }
  }

  @Singleton
  @Component(
    modules = [
      StateFragmentTest.TestModule::class, RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class, LoggerModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      LogStorageModule::class, PrimeTopicAssetsControllerModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, ApplicationStartupListenerModule::class,
      HintsAndSolutionConfigFastShowTestModule::class, HintsAndSolutionProdModule::class,
      WorkManagerConfigurationModule::class, LogUploadWorkerModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkConnectionUtilDebugModule::class,
      NetworkConnectionDebugUtilModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(fractionInputInteractionViewTest: FractionInputInteractionViewTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFractionInputInteractionViewTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(fractionInputInteractionViewTest: FractionInputInteractionViewTest) {
      component.inject(fractionInputInteractionViewTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
