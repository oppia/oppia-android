package org.oppia.app.parser

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer

@RunWith(AndroidJUnit4::class)
class StringToNumberWithUnitsParserTest {

  private var context: Context? = null
  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
  }

  fun getPendingAnswer(answerText: String): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTexttring = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setNumberWithUnits(StringToNumberWithUnitsParser().parseNumberWithUnits(answerTexttring))
        .build()
      userAnswerBuilder.plainAnswer = answerTexttring
    }
    return userAnswerBuilder.build()
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withNoText_hasCorrectPendingAnswerType() {

    val interactionObject = getPendingAnswer("").answer
    assertThat(interactionObject).isInstanceOf(InteractionObject::class.java)
    assertThat(getPendingAnswer("").answer.real).isWithin(1e-5).of(0.0)
    assertThat(interactionObject.numberWithUnits.fraction.denominator).isEqualTo(0)
    assertThat(interactionObject.numberWithUnits.fraction.numerator).isEqualTo(0)
    assertThat(interactionObject.numberWithUnits.fraction.wholeNumber).isEqualTo(0)
    assertThat(interactionObject.numberWithUnits.fraction.isNegative).isEqualTo(false)
    assertThat(interactionObject.numberWithUnits.unitOrBuilderList).isEmpty()

  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withNegativeWholeNumberText_hasCorrectPendingAnswer() {
    val interactionObject = getPendingAnswer("-9").answer
    assertThat(interactionObject).isInstanceOf(InteractionObject::class.java)
    assertThat(interactionObject.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS)
    assertThat(interactionObject.numberWithUnits.real).isLessThan(0.0)
    assertThat(interactionObject.numberWithUnits.real).isEqualTo(-9.0)
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withWholeNumberText_hasCorrectPendingAnswer() {
    val interactionObject = getPendingAnswer("9").answer
    assertThat(interactionObject).isInstanceOf(InteractionObject::class.java)
    assertThat(interactionObject.objectTypeCase).isEqualTo(InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS)
    assertThat(interactionObject.numberWithUnits.real).isEqualTo(9.0)
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withFraction_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("9/10").answer.numberWithUnits
    assertThat(numberWithUnits.fraction.numerator).isEqualTo(9)
    assertThat(numberWithUnits.fraction.denominator).isEqualTo(10)
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withNumberWithUnitsText_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("-6 1/5 km/hr").answer.numberWithUnits
    assertThat(numberWithUnits.fraction.isNegative).isEqualTo(true)
    assertThat(numberWithUnits.fraction.wholeNumber).isEqualTo(6)
    assertThat(numberWithUnits.fraction.numerator).isEqualTo(1)
    assertThat(numberWithUnits.fraction.denominator).isEqualTo(5)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("km/hr")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withCurrency_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("Rs 10000").answer.numberWithUnits
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("Rs")
    assertThat(numberWithUnits.real).isEqualTo(10000.0)
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withFractionWithWholeNumberValueAndUnit_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("5 9/10 km").answer.numberWithUnits
    assertThat(numberWithUnits.fraction.isNegative).isEqualTo(false)
    assertThat(numberWithUnits.fraction.wholeNumber).isEqualTo(5)
    assertThat(numberWithUnits.fraction.numerator).isEqualTo(9)
    assertThat(numberWithUnits.fraction.denominator).isEqualTo(10)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("km")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withCurrencyWithDot_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("100 Rs.").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(100.0)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("Rs.")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withDollarSymbol_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("$ 100").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(100.0)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("$")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withDecimalValueAndUnit_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("100.5 Km").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(100.5)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("Km")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withWithDays_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("3 1/2 days").answer.numberWithUnits
    assertThat(numberWithUnits.fraction.wholeNumber).isEqualTo(3)
    assertThat(numberWithUnits.fraction.numerator).isEqualTo(1)
    assertThat(numberWithUnits.fraction.denominator).isEqualTo(2)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("days")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withNegativeDecimal_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("-10.00 m").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(-10.0)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("m")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withInputtedWithSymbols_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("100.5 Km/m^2").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(100.5)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("Km/m^2")
  }

  @Test
  fun testNumberWithUnitsInputInteractionView_withInputtedWithBracketsSymbols_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("100.50 m^(-2)").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(100.5)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("m^(-2)")
  }

}