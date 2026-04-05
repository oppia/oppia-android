package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.NumberUnitExpression.SiPrefix
import org.oppia.android.app.model.NumberUnitExpression.Unit
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.testing.math.NumberWithUnitsSubject.Companion.assertThat
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumberWithUnitsParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class NumberWithUnitsParserTest {
  @Test
  fun testParser_emptyString_returnsEmptyExpressionError() {
    val error = parseNumberWithUnitsExpectingFailure("")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.EmptyExpressionError::class.java)
  }

  @Test
  fun testParser_whitespaceOnly_returnsEmptyExpressionError() {
    val error = parseNumberWithUnitsExpectingFailure("   ")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.EmptyExpressionError::class.java)
  }

  @Test
  fun testParser_integerOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("42")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.UnitExpectedError::class.java)
  }

  @Test
  fun testParser_realNumberOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("3.14")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.UnitExpectedError::class.java)
  }

  @Test
  fun testParser_negativeIntegerOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("-5")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.UnitExpectedError::class.java)
  }

  @Test
  fun testParser_negativeRealOnly_noUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("-2.5")
    assertThat(error).isInstanceOf(NumberWithUnitsParsingError.UnitExpectedError::class.java)
  }

  @Test
  fun testParser_dollarPrefix_integer_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$100")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(-50.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError::class.java
    )
  }

  @Test
  fun testParser_rupeePrefix_integer_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("₹500")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(500.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError::class.java
    )
  }

  @Test
  fun testParser_dollarSuffix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 dollars")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_centSuffix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 cents")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CENT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeeSuffix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 rupees")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_paiseSuffix_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 paise")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PAISA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithGram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 g")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 s")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithKelvin_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("300 K")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(300.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCelsius_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("25 degC")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(25.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CELSIUS)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithNewton_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 N")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithJoule_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 J")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithWatt_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 W")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithPascal_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("101325 Pa")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(101325.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithAmpere_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 A")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithVolt_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("220 V")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(220.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithOhm_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 ohm")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OHM)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithHertz_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Hz")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HERTZ)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithMole_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 mol")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCandela_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 cd")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CANDELA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithRadian_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 rad")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(3.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithDegree_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("90 deg")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(90.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DEGREE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithLiter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 L")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithInch_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("12 in")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(12.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithFoot_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("6 ft")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(6.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithYard_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 yd")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithGrain_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 gr")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithOunce_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 oz")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(8.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithSquareInch_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("25 sqinch")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(25.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithSquareFoot_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 sqft")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithSquareYard_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 sqyd")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCc_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("250 cc")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(250.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_CENTIMETER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCubicInch_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 cuin")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCubicFoot_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 cuft")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCubicYard_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 cuyd")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(3.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithSquareMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("20 m2")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(20.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithCubicMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 m3")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(8.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CUBIC_METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithMinute_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("30 min")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(30.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MINUTE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_integerWithHour_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 hr")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
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
      hasRealValueThat().isEqualTo(-10.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingDenominatorError::class.java
    )
  }

  @Test
  fun testParser_kiloGram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 kg")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_milliMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 mm")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      // Tokenizer treats each 'm' as MeterUnit; abbreviation "mm" is not milli+meter.
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.METER)
      }
    }
  }

  @Test
  fun testParser_centiMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 cm")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasSiPrefixThat().isEqualTo(SiPrefix.CENTI)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_milliGram_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("500 mg")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(500.0)
      // Tokenizer treats 'm' as MeterUnit and 'g' as GramUnit; abbreviation "mg" is not milli+gram.
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
      }
    }
  }

  @Test
  fun testParser_megaWatt_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 MW")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasSiPrefixThat().isEqualTo(SiPrefix.MEGA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_gigaHertz_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 GHz")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HERTZ)
        hasSiPrefixThat().isEqualTo(SiPrefix.GIGA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_nanoSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 ns")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.NANO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_microMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 um")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasSiPrefixThat().isEqualTo(SiPrefix.MICRO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_picoSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 ps")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.PICO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_femtoMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 fm")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasSiPrefixThat().isEqualTo(SiPrefix.FEMTO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_hectoPascal_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1013 hPa")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1013.0)
      // Tokenizer treats 'h' as HourUnit and 'Pa' as PascalUnit; abbreviated "hPa" is not hecto+Pa.
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
      }
    }
  }

  @Test
  fun testParser_decaMeter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 dam")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasSiPrefixThat().isEqualTo(SiPrefix.DECA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_deciLiter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 dL")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasSiPrefixThat().isEqualTo(SiPrefix.DECI)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_teraWatt_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 TW")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(3.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasSiPrefixThat().isEqualTo(SiPrefix.TERA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_petaJoule_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 PJ")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasSiPrefixThat().isEqualTo(SiPrefix.PETA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_exaJoule_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 EJ")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasSiPrefixThat().isEqualTo(SiPrefix.EXA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_zettaJoule_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 ZJ")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasSiPrefixThat().isEqualTo(SiPrefix.ZETTA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_yottaJoule_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 YJ")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasSiPrefixThat().isEqualTo(SiPrefix.YOTTA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_attoSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("500 as")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(500.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.ATTO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_zeptoSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 zs")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.ZEPTO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_yoctoSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 ys")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasSiPrefixThat().isEqualTo(SiPrefix.YOCTO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_kiloLiter_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 kL")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasSiPrefixThat().isEqualTo(SiPrefix.KILO)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_milliMole_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 mmol")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      // Tokenizer treats first 'm' as MeterUnit then "mol" as MoleUnit.
      hasUnitCountThat().isEqualTo(2)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
      }
      hasSuffixWithIndexThat(index = 1).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
      }
    }
  }

  @Test
  fun testParser_siPrefixAlone_noBaseUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 kilo")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError::class.java
    )
  }

  @Test
  fun testParser_unitWithPositiveExponent_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 m^2")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(8.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingExponentError::class.java
    )
  }

  @Test
  fun testParser_unitWithExponentNegativeButNoValue_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m^-")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingExponentError::class.java
    )
  }

  @Test
  fun testParser_twoUnitsMultiplied_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 kg m")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(9.0)
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
      hasRealValueThat().isEqualTo(1000.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterDivisionError::class.java
    )
  }

  @Test
  fun testParser_parenthesizedDenominator_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 kg m/(s^2)")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnbalancedParenthesesError::class.java
    )
  }

  @Test
  fun testParser_emptyParenthesizedDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("5 m/()")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterDivisionError::class.java
    )
  }

  @Test
  fun testParser_newtonMeterPerSecond_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 N m/s")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(200.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.InvalidTokenError::class.java
    )
  }

  @Test
  fun testParser_unitAlone_noNumber_returnsNumberExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("kg")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.NumberExpectedError::class.java
    )
  }

  @Test
  fun testParser_parenthesisAtStart_returnsNumberExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("(10 m)")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.NumberExpectedError::class.java
    )
  }

  @Test
  fun testParser_trailingNumber_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m 5")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.TrailingTokensError::class.java
    )
  }

  @Test
  fun testParser_dollarPrefixWithTrailingInvalid_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("$100 @")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.TrailingTokensError::class.java
    )
  }

  @Test
  fun testParser_dollarPrefixWithCompoundSuffixUnits_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("$10 kg")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.DuplicateCurrencyError::class.java
    )
  }

  @Test
  fun testParser_rupeePrefixWithRupeeSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("₹100 rupees")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.DuplicateCurrencyError::class.java
    )
  }

  @Test
  fun testParser_dollarPrefixWithCentSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("$50 cents")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.DuplicateCurrencyError::class.java
    )
  }

  @Test
  fun testParser_rupeePrefixWithPaiseSuffix_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("₹50 paise")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.DuplicateCurrencyError::class.java
    )
  }

  @Test
  fun testParser_dollarPrefixWithDollarSuffixAmongPhysicalUnits_returnsDuplicateCurrencyError() {
    val error = parseNumberWithUnitsExpectingFailure("$10 kg dollars")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.DuplicateCurrencyError::class.java
    )
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedError::class.java
    )
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
  fun testParser_meterSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 meter")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_metersPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 meters")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.METER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_gramsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("500 grams")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(500.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAM)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_feetSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("6 feet")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(6.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_footSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("1 foot")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(1.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_inchSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("12 inch")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(12.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_inchesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("12 inches")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(12.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_yardSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 yard")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_yardsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 yards")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_jouleSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 joule")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_joulesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 joules")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.JOULE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_wattSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 watt")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_wattsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 watts")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.WATT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_ampereSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 ampere")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.AMPERE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_voltSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("220 volt")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(220.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_voltsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("220 volts")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(220.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.VOLT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_ohmSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 ohms")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OHM)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_newtonSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 newton")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.NEWTON)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_hertzSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 hertz")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HERTZ)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_kelvinSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("300 kelvin")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(300.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.KELVIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_celsiusSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("25 celsius")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(25.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CELSIUS)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_radianSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 radian")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(3.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_radiansPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("3 radians")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(3.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RADIAN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_degreeSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("90 degree")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(90.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DEGREE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_degreesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("90 degrees")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(90.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DEGREE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_secondSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 second")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_secondsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("60 seconds")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(60.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SECOND)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_minuteSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("30 minute")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(30.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MINUTE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_minutesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("30 minutes")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(30.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MINUTE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_hourSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 hour")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_hoursPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 hours")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.HOUR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_moleSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 mole")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_molesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("2 moles")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(2.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.MOLE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_candelaSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("10 candela")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CANDELA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_ounceSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 ounce")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(8.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_ouncesPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("8 ounces")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(8.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.OUNCE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_grainSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 grain")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_grainsPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 grains")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.GRAIN)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_literSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 liter")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_litersPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 liters")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_litreSpelling_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 litre")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_litresPlural_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("5 litres")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(5.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.LITER)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_pascalSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Pa")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PASCAL)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_sqfeet_parsesAsSquareFoot() {
    val result = parseNumberWithUnitsExpectingSuccess("20 sqfeet")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(20.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_FOOT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_sqinch_parsesAsSquareInch() {
    val result = parseNumberWithUnitsExpectingSuccess("30 sqinch")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(30.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_INCH)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_sqyard_parsesAsSquareYard() {
    val result = parseNumberWithUnitsExpectingSuccess("40 sqyard")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(40.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.SQUARE_YARD)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_centSymbol_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 ¢")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CENT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Dollar")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_dollarsCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Dollars")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_centCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Cent")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CENT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_centsCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Cents")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.CENT)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeeCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Rupee")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_rupeesCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("100 Rupees")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.RUPEE)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_paisaSpelledOut_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 paisa")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PAISA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_paisaCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Paisa")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PAISA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_paiseCapitalized_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("50 Paise")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(50.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.PAISA)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_usd_parsesAsDollar() {
    val result = parseNumberWithUnitsExpectingSuccess("100 USD")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(100.0)
      hasUnitCountThat().isEqualTo(1)
      hasSuffixWithIndexThat(index = 0).apply {
        hasUnitThat().isEqualTo(Unit.DOLLAR)
        hasExponentThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParser_zeroWithUnit_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("0 m")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(0.0)
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
      hasRealValueThat().isEqualTo(0.0)
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
      hasRealValueThat().isEqualTo(-0.0)
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
      hasRealValueThat().isEqualTo(1000000.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(88.0)
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
      hasRealValueThat().isEqualTo(32.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(3.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(20.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(50.0)
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
      hasRealValueThat().isEqualTo(15.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(8.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(1000.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(200.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(3.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(50.0)
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
      hasRealValueThat().isEqualTo(72.0)
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
      hasRealValueThat().isEqualTo(1360.0)
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
      hasRealValueThat().isEqualTo(42000.0)
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
      hasRealValueThat().isEqualTo(18.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(50.0)
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
      hasRealValueThat().isEqualTo(200.0)
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
      hasRealValueThat().isEqualTo(4186.0)
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
      hasRealValueThat().isEqualTo(88.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(100.0)
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
      hasRealValueThat().isEqualTo(1000.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.TrailingTokensError::class.java
    )
  }

  @Test
  fun testParser_divisionWithNoNumeratorUnit_returnsUnitExpectedError() {
    val error = parseNumberWithUnitsExpectingFailure("10 /s")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingDenominatorError::class.java
    )
  }

  @Test
  fun testParser_emptyParenthesesInDenominator_compoundUnit_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m/()")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterDivisionError::class.java
    )
  }

  @Test
  fun testParser_unbalancedParenthesis_complexCompound_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m/(s^2 A")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnbalancedParenthesesError::class.java
    )
  }

  @Test
  fun testParser_missingExponentInCompound_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 kg m^/s")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingExponentError::class.java
    )
  }

  @Test
  fun testParser_missingExponentInDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s^")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingExponentError::class.java
    )
  }

  @Test
  fun testParser_missingExponentNegativeSignInDenominator_returnsError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s^-")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.MissingExponentError::class.java
    )
  }

  @Test
  fun testParser_siPrefixWithoutBaseUnitInCompound_returnsError() {
    // "10 m/kilo" → k recognized as SI prefix but "ilo" is not valid
    val error = parseNumberWithUnitsExpectingFailure("5 m/kilo")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError::class.java
    )
  }

  @Test
  fun testParser_divisionAfterParenthesizedDenominator_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/(s) /kg")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.TrailingTokensError::class.java
    )
  }

  @Test
  fun testParser_trailingNumberAfterCompound_returnsTrailingTokensError() {
    val error = parseNumberWithUnitsExpectingFailure("10 m/s 5")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.TrailingTokensError::class.java
    )
  }

  @Test
  fun testParser_numberInDenominator_returnsTrailingTokensError() {
    // "10 m/3" → '3' is not a unit
    val error = parseNumberWithUnitsExpectingFailure("10 m/3")
    assertThat(error).isInstanceOf(
      NumberWithUnitsParsingError.UnitExpectedAfterDivisionError::class.java
    )
  }

  @Test
  fun testParser_compoundUnitWithExtraWhitespace_parsesCorrectly() {
    val result = parseNumberWithUnitsExpectingSuccess("  10   kg   m^2  /  s^2  ")
    assertThat(result).apply {
      hasRealValueThat().isEqualTo(10.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(5.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(1.0)
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
      hasRealValueThat().isEqualTo(0.0)
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
      hasRealValueThat().isEqualTo(-0.0)
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
      hasRealValueThat().isEqualTo(0.0)
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
