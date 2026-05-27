package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.NumberWithUnitsTokenSubject.Companion.assertThat
import org.robolectric.annotation.Config

/** Tests for [NumberWithUnitsTokenizer]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@Config
class NumberWithUnitsTokenizerTest {
  @Parameter lateinit var input: String

  @Parameter lateinit var expected: String

  @Test
  fun testTokenize_emptyString_producesNoTokens() {
    val tokens = NumberWithUnitsTokenizer.tokenize("").toList()
    assertThat(tokens).isEmpty()
  }

  @Test
  @Iteration(" ", "input= ")
  @Iteration("\n", "input=\n")
  @Iteration("\t", "input=\t")
  @Iteration("\n\t", "input=\n\t")
  fun testTokenize_onlyWhitespace_producesNoTokens() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  @Iteration("    1", "input=    1", "expected=1")
  @Iteration("42    ", "input=42    ", "expected=42")
  fun testTokenize_positiveIntegerWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(expected.toInt())
  }

  @Test
  @Iteration("   3.14    ", "input=   3.14    ", "expected=3.14")
  @Iteration("  9.8  ", "input=  9.8  ", "expected=9.8")
  fun testTokenize_realNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_negativeNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("   -   2.5    ").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWithValueThat().isEqualTo(2.5)
  }

  @Test
  fun testTokenize_singleDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("7").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(7)
  }

  @Test
  @Iteration("17", "input=17", "expected=17")
  @Iteration("12345", "input=12345", "expected=12345")
  fun testTokenize_multiDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(expected.toInt())
  }

  @Test
  fun testTokenize_integerWithLeadingZeros_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("007").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(7)
  }

  @Test
  fun testTokenize_veryLargeInteger_producesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1111111111111111111111111").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  @Iteration("12.34", "input=12.34", "expected=12.34")
  @Iteration("1.0", "input=1.0", "expected=1.0")
  @Iteration("0.001", "input=0.001", "expected=0.001")
  fun testTokenize_validDecimalNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_decimalWithoutLeadingDigit_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize(".5").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInvalidToken()
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(5)
  }

  @Test
  fun testTokenize_decimalWithoutTrailingDigit_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize("12.").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_multipleDecimalPoints_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.2.3").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(1.2)
    assertThat(tokens[1]).isInvalidToken()
    assertThat(tokens[2]).isPositiveIntegerWithValueThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_negativeInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-1").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(1)
  }

  @Test
  fun testTokenize_negativeRealNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-42.84").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWithValueThat().isEqualTo(42.84)
  }

  @Test
  fun testTokenize_fractionsWithPositiveIntegers_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1/2").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumerator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/80").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isPositiveIntegerWithValueThat().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumeratorAndDenominator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/-80").toList()

    // Note the parser will be responsible for interpreting the double-negative as a positive,
    // or throw an error but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMinusSymbol()
    assertThat(tokens[4]).isPositiveIntegerWithValueThat().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithDecimal_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.5/3.7").toList()

    // Note the parser will be responsible for throwing an error since fractions with decimals
    // aren't valid, but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(1.5)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveRealNumberWithValueThat().isEqualTo(3.7)
  }

  @Test
  @Iteration("₹ 10", "input=₹ 10", "expected=₹")
  @Iteration("Rs 10", "input=Rs 10", "expected=Rs")
  fun testTokenize_rupeePrefixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo(expected)
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(10)
  }

  @Test
  @Iteration("10 rupee", "input=10 rupee", "expected=rupee")
  @Iteration("10 rupees", "input=10 rupees", "expected=rupees")
  @Iteration("10 Rupee", "input=10 Rupee", "expected=Rupee")
  @Iteration("10 Rupees", "input=10 Rupees", "expected=Rupees")
  fun testTokenize_rupeeSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 paise", "input=10 paise", "expected=paise")
  @Iteration("10 paisa", "input=10 paisa", "expected=paisa")
  @Iteration("10 Paise", "input=10 Paise", "expected=Paise")
  @Iteration("10 Paisa", "input=10 Paisa", "expected=Paisa")
  fun testTokenize_paisaSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_dollarPrefixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("$12.5").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("$")
    assertThat(tokens[1]).isPositiveRealNumberWithValueThat().isEqualTo(12.5)
  }

  @Test
  @Iteration("10 USD", "input=10 USD", "expected=USD")
  @Iteration("10 dollars", "input=10 dollars", "expected=dollars")
  @Iteration("10 dollar", "input=10 dollar", "expected=dollar")
  @Iteration("10 Dollars", "input=10 Dollars", "expected=Dollars")
  @Iteration("10 Dollar", "input=10 Dollar", "expected=Dollar")
  fun testTokenize_dollarSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 ¢", "input=10 ¢", "expected=¢")
  @Iteration("10 cents", "input=10 cents", "expected=cents")
  @Iteration("10 cent", "input=10 cent", "expected=cent")
  @Iteration("10 Cents", "input=10 Cents", "expected=Cents")
  @Iteration("10 Cent", "input=10 Cent", "expected=Cent")
  fun testTokenize_centSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 m", "input=20 m", "expected=m")
  @Iteration("20 meter", "input=20 meter", "expected=meter")
  @Iteration("20 meters", "input=20 meters", "expected=meters")
  fun testTokenize_correctMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Meter", "input=20 Meter", "expected=Meter")
  @Iteration("20 Meters", "input=20 Meters", "expected=Meters")
  fun testTokenize_incorrectMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 in", "input=20 in", "expected=in")
  @Iteration("20 inch", "input=20 inch", "expected=inch")
  @Iteration("20 inches", "input=20 inches", "expected=inches")
  fun testTokenize_correctInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 In", "input=20 In", "expected=In")
  @Iteration("20 Inch", "input=20 Inch", "expected=Inch")
  @Iteration("20 Inches", "input=20 Inches", "expected=Inches")
  fun testTokenize_incorrectInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 ft", "input=20 ft", "expected=ft")
  @Iteration("20 foot", "input=20 foot", "expected=foot")
  @Iteration("20 feet", "input=20 feet", "expected=feet")
  fun testTokenize_correctFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Ft", "input=20 Ft", "expected=Ft")
  @Iteration("20 Foot", "input=20 Foot", "expected=Foot")
  @Iteration("20 Feet", "input=20 Feet", "expected=Feet")
  fun testTokenize_incorrectFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 yd", "input=20 yd", "expected=yd")
  @Iteration("20 yard", "input=20 yard", "expected=yard")
  @Iteration("20 yards", "input=20 yards", "expected=yards")
  fun testTokenize_correctYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Yd", "input=20 Yd", "expected=Yd")
  @Iteration("20 Yard", "input=20 Yard", "expected=Yard")
  @Iteration("20 Yards", "input=20 Yards", "expected=Yards")
  fun testTokenize_incorrectYardUnits_parsesYottaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 g", "input=20 g", "expected=g")
  @Iteration("20 gram", "input=20 gram", "expected=gram")
  @Iteration("20 grams", "input=20 grams", "expected=grams")
  fun testTokenize_correctGramUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Gram", "input=20 Gram", "expected=Gram")
  @Iteration("20 Grams", "input=20 Grams", "expected=Grams")
  fun testTokenize_incorrectGramUnits_parsesGigaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 gr", "input=20 gr", "expected=gr")
  @Iteration("20 grain", "input=20 grain", "expected=grain")
  @Iteration("20 grains", "input=20 grains", "expected=grains")
  fun testTokenize_correctGrainUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Gr", "input=20 Gr", "expected=Gr")
  @Iteration("20 Grain", "input=20 Grain", "expected=Grain")
  @Iteration("20 Grains", "input=20 Grains", "expected=Grains")
  fun testTokenize_incorrectGrainUnits_parsesGigaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 oz", "input=20 oz", "expected=oz")
  @Iteration("20 ounce", "input=20 ounce", "expected=ounce")
  @Iteration("20 ounces", "input=20 ounces", "expected=ounces")
  fun testTokenize_correctOunceUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("20 Oz", "input=20 Oz", "expected=Oz")
  @Iteration("20 Ounce", "input=20 Ounce", "expected=Ounce")
  @Iteration("20 Ounces", "input=20 Ounces", "expected=Ounces")
  fun testTokenize_incorrectOunceUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_correctSquareMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m2").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m2")
  }

  @Test
  fun testTokenize_incorrectSquareMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M2").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("M2")
  }

  @Test
  @Iteration("10 sqft", "input=10 sqft", "expected=sqft")
  @Iteration("10 sqfeet", "input=10 sqfeet", "expected=sqfeet")
  fun testTokenize_correctSquareFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Sqft", "input=10 Sqft", "expected=Sqft")
  @Iteration("10 Sqfeet", "input=10 Sqfeet", "expected=Sqfeet")
  fun testTokenize_incorrectSquareFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 sqyd", "input=10 sqyd", "expected=sqyd")
  @Iteration("10 sqyard", "input=10 sqyard", "expected=sqyard")
  fun testTokenize_correctSquareYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Sqyd", "input=10 Sqyd", "expected=Sqyd")
  @Iteration("10 Sqyard", "input=10 Sqyard", "expected=Sqyard")
  fun testTokenize_incorrectSquareYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_correctCubicMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m3").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m3")
  }

  @Test
  fun testTokenize_incorrectCubicMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M3").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("M3")
  }

  @Test
  @Iteration("10 litre", "input=10 litre", "expected=litre")
  @Iteration("10 liter", "input=10 liter", "expected=liter")
  @Iteration("10 litres", "input=10 litres", "expected=litres")
  @Iteration("10 liters", "input=10 liters", "expected=liters")
  @Iteration("10 L", "input=10 L", "expected=L")
  @Iteration("10 l", "input=10 l", "expected=l")
  @Iteration("10 lt", "input=10 lt", "expected=lt")
  fun testTokenize_correctLiterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Litre", "input=10 Litre", "expected=Litre")
  @Iteration("10 Litres", "input=10 Litres", "expected=Litres")
  @Iteration("10 Liter", "input=10 Liter", "expected=Liter")
  @Iteration("10 Liters", "input=10 Liters", "expected=Liters")
  @Iteration("10 Lt", "input=10 Lt", "expected=Lt")
  fun testTokenize_incorrectLiterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_correctCubicCentimeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cc").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("cc")
  }

  @Test
  fun testTokenize_incorrectCubicCentimeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 CC").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("CC")
  }

  @Test
  fun testTokenize_correctCubicInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuin").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("cuin")
  }

  @Test
  fun testTokenize_incorrectCubicInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuin").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Cuin")
  }

  @Test
  fun testTokenize_correctCubicFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuft").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("cuft")
  }

  @Test
  fun testTokenize_incorrectCubicFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuft").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Cuft")
  }

  @Test
  fun testTokenize_correctCubicYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuyd").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("cuyd")
  }

  @Test
  fun testTokenize_incorrectCubicYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuyd").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Cuyd")
  }

  @Test
  @Iteration("310.15 K", "input=310.15 K", "expected=K")
  @Iteration("310.15 kelvin", "input=310.15 kelvin", "expected=kelvin")
  fun testTokenize_correctKelvinUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(310.15)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_incorrectKelvinUnits_uppercaseKelvin_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("310.15 Kelvin").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(310.15)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Kelvin")
  }

  @Test
  @Iteration("37 degC", "input=37 degC", "expected=degC")
  @Iteration("37 celsius", "input=37 celsius", "expected=celsius")
  fun testTokenize_correctCelsiusUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(37)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("37 C", "input=37 C", "expected=C")
  @Iteration("37 Celsius", "input=37 Celsius", "expected=Celsius")
  fun testTokenize_incorrectCelsiusUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(37)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 rad", "input=10 rad", "expected=rad")
  @Iteration("10 radian", "input=10 radian", "expected=radian")
  @Iteration("10 radians", "input=10 radians", "expected=radians")
  fun testTokenize_correctRadianUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Rad", "input=10 Rad", "expected=Rad")
  @Iteration("10 Radian", "input=10 Radian", "expected=Radian")
  fun testTokenize_incorrectRadianUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 deg", "input=10 deg", "expected=deg")
  @Iteration("10 degree", "input=10 degree", "expected=degree")
  @Iteration("10 degrees", "input=10 degrees", "expected=degrees")
  fun testTokenize_correctDegreeUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Deg", "input=10 Deg", "expected=Deg")
  @Iteration("10 Degree", "input=10 Degree", "expected=Degree")
  @Iteration("10 Degrees", "input=10 Degrees", "expected=Degrees")
  fun testTokenize_incorrectDegreeUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 s", "input=10 s", "expected=s")
  @Iteration("10 second", "input=10 second", "expected=second")
  @Iteration("10 seconds", "input=10 seconds", "expected=seconds")
  @Iteration("10 sec", "input=10 sec", "expected=sec")
  @Iteration("10 secs", "input=10 secs", "expected=secs")
  fun testTokenize_correctSecondUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 S", "input=10 S", "expected=S")
  @Iteration("10 Second", "input=10 Second", "expected=Second")
  @Iteration("10 Seconds", "input=10 Seconds", "expected=Seconds")
  fun testTokenize_incorrectSecondUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 min", "input=10 min", "expected=min")
  @Iteration("10 mins", "input=10 mins", "expected=mins")
  @Iteration("10 minute", "input=10 minute", "expected=minute")
  @Iteration("10 minutes", "input=10 minutes", "expected=minutes")
  fun testTokenize_correctMinuteUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Min", "input=10 Min", "expected=Min")
  @Iteration("10 Minute", "input=10 Minute", "expected=Minute")
  @Iteration("10 Minutes", "input=10 Minutes", "expected=Minutes")
  fun testTokenize_incorrectMinuteUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 h", "input=10 h", "expected=h")
  @Iteration("10 hr", "input=10 hr", "expected=hr")
  @Iteration("10 hrs", "input=10 hrs", "expected=hrs")
  @Iteration("10 hour", "input=10 hour", "expected=hour")
  @Iteration("10 hours", "input=10 hours", "expected=hours")
  fun testTokenize_correctHourUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 H", "input=10 H", "expected=H")
  @Iteration("10 Hr", "input=10 Hr", "expected=Hr")
  @Iteration("10 Hour", "input=10 Hour", "expected=Hour")
  @Iteration("10 Hours", "input=10 Hours", "expected=Hours")
  fun testTokenize_incorrectHourUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Hz", "input=10 Hz", "expected=Hz")
  @Iteration("10 hertz", "input=10 hertz", "expected=hertz")
  fun testTokenize_correctHertzUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_incorrectHertzUnits_hz_parsesHourUnitAndZeptoPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 hz").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("hz")
  }

  @Test
  fun testTokenize_incorrectHertzUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Hertz").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Hertz")
  }

  @Test
  @Iteration("10 mol", "input=10 mol", "expected=mol")
  @Iteration("10 mole", "input=10 mole", "expected=mole")
  @Iteration("10 moles", "input=10 moles", "expected=moles")
  fun testTokenize_correctMoleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Mol", "input=10 Mol", "expected=Mol")
  @Iteration("10 Mole", "input=10 Mole", "expected=Mole")
  @Iteration("10 Moles", "input=10 Moles", "expected=Moles")
  fun testTokenize_incorrectMoleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 cd", "input=10 cd", "expected=cd")
  @Iteration("10 candela", "input=10 candela", "expected=candela")
  fun testTokenize_correctCandelaUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Cd", "input=10 Cd", "expected=Cd")
  @Iteration("10 CD", "input=10 CD", "expected=CD")
  @Iteration("10 Candela", "input=10 Candela", "expected=Candela")
  fun testTokenize_incorrectCandelaUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 N", "input=10 N", "expected=N")
  @Iteration("10 newton", "input=10 newton", "expected=newton")
  fun testTokenize_correctNewtonUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_incorrectNewtonUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Newton").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Newton")
  }

  @Test
  @Iteration("10 J", "input=10 J", "expected=J")
  @Iteration("10 joule", "input=10 joule", "expected=joule")
  @Iteration("10 joules", "input=10 joules", "expected=joules")
  fun testTokenize_correctJouleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 j", "input=10 j", "expected=j")
  @Iteration("10 Joule", "input=10 Joule", "expected=Joule")
  @Iteration("10 Joules", "input=10 Joules", "expected=Joules")
  fun testTokenize_incorrectJouleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("100 W", "input=100 W", "expected=W")
  @Iteration("100 watt", "input=100 watt", "expected=watt")
  @Iteration("100 watts", "input=100 watts", "expected=watts")
  fun testTokenize_correctWattUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("100 Watt", "input=100 Watt", "expected=Watt")
  @Iteration("100 Watts", "input=100 Watts", "expected=Watts")
  fun testTokenize_incorrectWattUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("100 Pa", "input=100 Pa", "expected=Pa")
  fun testTokenize_correctPascalUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("100 pa", "input=100 pa", "expected=pa")
  @Iteration("100 pascal", "input=100 pascal", "expected=pascal")
  fun testTokenize_incorrectPascalUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 A", "input=10 A", "expected=A")
  @Iteration("10 ampere", "input=10 ampere", "expected=ampere")
  @Iteration("10 amperes", "input=10 amperes", "expected=amperes")
  fun testTokenize_correctAmpereUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Ampere", "input=10 Ampere", "expected=Ampere")
  @Iteration("10 Amperes", "input=10 Amperes", "expected=Amperes")
  fun testTokenize_incorrectAmpereUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 V", "input=10 V", "expected=V")
  @Iteration("10 volt", "input=10 volt", "expected=volt")
  @Iteration("10 volts", "input=10 volts", "expected=volts")
  fun testTokenize_correctVoltUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 v", "input=10 v", "expected=v")
  @Iteration("10 Volt", "input=10 Volt", "expected=Volt")
  @Iteration("10 Volts", "input=10 Volts", "expected=Volts")
  fun testTokenize_incorrectVoltUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 ohm", "input=10 ohm", "expected=ohm")
  @Iteration("10 ohms", "input=10 ohms", "expected=ohms")
  fun testTokenize_correctOhmUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 Ohm", "input=10 Ohm", "expected=Ohm")
  @Iteration("10 Ohms", "input=10 Ohms", "expected=Ohms")
  fun testTokenize_incorrectOhmUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 deca", "input=10 deca", "expected=deca")
  @Iteration("10 da", "input=10 da", "expected=da")
  fun testTokenize_deca_parsesDecaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 hecto", "input=10 hecto", "expected=hecto")
  @Iteration("10 h", "input=10 h", "expected=h")
  fun testTokenize_hecto_parsesHectoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 kilo", "input=10 kilo", "expected=kilo")
  @Iteration("10 k", "input=10 k", "expected=k")
  fun testTokenize_kilo_parsesKiloSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 mega", "input=10 mega", "expected=mega")
  @Iteration("10 M", "input=10 M", "expected=M")
  fun testTokenize_mega_parsesMegaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 giga", "input=10 giga", "expected=giga")
  @Iteration("10 G", "input=10 G", "expected=G")
  fun testTokenize_giga_parsesGigaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 tera", "input=10 tera", "expected=tera")
  @Iteration("10 T", "input=10 T", "expected=T")
  fun testTokenize_tera_parsesTeraSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 peta", "input=10 peta", "expected=peta")
  @Iteration("10 P", "input=10 P", "expected=P")
  fun testTokenize_peta_parsesPetaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 exa", "input=10 exa", "expected=exa")
  @Iteration("10 E", "input=10 E", "expected=E")
  fun testTokenize_exa_parsesExaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 zetta", "input=10 zetta", "expected=zetta")
  @Iteration("10 Z", "input=10 Z", "expected=Z")
  fun testTokenize_zetta_parsesZettaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 yotta", "input=10 yotta", "expected=yotta")
  @Iteration("10 Y", "input=10 Y", "expected=Y")
  fun testTokenize_yotta_parsesYottaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 deci", "input=10 deci", "expected=deci")
  @Iteration("10 d", "input=10 d", "expected=d")
  fun testTokenize_deci_parsesDeciSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 centi", "input=10 centi", "expected=centi")
  @Iteration("10 c", "input=10 c", "expected=c")
  fun testTokenize_centi_parsesCentiSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 milli", "input=10 milli", "expected=milli")
  @Iteration("10 m", "input=10 m", "expected=m")
  fun testTokenize_milli_parsesMilliSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 micro", "input=10 micro", "expected=micro")
  @Iteration("10 u", "input=10 u", "expected=u")
  fun testTokenize_micro_parsesMicroSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 nano", "input=10 nano", "expected=nano")
  @Iteration("10 n", "input=10 n", "expected=n")
  fun testTokenize_nano_parsesNanoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 pico", "input=10 pico", "expected=pico")
  @Iteration("10 p", "input=10 p", "expected=p")
  fun testTokenize_pico_parsesPicoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 femto", "input=10 femto", "expected=femto")
  @Iteration("10 f", "input=10 f", "expected=f")
  fun testTokenize_femto_parsesFemtoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 atto", "input=10 atto", "expected=atto")
  @Iteration("10 a", "input=10 a", "expected=a")
  fun testTokenize_atto_parsesAttoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 zepto", "input=10 zepto", "expected=zepto")
  @Iteration("10 z", "input=10 z", "expected=z")
  fun testTokenize_zepto_parsesZeptoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  @Iteration("10 yocto", "input=10 yocto", "expected=yocto")
  @Iteration("10 y", "input=10 y", "expected=y")
  fun testTokenize_yocto_parsesYoctoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo(expected)
  }

  @Test
  fun testTokenize_accelerationWithSpacedUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(" 9.8 m s ^ -2 ").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(9.8)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isMinusSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_velocityMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m/s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  fun testTokenize_velocityKilometerPerHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 km/h").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("km")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("h")
  }

  @Test
  fun testTokenize_accelerationMeterPerSecondSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("9.8 m/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(9.8)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_dollarsPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("20 dollars / m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("dollars")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_dollarPrefixPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("$ 20 / m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("$")
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(20)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_rupeesPerSquareMeterWithDivide_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("₹ 100 /m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("₹")
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_rupeesWithMeterNegativeExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("₹ 100 m^-2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("₹")
    assertThat(tokens[1]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[2]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isMinusSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_kilogramPerMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / m / s").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isDivideSymbol()
    assertThat(tokens[5]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  fun testTokenize_newtonPerMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 N / m / s").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("N")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isDivideSymbol()
    assertThat(tokens[5]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  fun testTokenize_kilogramPerMeterTimesSecondWithParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / (m * s)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[7]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_jouleWithUnitProductInParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("50 J / (kg * K)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(50)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("J")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isUnitWithRawValueThat().isEqualTo("K")
    assertThat(tokens[7]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_wattPerSquareMeterKelvin_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 W / (m^2 * K)").toList()

    assertThat(tokens).hasSize(10)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("W")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWithValueThat().isEqualTo(2)
    assertThat(tokens[7]).isMultiplySymbol()
    assertThat(tokens[8]).isUnitWithRawValueThat().isEqualTo("K")
    assertThat(tokens[9]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_realNumberWithBracketedUnitsAndExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("12.50 (kg * m)^-3").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(12.50)
    assertThat(tokens[1]).isLeftParenthesisSymbol()
    assertThat(tokens[2]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[3]).isMultiplySymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[5]).isRightParenthesisSymbol()
    assertThat(tokens[6]).isExponentiationSymbol()
    assertThat(tokens[7]).isMinusSymbol()
    assertThat(tokens[8]).isPositiveIntegerWithValueThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_bracketedUnitsSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("(m * s)^2").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[4]).isRightParenthesisSymbol()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_nestedParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("((m))").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isLeftParenthesisSymbol()
    assertThat(tokens[2]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[3]).isRightParenthesisSymbol()
    assertThat(tokens[4]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_complexNestedExpression_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / ((m * s) * K)").toList()

    assertThat(tokens).hasSize(12)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isLeftParenthesisSymbol()
    assertThat(tokens[5]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[6]).isMultiplySymbol()
    assertThat(tokens[7]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[8]).isRightParenthesisSymbol()
    assertThat(tokens[9]).isMultiplySymbol()
    assertThat(tokens[10]).isUnitWithRawValueThat().isEqualTo("K")
    assertThat(tokens[11]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_densityKilogramPerCubicMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 kg/m^3").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1000)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_densityGramPerCubicCentimeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.7 g/cc").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(2.7)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("g")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("cc")
  }

  @Test
  fun testTokenize_pressurePascal_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("101325 Pa").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(101325)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("Pa")
  }

  @Test
  fun testTokenize_pressureNewtonPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 N/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("N")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_electricalResistanceOhmMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.7 ohm * m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(1.7)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("ohm")
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
  }

  @Test
  fun testTokenize_electricFieldVoltPerMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("500 V/m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(500)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("V")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
  }

  @Test
  fun testTokenize_powerWattPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 W/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1000)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("W")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_energyKilowattHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("500 kW * h").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(500)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kW")
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("h")
  }

  @Test
  fun testTokenize_workNewtonMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 N * m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("N")
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
  }

  @Test
  fun testTokenize_frequencyKilohertz_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.4 kHz").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(2.4)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kHz")
  }

  @Test
  fun testTokenize_angularVelocityRadPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("3.14 rad/s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(3.14)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("rad")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  fun testTokenize_angularAcceleration_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.5 rad/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(2.5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("rad")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_molarConcentration_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("0.5 mol/L").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(0.5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("mol")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("L")
  }

  @Test
  fun testTokenize_molarMass_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("18 g/mol").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(18)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("g")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("mol")
  }

  @Test
  fun testTokenize_luminousIntensityCandelaPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 cd/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1000)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("cd")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_milligramPerMilliliter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 milligram/millilitre").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(5)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("milligram")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("millilitre")
  }

  @Test
  fun testTokenize_microgramPerLiter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 ug/l").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(100)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("ug")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("l")
  }

  @Test
  fun testTokenize_nanometerPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("50 nm/s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(50)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("nm")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  fun testTokenize_ouncesPerSquareInch_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("14.7 oz/in^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(14.7)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("oz")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("in")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_yardPerHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("60 yd/h").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(60)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("yd")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("h")
  }

  @Test
  fun testTokenize_feetPerSecondSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("32 ft/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(32)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("ft")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_compoundUnitWithExtraWhitespace_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("  10   kg  /  m  ^  2  ").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_compoundUnitWithNoWhitespace_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10kg/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_unitWithLargePositiveExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^10").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWithValueThat().isEqualTo(10)
  }

  @Test
  fun testTokenize_unitWithLargeNegativeExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^-10").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isMinusSymbol()
    assertThat(tokens[4]).isPositiveIntegerWithValueThat().isEqualTo(10)
  }

  @Test
  fun testTokenize_multipleUnitsWithExponents_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^2 * s^-1").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(1)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWithValueThat().isEqualTo(2)
    assertThat(tokens[4]).isMultiplySymbol()
    assertThat(tokens[5]).isUnitWithRawValueThat().isEqualTo("s")
    assertThat(tokens[6]).isExponentiationSymbol()
    assertThat(tokens[7]).isMinusSymbol()
    assertThat(tokens[8]).isPositiveIntegerWithValueThat().isEqualTo(1)
  }

  @Test
  fun testTokenize_scientificNotationStyleNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("6.022 * 10^23 mol^-1").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(6.022)
    assertThat(tokens[1]).isMultiplySymbol()
    assertThat(tokens[2]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isPositiveIntegerWithValueThat().isEqualTo(23)
    assertThat(tokens[5]).isUnitWithRawValueThat().isEqualTo("mol")
    assertThat(tokens[6]).isExponentiationSymbol()
    assertThat(tokens[7]).isMinusSymbol()
    assertThat(tokens[8]).isPositiveIntegerWithValueThat().isEqualTo(1)
  }

  @Test
  fun testTokenize_emptyParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("()").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_unmatchedLeftParenthesis_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("(m").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("m")
  }

  @Test
  fun testTokenize_unmatchedRightParenthesis_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("m)").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[1]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_gravitationalConstant_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("6.674 N * m^2 / kg^2").toList()

    assertThat(tokens).hasSize(10)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(6.674)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("N")
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWithValueThat().isEqualTo(2)
    assertThat(tokens[6]).isDivideSymbol()
    assertThat(tokens[7]).isUnitWithRawValueThat().isEqualTo("kg")
    assertThat(tokens[8]).isExponentiationSymbol()
    assertThat(tokens[9]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_specificHeatCapacity_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("4.186 J / (g * degC)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(4.186)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("J")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("g")
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isUnitWithRawValueThat().isEqualTo("degC")
    assertThat(tokens[7]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_thermalConductivity_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("0.6 W / (m * K)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveRealNumberWithValueThat().isEqualTo(0.6)
    assertThat(tokens[1]).isUnitWithRawValueThat().isEqualTo("W")
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isUnitWithRawValueThat().isEqualTo("K")
    assertThat(tokens[7]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_onlyOperators_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("/ * ^").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isDivideSymbol()
    assertThat(tokens[1]).isMultiplySymbol()
    assertThat(tokens[2]).isExponentiationSymbol()
  }

  @Test
  fun testTokenize_consecutiveExponents_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("m^^2").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[1]).isExponentiationSymbol()
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWithValueThat().isEqualTo(2)
  }

  @Test
  fun testTokenize_consecutiveDivisions_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("m//s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isUnitWithRawValueThat().isEqualTo("m")
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isUnitWithRawValueThat().isEqualTo("s")
  }

  @Test
  @Iteration("10 °C", "input=10 °C")
  @Iteration("10 °", "input=10 °")
  @Iteration("10 Ω", "input=10 Ω")
  fun testTokenize_unknownSymbols_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWithValueThat().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }
}
