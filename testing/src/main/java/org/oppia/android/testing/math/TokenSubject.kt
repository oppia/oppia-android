package org.oppia.android.testing.math

import com.google.common.truth.BooleanSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import org.oppia.android.testing.math.TokenSubject.Companion.assertThat
import org.oppia.android.util.math.MathTokenizer.Companion.Token
import org.oppia.android.util.math.MathTokenizer.Companion.Token.DivideSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.EqualsSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.ExponentiationSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.FunctionName
import org.oppia.android.util.math.MathTokenizer.Companion.Token.IncompleteFunctionName
import org.oppia.android.util.math.MathTokenizer.Companion.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Companion.Token.LeftParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.MinusSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.MultiplySymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PlusSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PositiveInteger
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PositiveRealNumber
import org.oppia.android.util.math.MathTokenizer.Companion.Token.RightParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.SquareRootSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.VariableName

/**
 * Truth subject for verifying properties of [Token]s.
 *
 * Call [assertThat] to create the subject.
 */
class TokenSubject(
  metadata: FailureMetadata,
  private val actual: Token
) : Subject(metadata, actual) {
  /** Returns an [IntegerSubject] to test [Token.startIndex]. */
  fun hasStartIndexThat(): IntegerSubject = assertThat(actual.startIndex)

  /** Returns an [IntegerSubject] to test [Token.endIndex]. */
  fun hasEndIndexThat(): IntegerSubject = assertThat(actual.endIndex)

  /**
   * Verifies that the [Token] being tested is a [PositiveInteger], and returns an [IntegerSubject]
   * to test its [PositiveInteger.parsedValue].
   */
  fun isPositiveIntegerWhoseValue(): IntegerSubject {
    return assertThat(actual.asVerifiedType<PositiveInteger>().parsedValue)
  }

  /**
   * Verifies that the [Token] being tested is a [PositiveRealNumber], and returns a [DoubleSubject]
   * to test its [PositiveRealNumber.parsedValue].
   */
  fun isPositiveRealNumberWhoseValue(): DoubleSubject {
    return assertThat(actual.asVerifiedType<PositiveRealNumber>().parsedValue)
  }

  /**
   * Verifies that the [Token] being tested is a [VariableName], and returns a [StringSubject] to
   * test its [VariableName.parsedName].
   */
  fun isVariableWhoseName(): StringSubject {
    return assertThat(actual.asVerifiedType<VariableName>().parsedName)
  }

  /**
   * Verifies that the [Token] being tested is a [FunctionName], and returns a [FunctionNameSubject]
   * to test specific attributes of the function name.
   */
  fun isFunctionNameThat(): FunctionNameSubject {
    return FunctionNameSubject.assertThat(actual.asVerifiedType())
  }

  /** Verifies that the [Token] being tested is a [MinusSymbol]. */
  fun isMinusSymbol() {
    actual.asVerifiedType<MinusSymbol>()
  }

  /** Verifies that the [Token] being tested is a [SquareRootSymbol]. */
  fun isSquareRootSymbol() {
    actual.asVerifiedType<SquareRootSymbol>()
  }

  /** Verifies that the [Token] being tested is a [PlusSymbol]. */
  fun isPlusSymbol() {
    actual.asVerifiedType<PlusSymbol>()
  }

  /** Verifies that the [Token] being tested is a [MultiplySymbol]. */
  fun isMultiplySymbol() {
    actual.asVerifiedType<MultiplySymbol>()
  }

  /** Verifies that the [Token] being tested is a [DivideSymbol]. */
  fun isDivideSymbol() {
    actual.asVerifiedType<DivideSymbol>()
  }

  /** Verifies that the [Token] being tested is an [ExponentiationSymbol]. */
  fun isExponentiationSymbol() {
    actual.asVerifiedType<ExponentiationSymbol>()
  }

  /** Verifies that the [Token] being tested is an [EqualsSymbol]. */
  fun isEqualsSymbol() {
    actual.asVerifiedType<EqualsSymbol>()
  }

  /** Verifies that the [Token] being tested is a [LeftParenthesisSymbol]. */
  fun isLeftParenthesisSymbol() {
    actual.asVerifiedType<LeftParenthesisSymbol>()
  }

  /** Verifies that the [Token] being tested is a [RightParenthesisSymbol]. */
  fun isRightParenthesisSymbol() {
    actual.asVerifiedType<RightParenthesisSymbol>()
  }

  /** Verifies that the [Token] being tested is an [InvalidToken]. */
  fun isInvalidToken() {
    actual.asVerifiedType<InvalidToken>()
  }

  /** Verifies that the [Token] being tested is an [IncompleteFunctionName]. */
  fun isIncompleteFunctionName() {
    actual.asVerifiedType<IncompleteFunctionName>()
  }

  /**
   * Truth subject for verifying properties of [FunctionName].
   *
   * Call [assertThat] to create the subject.
   */
  class FunctionNameSubject(
    metadata: FailureMetadata,
    private val actual: FunctionName
  ) : Subject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test the value of [FunctionName.parsedName] for the function
     * name being tested by this subject.
     */
    fun hasNameThat(): StringSubject = assertThat(actual.parsedName)

    /**
     * Returns a [BooleanSubject] to test the value of [FunctionName.isAllowedFunction] for the
     * function name being tested by this subject.
     */
    fun hasIsAllowedPropertyThat(): BooleanSubject = assertThat(actual.isAllowedFunction)

    companion object {
      /**
       * Returns a new [FunctionNameSubject] to verify aspects of the specified [FunctionName]
       * value.
       */
      internal fun assertThat(actual: FunctionName): FunctionNameSubject =
        assertAbout(::FunctionNameSubject).that(actual)
    }
  }

  companion object {
    /** Returns a new [TokenSubject] to verify aspects of the specified [Token] value. */
    fun assertThat(actual: Token): TokenSubject = assertAbout(::TokenSubject).that(actual)

    private inline fun <reified T : Token> Token.asVerifiedType(): T {
      assertThat(this).isInstanceOf(T::class.java)
      return this as T
    }
  }
}
