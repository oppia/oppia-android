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
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.NumberWithUnits.newBuilder
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToNumberWithUnitsParser.UnitsObjectFactory

@RunWith(AndroidJUnit4::class)
class StringToNumberWithUnitsParserTest {

  lateinit var context: Context
  lateinit var nwuof: StringToNumberWithUnitsParser
  lateinit var uof: UnitsObjectFactory

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
    nwuof = StringToNumberWithUnitsParser()
    uof = UnitsObjectFactory()
  }

  fun getPendingAnswer(answerText: String): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTexttring = answerText
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setNumberWithUnits(StringToNumberWithUnitsParser().parseNumberWithUnits(answerTexttring))
        .build()
      userAnswerBuilder.plainAnswer = answerTexttring
    }
    return userAnswerBuilder.build()
  }

  fun numberUnit(exponent: Int, unit: String): NumberUnit {
    return NumberUnit.newBuilder().setExponent(exponent).setUnit(unit).build()
  }

  fun numberWithUnits(
    real: Double?,
    fraction: Fraction?,
    units: String
  ): NumberWithUnits {
    if (real != null)
      return newBuilder().setReal(real).addAllUnit(uof.fromRawInputString(units).units.asIterable()).build()
    else
      return newBuilder().setFraction(fraction).addAllUnit(uof.fromRawInputString(units).units.asIterable()).build()
  }

  fun fraction(
    isNegative: Boolean,
    wholeNumber: Int,
    numerator: Int,
    denominator: Int
  ): Fraction {
    return Fraction.newBuilder().setIsNegative(isNegative)
      .setWholeNumber(wholeNumber).setNumerator(numerator).setDenominator(denominator).build()
  }

  @Test
  fun testFromStringToListFunction_hasCorrectListOfNumberUnitsfromString() {
    assertThat(uof.fromStringToList("kg / kg^2 K mol / (N m s^2) K s")).isEqualTo(
      listOf(
        numberUnit(exponent = -1, unit = "kg"), numberUnit(exponent = 2, unit = "K"),
        numberUnit(exponent = 1, unit = "mol"), numberUnit(exponent = -1, unit = "N"),
        numberUnit(exponent = -1, unit = "m"), numberUnit(exponent = -1, unit = "s")
      )
    )
    assertThat(uof.fromStringToList("mol/(kg / (N m / s^2)")).isEqualTo(
      listOf(
        numberUnit(exponent = 1, unit = "mol"), numberUnit(exponent = -1, unit = "kg"),
        numberUnit(exponent = 1, unit = "N"), numberUnit(exponent = 1, unit = "m"),
        numberUnit(exponent = -2, unit = "s")
      )
    )
    assertThat(uof.fromStringToList("kg per kg^2 K mol per (N m s^2) K s")).isEqualTo(
      listOf(
        numberUnit(exponent = -1, unit = "kg"), numberUnit(exponent = 2, unit = "K"),
        numberUnit(exponent = 1, unit = "mol"), numberUnit(exponent = -1, unit = "N"),
        numberUnit(exponent = -1, unit = "m"), numberUnit(exponent = -1, unit = "s")
      )
    )
  }

  @Test
  fun testUnitsWithArrayList_hasCorrectConvertedUnitsFromListToStringFormat() {
    assertThat(
      StringToNumberWithUnitsParser.Units(
        arrayListOf(
          numberUnit(exponent = -1, unit = "kg"), numberUnit(exponent = 2, unit = "K"),
          numberUnit(exponent = 1, unit = "mol"), numberUnit(exponent = -1, unit = "N"),
          numberUnit(exponent = -1, unit = "m"), numberUnit(exponent = -1, unit = "s")
        )
      ).unitToString()
    ).isEqualTo("kg^-1 K^2 mol N^-1 m^-1 s^-1")
    assertThat(
      StringToNumberWithUnitsParser.Units(
        arrayListOf(
          numberUnit(exponent = 1, unit = "mol"), numberUnit(exponent = -1, unit = "kg"),
          numberUnit(exponent = 1, unit = "N"), numberUnit(exponent = 1, unit = "m"),
          numberUnit(exponent = -2, unit = "s")
        )
      ).unitToString()
    ).isEqualTo(
      "mol kg^-1 N m s^-2"
    )
  }

  @Test
  fun testStringToLexical_hasCorrectConvertedUnitsFromListToLexicalFormat() {
    assertThat(uof.stringToLexical("kg per kg^2 K mol / (N m s^2) K s")).isEqualTo(
      arrayListOf(
        "kg", "/", "kg^2", "*", "K", "*", "mol", "/", "(", "N", "*", "m", "*",
        "s^2", ")", "K", "*", "s"
      )
    )
    assertThat(uof.stringToLexical("kg (K mol) m/s^2 r t / (l/ n) / o")).isEqualTo(
      arrayListOf(
        "kg", "(", "K", "*", "mol", ")", "m", "/", "s^2", "*", "r", "*", "t",
        "/", "(", "l", "/", "n", ")", "/", "o"
      )
    )
    assertThat(uof.stringToLexical("mol per (kg per (N m per s^2)*K)")).isEqualTo(
      arrayListOf(
        "mol", "/", "(", "kg", "/", "(", "N", "*", "m", "/", "s^2", ")", "*",
        "K", ")"
      )
    )
  }

  @Test
  fun testNumberWithUnitsToStringFunction_hasCorrectConvertedStringFormatFromNumberWithUnits() {
    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "real",
        numberWithUnits(
          2.02, null, "m / s^2"
        )
      )
    ).isEqualTo("2.02 m s^-2")

    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "real",
        numberWithUnits(2.02, null, "Rs")
      )
    ).isEqualTo("Rs 2.02")

    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "real",
        numberWithUnits(2.0, null, "")
      )
    ).isEqualTo("2.0")

    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "fraction", numberWithUnits(
          null,
          fraction(true, 0, 4, 3), "m / s^2"
        )
      )
    ).isEqualTo("-4/3 m s^-2")
    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "fraction", numberWithUnits(
          null,
          fraction(false, 0, 4, 3), "$ per hour"
        )
      )
    ).isEqualTo("$ 4/3 hour^-1")
    assertThat(
      StringToNumberWithUnitsParser().numberWithUnitsToString(
        "real",
        numberWithUnits(
          40.0, null, "Rs per hour"
        )
      ).toString(
      )
    ).isEqualTo("Rs 40.0 hour^-1")
  }

  fun testFromRawInputStringFunction_hasCorrectParsedUnitsString() {
    assertThat(uof.fromRawInputString("kg per (K mol^-2)").units)
      .isEqualTo(StringToNumberWithUnitsParser.Units(UnitsObjectFactory().fromStringToList("kg / (K mol^-2)")).units)
    assertThat(uof.fromRawInputString("kg per (K mol^-2)").units)
      .isEqualTo(StringToNumberWithUnitsParser.Units(UnitsObjectFactory().fromStringToList("kg / (K mol^-2)")).units)
    assertThat(uof.fromRawInputString("kg / (K mol^-2) N / m^2").units)
      .isEqualTo(StringToNumberWithUnitsParser.Units(UnitsObjectFactory().fromStringToList("kg / (K mol^-2) N / m^2")).units)
  }

  fun testParseNumberWithUnitsFunction_hasCorrectConvertedNumberWithUnitsFromString() {
    assertThat(nwuof.parseNumberWithUnits("2.02 kg / m^3")).isEqualTo(
      numberWithUnits(2.02, null, "kg / m^3")
    )

    assertThat(nwuof.parseNumberWithUnits("2 / 3 kg / m^3")).isEqualTo(
      numberWithUnits(
        null, fraction(
          false, 0, 2, 3
        ), "kg / m^3"
      )
    )
    assertThat(nwuof.parseNumberWithUnits("2.0")).isEqualTo(
      numberWithUnits(2.0, null, "")
    )
    assertThat(nwuof.parseNumberWithUnits("2 / 3")).isEqualTo(
      numberWithUnits(
        null, fraction(
          false, 0, 2, 3
        ), ""
      )
    )
    assertThat(nwuof.parseNumberWithUnits("$ 2.02")).isEqualTo(
      numberWithUnits(
        2.02, fraction(
          false, 0, 0, 1
        ), "$"
      )
    )
    assertThat(nwuof.parseNumberWithUnits("Rs 2 / 3 per hour")).isEqualTo(
      numberWithUnits(
        null, fraction(
          false, 0, 2, 3
        ), "Rs / hour"
      )
    )
  }

  @Test
  fun testFractionInputInteractionView_withInputtedDividerMoreThanOnce_errorIsDisplayedForInvalidValues() {
//    assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsRealTimeError("3* kg",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_VALUE.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsRealTimeError("$ 3*",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_VALUE.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsRealTimeError("Rs 3^",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_VALUE.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsRealTimeError("3* kg",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_VALUE.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("Rs5",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("$",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("kg 2 s^2",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("2 m/s#",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_UNIT_CHARS.getErrorMessageFromStringRes(context)
//    )
//assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("@ 2",context)).isEqualTo(
//      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
//    )
assertThat(StringToNumberWithUnitsParser().getNumberWithUnitsSubmitTimeError("2 / 3 kg&^-2",context)).isEqualTo(
      StringToNumberWithUnitsParser.NumberWithUnitsParsingError.INVALID_UNIT_CHARS.getErrorMessageFromStringRes(context)
    )


//      nwuof.fromRawInputString("Rs5");
//    }).toThrow(new Error(errors.INVALID_CURRENCY_FORMAT));
//    expect(() => {
//      nwuof.fromRawInputString("Rs5");
//    }).toThrow(new Error(errors.INVALID_CURRENCY));
//    expect(() => {
//      nwuof.fromRawInputString("$");
//    }).toThrow(new Error(errors.INVALID_CURRENCY));
//    expect(() => {
//      nwuof.fromRawInputString("kg 2 s^2");
//    }).toThrow(new Error(errors.INVALID_CURRENCY));
//    expect(() => {
//      nwuof.fromRawInputString("2 m/s#");
//    }).toThrow(new Error(errors.INVALID_UNIT_CHARS));
//    expect(() => {
//      nwuof.fromRawInputString("@ 2");
//    }).toThrow(new Error(errors.INVALID_CURRENCY));
//    expect(() => {
//      nwuof.fromRawInputString("2 / 3 kg&^-2");
//    }).toThrow(new Error(errors.INVALID_UNIT_CHARS));

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
  fun testNumberWithUnitsInputInteractionView_withNegativeDecimal_hasCorrectPendingAnswer() {
    val numberWithUnits = getPendingAnswer("-10.00 m").answer.numberWithUnits
    assertThat(numberWithUnits.real).isEqualTo(-10.0)
    assertThat(numberWithUnits.getUnit(0).unit).isEqualTo("m")
  }

}