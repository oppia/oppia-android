package org.oppia.android.testing.math

import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.oppia.android.util.math.NumberWithUnitsTokenizer
import org.oppia.android.util.math.NumberWithUnitsTokenizer.Companion.Token

// TODO(#6151): Add tests for this class.

/**
 * A Truth [Subject] for testing [Token] objects from [NumberWithUnitsTokenizer].
 */
class NumberWithUnitsTokenSubject(
  metadata: FailureMetadata,
  private val actual: Token
) : Subject(metadata, actual) {

  /**
   * Verifies that the token is a positive integer and returns an [IntegerSubject] for the value.
   *
   * @return an [IntegerSubject] to verify the integer value
   * @throws AssertionError if the token is not a [Token.PositiveInteger]
   */
  fun isPositiveIntegerWhoseValue(): IntegerSubject {
    return Truth.assertThat(actual.asVerifiedType<Token.PositiveInteger>().parsedValue)
  }

  /**
   * Verifies that the token is a positive real number and returns a [DoubleSubject] for the value.
   *
   * @return a [DoubleSubject] to verify the real number value
   * @throws AssertionError if the token is not a [Token.PositiveRealNumber]
   */
  fun isPositiveRealNumberWhoseValue(): DoubleSubject {
    return Truth.assertThat(actual.asVerifiedType<Token.PositiveRealNumber>().parsedValue)
  }

  /**
   * Verifies that the token is a minus symbol.
   *
   * @throws AssertionError if the token is not a [Token.MinusSymbol]
   */
  fun isMinusSymbol() {
    actual.asVerifiedType<Token.MinusSymbol>()
  }

  /**
   * Verifies that the token is a divide symbol.
   *
   * @throws AssertionError if the token is not a [Token.DivideSymbol]
   */
  fun isDivideSymbol() {
    actual.asVerifiedType<Token.DivideSymbol>()
  }

  /**
   * Verifies that the token is an exponentiation symbol.
   *
   * @throws AssertionError if the token is not a [Token.ExponentiationSymbol]
   */
  fun isExponentiationSymbol() {
    actual.asVerifiedType<Token.ExponentiationSymbol>()
  }

  /**
   * Verifies that the token is a multiply symbol.
   *
   * @throws AssertionError if the token is not a [Token.MultiplySymbol]
   */
  fun isMultiplySymbol() {
    actual.asVerifiedType<Token.MultiplySymbol>()
  }

  /**
   * Verifies that the token is a left parenthesis symbol.
   *
   * @throws AssertionError if the token is not a [Token.LeftParenthesisSymbol]
   */
  fun isLeftParenthesisSymbol() {
    actual.asVerifiedType<Token.LeftParenthesisSymbol>()
  }

  /**
   * Verifies that the token is a right parenthesis symbol.
   *
   * @throws AssertionError if the token is not a [Token.RightParenthesisSymbol]
   */
  fun isRightParenthesisSymbol() {
    actual.asVerifiedType<Token.RightParenthesisSymbol>()
  }

  /**
   * Verifies that the token is a dollar prefix unit.
   *
   * @throws AssertionError if the token is not a [Token.DollarPrefixUnit]
   */
  fun isDollarPrefixUnit() {
    actual.asVerifiedType<Token.DollarPrefixUnit>()
  }

  /**
   * Verifies that the token is a dollar suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isDollarSuffixUnit() {
    assertUnitIn("dollar", "dollars", "Dollar", "Dollars", "USD")
  }

  /**
   * Verifies that the token is a cent suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCentSuffixUnit() {
    assertUnitIn("cent", "cents", "Cent", "Cents", "¢")
  }

  /**
   * Verifies that the token is a rupee prefix unit.
   *
   * @throws AssertionError if the token is not a [Token.RupeePrefixUnit]
   */
  fun isRupeePrefixUnit() {
    actual.asVerifiedType<Token.RupeePrefixUnit>()
  }

  /**
   * Verifies that the token is a rupee suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isRupeeSuffixUnit() {
    assertUnitIn("rupee", "rupees", "Rupee", "Rupees")
  }

  /**
   * Verifies that the token is a paisa suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isPaisaSuffixUnit() {
    assertUnitIn("paisa", "paise", "Paisa", "Paise")
  }

  // Length Units

  /**
   * Verifies that the token is a meter unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isMeterUnit() {
    assertUnitIn("m", "meter", "meters")
  }

  /**
   * Verifies that the token is an inch unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isInchUnit() {
    assertUnitIn("in", "inch", "inches")
  }

  /**
   * Verifies that the token is a foot unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isFootUnit() {
    assertUnitIn("ft", "foot", "feet")
  }

  /**
   * Verifies that the token is a yard unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isYardUnit() {
    assertUnitIn("yd", "yard", "yards")
  }

  // Mass Units

  /**
   * Verifies that the token is a gram unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isGramUnit() {
    assertUnitIn("g", "gram", "grams")
  }

  /**
   * Verifies that the token is a grain unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isGrainUnit() {
    assertUnitIn("gr", "grain", "grains")
  }

  /**
   * Verifies that the token is an ounce unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isOunceUnit() {
    assertUnitIn("oz", "ounce", "ounces")
  }

  // Area Units

  /**
   * Verifies that the token is a square meter unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isSquareMeterUnit() {
    assertUnitIn("m2")
  }

  /**
   * Verifies that the token is a square inch unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isSquareInchUnit() {
    assertUnitIn("sqin", "sqinch")
  }

  /**
   * Verifies that the token is a square foot unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isSquareFootUnit() {
    assertUnitIn("sqft", "sqfeet")
  }

  /**
   * Verifies that the token is a square yard unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isSquareYardUnit() {
    assertUnitIn("sqyd", "sqyard")
  }

  // Volume Units

  /**
   * Verifies that the token is a cubic meter unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCubicMeterUnit() {
    assertUnitIn("m3")
  }

  /**
   * Verifies that the token is a liter unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isLiterUnit() {
    assertUnitIn("l", "L", "lt", "liter", "liters", "litre", "litres")
  }

  /**
   * Verifies that the token is a cubic centimeter unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCubicCentimeterUnit() {
    assertUnitIn("cc")
  }

  /**
   * Verifies that the token is a cubic inch unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCubicInchUnit() {
    assertUnitIn("cuin")
  }

  /**
   * Verifies that the token is a cubic foot unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCubicFootUnit() {
    assertUnitIn("cuft")
  }

  /**
   * Verifies that the token is a cubic yard unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCubicYardUnit() {
    assertUnitIn("cuyd")
  }

  // Temperature Units

  /**
   * Verifies that the token is a kelvin unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isKelvinUnit() {
    assertUnitIn("K", "kelvin")
  }

  /**
   * Verifies that the token is a celsius unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCelsiusUnit() {
    assertUnitIn("degC", "celsius")
  }

  // Angle Units

  /**
   * Verifies that the token is a radian unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isRadianUnit() {
    assertUnitIn("rad", "radian", "radians")
  }

  /**
   * Verifies that the token is a degree unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isDegreeUnit() {
    assertUnitIn("deg", "degree", "degrees")
  }

  // Time Units

  /**
   * Verifies that the token is a second unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isSecondUnit() {
    assertUnitIn("s", "sec", "secs", "second", "seconds")
  }

  /**
   * Verifies that the token is a minute unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isMinuteUnit() {
    assertUnitIn("min", "mins", "minute", "minutes")
  }

  /**
   * Verifies that the token is an hour unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isHourUnit() {
    assertUnitIn("h", "hr", "hrs", "hour", "hours")
  }

  // Frequency Units

  /**
   * Verifies that the token is a hertz unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isHertzUnit() {
    assertUnitIn("Hz", "hertz")
  }

  // SI Base Units

  /**
   * Verifies that the token is a mole unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isMoleUnit() {
    assertUnitIn("mol", "mole", "moles")
  }

  /**
   * Verifies that the token is a candela unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isCandelaUnit() {
    assertUnitIn("cd", "candela")
  }

  // Derived SI Units

  /**
   * Verifies that the token is a newton unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isNewtonUnit() {
    assertUnitIn("N", "newton", "newtons")
  }

  /**
   * Verifies that the token is a joule unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isJouleUnit() {
    assertUnitIn("J", "joule", "joules")
  }

  /**
   * Verifies that the token is a watt unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isWattUnit() {
    assertUnitIn("W", "watt", "watts")
  }

  /**
   * Verifies that the token is a pascal unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isPascalUnit() {
    assertUnitIn("Pa", "pascal", "pascals")
  }

  /**
   * Verifies that the token is an ampere unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isAmpereUnit() {
    assertUnitIn("A", "ampere", "amperes")
  }

  /**
   * Verifies that the token is a volt unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isVoltUnit() {
    assertUnitIn("V", "volt", "volts")
  }

  /**
   * Verifies that the token is an ohm unit.
   *
   * @throws AssertionError if the token is not a [Token.Unit] with one of this method's accepted raw values
   */
  fun isOhmUnit() {
    assertUnitIn("ohm", "ohms")
  }

  /**
   * Verifies that the token is an invalid token.
   *
   * @throws AssertionError if the token is not a [Token.InvalidToken]
   */
  fun isInvalidToken() {
    actual.asVerifiedType<Token.InvalidToken>()
  }

  /**
   * Verifies that the token is a SI prefix with the expected value.
   *
   * @param expectedValue the expected SI prefix symbol (for example, "k" or "M")
   * @throws AssertionError if the token is not a [Token.SiPrefix] or doesn't have the expected value
   */
  fun isSiPrefixWithValue(expectedValue: String) {
    val siPrefix = actual.asVerifiedType<Token.SiPrefix>()
    Truth.assertThat(siPrefix.prefix).isEqualTo(expectedValue)
  }

  /**
   * Verifies that the token is a [Token.Unit] with exactly the specified raw value.
   *
   * @param expectedValue the exact raw unit text expected from tokenization
   * @throws AssertionError if the token is not a [Token.Unit] or has a different raw value
   */
  fun isUnitWithRawValue(expectedValue: String) {
    val unit = actual.asVerifiedType<Token.Unit>()
    Truth.assertThat(unit.unit).isEqualTo(expectedValue)
  }

  /**
   * Verifies that the token is a [Token.Unit] whose raw value is in [expectedRawValues].
   */
  private fun assertUnitIn(vararg expectedRawValues: String) {
    val unit = actual.asVerifiedType<Token.Unit>()
    Truth.assertThat(expectedRawValues.toList()).contains(unit.unit)
  }

  companion object {
    /**
     * Creates a new [NumberWithUnitsTokenSubject] to verify aspects of the specified [Token] value.
     *
     * This is the main entry point for using this Truth subject. It follows the standard
     * Truth pattern for creating subjects.
     *
     * @param actual the [Token] to create assertions for
     * @return a new [NumberWithUnitsTokenSubject] for making assertions
     */
    fun assertThat(actual: Token): NumberWithUnitsTokenSubject =
      Truth.assertAbout(::NumberWithUnitsTokenSubject).that(actual)

    /**
     * Extension function that safely casts a [Token] to a specific subtype.
     *
     * This method performs a type check before casting to ensure the token is of the
     * expected type. If the type check fails, it throws an appropriate `AssertionError`
     * through Truth's assertion mechanism.
     *
     * @param T the expected token subtype to cast to
     * @return the token cast to the expected type
     * @throws AssertionError if the token is not of the expected type
     */
    private inline fun <reified T : Token> Token.asVerifiedType(): T {
      assertThat(this).isInstanceOf(T::class.java)
      return this as T
    }
  }
}
