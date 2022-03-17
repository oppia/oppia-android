package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.parser.StringToFractionParser
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [FractionParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class FractionParserTest {
  private lateinit var fractionParser: FractionParser

  @Before
  fun setUp() {
    fractionParser = FractionParser()
  }

  @Test
  fun testSubmitTimeError_regularFraction_returnsValid() {
    val error = fractionParser.getSubmitTimeError("1/2")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_regularNegativeFractionWithExtraSpaces_returnsValid() {
    val error = fractionParser.getSubmitTimeError(" -1   / 2 ")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_atLengthLimit_returnsValid() {
    val error = fractionParser.getSubmitTimeError("1234567/1234567")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_wholeNumber_returnsValid() {
    val error = fractionParser.getSubmitTimeError("888")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_wholeNegativeNumber_returnsValid() {
    val error = fractionParser.getSubmitTimeError("-777")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_mixedNumber_returnsValid() {
    val error = fractionParser.getSubmitTimeError("11 22/33")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_tenDigitNumber_returnsNumberTooLong() {
    val error = fractionParser.getSubmitTimeError("0123456789")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.NUMBER_TOO_LONG)
  }

  @Test
  fun testSubmitTimeError_nonDigits_returnsInvalidFormat() {
    val error = fractionParser.getSubmitTimeError("jdhfc")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_divisionByZero_returnsDivisionByZero() {
    val error = fractionParser.getSubmitTimeError("123/0")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.DIVISION_BY_ZERO)
  }

  @Test
  fun testSubmitTimeError_ambiguousSpacing_returnsInvalidFormat() {
    val error = fractionParser.getSubmitTimeError("1 2 3/4")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_emptyString_returnsInvalidFormat() {
    val error = fractionParser.getSubmitTimeError("")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_noDenominator_returnsInvalidFormat() {
    val error = fractionParser.getSubmitTimeError("3/")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_regularFraction_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("2/3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_regularNegativeFraction_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("-2/3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_wholeNumber_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("4")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_wholeNegativeNumber_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("-4")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_mixedNumber_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("5 2/3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_mixedNegativeNumber_returnsValid() {
    val error = fractionParser.getRealTimeAnswerError("-5 2/3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_nonDigits_returnsInvalidChars() {
    val error = fractionParser.getRealTimeAnswerError("abc")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_CHARS)
  }

  @Test
  fun testRealTimeError_noNumerator_returnsInvalidFormat() {
    val error = fractionParser.getRealTimeAnswerError("/3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_severalSlashes_invalidFormat_returnsInvalidFormat() {
    val error = fractionParser.getRealTimeAnswerError("1/3/8")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_severalDashes_returnsInvalidFormat() {
    val error = fractionParser.getRealTimeAnswerError("-1/-3")
    assertThat(error).isEqualTo(FractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testParseFraction_divisionByZero_returnsFraction() {
    val parseFraction = fractionParser.parseFraction("8/0")
    val parseFractionFromString = fractionParser.parseFractionFromString("8/0")
    val expectedFraction = Fraction.newBuilder().apply {
      numerator = 8
      denominator = 0
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_multipleFractions_failsWithError() {
    val parseFraction = fractionParser.parseFraction("7 1/2 4/5")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      fractionParser.parseFractionFromString("7 1/2 4/5")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: 7 1/2 4/5")
  }

  @Test
  fun testParseFraction_nonDigits_failsWithError() {
    val parseFraction = fractionParser.parseFraction("abc")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      fractionParser.parseFractionFromString("abc")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: abc")
  }

  @Test
  fun testParseFraction_regularFraction_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("1/2")
    val parseFraction = fractionParser.parseFraction("1/2")
    val expectedFraction = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_regularNegativeFraction_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("-8/4")
    val parseFraction = fractionParser.parseFraction("-8/4")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 8
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_wholeNumber_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("7")
    val parseFraction = fractionParser.parseFraction("7")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_wholeNegativeNumber_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("-7")
    val parseFraction = fractionParser.parseFraction("-7")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_mixedNumber_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("1 3/4")
    val parseFraction = fractionParser.parseFraction("1 3/4")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1
      numerator = 3
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_negativeMixedNumber_returnsFraction() {
    val parseFractionFromString = fractionParser.parseFractionFromString("-123 456/7")
    val parseFraction = fractionParser.parseFraction("-123 456/7")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 123
      numerator = 456
      denominator = 7
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_longMixedNumber_returnsFraction() {
    val parseFractionFromString = fractionParser
      .parseFractionFromString("1234567 1234567/1234567")
    val parseFraction = fractionParser
      .parseFraction("1234567 1234567/1234567")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1234567
      numerator = 1234567
      denominator = 1234567
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }
}
