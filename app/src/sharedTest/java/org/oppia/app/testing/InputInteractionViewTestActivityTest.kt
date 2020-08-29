package org.oppia.app.testing

import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.RatioExpression
import org.robolectric.annotation.LooperMode

/** Tests for [InputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class InputInteractionViewTestActivityTest {

  @Test
  fun testFractionInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
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
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberText_hasCorrectPendingAnswer() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-9"))
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
  fun testFractionInputInteractionView_withInputtedWholeNumberText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9"))
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
  fun testFractionInputInteractionView_withInputtedFractionText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedNegativeValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedWholeNumberValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberValue_hasCorrectPendingAnswer() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testFractionInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "9/5"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_fraction_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9/5")))
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeSymbolOtherThanAt0_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedNegativeSymbolAt0AndMoreThanOnce_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedDividerMoreThanOnce_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedDividerAtStart_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
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
  fun testFractionInputInteractionView_withInputtedPartialValue_numberFormatErrorIsNotDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "5 5/"
        )
      )
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  fun testFractionInputInteractionView_withInputtedPartialValue_clickSubmitButton_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "5 5/"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
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
  fun testFractionInputInteractionView_withInputtedValidValue_clickSubmitButton_noErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "3 1/2"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  fun testFractionInputInteractionView_withInputtedDivideByZero_errorIsNotDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "1/0"
        )
      )
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  fun testFractionInputInteractionView_withInputtedDivideByZero_clickSubmitButton_divideByZeroErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        typeText(
          "1/0"
        )
      )
    closeSoftKeyboard()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
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
  fun testFractionInputInteractionView_withInputtedInvalidCharacter_invalidCharacterErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          typeText(
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
  fun testFractionInputInteractionView_withInputtedLongNumber_clickSubmitButton_numberTooLongErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          typeText(
            "12345678"
          )
        )
      closeSoftKeyboard()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.fraction_input_error))
        .check(
          matches(
            withText(
              R.string.fraction_error_larger_than_seven_digits
            )
          )
        )
    }
  }

  @Test
  fun testNumericInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.real).isWithin(1e-5).of(0.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        typeText(
          "9"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(9.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswerWithDecimalValues() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        typeText(
          "9.5"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(9.5)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedNegativeDecimal_hasCorrectPendingAnswerWithDecimalValues() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        typeText(
          "-9.5"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(-9.5)
      assertThat(pendingAnswer.answer.real).isLessThan(0.0)
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testNumberInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9")))
  }

  @Test
  fun testNumericInputInteractionView_withInputtedInvalidCharacter_invalidCharacterErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          typeText(
            "/"
          )
        )
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_invalid_format
            )
          )
        )
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedLongNumber_clickSubmitButton_numberTooLongErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          typeText(
            "-12345678.6787687678"
          )
        )
      closeSoftKeyboard()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_larger_than_fifteen_characters
            )
          )
        )
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedLongNonDecimalNumber_clickSubmitButton_numberTooLongErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          typeText(
            "1234567886787687678"
          )
        )
      closeSoftKeyboard()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_larger_than_fifteen_characters
            )
          )
        )
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedMinusSymbol_clickSubmitButton_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          typeText(
            "-"
          )
        )
      closeSoftKeyboard()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_invalid_format
            )
          )
        )
    }
  }

  fun testNumericInputInteractionView_withInputtedNegativeSymbolOtherThanAt0_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("55-"))
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  fun testNumericInputInteractionView_withInputtedNegativeSymbolAt0AndMoreThanOnce_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        typeText(
          "--55"
        )
      )
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  fun testNumericInputInteractionView_withInputtedFloatingPointMoreThanOnce_numberFormatErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        typeText(
          "5.5."
        )
      )
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  fun testNumericInputInteractionView_withInputtedFloatingPointAtStart_numberStartingWithFloatingPointErrorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText(".5"))
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_starting_with_floating_point
          )
        )
      )
  }

  @Test
  fun testTextInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.normalizedString).isEmpty()
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_text_input_interaction_view))
      .perform(
        typeText(
          "abc"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.NORMALIZED_STRING
      )
      assertThat(pendingAnswer.answer.normalizedString).isEqualTo("abc")
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTextInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() { // ktlint-disable max-line-length
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_text_input_interaction_view))
      .perform(
        typeText(
          "abc"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_text_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("abc")))
  }

  @Test
  fun testRatioInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.ratioExpression).isInstanceOf(RatioExpression::class.java)
      assertThat(pendingAnswer.answer.ratioExpression.ratioComponentCount).isEqualTo(0)
    }
  }

  @Test
  fun testRatioInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "1:2:3"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
      )
      assertThat(pendingAnswer.answer.ratioExpression.ratioComponentList)
        .isEqualTo(listOf(1, 2, 3))
    }
  }

  @Test
  fun testRatioInputView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "1:2"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_ratio_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("1:2")))
  }

  @Test
  fun testRatioInputView_withInputtedTwoColonsTogether_colonsTogetherFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "1::2"
        )
      )
    onView(withId(R.id.ratio_input_error))
      .check(
        matches(
          withText(
            R.string.ratio_error_invalid_colons
          )
        )
      )
  }

  @Test
  fun testRatioInputInteractionView_withInputtedSpacesBetweenComponents_hasCorrectPendingAnswer() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "1   : 2 : 3 : 4 "
        )
      )
    onView(withId(R.id.test_ratio_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("1:2:3:4")))
  }

  @Test
  fun testRatioInputInteractionView_withInputtedNegativeRatio_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "-1:2:3:4"
        )
      )
    onView(withId(R.id.ratio_input_error))
      .check(
        matches(
          withText(
            R.string.ratio_error_invalid_format
          )
        )
      )
  }

  @Test
  fun testRatioInputInteractionView_withFractionRatio_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        typeText(
          "1/2:3:4"
        )
      )
    onView(withId(R.id.ratio_input_error))
      .check(
        matches(
          withText(
            R.string.ratio_error_invalid_format
          )
        )
      )
  }
}
