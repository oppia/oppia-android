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
   * Verifies that the token is an invalid token.
   *
   * @throws AssertionError if the token is not a [Token.InvalidToken]
   */
  fun isInvalidToken() {
    actual.asVerifiedType<Token.InvalidToken>()
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
