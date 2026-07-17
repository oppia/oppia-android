package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.NumberUnitExpression.SiPrefix
import org.oppia.android.app.model.NumberUnitExpression.Unit
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.NumberWithUnitsSubject.Companion.assertThat
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult
import org.oppia.android.util.math.NumberWithUnitsParsingError.DuplicateCurrencyError
import org.oppia.android.util.math.NumberWithUnitsParsingError.EmptyExpressionError
import org.oppia.android.util.math.NumberWithUnitsParsingError.InvalidTokenError
import org.oppia.android.util.math.NumberWithUnitsParsingError.InvalidUnitError
import org.oppia.android.util.math.NumberWithUnitsParsingError.MissingDenominatorError
import org.oppia.android.util.math.NumberWithUnitsParsingError.MissingExponentError
import org.oppia.android.util.math.NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError
import org.oppia.android.util.math.NumberWithUnitsParsingError.NumberExpectedError
import org.oppia.android.util.math.NumberWithUnitsParsingError.TrailingTokensError
import org.oppia.android.util.math.NumberWithUnitsParsingError.UnbalancedParenthesesError
import org.oppia.android.util.math.NumberWithUnitsParsingError.UnitExpectedAfterDivisionError
import org.oppia.android.util.math.NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError
import org.oppia.android.util.math.NumberWithUnitsParsingError.UnitExpectedError
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumberWithUnitsParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class NumberWithUnitsParserTest {
  @Parameter lateinit var input: String
  @Parameter lateinit var expectedUnit: String
  @Parameter lateinit var expectedSiPrefix: String

  @Test
  fun testParser_emptyString_returnsEmptyExpressionError() {
    val error = parseNumberWithUnitsExpectingFailure("")
    assertThat(error).isInstanceOf(EmptyExpressionError::class.java)
  }

  @Test
  fun testParser_whitespaceOnly_returnsEmptyExpressionError() {
    val error = parseNumberWithUnitsExpectingFailure("   ")
    assertThat(error).isInstanceOf(EmptyExpressionError::class.java)
  }

  @Test
  fun testParser_integerOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("42")
    assertThat(error).isInstanceOf(UnitExpectedError::class.java)
  }

  @Test
  fun testParser_realNumberOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("3.14")
    assertThat(error).isInstanceOf(UnitExpectedError::class.java)
  }

  @Test
  fun testParser_negativeIntegerOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("-5")
    assertThat(error).isInstanceOf(UnitExpectedError::class.java)
  }

  @Test
  fun testParser_negativeRealOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("-2.5")
    assertThat(error).isInstanceOf(UnitExpectedError::class.java)
  }

  @Test
  fun testParser_invalidUnit_returnsInvalidUnitError() {
    val error = parseNumberWithUnitsExpectingFailure("42 invalid")
    assertThat(error).isInstanceOf(InvalidUnitError::class.java)
    val invalidUnitError = error as InvalidUnitError
    assertThat(invalidUnitError.invalidUnit).isEqualTo("invalid")
  }

  @Test
  fun testParser_dollarPrefix_integer_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$100")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefix_realNumber_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$99.99")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(99.99)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefix_negativeNumber_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$-50")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-50.0)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefix_fraction_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$1/2")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefix_withAdditionalSuffixUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$100 kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefix_noNumber_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("$ kg")
    assertThat(error).isInstanceOf(NumberExpectedAfterCurrencyPrefixError::class.java)
  }

  @Test
  fun testParser_dollarPrefix_bareMinus_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("$-")
    assertThat(error).isInstanceOf(NumberExpectedAfterCurrencyPrefixError::class.java)
  }

  @Test
  fun testParser_rupeePrefix_integer_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("₹500")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(500.0)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeePrefix_realNumber_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("₹10.5")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.5)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeePrefix_noNumber_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("₹ kg")
    assertThat(error).isInstanceOf(NumberExpectedAfterCurrencyPrefixError::class.java)
  }

  @Test
  @Iteration("dollarSuffix", "input=100 dollars", "expectedUnit=DOLLAR")
  @Iteration("centSuffix", "input=50 cents", "expectedUnit=CENT")
  @Iteration("rupeeSuffix", "input=100 rupees", "expectedUnit=RUPEE")
  @Iteration("paiseSuffix", "input=50 paise", "expectedUnit=PAISA")
  @Iteration("meterSpelledOut", "input=10 meter", "expectedUnit=METER")
  @Iteration("metersPlural", "input=10 meters", "expectedUnit=METER")
  @Iteration("gramsPlural", "input=500 grams", "expectedUnit=GRAM")
  @Iteration("feetSpelledOut", "input=6 feet", "expectedUnit=FOOT")
  @Iteration("footSpelledOut", "input=1 foot", "expectedUnit=FOOT")
  @Iteration("inchSpelledOut", "input=12 inch", "expectedUnit=INCH")
  @Iteration("inchesPlural", "input=12 inches", "expectedUnit=INCH")
  @Iteration("yardSpelledOut", "input=10 yard", "expectedUnit=YARD")
  @Iteration("yardsPlural", "input=10 yards", "expectedUnit=YARD")
  @Iteration("jouleSpelledOut", "input=100 joule", "expectedUnit=JOULE")
  @Iteration("joulesPlural", "input=100 joules", "expectedUnit=JOULE")
  @Iteration("wattSpelledOut", "input=60 watt", "expectedUnit=WATT")
  @Iteration("wattsPlural", "input=60 watts", "expectedUnit=WATT")
  @Iteration("ampereSpelledOut", "input=5 ampere", "expectedUnit=AMPERE")
  @Iteration("voltSpelledOut", "input=220 volt", "expectedUnit=VOLT")
  @Iteration("voltsPlural", "input=220 volts", "expectedUnit=VOLT")
  @Iteration("ohmSpelledOut", "input=100 ohms", "expectedUnit=OHM")
  @Iteration("newtonSpelledOut", "input=10 newton", "expectedUnit=NEWTON")
  @Iteration("hertzSpelledOut", "input=50 hertz", "expectedUnit=HERTZ")
  @Iteration("kelvinSpelledOut", "input=300 kelvin", "expectedUnit=KELVIN")
  @Iteration("celsiusSpelledOut", "input=25 celsius", "expectedUnit=CELSIUS")
  @Iteration("radianSpelledOut", "input=3 radian", "expectedUnit=RADIAN")
  @Iteration("radiansPlural", "input=3 radians", "expectedUnit=RADIAN")
  @Iteration("degreeSpelledOut", "input=90 degree", "expectedUnit=DEGREE")
  @Iteration("degreesPlural", "input=90 degrees", "expectedUnit=DEGREE")
  @Iteration("secondSpelledOut", "input=60 second", "expectedUnit=SECOND")
  @Iteration("secondsPlural", "input=60 seconds", "expectedUnit=SECOND")
  @Iteration("minuteSpelledOut", "input=30 minute", "expectedUnit=MINUTE")
  @Iteration("minutesPlural", "input=30 minutes", "expectedUnit=MINUTE")
  @Iteration("hourSpelledOut", "input=2 hour", "expectedUnit=HOUR")
  @Iteration("hoursPlural", "input=2 hours", "expectedUnit=HOUR")
  @Iteration("moleSpelledOut", "input=2 mole", "expectedUnit=MOLE")
  @Iteration("molesPlural", "input=2 moles", "expectedUnit=MOLE")
  @Iteration("candelaSpelledOut", "input=10 candela", "expectedUnit=CANDELA")
  @Iteration("ounceSpelledOut", "input=8 ounce", "expectedUnit=OUNCE")
  @Iteration("ouncesPlural", "input=8 ounces", "expectedUnit=OUNCE")
  @Iteration("grainSpelledOut", "input=100 grain", "expectedUnit=GRAIN")
  @Iteration("grainsPlural", "input=100 grains", "expectedUnit=GRAIN")
  @Iteration("literSpelledOut", "input=5 liter", "expectedUnit=LITER")
  @Iteration("litersPlural", "input=5 liters", "expectedUnit=LITER")
  @Iteration("litreSpelling", "input=5 litre", "expectedUnit=LITER")
  @Iteration("litresPlural", "input=5 litres", "expectedUnit=LITER")
  @Iteration("pascalSpelledOut", "input=100 Pa", "expectedUnit=PASCAL")
  @Iteration("sqfeet", "input=20 sqfeet", "expectedUnit=SQUARE_FOOT")
  @Iteration("sqinch", "input=30 sqinch", "expectedUnit=SQUARE_INCH")
  @Iteration("sqyard", "input=40 sqyard", "expectedUnit=SQUARE_YARD")
  fun testParser_spelledOutOrPluralUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess(input)
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.valueOf(expectedUnit))
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_newtonMeterSuffix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Nm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(50.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_jouleSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 Js")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 Ws")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_newtonMilliampere_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("7 NmA")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(7.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasSiPrefixThat().isEqualTo(SiPrefix.MILLI)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withSiPrefix_kilogramMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 kgm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_numberTouchingUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("12Nm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(12.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withExponent_appliesExponentToLastUnit() {
    val result = parseNumberWithUnitsExpectingSuccess("5 Nm^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_inDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m/Ns")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withDivision_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 Nm/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_spacedEquivalent_matchesCompound() {
    // "N m" (space-separated) should produce the same result as "Nm" (compound).
    val spacedResult = parseNumberWithUnitsExpectingSuccess("50 N m")
    val compoundResult = parseNumberWithUnitsExpectingSuccess("50 Nm")
    assertThat(spacedResult).isEqualTo(compoundResult)
  }

  @Test
  fun testParser_compoundUnit_completelyInvalid_returnsInvalidUnitError() {
    val error = parseNumberWithUnitsExpectingFailure("5 xyz")
    assertThat(error).isInstanceOf(InvalidUnitError::class.java)
    val invalidUnitError = error as InvalidUnitError
    assertThat(invalidUnitError.invalidUnit).isEqualTo("xyz")
  }

  @Test
  fun testParser_compoundUnit_partiallyInvalid_returnsError() {
    // "Nxyz" — "N" is valid (Newton), but "xyz" cannot be resolved.
    // Decomposition requires size > 1, so single valid prefix with invalid rest fails.
    val error = parseNumberWithUnitsExpectingFailure("5 Nxyz")
    assertThat(error).isInstanceOf(InvalidUnitError::class.java)
  }

  @Test
  fun testParser_compoundUnit_singleCharUnit_noDecomposition_parsesCorrectly() {
    // Single-char tokens like "N" should still parse normally (no decomposition needed).
    val result = parseNumberWithUnitsExpectingSuccess("10 N")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_mixedSpacedAndCompound_parsesCorrectly() {
    // "Nm kg" — compound "Nm" followed by space-separated "kg".
    val result = parseNumberWithUnitsExpectingSuccess("3 Nm kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_pascalAbbrevWithSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Pas")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withCurrencyPrefix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$100 Nm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(3)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_voltAmpere_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("220 VA")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(220.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_negativeNumber_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-5 Nm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_realNumber_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3.14 Nm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.14)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_fractionValue_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1/2 Nm")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_jouleKelvin_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 JK")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(8.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("12 Wm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(12.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_voltSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("6 Vs")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(6.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_pascalMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("20 Pam")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(20.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_ampereSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("15 As")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(15.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_hertzSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("44 Hzs")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(44.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HERTZ)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_kelvinMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("300 Km")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(300.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_threeUnits_jouleKelvinSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 JKs")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(2.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattAmpere_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("9 WA")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(9.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_voltAmpereNoSpace_parsesCorrectly() {
    // Number touching compound unit without any space.
    val result = parseNumberWithUnitsExpectingSuccess("110VA")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(110.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattKilogram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Wkg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(50.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_jouleMillisecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("4 Jms")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(4.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.MILLI)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_pascalSecond_withDivision_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 Pas/m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_voltMeter_inDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 A/Vm")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattSecond_spacedEquivalent_matchesCompound() {
    val spacedResult = parseNumberWithUnitsExpectingSuccess("5 W s")
    val compoundResult = parseNumberWithUnitsExpectingSuccess("5 Ws")
    assertThat(spacedResult).isEqualTo(compoundResult)
  }

  @Test
  fun testParser_compoundUnit_voltAmpere_spacedEquivalent_matchesCompound() {
    val spacedResult = parseNumberWithUnitsExpectingSuccess("220 V A")
    val compoundResult = parseNumberWithUnitsExpectingSuccess("220 VA")
    assertThat(spacedResult).isEqualTo(compoundResult)
  }

  @Test
  fun testParser_compoundUnit_jouleSecond_withExponent_appliesExponentToLastUnit() {
    val result = parseNumberWithUnitsExpectingSuccess("5 Js^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withNegativeExponent_appliesExponentToLastUnit() {
    val result = parseNumberWithUnitsExpectingSuccess("5 Nm^-2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withExponent_spacedEquivalent_matchesCompound() {
    val spacedResult = parseNumberWithUnitsExpectingSuccess("5 N m^2")
    val compoundResult = parseNumberWithUnitsExpectingSuccess("5 Nm^2")
    assertThat(spacedResult).isEqualTo(compoundResult)
  }

  @Test
  fun testParser_compoundUnit_withExponentAndDivision_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 Nm^2/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_withSiPrefix_andExponent_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 kgm^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_compoundUnit_wattPartiallyInvalid_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 Wxyz")
    assertThat(error).isInstanceOf(InvalidUnitError::class.java)
  }

  @Test
  @Iteration("integerWithMeter", "input=10 m", "expectedUnit=METER")
  @Iteration("integerWithGram", "input=5 g", "expectedUnit=GRAM")
  @Iteration("integerWithSecond", "input=60 s", "expectedUnit=SECOND")
  @Iteration("integerWithKelvin", "input=300 K", "expectedUnit=KELVIN")
  @Iteration("integerWithCelsius", "input=25 degC", "expectedUnit=CELSIUS")
  @Iteration("integerWithNewton", "input=10 N", "expectedUnit=NEWTON")
  @Iteration("integerWithJoule", "input=100 J", "expectedUnit=JOULE")
  @Iteration("integerWithWatt", "input=60 W", "expectedUnit=WATT")
  @Iteration("integerWithPascal", "input=101325 Pa", "expectedUnit=PASCAL")
  @Iteration("integerWithAmpere", "input=5 A", "expectedUnit=AMPERE")
  @Iteration("integerWithVolt", "input=220 V", "expectedUnit=VOLT")
  @Iteration("integerWithOhm", "input=100 ohm", "expectedUnit=OHM")
  @Iteration("integerWithHertz", "input=50 Hz", "expectedUnit=HERTZ")
  @Iteration("integerWithMole", "input=2 mol", "expectedUnit=MOLE")
  @Iteration("integerWithCandela", "input=10 cd", "expectedUnit=CANDELA")
  @Iteration("integerWithRadian", "input=3 rad", "expectedUnit=RADIAN")
  @Iteration("integerWithDegree", "input=90 deg", "expectedUnit=DEGREE")
  @Iteration("integerWithLiter", "input=5 L", "expectedUnit=LITER")
  @Iteration("integerWithInch", "input=12 in", "expectedUnit=INCH")
  @Iteration("integerWithFoot", "input=6 ft", "expectedUnit=FOOT")
  @Iteration("integerWithYard", "input=10 yd", "expectedUnit=YARD")
  @Iteration("integerWithGrain", "input=100 gr", "expectedUnit=GRAIN")
  @Iteration("integerWithOunce", "input=8 oz", "expectedUnit=OUNCE")
  @Iteration("integerWithSquareInch", "input=25 sqinch", "expectedUnit=SQUARE_INCH")
  @Iteration("integerWithSquareFoot", "input=100 sqft", "expectedUnit=SQUARE_FOOT")
  @Iteration("integerWithSquareYard", "input=50 sqyd", "expectedUnit=SQUARE_YARD")
  @Iteration("integerWithCc", "input=250 cc", "expectedUnit=CUBIC_CENTIMETER")
  @Iteration("integerWithCubicInch", "input=10 cuin", "expectedUnit=CUBIC_INCH")
  @Iteration("integerWithCubicFoot", "input=5 cuft", "expectedUnit=CUBIC_FOOT")
  @Iteration("integerWithCubicYard", "input=3 cuyd", "expectedUnit=CUBIC_YARD")
  @Iteration("integerWithSquareMeter", "input=20 m2", "expectedUnit=SQUARE_METER")
  @Iteration("integerWithCubicMeter", "input=8 m3", "expectedUnit=CUBIC_METER")
  @Iteration("integerWithMinute", "input=30 min", "expectedUnit=MINUTE")
  @Iteration("integerWithHour", "input=2 hr", "expectedUnit=HOUR")
  fun testParser_abbreviatedUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess(input)
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.valueOf(expectedUnit))
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_realNumberWithMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3.14 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.14)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_realNumberWithKelvin_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("273.15 K")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(273.15)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_negativeIntegerWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-10 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_negativeRealWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-3.5 kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-3.5)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_fractionWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1/2 m")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_negativeFractionWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-3/4 kg")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isTrue()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(3)
        hasDenominatorThat().isEqualTo(4)
      }
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_fractionMissingDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("1/ m")
    assertThat(error).isInstanceOf(MissingDenominatorError::class.java)
  }

  @Test
  @Iteration("kiloGram", "input=5 kg", "expectedUnit=GRAM", "expectedSiPrefix=KILO")
  @Iteration("milliMeter", "input=100 mm", "expectedUnit=METER", "expectedSiPrefix=MILLI")
  @Iteration("centiMeter", "input=50 cm", "expectedUnit=METER", "expectedSiPrefix=CENTI")
  @Iteration("milliGram", "input=500 mg", "expectedUnit=GRAM", "expectedSiPrefix=MILLI")
  @Iteration("megaWatt", "input=5 MW", "expectedUnit=WATT", "expectedSiPrefix=MEGA")
  @Iteration("gigaHertz", "input=2 GHz", "expectedUnit=HERTZ", "expectedSiPrefix=GIGA")
  @Iteration("nanoSecond", "input=100 ns", "expectedUnit=SECOND", "expectedSiPrefix=NANO")
  @Iteration("microMeter", "input=10 um", "expectedUnit=METER", "expectedSiPrefix=MICRO")
  @Iteration("picoSecond", "input=50 ps", "expectedUnit=SECOND", "expectedSiPrefix=PICO")
  @Iteration("femtoMeter", "input=1 fm", "expectedUnit=METER", "expectedSiPrefix=FEMTO")
  @Iteration("hectoPascal", "input=1013 hPa", "expectedUnit=PASCAL", "expectedSiPrefix=HECTO")
  @Iteration("decaMeter", "input=10 dam", "expectedUnit=METER", "expectedSiPrefix=DECA")
  @Iteration("deciLiter", "input=5 dL", "expectedUnit=LITER", "expectedSiPrefix=DECI")
  @Iteration("teraWatt", "input=3 TW", "expectedUnit=WATT", "expectedSiPrefix=TERA")
  @Iteration("petaJoule", "input=1 PJ", "expectedUnit=JOULE", "expectedSiPrefix=PETA")
  @Iteration("exaJoule", "input=2 EJ", "expectedUnit=JOULE", "expectedSiPrefix=EXA")
  @Iteration("zettaJoule", "input=1 ZJ", "expectedUnit=JOULE", "expectedSiPrefix=ZETTA")
  @Iteration("yottaJoule", "input=1 YJ", "expectedUnit=JOULE", "expectedSiPrefix=YOTTA")
  @Iteration("attoSecond", "input=500 as", "expectedUnit=SECOND", "expectedSiPrefix=ATTO")
  @Iteration("zeptoSecond", "input=1 zs", "expectedUnit=SECOND", "expectedSiPrefix=ZEPTO")
  @Iteration("yoctoSecond", "input=1 ys", "expectedUnit=SECOND", "expectedSiPrefix=YOCTO")
  @Iteration("kiloLiter", "input=2 kL", "expectedUnit=LITER", "expectedSiPrefix=KILO")
  @Iteration("milliMole", "input=10 mmol", "expectedUnit=MOLE", "expectedSiPrefix=MILLI")
  fun testParser_siPrefixedUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess(input)
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.valueOf(expectedUnit))
        hasSiPrefixThat().isEqualTo(SiPrefix.valueOf(expectedSiPrefix))
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_siPrefixAlone_noBaseUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 kilo")
    assertThat(error).isInstanceOf(UnitExpectedAfterSiPrefixError::class.java)
  }

  @Test
  fun testParser_unitWithPositiveExponent_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_unitWithNegativeExponent_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 s^-1")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_unitWithExponent3_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 m^3")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(8.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(3)
      }
    }
  }

  @Test
  fun testParser_unitWithMissingExponent_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m^")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_unitWithExponentNegativeButNoValue_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m^-")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_unitWithMissingExponentFollowedByCompoundUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg^Nm")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_unitWithMissingExponentFollowedByUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg^N")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_twoUnitsMultiplied_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 kg m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_threeUnitsMultiplied_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_meterPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_meterPerSecondSquared_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("9 m/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(9.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_kgPerCubicMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1000 kg/m^3")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1000.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-3)
      }
    }
  }

  @Test
  fun testParser_divisionWithNoUnitAfter_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/")
    assertThat(error).isInstanceOf(UnitExpectedAfterDivisionError::class.java)
  }

  @Test
  fun testParser_parenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 kg m/(s^2)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_parenthesizedDenominatorMultipleUnits_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 J/(kg K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_unbalancedParenthesis_missingClose_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 m/(s")
    assertThat(error).isInstanceOf(UnbalancedParenthesesError::class.java)
  }

  @Test
  fun testParser_emptyParenthesizedDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 m/()")
    assertThat(error).isInstanceOf(UnitExpectedAfterDivisionError::class.java)
  }

  @Test
  fun testParser_newtonMeterPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 N m/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_kgMeterPerSecondSquared_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_wattPerSquareMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 W/m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_extraWhitespace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("  10   m  ")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarWithSpace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$ 100")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeeWithSpace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("₹ 200")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(200.0)
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_invalidTokenAtStart_returnsInvalidTokenError() {
    val error = parseNumberWithUnitsExpectingFailure("@100 m")
    assertThat(error).isInstanceOf(InvalidTokenError::class.java)
  }

  @Test
  fun testParser_unitAlone_noNumber_returnsNumberExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("kg")
    assertThat(error).isInstanceOf(NumberExpectedError::class.java)
  }

  @Test
  fun testParser_bareMinus_returnsNumberExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("-")
    assertThat(error).isInstanceOf(NumberExpectedError::class.java)
  }

  @Test
  fun testParser_parenthesisAtStart_returnsNumberExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("(10 m)")
    assertThat(error).isInstanceOf(NumberExpectedError::class.java)
  }

  @Test
  fun testParser_trailingNumber_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m 5")
    assertThat(error).isInstanceOf(TrailingTokensError::class.java)
  }

  @Test
  fun testParser_dollarPrefixWithTrailingInvalid_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("$100 @")
    assertThat(error).isInstanceOf(TrailingTokensError::class.java)
  }

  @Test
  fun testParser_dollarPrefixWithCompoundSuffixUnits_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$10 kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefixWithDivision_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$10 kg/m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefixWithDollarSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("$100 dollars")
    assertThat(error).isInstanceOf(DuplicateCurrencyError::class.java)
  }

  @Test
  fun testParser_rupeePrefixWithRupeeSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("₹100 rupees")
    assertThat(error).isInstanceOf(DuplicateCurrencyError::class.java)
  }

  @Test
  fun testParser_dollarPrefixWithCentSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("$50 cents")
    assertThat(error).isInstanceOf(DuplicateCurrencyError::class.java)
  }

  @Test
  fun testParser_rupeePrefixWithPaiseSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("₹50 paise")
    assertThat(error).isInstanceOf(DuplicateCurrencyError::class.java)
  }

  @Test
  fun testParser_dollarPrefixWithDollarSuffixAmongPhysicalUnits_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("$10 kg dollars")
    assertThat(error).isInstanceOf(DuplicateCurrencyError::class.java)
  }

  @Test
  fun testParser_fractionWithLargeNumbers_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("99/100 m")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasNumeratorThat().isEqualTo(99)
        hasDenominatorThat().isEqualTo(100)
      }
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_fractionOnlyNoUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("1/2")
    assertThat(error).isInstanceOf(UnitExpectedError::class.java)
  }

  @Test
  fun testParser_dollarPrefix_negativeFraction_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$-1/4")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isTrue()
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(4)
      }
      hasUnitCountThat().isEqualTo(1)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  @Iteration("centSymbol", "input=50 ¢", "expectedUnit=CENT")
  @Iteration("dollarCapitalized", "input=100 Dollar", "expectedUnit=DOLLAR")
  @Iteration("dollarsCapitalized", "input=100 Dollars", "expectedUnit=DOLLAR")
  @Iteration("centCapitalized", "input=50 Cent", "expectedUnit=CENT")
  @Iteration("centsCapitalized", "input=50 Cents", "expectedUnit=CENT")
  @Iteration("rupeeCapitalized", "input=100 Rupee", "expectedUnit=RUPEE")
  @Iteration("rupeesCapitalized", "input=100 Rupees", "expectedUnit=RUPEE")
  @Iteration("paisaSpelledOut", "input=50 paisa", "expectedUnit=PAISA")
  @Iteration("paisaCapitalized", "input=50 Paisa", "expectedUnit=PAISA")
  @Iteration("paiseCapitalized", "input=50 Paise", "expectedUnit=PAISA")
  @Iteration("usd", "input=100 USD", "expectedUnit=DOLLAR")
  fun testParser_caseInsensitiveCurrencyAlias_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess(input)
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.valueOf(expectedUnit))
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_zeroWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(0.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_zeroPointZeroWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0.0 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(0.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_negativeZeroWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-0 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-0.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_largeNumberWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1000000 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1000000.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_smallDecimalWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0.001 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-6).of(0.001)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_meterPerSecondWithExplicitExponent1_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m^1/s^1")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_numeratorWithMultipleUnitsAndDivision_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/s^3")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
    }
  }

  @Test
  fun testParser_parenthesizedDenominatorWithExponents_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/(s^3 A)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_kiloNewtonMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 kN m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_kiloWattPerSquareMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 kW/m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_megaJoulePerKilogram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 MJ/kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasSiPrefixThat().isEqualTo(SiPrefix.MEGA)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_gigaWattHour_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 GW hr")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasSiPrefixThat().isEqualTo(SiPrefix.GIGA)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_siPrefixedUnitWithExponentInCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 kN^2 m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_siPrefixedUnitInDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 N/kN")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_siPrefixedUnitInParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 W/(cm^2 kN)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasSiPrefixThat().isEqualTo(SiPrefix.CENTI)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_feetPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("88 ft/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(88.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_feetPerSecondSquared_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("32 ft/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(32.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_inchPerMinute_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 in/min")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.INCH)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.MINUTE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_yardPerHour_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 yd/hr")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.YARD)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_ouncePerCubicInch_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 oz/cuin")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_INCH)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_grainPerCc_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 gr/cc")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAIN)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_CENTIMETER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_ouncePerSquareFoot_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 oz/sqft")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_FOOT)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_squareMeterPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m2/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_cubicMeterPerKilogram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 m3/kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_literPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("20 L/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(20.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_ccPerMinute_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 cc/min")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_CENTIMETER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.MINUTE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_squareFootPerHour_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 sqft/hr")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(50.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_FOOT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_cubicFootPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("15 cuft/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(15.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_FOOT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_fourUnitsCompound_voltageInBaseUnits_parsesCorrectly() {
    // 1 V = 1 kg⋅m²/s³⋅A⁻¹
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/(s^3 A)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_fourUnitsCompound_resistanceInBaseUnits_parsesCorrectly() {
    // 1 Ω = 1 kg⋅m²/s³⋅A⁻²
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/(s^3 A^2)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_fiveUnitsCompound_specificHeatCapacity_parsesCorrectly() {
    // Specific heat capacity: J/(kg·K) = m²⋅s⁻²⋅K⁻¹ → expressed as kg m^2/(s^2 mol K)
    val result = parseNumberWithUnitsExpectingSuccess("8 kg m^2/(s^2 mol K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(8.0)
      hasUnitCountThat().isEqualTo(5)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 4).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_fiveUnitsNumeratorOnly_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m s A V")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(5)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 4).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_sixUnitsCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2 cd/(s^3 A mol)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(6)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.CANDELA)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 4).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 5).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_allUnitsWithExplicitExponents_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg^1 m^2 s^-3 A^-1")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_multipleUnitsInParenthesizedDenominator_allWithExponents_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg/(m^2 s^3)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
    }
  }

  @Test
  fun testParser_threeUnitsInParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 N/(m^2 s K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_singleUnitInParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m/(s)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_realNumberWithFourUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3.14 kg m^2/(s^2 K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.14)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_negativeRealWithAcceleration_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-9.81 m/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-9.81)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_negativeRealWithThreeUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-2.5 kg m/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-2.5)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_smallDecimalWithCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0.001 kg m/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-6).of(0.001)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_largeRealWithCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("999999.99 m^3/(kg s^2)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-2).of(999999.99)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(3)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_fractionWithThreeUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1/2 kg m/s^2")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_fractionWithParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1/2 kg/(m^3)")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-3)
      }
    }
  }

  @Test
  fun testParser_negativeFractionWithFourUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-1/4 N m/(s^2 kg)")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isTrue()
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(4)
      }
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_fractionWithFiveUnits_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3/7 kg m^2/(s^2 mol K)")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNegativePropertyThat().isFalse()
        hasNumeratorThat().isEqualTo(3)
        hasDenominatorThat().isEqualTo(7)
      }
      hasUnitCountThat().isEqualTo(5)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 4).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefixWithThreeUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$10 kg m/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(4)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_rupeePrefixWithFourUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("₹100 kg m^2/(s^2 K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(5)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_dollarPrefixFractionWithThreeUnitCompound_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$1/4 kg m/s^2")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(4)
      }
      hasUnitCountThat().isEqualTo(4)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_unitWithExponent5_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 m^5/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(5)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_unitWithNegativeExponent4InDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 W/m^4")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-4)
      }
    }
  }

  @Test
  fun testParser_highExponentInParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg/(m^4 s^5)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-4)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-5)
      }
    }
  }

  @Test
  fun testParser_exponentZeroInCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m^0 s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(0)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_exponentZeroInDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m/s^0")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testParser_negativeExponentInParenthesizedDenominator_multipleUnits_parsesCorrectly() {
    // s^-2 and A^-1 in denominator → negated → s^2 and A^1
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/(s^-2 A^-1)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_mixedPositiveAndNegativeExponentsInDenominator_parsesCorrectly() {
    // In denominator: s^2 (→ -2), A^-1 (→ 1)
    val result = parseNumberWithUnitsExpectingSuccess("1 kg/(s^2 A^-1)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_compoundUnitOrdering_numeratorBeforeDenominator() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2/(s^3 A)")
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_sixUnitOrdering_preservesInputOrder() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m^2 cd/(s^3 A mol)")
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(6)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.CANDELA)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
      }
      hasSuffixWithIndexThat(index = 4).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
      }
      hasSuffixWithIndexThat(index = 5).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
      }
    }
  }

  @Test
  fun testParser_dollarPrefixCompoundOrdering_dollarFirstThenNumeratorThenDenominator() {
    val result = parseNumberWithUnitsExpectingSuccess("$10 kg m/(s^2 K)")
    assertThat(result).apply {
      hasUnitCountThat().isEqualTo(5)
      hasPrefixThat().apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_stefanBoltzmannConstant_parsesCorrectly() {
    // σ = 5.67 W/(m^2 K^4)
    val result = parseNumberWithUnitsExpectingSuccess("5 W/(m^2 K^4)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-4)
      }
    }
  }

  @Test
  fun testParser_electricFieldStrength_parsesCorrectly() {
    // E = V/m
    val result = parseNumberWithUnitsExpectingSuccess("1000 V/m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1000.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_magneticFieldStrength_parsesCorrectly() {
    // Tesla = kg/(A⋅s²)
    val result = parseNumberWithUnitsExpectingSuccess("1 kg/(A s^2)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_thermalConductivity_parsesCorrectly() {
    // W/(m⋅K)
    val result = parseNumberWithUnitsExpectingSuccess("200 W/(m K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(200.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_viscosity_parsesCorrectly() {
    // Pa⋅s = kg/(m⋅s)
    val result = parseNumberWithUnitsExpectingSuccess("1 Pa s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_kinematicViscosity_parsesCorrectly() {
    // m²/s
    val result = parseNumberWithUnitsExpectingSuccess("1 m^2/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_momentOfInertia_parsesCorrectly() {
    // kg⋅m²
    val result = parseNumberWithUnitsExpectingSuccess("10 kg m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_angularVelocity_parsesCorrectly() {
    // rad/s
    val result = parseNumberWithUnitsExpectingSuccess("3 rad/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(3.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_angularAcceleration_parsesCorrectly() {
    // rad/s²
    val result = parseNumberWithUnitsExpectingSuccess("5 rad/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_torque_parsesCorrectly() {
    // N⋅m
    val result = parseNumberWithUnitsExpectingSuccess("50 N m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(50.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_surfaceTension_parsesCorrectly() {
    // N/m
    val result = parseNumberWithUnitsExpectingSuccess("72 N/m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(72.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_heatFlux_parsesCorrectly() {
    // W/m²
    val result = parseNumberWithUnitsExpectingSuccess("1360 W/m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1360.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_specificEnergy_parsesCorrectly() {
    // J/kg = m²/s²
    val result = parseNumberWithUnitsExpectingSuccess("42000 J/kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(42000.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_molarMass_parsesCorrectly() {
    // kg/mol
    val result = parseNumberWithUnitsExpectingSuccess("18 kg/mol")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(18.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_luminousFlux_parsesCorrectly() {
    // cd⋅rad²
    val result = parseNumberWithUnitsExpectingSuccess("100 cd rad^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CANDELA)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testParser_divisionScopesMultipleUnitsWithExponents_parsesCorrectly() {
    // 10 N m / s^2 kg → N and m in numerator, s^2 and kg in denominator
    val result = parseNumberWithUnitsExpectingSuccess("10 N m/s^2 kg")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_threeNumeratorUnitsWithDivision_parsesCorrectly() {
    // 1 kg m s / A → kg, m, s in numerator; A in denominator
    val result = parseNumberWithUnitsExpectingSuccess("1 kg m s/A")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_singleNumeratorDividedByThreeUnits_parsesCorrectly() {
    // 1 J / kg m K → J in numerator; kg, m, K in denominator
    val result = parseNumberWithUnitsExpectingSuccess("1 J/kg m K")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutMeterPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 meter/second")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutKilogramMeterPerSecondSquared_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kilogram meter/second^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_spelledOutNewtonMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 newton meter")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(50.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_spelledOutWattPerSquareMeterKelvin_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("200 watt/(meter^2 kelvin)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(200.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutJoulePerKilogramKelvin_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("4186 joule/(kilogram kelvin)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(4186.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutFeetPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("88 feet/second")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(88.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutOuncePerCubicInch_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 ounce/cuin")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_INCH)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_spelledOutVoltPerMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 volt/meter")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(100.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_kilogramPerCubicMeterSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1000 kilogram/meter^3")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1000.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-3)
      }
    }
  }

  @Test
  fun testParser_megawattPerSquareMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 megawatt/meter^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasSiPrefixThat().isEqualTo(SiPrefix.MEGA)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_doubleDivision_returnsTrailingTokensError() {
    // "10 m/s/kg" → after parsing m/s, the second '/' is a trailing token
    val error = parseNumberWithUnitsExpectingFailure("10 m/s/kg")
    assertThat(error).isInstanceOf(TrailingTokensError::class.java)
  }

  @Test
  fun testParser_divisionWithNoNumeratorUnit_returnsMissingDenominatorError() {
    val error = parseNumberWithUnitsExpectingFailure("10 /s")
    assertThat(error).isInstanceOf(MissingDenominatorError::class.java)
  }

  @Test
  fun testParser_emptyParenthesesInDenominator_compoundUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m/()")
    assertThat(error).isInstanceOf(UnitExpectedAfterDivisionError::class.java)
  }

  @Test
  fun testParser_unbalancedParenthesis_complexCompound_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m/(s^2 A")
    assertThat(error).isInstanceOf(UnbalancedParenthesesError::class.java)
  }

  @Test
  fun testParser_missingExponentInCompound_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m^/s")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_missingExponentInDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s^")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_missingExponentNegativeSignInDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s^-")
    assertThat(error).isInstanceOf(MissingExponentError::class.java)
  }

  @Test
  fun testParser_siPrefixWithoutBaseUnitInCompound_returnsError() {
    // "10 m/kilo" → k recognized as SI prefix but "ilo" is not valid
    val error = parseNumberWithUnitsExpectingFailure("5 m/kilo")
    assertThat(error).isInstanceOf(UnitExpectedAfterSiPrefixError::class.java)
  }

  @Test
  fun testParser_divisionAfterParenthesizedDenominator_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/(s) /kg")
    assertThat(error).isInstanceOf(TrailingTokensError::class.java)
  }

  @Test
  fun testParser_trailingNumberAfterCompound_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s 5")
    assertThat(error).isInstanceOf(TrailingTokensError::class.java)
  }

  @Test
  fun testParser_numberInDenominator_returnsUnitExpectedAfterDivisionError() {
    // "10 m/3" → '3' is not a unit
    val error = parseNumberWithUnitsExpectingFailure("10 m/3")
    assertThat(error).isInstanceOf(UnitExpectedAfterDivisionError::class.java)
  }

  @Test
  fun testParser_compoundUnitWithExtraWhitespace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("  10   kg   m^2  /  s^2  ")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(10.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_parenthesizedDenominatorWithExtraWhitespace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1  kg  m^2 / ( s^3   A )")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(4)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-3)
      }
      hasSuffixWithIndexThat(index = 3).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_tabsInCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5\tkg\tm/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(5.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_sameUnitInNumeratorAndDenominator_parsesCorrectly() {
    // m in numerator and m^2 in denominator → results in m^1 and m^-2 separately
    val result = parseNumberWithUnitsExpectingSuccess("1 m/m^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      // Parser does not simplify, both m occurrences appear
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_sameUnitRepeatedInNumerator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 m m")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_sameUnitRepeatedInParenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 kg/(s s)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(1.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_zeroWithCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0 kg m/s^2")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(0.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-2)
      }
    }
  }

  @Test
  fun testParser_negativeZeroWithCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("-0 m/s")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(-0.0)
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_zeroPointZeroWithCompoundUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0.0 W/(m^2 K)")
    assertThat(result).apply {
      hasRealValueThat().isWithin(1e-5).of(0.0)
      hasUnitCountThat().isEqualTo(3)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(-2)
      }
      hasSuffixWithIndexThat(index = 2).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(-1)
      }
    }
  }

  @Test
  fun testParser_fractionWithZeroDenominator_parsesAsFraction() {
    // Parser doesn't validate zero denominator - that's a semantic concern.
    val result = parseNumberWithUnitsExpectingSuccess("1/0 m")
    assertThat(result).apply {
      hasFractionValueThat().apply {
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(0)
      }
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  private fun parseNumberWithUnitsExpectingSuccess(
    expression: String
  ): NumberWithUnitsExpression {
    val parsingResult = parseNumberWithUnits(expression)
    return expectSuccessfulParsingResult(parsingResult)
  }

  private fun parseNumberWithUnitsExpectingFailure(
    expression: String
  ): NumberWithUnitsParsingError {
    val parsingResult = parseNumberWithUnits(expression)
    return expectFailingParsingResult(parsingResult)
  }

  private fun parseNumberWithUnits(
    expression: String,
  ): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
    return NumberWithUnitsParser.parseNumberWithUnits(expression)
  }

  private fun expectSuccessfulParsingResult(
    result: NumberWithUnitsParsingResult<NumberWithUnitsExpression>
  ): NumberWithUnitsExpression {
    assertThat(result).isInstanceOf(
      NumberWithUnitsParsingResult.Success::class.java
    )
    return (result as NumberWithUnitsParsingResult.Success<NumberWithUnitsExpression>).result
  }

  private fun <T> expectFailingParsingResult(
    result: NumberWithUnitsParsingResult<T>
  ): NumberWithUnitsParsingError {
    assertThat(result).isInstanceOf(
      NumberWithUnitsParsingResult.Failure::class.java
    )
    return (result as NumberWithUnitsParsingResult.Failure).error
  }
}
