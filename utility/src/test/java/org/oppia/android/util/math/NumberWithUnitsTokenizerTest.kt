package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.UnitTokenSubject.Companion.assertThat
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
  @Iteration("  1 0  0   ", "input=  1 0  0   ", "expected=100")
  fun testTokenize_positiveIntegerWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  @Iteration("   3  .  1   4    ", "input=   3  .  1   4    ", "expected=3.14")
  @Iteration("9.  8  ", "input=9.  8  ", "expected=9.8")
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
  @Iteration("20 M", "input=20 M")
  @Iteration("20 Meter", "input=20 Meter")
  @Iteration("20 Meters", "input=20 Meters")
  fun testTokenize_incorrectMeterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)

    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
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

    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
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
  fun testTokenize_incorrectYardUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isNotEmpty()
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)

    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
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
  @Iteration("20 G", "input=20 G")
  @Iteration("20 Gram", "input=20 Gram")
  @Iteration("20 Grams", "input=20 Grams")
  fun testTokenize_incorrectGramUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)

    // First invalid token; later tokens can map to other valid units.
    // Here 'm' of "Gram(s)" is a valid meter unit
    assertThat(tokens[1]).isInvalidToken()
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
  fun testTokenize_incorrectGrainUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(20)
    // First invalid token; later tokens can map to other valid units.
    // Here 'in' of "Grain(s)" is a valid inch unit
    assertThat(tokens[1]).isInvalidToken()
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
    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
  }

  @Test
  fun testTokenize_correctSquareMeterUnits_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 m2").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isSquareMeterUnit()
  }

  @Test
  fun testTokenize_incorrectSquareMeterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M2").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
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
  fun testTokenize_incorrectCubicMeterUnits_parsesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("10 M3").toList()

    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(10)
    assertThat(tokens[1]).isInvalidToken()
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
    tokens.drop(1).forEach {
      assertThat(it).isInvalidToken()
    }
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
    assertThat(tokens[2]).isInvalidToken() // u
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
    assertThat(tokens[2]).isInvalidToken() // u
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
    assertThat(tokens[2]).isInvalidToken() // u
    assertThat(tokens[3]).isYardUnit() // yd
  }
}
