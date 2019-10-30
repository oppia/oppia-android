package org.oppia.app.testing

import android.content.res.Configuration
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.customview.interaction.NumericInputInteractionView
import org.oppia.app.customview.interaction.TextInputInteractionView
import org.oppia.app.testing.InputInteractionViewTestActivity

/** Tests for [InputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
class InputInteractionViewTestActivityTest {
  @get:Rule
  var activityTestRule: ActivityTestRule<InputInteractionViewTestActivity> = ActivityTestRule(
    InputInteractionViewTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )
  private lateinit var activityScenario: ActivityScenario<InputInteractionViewTestActivity>

  @Before
  fun setUp() {
    activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
  }

  @Test
  fun testNumericInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().real).isWithin(1e-5).of(0.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.REAL)
      assertThat(textAnswerRetriever.getPendingAnswer().real).isEqualTo(9.0)
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswerWithDecimalValues() {
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9.5"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.REAL)
      assertThat(textAnswerRetriever.getPendingAnswer().real).isEqualTo(9.5)
    }
  }

  @Test
  fun testNumberInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    onView(withId(R.id.test_number_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("9")))
  }

  fun testTextInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_text_input_interaction_view) as TextInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().normalizedString).isEmpty()
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_text_input_interaction_view)).perform(typeText("abc"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_text_input_interaction_view) as TextInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.NORMALIZED_STRING)
      assertThat(textAnswerRetriever.getPendingAnswer().normalizedString).isEqualTo("abc")
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
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
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.denominator).isEqualTo(0)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.numerator).isEqualTo(0)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.wholeNumber).isEqualTo(0)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-9"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(true)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedWholeNumberText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(false)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedFractionText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9/10"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(false)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.numerator).isEqualTo(9)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-9/10"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(true)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.numerator).isEqualTo(9)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedWholeNumberValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("5 9/10"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(false)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.wholeNumber).isEqualTo(5)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.numerator).isEqualTo(9)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedNegativeWholeNumberValue_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("-5 9/10"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_fraction_input_interaction_view) as FractionInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer()).isInstanceOf(InteractionObject::class.java)
      assertThat(textAnswerRetriever.getPendingAnswer().objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.FRACTION)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.isNegative).isEqualTo(true)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.wholeNumber).isEqualTo(5)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.numerator).isEqualTo(9)
      assertThat(textAnswerRetriever.getPendingAnswer().fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  fun testFractionInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    onView(withId(R.id.test_fraction_input_interaction_view)).perform(typeText("9/5"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_fraction_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9/5")))
  }
}
