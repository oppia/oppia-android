package org.oppia.app.testing

import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
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

/** Tests for [InputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
class InputInteractionViewTestActivityTest {

  @Test
  fun testNumericInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.real).isWithin(1e-5).of(0.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.REAL)
      assertThat(pendingAnswer.answer.real).isEqualTo(9.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswerWithDecimalValues() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9.5"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.REAL)
      assertThat(pendingAnswer.answer.real).isEqualTo(9.5)
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testNumberInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("9")))
  }

  @Test
  fun testTextInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.normalizedString).isEmpty()
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_text_input_interaction_view)).perform(typeText("abc"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.NORMALIZED_STRING)
      assertThat(pendingAnswer.answer.normalizedString).isEqualTo("abc")
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTextInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_text_input_interaction_view)).perform(typeText("abc"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_text_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("abc")))
  }

  @Test
  fun testFractionInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(0)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedWholeNumberText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedFractionText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9/10"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-9/10"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedWholeNumberValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("5 9/10"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(5)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-55 59/9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(55)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(59)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(9)
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testFractionInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9/5"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_fraction_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9/5")))
  }
}
