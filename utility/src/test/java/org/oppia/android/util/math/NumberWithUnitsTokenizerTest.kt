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
import org.oppia.android.util.math.NumberWithUnitsTokenizer.Companion.Token
import org.robolectric.annotation.Config

/** Tests for [NumberWithUnitsTokenizer]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@Config
class NumberWithUnitsTokenizerTest {
  @Parameter
  lateinit var input: String

  @Parameter
  lateinit var expected: String

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
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  @Iteration("   3.14    ", "input=   3.14    ", "expected=3.14")
  @Iteration("  9.8  ", "input=  9.8  ", "expected=9.8")
  fun testTokenize_realNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_negativeNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("   -   2.5    ").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWhoseValue().isEqualTo(2.5)
  }

  @Test
  fun testTokenize_singleDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("7").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(7)
  }

  @Test
  @Iteration("17", "input=17", "expected=17")
  @Iteration("12345", "input=12345", "expected=12345")
  fun testTokenize_multiDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  fun testTokenize_integerWithLeadingZeros_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("007").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(7)
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
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_decimalWithoutLeadingDigit_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize(".5").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInvalidToken()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(5)
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
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(1.2)
    assertThat(tokens[1]).isInvalidToken()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  fun testTokenize_negativeInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-1").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(1)
  }

  @Test
  fun testTokenize_negativeRealNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-42.84").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWhoseValue().isEqualTo(42.84)
  }

  @Test
  fun testTokenize_fractionsWithPositiveIntegers_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1/2").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumerator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/80").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isPositiveIntegerWhoseValue().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumeratorAndDenominator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/-80").toList()

    // Note the parser will be responsible for interpreting the double-negative as a positive,
    // or throw an error but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMinusSymbol()
    assertThat(tokens[4]).isPositiveIntegerWhoseValue().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithDecimal_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.5/3.7").toList()

    // Note the parser will be responsible for throwing an error since fractions with decimals
    // aren't valid, but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(1.5)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveRealNumberWhoseValue().isEqualTo(3.7)
  }

  @Test
  @Iteration("₹ 10", "input=₹ 10", "expected=10")
  @Iteration("Rs 10", "input=Rs 10", "expected=10")
  fun testTokenize_rupeePrefixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isRupeePrefixUnit()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  @Iteration("10 rupee", "input=10 rupee")
  @Iteration("10 rupees", "input=10 rupees")
  @Iteration("10 Rupee", "input=10 Rupee")
  @Iteration("10 Rupees", "input=10 Rupees")
  fun testTokenize_rupeeSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isRupeeSuffixUnit()
  }

  @Test
  @Iteration("10 paise", "input=10 paise")
  @Iteration("10 paisa", "input=10 paisa")
  @Iteration("10 Paise", "input=10 Paise")
  @Iteration("10 Paisa", "input=10 Paisa")
  fun testTokenize_paisaSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isPaisaSuffixUnit()
  }

  @Test
  fun testTokenize_dollarPrefixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("$12.5").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isDollarPrefixUnit()
    assertThat(tokens[1]).isPositiveRealNumberWhoseValue().isEqualTo(12.5)
  }

  @Test
  @Iteration("10 USD", "input=10 USD")
  @Iteration("10 dollars", "input=10 dollars")
  @Iteration("10 dollar", "input=10 dollar")
  @Iteration("10 Dollars", "input=10 Dollars")
  @Iteration("10 Dollar", "input=10 Dollar")
  fun testTokenize_dollarSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isDollarSuffixUnit()
  }

  @Test
  @Iteration("10 ¢", "input=10 ¢")
  @Iteration("10 cents", "input=10 cents")
  @Iteration("10 cent", "input=10 cent")
  @Iteration("10 Cents", "input=10 Cents")
  @Iteration("10 Cent", "input=10 Cent")
  fun testTokenize_centSuffixUnit_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCentSuffixUnit()
  }

  @Test
  @Iteration("20 m", "input=20 m")
  @Iteration("20 meter", "input=20 meter")
  @Iteration("20 meters", "input=20 meters")
  fun testTokenize_correctMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isMeterUnit()
  }

  @Test
  @Iteration("20 Meter", "input=20 Meter")
  @Iteration("20 Meters", "input=20 Meters")
  fun testTokenize_incorrectMeterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
    assertThat(tokens[2]).isInvalidToken()
  }

  @Test
  @Iteration("20 in", "input=20 in")
  @Iteration("20 inch", "input=20 inch")
  @Iteration("20 inches", "input=20 inches")
  fun testTokenize_correctInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isInchUnit()
  }

  @Test
  @Iteration("20 In", "input=20 In")
  @Iteration("20 Inch", "input=20 Inch")
  @Iteration("20 Inches", "input=20 Inches")
  fun testTokenize_incorrectInchUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("20 ft", "input=20 ft")
  @Iteration("20 foot", "input=20 foot")
  @Iteration("20 feet", "input=20 feet")
  fun testTokenize_correctFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isFootUnit()
  }

  @Test
  @Iteration("20 Ft", "input=20 Ft")
  @Iteration("20 Foot", "input=20 Foot")
  @Iteration("20 Feet", "input=20 Feet")
  fun testTokenize_incorrectFootUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)

    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
  }

  @Test
  @Iteration("20 yd", "input=20 yd")
  @Iteration("20 yard", "input=20 yard")
  @Iteration("20 yards", "input=20 yards")
  fun testTokenize_correctYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isYardUnit()
  }

  @Test
  @Iteration("20 Yd", "input=20 Yd")
  @Iteration("20 Yard", "input=20 Yard")
  @Iteration("20 Yards", "input=20 Yards")
  fun testTokenize_incorrectYardUnits_parsesYottaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isSiPrefixWithValue("Y")
  }

  @Test
  @Iteration("20 g", "input=20 g")
  @Iteration("20 gram", "input=20 gram")
  @Iteration("20 grams", "input=20 grams")
  fun testTokenize_correctGramUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isGramUnit()
  }

  @Test
  @Iteration("20 Gram", "input=20 Gram")
  @Iteration("20 Grams", "input=20 Grams")
  fun testTokenize_incorrectGramUnits_parsesGigaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isSiPrefixWithValue("G")
  }

  @Test
  @Iteration("20 gr", "input=20 gr")
  @Iteration("20 grain", "input=20 grain")
  @Iteration("20 grains", "input=20 grains")
  fun testTokenize_correctGrainUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isGrainUnit()
  }

  @Test
  @Iteration("20 Gr", "input=20 Gr")
  @Iteration("20 Grain", "input=20 Grain")
  @Iteration("20 Grains", "input=20 Grains")
  fun testTokenize_incorrectGrainUnits_parsesGigaPrefixAndRemaining() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isSiPrefixWithValue("G")
    assertThat(tokens[2]).isInvalidToken()
  }

  @Test
  @Iteration("20 oz", "input=20 oz")
  @Iteration("20 ounce", "input=20 ounce")
  @Iteration("20 ounces", "input=20 ounces")
  fun testTokenize_correctOunceUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isOunceUnit()
  }

  @Test
  @Iteration("20 Oz", "input=20 Oz")
  @Iteration("20 Ounce", "input=20 Ounce")
  @Iteration("20 Ounces", "input=20 Ounces")
  fun testTokenize_incorrectOunceUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  fun testTokenize_correctSquareMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m2").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSquareMeterUnit()
  }

  @Test
  fun testTokenize_incorrectSquareMeterUnits_parsesMegaPrefixAndInteger() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M2").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  @Iteration("10 sqft", "input=10 sqft")
  @Iteration("10 sqfeet", "input=10 sqfeet")
  fun testTokenize_correctSquareFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSquareFootUnit()
  }

  @Test
  @Iteration("10 Sqft", "input=10 Sqft")
  @Iteration("10 Sqfeet", "input=10 Sqfeet")
  fun testTokenize_incorrectSquareFootUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 sqyd", "input=10 sqyd")
  @Iteration("10 sqyard", "input=10 sqyard")
  fun testTokenize_correctSquareYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSquareYardUnit()
  }

  @Test
  @Iteration("10 Sqyd", "input=10 Sqyd")
  @Iteration("10 Sqyard", "input=10 Sqyard")
  fun testTokenize_incorrectSquareYardUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  fun testTokenize_correctCubicMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m3").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCubicMeterUnit()
  }

  @Test
  fun testTokenize_incorrectCubicMeterUnits_parsesMegaPrefixAndInteger() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M3").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  @Iteration("10 litre", "input=10 litre")
  @Iteration("10 liter", "input=10 liter")
  @Iteration("10 litres", "input=10 litres")
  @Iteration("10 liters", "input=10 liters")
  @Iteration("10 L", "input=10 L")
  @Iteration("10 l", "input=10 l")
  @Iteration("10 lt", "input=10 lt")
  fun testTokenize_correctLiterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isLiterUnit()
  }

  @Test
  @Iteration("10 Litre", "input=10 Litre")
  @Iteration("10 Litres", "input=10 Litres")
  @Iteration("10 Liter", "input=10 Liter")
  @Iteration("10 Liters", "input=10 Liters")
  @Iteration("10 Lt", "input=10 Lt")
  fun testTokenize_incorrectLiterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  fun testTokenize_correctCubicCentimeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cc").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCubicCentimeterUnit()
  }

  @Test
  fun testTokenize_incorrectCubicCentimeterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 CC").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
  }

  @Test
  fun testTokenize_correctCubicInchUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuin").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCubicInchUnit()
  }

  @Test
  fun testTokenize_incorrectCubicInchUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuin").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken() // C
    assertThat(tokens[2]).isSiPrefixWithValue("u") // u
    assertThat(tokens[3]).isInchUnit() // in
  }

  @Test
  fun testTokenize_correctCubicFootUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuft").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCubicFootUnit()
  }

  @Test
  fun testTokenize_incorrectCubicFootUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuft").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken() // C
    assertThat(tokens[2]).isSiPrefixWithValue("u") // u
    assertThat(tokens[3]).isFootUnit() // ft
  }

  @Test
  fun testTokenize_correctCubicYardUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 cuyd").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCubicYardUnit()
  }

  @Test
  fun testTokenize_incorrectCubicYardUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Cuyd").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken() // C
    assertThat(tokens[2]).isSiPrefixWithValue("u") // u
    assertThat(tokens[3]).isYardUnit() // yd
  }

  @Test
  @Iteration("310.15 K", "input=310.15 K")
  @Iteration("310.15 kelvin", "input=310.15 kelvin")
  fun testTokenize_correctKelvinUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(310.15)
    assertThat(tokens[1]).isKelvinUnit()
  }

  @Test
  fun testTokenize_incorrectKelvinUnits_uppercaseKelvin_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("310.15 Kelvin").toList()

    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(310.15)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("37 degC", "input=37 degC")
  @Iteration("37 celsius", "input=37 celsius")
  fun testTokenize_correctCelsiusUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(37)
    assertThat(tokens[1]).isCelsiusUnit()
  }

  @Test
  @Iteration("37 °C", "input=37 °C")
  @Iteration("37 C", "input=37 C")
  @Iteration("37 Celsius", "input=37 Celsius")
  fun testTokenize_incorrectCelsiusUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(37)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 rad", "input=10 rad")
  @Iteration("10 radian", "input=10 radian")
  @Iteration("10 radians", "input=10 radians")
  fun testTokenize_correctRadianUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isRadianUnit()
  }

  @Test
  @Iteration("10 Rad", "input=10 Rad")
  @Iteration("10 Radian", "input=10 Radian")
  fun testTokenize_incorrectRadianUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 deg", "input=10 deg")
  @Iteration("10 degree", "input=10 degree")
  @Iteration("10 degrees", "input=10 degrees")
  fun testTokenize_correctDegreeUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isDegreeUnit()
  }

  @Test
  @Iteration("10 Deg", "input=10 Deg")
  @Iteration("10 Degree", "input=10 Degree")
  @Iteration("10 Degrees", "input=10 Degrees")
  @Iteration("10 °", "input=10 °")
  fun testTokenize_incorrectDegreeUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 s", "input=10 s")
  @Iteration("10 second", "input=10 second")
  @Iteration("10 seconds", "input=10 seconds")
  @Iteration("10 sec", "input=10 sec")
  @Iteration("10 secs", "input=10 secs")
  fun testTokenize_correctSecondUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSecondUnit()
  }

  @Test
  @Iteration("10 S", "input=10 S")
  @Iteration("10 Second", "input=10 Second")
  @Iteration("10 Seconds", "input=10 Seconds")
  fun testTokenize_incorrectSecondUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 min", "input=10 min")
  @Iteration("10 mins", "input=10 mins")
  @Iteration("10 minute", "input=10 minute")
  @Iteration("10 minutes", "input=10 minutes")
  fun testTokenize_correctMinuteUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isUnitWithRawValue(input.substringAfter(" "))
    assertThat(tokens[1]).isMinuteUnit()
  }

  @Test
  @Iteration("10 Min", "input=10 Min")
  @Iteration("10 Minute", "input=10 Minute")
  @Iteration("10 Minutes", "input=10 Minutes")
  fun testTokenize_incorrectMinuteUnits_parsesMegaPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
  }

  @Test
  @Iteration("10 h", "input=10 h")
  @Iteration("10 hr", "input=10 hr")
  @Iteration("10 hrs", "input=10 hrs")
  @Iteration("10 hour", "input=10 hour")
  @Iteration("10 hours", "input=10 hours")
  fun testTokenize_correctHourUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isHourUnit()
  }

  @Test
  @Iteration("10 H", "input=10 H")
  @Iteration("10 Hr", "input=10 Hr")
  @Iteration("10 Hour", "input=10 Hour")
  @Iteration("10 Hours", "input=10 Hours")
  fun testTokenize_incorrectHourUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 Hz", "input=10 Hz")
  @Iteration("10 hertz", "input=10 hertz")
  fun testTokenize_correctHertzUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isHertzUnit()
  }

  @Test
  fun testTokenize_incorrectHertzUnits_hz_parsesHourUnitAndZeptoPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 hz").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isHourUnit()
    assertThat(tokens[2]).isSiPrefixWithValue("z")
  }

  @Test
  fun testTokenize_incorrectHertzUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Hertz").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 mol", "input=10 mol")
  @Iteration("10 mole", "input=10 mole")
  @Iteration("10 moles", "input=10 moles")
  fun testTokenize_correctMoleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isMoleUnit()
  }

  @Test
  @Iteration("10 Mol", "input=10 Mol")
  @Iteration("10 Mole", "input=10 Mole")
  @Iteration("10 Moles", "input=10 Moles")
  fun testTokenize_incorrectMoleUnits_parsesMegaPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
    assertThat(tokens[2]).isInvalidToken()
  }

  @Test
  @Iteration("10 cd", "input=10 cd")
  @Iteration("10 candela", "input=10 candela")
  fun testTokenize_correctCandelaUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isCandelaUnit()
  }

  @Test
  @Iteration("10 Cd", "input=10 Cd")
  @Iteration("10 CD", "input=10 CD")
  @Iteration("10 Candela", "input=10 Candela")
  fun testTokenize_incorrectCandelaUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 N", "input=10 N")
  @Iteration("10 newton", "input=10 newton")
  fun testTokenize_correctNewtonUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isNewtonUnit()
  }

  @Test
  fun testTokenize_incorrectNewtonUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 Newton").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 J", "input=10 J")
  @Iteration("10 joule", "input=10 joule")
  @Iteration("10 joules", "input=10 joules")
  fun testTokenize_correctJouleUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isJouleUnit()
  }

  @Test
  @Iteration("10 j", "input=10 j")
  @Iteration("10 Joule", "input=10 Joule")
  @Iteration("10 Joules", "input=10 Joules")
  fun testTokenize_incorrectJouleUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("100 W", "input=100 W")
  @Iteration("100 watt", "input=100 watt")
  @Iteration("100 watts", "input=100 watts")
  fun testTokenize_correctWattUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isWattUnit()
  }

  @Test
  @Iteration("100 Watt", "input=100 Watt")
  @Iteration("100 Watts", "input=100 Watts")
  fun testTokenize_incorrectWattUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("100 Pa", "input=100 Pa")
  fun testTokenize_correctPascalUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isPascalUnit()
  }

  @Test
  @Iteration("100 pa", "input=100 pa")
  @Iteration("100 pascal", "input=100 pascal")
  fun testTokenize_incorrectPascalUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 A", "input=10 A")
  @Iteration("10 ampere", "input=10 ampere")
  @Iteration("10 amperes", "input=10 amperes")
  fun testTokenize_correctAmpereUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isAmpereUnit()
  }

  @Test
  @Iteration("10 Ampere", "input=10 Ampere")
  @Iteration("10 Amperes", "input=10 Amperes")
  fun testTokenize_incorrectAmpereUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 V", "input=10 V")
  @Iteration("10 volt", "input=10 volt")
  @Iteration("10 volts", "input=10 volts")
  fun testTokenize_correctVoltUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isVoltUnit()
  }

  @Test
  @Iteration("10 v", "input=10 v")
  @Iteration("10 Volt", "input=10 Volt")
  @Iteration("10 Volts", "input=10 Volts")
  fun testTokenize_incorrectVoltUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 ohm", "input=10 ohm")
  @Iteration("10 ohms", "input=10 ohms")
  fun testTokenize_correctOhmUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isOhmUnit()
  }

  @Test
  @Iteration("10 Ohm", "input=10 Ohm")
  @Iteration("10 Ohms", "input=10 Ohms")
  @Iteration("10 Ω", "input=10 Ω")
  fun testTokenize_incorrectOhmUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
  }

  @Test
  @Iteration("10 deca", "input=10 deca")
  @Iteration("10 da", "input=10 da")
  fun testTokenize_deca_parsesDecaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("da")
  }

  @Test
  @Iteration("10 hecto", "input=10 hecto")
  // Note: "h" is also the symbol for hour
  // @Iteration("10 h", "input=10 h")
  fun testTokenize_hecto_parsesHectoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("h")
  }

  @Test
  @Iteration("10 kilo", "input=10 kilo")
  @Iteration("10 k", "input=10 k")
  fun testTokenize_kilo_parsesKiloSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
  }

  @Test
  @Iteration("10 mega", "input=10 mega")
  @Iteration("10 M", "input=10 M")
  fun testTokenize_mega_parsesMegaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("M")
  }

  @Test
  @Iteration("10 giga", "input=10 giga")
  @Iteration("10 G", "input=10 G")
  fun testTokenize_giga_parsesGigaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("G")
  }

  @Test
  @Iteration("10 tera", "input=10 tera")
  @Iteration("10 T", "input=10 T")
  fun testTokenize_tera_parsesTeraSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("T")
  }

  @Test
  @Iteration("10 peta", "input=10 peta")
  @Iteration("10 P", "input=10 P")
  fun testTokenize_peta_parsesPetaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("P")
  }

  @Test
  @Iteration("10 exa", "input=10 exa")
  @Iteration("10 E", "input=10 E")
  fun testTokenize_exa_parsesExaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("E")
  }

  @Test
  @Iteration("10 zetta", "input=10 zetta")
  @Iteration("10 Z", "input=10 Z")
  fun testTokenize_zetta_parsesZettaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("Z")
  }

  @Test
  @Iteration("10 yotta", "input=10 yotta")
  @Iteration("10 Y", "input=10 Y")
  fun testTokenize_yotta_parsesYottaSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("Y")
  }

  @Test
  @Iteration("10 deci", "input=10 deci")
  @Iteration("10 d", "input=10 d")
  fun testTokenize_deci_parsesDeciSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("d")
  }

  @Test
  @Iteration("10 centi", "input=10 centi")
  @Iteration("10 c", "input=10 c")
  fun testTokenize_centi_parsesCentiSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("c")
  }

  @Test
  @Iteration("10 milli", "input=10 milli")
  // Note: "m" is also the symbol for meter
  // @Iteration("10 m", "input=10 m")
  fun testTokenize_milli_parsesMilliSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("m")
  }

  @Test
  @Iteration("10 micro", "input=10 micro")
  @Iteration("10 u", "input=10 u")
  fun testTokenize_micro_parsesMicroSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("u")
  }

  @Test
  @Iteration("10 nano", "input=10 nano")
  @Iteration("10 n", "input=10 n")
  fun testTokenize_nano_parsesNanoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("n")
  }

  @Test
  @Iteration("10 pico", "input=10 pico")
  @Iteration("10 p", "input=10 p")
  fun testTokenize_pico_parsesPicoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("p")
  }

  @Test
  @Iteration("10 femto", "input=10 femto")
  @Iteration("10 f", "input=10 f")
  fun testTokenize_femto_parsesFemtoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("f")
  }

  @Test
  @Iteration("10 atto", "input=10 atto")
  @Iteration("10 a", "input=10 a")
  fun testTokenize_atto_parsesAttoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("a")
  }

  @Test
  @Iteration("10 zepto", "input=10 zepto")
  @Iteration("10 z", "input=10 z")
  fun testTokenize_zepto_parsesZeptoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("z")
  }

  @Test
  @Iteration("10 yocto", "input=10 yocto")
  @Iteration("10 y", "input=10 y")
  fun testTokenize_yocto_parsesYoctoSiPrefix() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("y")
  }

  @Test
  fun testTokenize_accelerationWithSpacedUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(" 9.8 m s ^ -2 ").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(9.8)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isSecondUnit()
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isMinusSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_velocityMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m/s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
  }

  @Test
  fun testTokenize_velocityKilometerPerHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 km/h").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isMeterUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isHourUnit()
  }

  @Test
  fun testTokenize_accelerationMeterPerSecondSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("9.8 m/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(9.8)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_dollarsPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("20 dollars / m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[1]).isDollarSuffixUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_dollarPrefixPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("$ 20 / m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isDollarPrefixUnit()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(20)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_rupeesPerSquareMeterWithDivide_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("₹ 100 /m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isRupeePrefixUnit()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_rupeesWithMeterNegativeExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("₹ 100 m^-2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isRupeePrefixUnit()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[2]).isMeterUnit()
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isMinusSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_kilogramPerMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / m / s").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(5)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isDivideSymbol()
    assertThat(tokens[6]).isSecondUnit()
  }

  @Test
  fun testTokenize_newtonPerMeterPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 N / m / s").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isNewtonUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isDivideSymbol()
    assertThat(tokens[5]).isSecondUnit()
  }

  @Test
  fun testTokenize_kilogramPerMeterTimesSecondWithParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / (m * s)").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(5)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isLeftParenthesisSymbol()
    assertThat(tokens[5]).isMeterUnit()
    assertThat(tokens[6]).isMultiplySymbol()
    assertThat(tokens[7]).isSecondUnit()
    assertThat(tokens[8]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_jouleWithUnitProductInParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("50 J / (kg * K)").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(50)
    assertThat(tokens[1]).isJouleUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isSiPrefixWithValue("k")
    assertThat(tokens[5]).isGramUnit()
    assertThat(tokens[6]).isMultiplySymbol()
    assertThat(tokens[7]).isKelvinUnit()
    assertThat(tokens[8]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_wattPerSquareMeterKelvin_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 W / (m^2 * K)").toList()

    assertThat(tokens).hasSize(10)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(5)
    assertThat(tokens[1]).isWattUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[7]).isMultiplySymbol()
    assertThat(tokens[8]).isKelvinUnit()
    assertThat(tokens[9]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_realNumberWithBracketedUnitsAndExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("12.50 (kg * m)^-3").toList()

    assertThat(tokens).hasSize(10)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(12.50)
    assertThat(tokens[1]).isLeftParenthesisSymbol()
    assertThat(tokens[2]).isSiPrefixWithValue("k")
    assertThat(tokens[3]).isGramUnit()
    assertThat(tokens[4]).isMultiplySymbol()
    assertThat(tokens[5]).isMeterUnit()
    assertThat(tokens[6]).isRightParenthesisSymbol()
    assertThat(tokens[7]).isExponentiationSymbol()
    assertThat(tokens[8]).isMinusSymbol()
    assertThat(tokens[9]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  fun testTokenize_bracketedUnitsSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("(m * s)^2").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isSecondUnit()
    assertThat(tokens[4]).isRightParenthesisSymbol()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_nestedParentheses_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("((m))").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
    assertThat(tokens[1]).isLeftParenthesisSymbol()
    assertThat(tokens[2]).isMeterUnit()
    assertThat(tokens[3]).isRightParenthesisSymbol()
    assertThat(tokens[4]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_complexNestedExpression_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 kg / ((m * s) * K)").toList()

    assertThat(tokens).hasSize(13)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(5)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isLeftParenthesisSymbol()
    assertThat(tokens[5]).isLeftParenthesisSymbol()
    assertThat(tokens[6]).isMeterUnit()
    assertThat(tokens[7]).isMultiplySymbol()
    assertThat(tokens[8]).isSecondUnit()
    assertThat(tokens[9]).isRightParenthesisSymbol()
    assertThat(tokens[10]).isMultiplySymbol()
    assertThat(tokens[11]).isKelvinUnit()
    assertThat(tokens[12]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_densityKilogramPerCubicMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 kg/m^3").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1000)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  fun testTokenize_densityGramPerCubicCentimeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.7 g/cc").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(2.7)
    assertThat(tokens[1]).isGramUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isCubicCentimeterUnit()
  }

  @Test
  fun testTokenize_pressurePascal_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("101325 Pa").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(101325)
    assertThat(tokens[1]).isPascalUnit()
  }

  @Test
  fun testTokenize_pressureNewtonPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 N/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isNewtonUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_electricalResistanceOhmMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.7 ohm * m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(1.7)
    assertThat(tokens[1]).isOhmUnit()
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isMeterUnit()
  }

  @Test
  fun testTokenize_electricFieldVoltPerMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("500 V/m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(500)
    assertThat(tokens[1]).isVoltUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
  }

  @Test
  fun testTokenize_powerWattPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 W/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1000)
    assertThat(tokens[1]).isWattUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_energyKilowattHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("500 kW * h").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(500)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isWattUnit()
    assertThat(tokens[3]).isMultiplySymbol()
    assertThat(tokens[4]).isHourUnit()
  }

  @Test
  fun testTokenize_workNewtonMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 N * m").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isNewtonUnit()
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isMeterUnit()
  }

  @Test
  fun testTokenize_frequencyKilohertz_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.4 kHz").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(2.4)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isHertzUnit()
  }

  @Test
  fun testTokenize_angularVelocityRadPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("3.14 rad/s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(3.14)
    assertThat(tokens[1]).isRadianUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
  }

  @Test
  fun testTokenize_angularAcceleration_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("2.5 rad/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(2.5)
    assertThat(tokens[1]).isRadianUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_molarConcentration_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("0.5 mol/L").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(0.5)
    assertThat(tokens[1]).isMoleUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLiterUnit()
  }

  @Test
  fun testTokenize_molarMass_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("18 g/mol").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(18)
    assertThat(tokens[1]).isGramUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMoleUnit()
  }

  @Test
  fun testTokenize_luminousIntensityCandelaPerSquareMeter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1000 cd/m^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1000)
    assertThat(tokens[1]).isCandelaUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_milligramPerMilliliter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("5 milligram/millilitre").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(5)
    assertThat(tokens[1]).isSiPrefixWithValue("m")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isSiPrefixWithValue("m")
    assertThat(tokens[5]).isLiterUnit()
  }

  @Test
  fun testTokenize_microgramPerLiter_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("100 ug/l").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(100)
    assertThat(tokens[1]).isSiPrefixWithValue("u")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isLiterUnit()
  }

  @Test
  fun testTokenize_nanometerPerSecond_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("50 nm/s").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(50)
    assertThat(tokens[1]).isSiPrefixWithValue("n")
    assertThat(tokens[2]).isMeterUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isSecondUnit()
  }

  @Test
  fun testTokenize_ouncesPerSquareInch_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("14.7 oz/in^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(14.7)
    assertThat(tokens[1]).isOunceUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isInchUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_yardPerHour_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("60 yd/h").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(60)
    assertThat(tokens[1]).isYardUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isHourUnit()
  }

  @Test
  fun testTokenize_feetPerSecondSquared_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("32 ft/s^2").toList()

    assertThat(tokens).hasSize(6)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(32)
    assertThat(tokens[1]).isFootUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_compoundUnitWithExtraWhitespace_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("  10   kg  /  m  ^  2  ").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_compoundUnitWithNoWhitespace_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10kg/m^2").toList()

    assertThat(tokens).hasSize(7)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSiPrefixWithValue("k")
    assertThat(tokens[2]).isGramUnit()
    assertThat(tokens[3]).isDivideSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isExponentiationSymbol()
    assertThat(tokens[6]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_unitWithLargePositiveExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^10").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWhoseValue().isEqualTo(10)
  }

  @Test
  fun testTokenize_unitWithLargeNegativeExponent_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^-10").toList()

    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isMinusSymbol()
    assertThat(tokens[4]).isPositiveIntegerWhoseValue().isEqualTo(10)
  }

  @Test
  fun testTokenize_multipleUnitsWithExponents_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1 m^2 * s^-1").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isMeterUnit()
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[4]).isMultiplySymbol()
    assertThat(tokens[5]).isSecondUnit()
    assertThat(tokens[6]).isExponentiationSymbol()
    assertThat(tokens[7]).isMinusSymbol()
    assertThat(tokens[8]).isPositiveIntegerWhoseValue().isEqualTo(1)
  }

  @Test
  fun testTokenize_scientificNotationStyleNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("6.022 * 10^23 mol^-1").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(6.022)
    assertThat(tokens[1]).isMultiplySymbol()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[3]).isExponentiationSymbol()
    assertThat(tokens[4]).isPositiveIntegerWhoseValue().isEqualTo(23)
    assertThat(tokens[5]).isMoleUnit()
    assertThat(tokens[6]).isExponentiationSymbol()
    assertThat(tokens[7]).isMinusSymbol()
    assertThat(tokens[8]).isPositiveIntegerWhoseValue().isEqualTo(1)
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
    assertThat(tokens[1]).isMeterUnit()
  }

  @Test
  fun testTokenize_unmatchedRightParenthesis_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("m)").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMeterUnit()
    assertThat(tokens[1]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_gravitationalConstant_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("6.674 N * m^2 / kg^2").toList()

    assertThat(tokens).hasSize(11)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(6.674)
    assertThat(tokens[1]).isNewtonUnit()
    assertThat(tokens[2]).isMultiplySymbol()
    assertThat(tokens[3]).isMeterUnit()
    assertThat(tokens[4]).isExponentiationSymbol()
    assertThat(tokens[5]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[6]).isDivideSymbol()
    assertThat(tokens[7]).isSiPrefixWithValue("k")
    assertThat(tokens[8]).isGramUnit()
    assertThat(tokens[9]).isExponentiationSymbol()
    assertThat(tokens[10]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_specificHeatCapacity_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("4.186 J / (g * degC)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(4.186)
    assertThat(tokens[1]).isJouleUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isGramUnit()
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isCelsiusUnit()
    assertThat(tokens[7]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_thermalConductivity_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("0.6 W / (m * K)").toList()

    assertThat(tokens).hasSize(8)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(0.6)
    assertThat(tokens[1]).isWattUnit()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isLeftParenthesisSymbol()
    assertThat(tokens[4]).isMeterUnit()
    assertThat(tokens[5]).isMultiplySymbol()
    assertThat(tokens[6]).isKelvinUnit()
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
    assertThat(tokens[0]).isMeterUnit()
    assertThat(tokens[1]).isExponentiationSymbol()
    assertThat(tokens[2]).isExponentiationSymbol()
    assertThat(tokens[3]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_consecutiveDivisions_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("m//s").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isMeterUnit()
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isSecondUnit()
  }
}
