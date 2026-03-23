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
   * @throws AssertionError if the token is not a [Token.DollarSuffixUnit]
   */
  fun isDollarSuffixUnit() {
    actual.asVerifiedType<Token.DollarSuffixUnit>()
  }

  /**
   * Verifies that the token is a cent suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.CentSuffixUnit]
   */
  fun isCentSuffixUnit() {
    actual.asVerifiedType<Token.CentSuffixUnit>()
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
   * @throws AssertionError if the token is not a [Token.RupeeSuffixUnit]
   */
  fun isRupeeSuffixUnit() {
    actual.asVerifiedType<Token.RupeeSuffixUnit>()
  }

  /**
   * Verifies that the token is a paisa suffix unit.
   *
   * @throws AssertionError if the token is not a [Token.PaisaSuffixUnit]
   */
  fun isPaisaSuffixUnit() {
    actual.asVerifiedType<Token.PaisaSuffixUnit>()
  }

  // Length Units

  /**
   * Verifies that the token is a meter unit.
   *
   * @throws AssertionError if the token is not a [Token.MeterUnit]
   */
  fun isMeterUnit() {
    actual.asVerifiedType<Token.MeterUnit>()
  }

  /**
   * Verifies that the token is an inch unit.
   *
   * @throws AssertionError if the token is not a [Token.InchUnit]
   */
  fun isInchUnit() {
    actual.asVerifiedType<Token.InchUnit>()
  }

  /**
   * Verifies that the token is a foot unit.
   *
   * @throws AssertionError if the token is not a [Token.FootUnit]
   */
  fun isFootUnit() {
    actual.asVerifiedType<Token.FootUnit>()
  }

  /**
   * Verifies that the token is a yard unit.
   *
   * @throws AssertionError if the token is not a [Token.YardUnit]
   */
  fun isYardUnit() {
    actual.asVerifiedType<Token.YardUnit>()
  }

  // Mass Units

  /**
   * Verifies that the token is a gram unit.
   *
   * @throws AssertionError if the token is not a [Token.GramUnit]
   */
  fun isGramUnit() {
    actual.asVerifiedType<Token.GramUnit>()
  }

  /**
   * Verifies that the token is a grain unit.
   *
   * @throws AssertionError if the token is not a [Token.GrainUnit]
   */
  fun isGrainUnit() {
    actual.asVerifiedType<Token.GrainUnit>()
  }

  /**
   * Verifies that the token is an ounce unit.
   *
   * @throws AssertionError if the token is not a [Token.OunceUnit]
   */
  fun isOunceUnit() {
    actual.asVerifiedType<Token.OunceUnit>()
  }

  // Area Units

  /**
   * Verifies that the token is a square meter unit.
   *
   * @throws AssertionError if the token is not a [Token.SquareMeterUnit]
   */
  fun isSquareMeterUnit() {
    actual.asVerifiedType<Token.SquareMeterUnit>()
  }

  /**
   * Verifies that the token is a square inch unit.
   *
   * @throws AssertionError if the token is not a [Token.SquareInchUnit]
   */
  fun isSquareInchUnit() {
    actual.asVerifiedType<Token.SquareInchUnit>()
  }

  /**
   * Verifies that the token is a square foot unit.
   *
   * @throws AssertionError if the token is not a [Token.SquareFootUnit]
   */
  fun isSquareFootUnit() {
    actual.asVerifiedType<Token.SquareFootUnit>()
  }

  /**
   * Verifies that the token is a square yard unit.
   *
   * @throws AssertionError if the token is not a [Token.SquareYardUnit]
   */
  fun isSquareYardUnit() {
    actual.asVerifiedType<Token.SquareYardUnit>()
  }

  // Volume Units

  /**
   * Verifies that the token is a cubic meter unit.
   *
   * @throws AssertionError if the token is not a [Token.CubicMeterUnit]
   */
  fun isCubicMeterUnit() {
    actual.asVerifiedType<Token.CubicMeterUnit>()
  }

  /**
   * Verifies that the token is a liter unit.
   *
   * @throws AssertionError if the token is not a [Token.LiterUnit]
   */
  fun isLiterUnit() {
    actual.asVerifiedType<Token.LiterUnit>()
  }

  /**
   * Verifies that the token is a cubic centimeter unit.
   *
   * @throws AssertionError if the token is not a [Token.CubicCentimeterUnit]
   */
  fun isCubicCentimeterUnit() {
    actual.asVerifiedType<Token.CubicCentimeterUnit>()
  }

  /**
   * Verifies that the token is a cubic inch unit.
   *
   * @throws AssertionError if the token is not a [Token.CubicInchUnit]
   */
  fun isCubicInchUnit() {
    actual.asVerifiedType<Token.CubicInchUnit>()
  }

  /**
   * Verifies that the token is a cubic foot unit.
   *
   * @throws AssertionError if the token is not a [Token.CubicFootUnit]
   */
  fun isCubicFootUnit() {
    actual.asVerifiedType<Token.CubicFootUnit>()
  }

  /**
   * Verifies that the token is a cubic yard unit.
   *
   * @throws AssertionError if the token is not a [Token.CubicYardUnit]
   */
  fun isCubicYardUnit() {
    actual.asVerifiedType<Token.CubicYardUnit>()
  }

  // Temperature Units

  /**
   * Verifies that the token is a kelvin unit.
   *
   * @throws AssertionError if the token is not a [Token.KelvinUnit]
   */
  fun isKelvinUnit() {
    actual.asVerifiedType<Token.KelvinUnit>()
  }

  /**
   * Verifies that the token is a celsius unit.
   *
   * @throws AssertionError if the token is not a [Token.CelsiusUnit]
   */
  fun isCelsiusUnit() {
    actual.asVerifiedType<Token.CelsiusUnit>()
  }

  // Angle Units

  /**
   * Verifies that the token is a radian unit.
   *
   * @throws AssertionError if the token is not a [Token.RadianUnit]
   */
  fun isRadianUnit() {
    actual.asVerifiedType<Token.RadianUnit>()
  }

  /**
   * Verifies that the token is a degree unit.
   *
   * @throws AssertionError if the token is not a [Token.DegreeUnit]
   */
  fun isDegreeUnit() {
    actual.asVerifiedType<Token.DegreeUnit>()
  }

  // Time Units

  /**
   * Verifies that the token is a second unit.
   *
   * @throws AssertionError if the token is not a [Token.SecondUnit]
   */
  fun isSecondUnit() {
    actual.asVerifiedType<Token.SecondUnit>()
  }

  /**
   * Verifies that the token is a minute unit.
   *
   * @throws AssertionError if the token is not a [Token.MinuteUnit]
   */
  fun isMinuteUnit() {
    actual.asVerifiedType<Token.MinuteUnit>()
  }

  /**
   * Verifies that the token is an hour unit.
   *
   * @throws AssertionError if the token is not a [Token.HourUnit]
   */
  fun isHourUnit() {
    actual.asVerifiedType<Token.HourUnit>()
  }

  // Frequency Units

  /**
   * Verifies that the token is a hertz unit.
   *
   * @throws AssertionError if the token is not a [Token.HertzUnit]
   */
  fun isHertzUnit() {
    actual.asVerifiedType<Token.HertzUnit>()
  }

  // SI Base Units

  /**
   * Verifies that the token is a mole unit.
   *
   * @throws AssertionError if the token is not a [Token.MoleUnit]
   */
  fun isMoleUnit() {
    actual.asVerifiedType<Token.MoleUnit>()
  }

  /**
   * Verifies that the token is a candela unit.
   *
   * @throws AssertionError if the token is not a [Token.CandelaUnit]
   */
  fun isCandelaUnit() {
    actual.asVerifiedType<Token.CandelaUnit>()
  }

  // Derived SI Units

  /**
   * Verifies that the token is a newton unit.
   *
   * @throws AssertionError if the token is not a [Token.NewtonUnit]
   */
  fun isNewtonUnit() {
    actual.asVerifiedType<Token.NewtonUnit>()
  }

  /**
   * Verifies that the token is a joule unit.
   *
   * @throws AssertionError if the token is not a [Token.JouleUnit]
   */
  fun isJouleUnit() {
    actual.asVerifiedType<Token.JouleUnit>()
  }

  /**
   * Verifies that the token is a watt unit.
   *
   * @throws AssertionError if the token is not a [Token.WattUnit]
   */
  fun isWattUnit() {
    actual.asVerifiedType<Token.WattUnit>()
  }

  /**
   * Verifies that the token is a pascal unit.
   *
   * @throws AssertionError if the token is not a [Token.PascalUnit]
   */
  fun isPascalUnit() {
    actual.asVerifiedType<Token.PascalUnit>()
  }

  /**
   * Verifies that the token is an ampere unit.
   *
   * @throws AssertionError if the token is not a [Token.AmpereUnit]
   */
  fun isAmpereUnit() {
    actual.asVerifiedType<Token.AmpereUnit>()
  }

  /**
   * Verifies that the token is a volt unit.
   *
   * @throws AssertionError if the token is not a [Token.VoltUnit]
   */
  fun isVoltUnit() {
    actual.asVerifiedType<Token.VoltUnit>()
  }

  /**
   * Verifies that the token is an ohm unit.
   *
   * @throws AssertionError if the token is not a [Token.OhmUnit]
   */
  fun isOhmUnit() {
    actual.asVerifiedType<Token.OhmUnit>()
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
   * @param expectedValue the expected [Token.SiPrefixValue] to match
   * @throws AssertionError if the token is not a [Token.SiPrefix] or doesn't have the expected value
   */
  fun isSiPrefixWithValue(expectedValue: Token.SiPrefixValue) {
    val siPrefix = actual.asVerifiedType<Token.SiPrefix>()
    Truth.assertThat(siPrefix.prefixValue).isEqualTo(expectedValue)
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
