package org.oppia.android.testing.math

import com.google.common.truth.ComparableSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IterableSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.MultipleRedundantParenthesesSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.NoVariableOrNumberAfterBinaryOperatorSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.NoVariableOrNumberBeforeBinaryOperatorSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.NumberAfterVariableSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.RedundantParenthesesForIndividualTermsSubject.Companion.assertThat
import org.oppia.android.testing.math.MathParsingErrorSubject.SubsequentBinaryOperatorsSubject.Companion.assertThat
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.MathParsingError
import org.oppia.android.util.math.MathParsingError.DisabledVariablesInUseError
import org.oppia.android.util.math.MathParsingError.EquationHasTooManyEqualsError
import org.oppia.android.util.math.MathParsingError.EquationIsMissingEqualsError
import org.oppia.android.util.math.MathParsingError.EquationMissingLhsOrRhsError
import org.oppia.android.util.math.MathParsingError.ExponentIsVariableExpressionError
import org.oppia.android.util.math.MathParsingError.ExponentTooLargeError
import org.oppia.android.util.math.MathParsingError.FunctionNameIncompleteError
import org.oppia.android.util.math.MathParsingError.GenericError
import org.oppia.android.util.math.MathParsingError.HangingSquareRootError
import org.oppia.android.util.math.MathParsingError.InvalidFunctionInUseError
import org.oppia.android.util.math.MathParsingError.MultipleRedundantParenthesesError
import org.oppia.android.util.math.MathParsingError.NestedExponentsError
import org.oppia.android.util.math.MathParsingError.NoVariableOrNumberAfterBinaryOperatorError
import org.oppia.android.util.math.MathParsingError.NoVariableOrNumberBeforeBinaryOperatorError
import org.oppia.android.util.math.MathParsingError.NumberAfterVariableError
import org.oppia.android.util.math.MathParsingError.RedundantParenthesesForIndividualTermsError
import org.oppia.android.util.math.MathParsingError.SingleRedundantParenthesesError
import org.oppia.android.util.math.MathParsingError.SpacesBetweenNumbersError
import org.oppia.android.util.math.MathParsingError.SubsequentBinaryOperatorsError
import org.oppia.android.util.math.MathParsingError.SubsequentUnaryOperatorsError
import org.oppia.android.util.math.MathParsingError.TermDividedByZeroError
import org.oppia.android.util.math.MathParsingError.UnbalancedParenthesesError
import org.oppia.android.util.math.MathParsingError.UnnecessarySymbolsError
import org.oppia.android.util.math.MathParsingError.VariableInNumericExpressionError

/**
 * Truth subject for verifying properties of [MathParsingError]s.
 *
 * Call [assertThat] to create the subject.
 */
class MathParsingErrorSubject private constructor(
  metadata: FailureMetadata,
  private val actual: MathParsingError
) : Subject(metadata, actual) {
  /** Verifies that the [MathParsingError] being tested is a [SpacesBetweenNumbersError]. */
  fun isSpacesBetweenNumbers() {
    assertThat(actual).isEqualTo(SpacesBetweenNumbersError)
  }

  /** Verifies that the [MathParsingError] being tested is an [UnbalancedParenthesesError]. */
  fun isUnbalancedParentheses() {
    assertThat(actual).isEqualTo(UnbalancedParenthesesError)
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [SingleRedundantParenthesesError], and
   * returns a [SingleRedundantParenthesesSubject] to test its specific attributes.
   */
  fun isSingleRedundantParenthesesThat(): SingleRedundantParenthesesSubject {
    return SingleRedundantParenthesesSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [MultipleRedundantParenthesesError], and
   * returns a [MultipleRedundantParenthesesSubject] to test its specific attributes.
   */
  fun isMultipleRedundantParenthesesThat(): MultipleRedundantParenthesesSubject {
    return MultipleRedundantParenthesesSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is a
   * [RedundantParenthesesForIndividualTermsError], and returns a
   * [RedundantParenthesesForIndividualTermsSubject] to test its specific attributes.
   */
  fun isRedundantIndividualTermsParensThat(): RedundantParenthesesForIndividualTermsSubject {
    return RedundantParenthesesForIndividualTermsSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is an [UnnecessarySymbolsError], and returns
   * a [StringSubject] to verify the symbol's specific value.
   */
  fun isUnnecessarySymbolWithSymbolThat(): StringSubject {
    return assertThat(verifyAsType<UnnecessarySymbolsError>().invalidSymbol)
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [NumberAfterVariableError], and returns
   * a [NumberAfterVariableSubject] to test its specific attributes.
   */
  fun isNumberAfterVariableThat(): NumberAfterVariableSubject {
    return NumberAfterVariableSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [SubsequentBinaryOperatorsError], and
   * returns a [SubsequentBinaryOperatorsSubject] to test its specific attributes.
   */
  fun isSubsequentBinaryOperatorsThat(): SubsequentBinaryOperatorsSubject {
    return SubsequentBinaryOperatorsSubject.assertThat(verifyAsType())
  }

  /** Verifies that the [MathParsingError] being tested is a [SubsequentUnaryOperatorsError]. */
  fun isSubsequentUnaryOperators() {
    assertThat(actual).isEqualTo(SubsequentUnaryOperatorsError)
  }

  /**
   * Verifies that the [MathParsingError] being tested is a
   * [NoVariableOrNumberBeforeBinaryOperatorError], and returns a
   * [NoVariableOrNumberBeforeBinaryOperatorSubject] to test its specific attributes.
   */
  fun isNoVarOrNumBeforeBinaryOperatorThat(): NoVariableOrNumberBeforeBinaryOperatorSubject {
    return NoVariableOrNumberBeforeBinaryOperatorSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is a
   * [NoVariableOrNumberAfterBinaryOperatorError], and returns a
   * [NoVariableOrNumberAfterBinaryOperatorSubject] to test its specific attributes.
   */
  fun isNoVariableOrNumberAfterBinaryOperatorThat(): NoVariableOrNumberAfterBinaryOperatorSubject {
    return NoVariableOrNumberAfterBinaryOperatorSubject.assertThat(verifyAsType())
  }

  /**
   * Verifies that the [MathParsingError] being tested is an [ExponentIsVariableExpressionError].
   */
  fun isExponentIsVariableExpression() {
    assertThat(actual).isEqualTo(ExponentIsVariableExpressionError)
  }

  /** Verifies that the [MathParsingError] being tested is an [ExponentTooLargeError]. */
  fun isExponentTooLarge() {
    assertThat(actual).isEqualTo(ExponentTooLargeError)
  }

  /** Verifies that the [MathParsingError] being tested is a [NestedExponentsError]. */
  fun isNestedExponents() {
    assertThat(actual).isEqualTo(NestedExponentsError)
  }

  /** Verifies that the [MathParsingError] being tested is a [HangingSquareRootError]. */
  fun isHangingSquareRoot() {
    assertThat(actual).isEqualTo(HangingSquareRootError)
  }

  /** Verifies that the [MathParsingError] being tested is a [TermDividedByZeroError]. */
  fun isTermDividedByZero() {
    assertThat(actual).isEqualTo(TermDividedByZeroError)
  }

  /** Verifies that the [MathParsingError] being tested is a [VariableInNumericExpressionError]. */
  fun isVariableInNumericExpression() {
    assertThat(actual).isEqualTo(VariableInNumericExpressionError)
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [DisabledVariablesInUseError], and
   * returns an [IterableSubject] to verify the specific disallowed variables in use.
   */
  fun isDisabledVariablesInUseWithVariablesThat(): IterableSubject {
    return assertThat(verifyAsType<DisabledVariablesInUseError>().variables)
  }

  /** Verifies that the [MathParsingError] being tested is an [EquationIsMissingEqualsError]. */
  fun isEquationIsMissingEquals() {
    assertThat(actual).isEqualTo(EquationIsMissingEqualsError)
  }

  /** Verifies that the [MathParsingError] being tested is an [EquationHasTooManyEqualsError]. */
  fun isEquationHasTooManyEquals() {
    assertThat(actual).isEqualTo(EquationHasTooManyEqualsError)
  }

  /** Verifies that the [MathParsingError] being tested is an [EquationMissingLhsOrRhsError]. */
  fun isEquationMissingLhsOrRhs() {
    assertThat(actual).isEqualTo(EquationMissingLhsOrRhsError)
  }

  /**
   * Verifies that the [MathParsingError] being tested is a [InvalidFunctionInUseError], and returns
   * a [StringSubject] to verify the specific function name in used.
   */
  fun isInvalidFunctionInUseWithNameThat(): StringSubject {
    return assertThat(verifyAsType<InvalidFunctionInUseError>().functionName)
  }

  /** Verifies that the [MathParsingError] being tested is a [FunctionNameIncompleteError]. */
  fun isFunctionNameIncomplete() {
    assertThat(actual).isEqualTo(FunctionNameIncompleteError)
  }

  /** Verifies that the [MathParsingError] being tested is a [GenericError]. */
  fun isGenericError() {
    assertThat(actual).isEqualTo(GenericError)
  }

  private inline fun <reified T : MathParsingError> verifyAsType(): T {
    assertThat(actual).isInstanceOf(T::class.java)
    return actual as T
  }

  /**
   * Truth subject for verifying properties of [SingleRedundantParenthesesError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class SingleRedundantParenthesesSubject private constructor(
    metadata: FailureMetadata,
    private val actual: SingleRedundantParenthesesError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test the value of
     * [SingleRedundantParenthesesError.rawExpression] for the error being tested by this subject.
     */
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    /**
     * Returns a [MathExpressionSubject] to test the value of
     * [SingleRedundantParenthesesError.expression] for the error being tested by this subject.
     */
    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      /**
       * Returns a new [SingleRedundantParenthesesSubject] to verify aspects of the specified
       * [SingleRedundantParenthesesError] value.
       */
      internal fun assertThat(
        actual: SingleRedundantParenthesesError
      ): SingleRedundantParenthesesSubject {
        return assertAbout(::SingleRedundantParenthesesSubject).that(actual)
      }
    }
  }

  /**
   * Truth subject for verifying properties of [MultipleRedundantParenthesesError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class MultipleRedundantParenthesesSubject private constructor(
    metadata: FailureMetadata,
    private val actual: MultipleRedundantParenthesesError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test the value of
     * [MultipleRedundantParenthesesError.rawExpression] for the error being tested by this subject.
     */
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    /**
     * Returns a [MathExpressionSubject] to test the value of
     * [MultipleRedundantParenthesesError.expression] for the error being tested by this
     * subject.
     */
    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      /**
       * Returns a new [MultipleRedundantParenthesesSubject] to verify aspects of the specified
       * [MultipleRedundantParenthesesError] value.
       */
      internal fun assertThat(
        actual: MultipleRedundantParenthesesError
      ): MultipleRedundantParenthesesSubject {
        return assertAbout(::MultipleRedundantParenthesesSubject).that(actual)
      }
    }
  }

  /**
   * Truth subject for verifying properties of [RedundantParenthesesForIndividualTermsError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class RedundantParenthesesForIndividualTermsSubject private constructor(
    metadata: FailureMetadata,
    private val actual: RedundantParenthesesForIndividualTermsError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test the value of
     * [RedundantParenthesesForIndividualTermsError.rawExpression] for the error being tested by
     * this subject.
     */
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    /**
     * Returns a [MathExpressionSubject] to test the value of
     * [RedundantParenthesesForIndividualTermsError.expression] for the error being tested by this
     * subject.
     */
    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      /**
       * Returns a new [RedundantParenthesesForIndividualTermsSubject] to verify aspects of the
       * specified [RedundantParenthesesForIndividualTermsError] value.
       */
      internal fun assertThat(
        actual: RedundantParenthesesForIndividualTermsError
      ): RedundantParenthesesForIndividualTermsSubject {
        return assertAbout(::RedundantParenthesesForIndividualTermsSubject).that(actual)
      }
    }
  }

  /**
   * Truth subject for verifying properties of [NumberAfterVariableError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class NumberAfterVariableSubject private constructor(
    metadata: FailureMetadata,
    private val actual: NumberAfterVariableError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [RealSubject] to test the value of [NumberAfterVariableError.number] for the error
     * being tested by this subject.
     */
    fun hasNumberThat(): RealSubject = assertThat(actual.number)

    /**
     * Returns a [StringSubject] to test the value of [NumberAfterVariableError.variable] for the
     * error being tested by this subject.
     */
    fun hasVariableThat(): StringSubject = assertThat(actual.variable)

    companion object {
      /**
       * Returns a new [NumberAfterVariableSubject] to verify aspects of the specified
       * [NumberAfterVariableError] value.
       */
      internal fun assertThat(actual: NumberAfterVariableError): NumberAfterVariableSubject =
        assertAbout(::NumberAfterVariableSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [SubsequentBinaryOperatorsError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class SubsequentBinaryOperatorsSubject private constructor(
    metadata: FailureMetadata,
    private val actual: SubsequentBinaryOperatorsError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test the value of [SubsequentBinaryOperatorsError.operator1] for
     * the error being tested by this subject.
     */
    fun hasFirstOperatorThat(): StringSubject = assertThat(actual.operator1)

    /**
     * Returns a [StringSubject] to test the value of [SubsequentBinaryOperatorsError.operator2] for
     * the error being tested by this subject.
     */
    fun hasSecondOperatorThat(): StringSubject = assertThat(actual.operator2)

    companion object {
      /**
       * Returns a new [SubsequentBinaryOperatorsSubject] to verify aspects of the
       * specified [SubsequentBinaryOperatorsError] value.
       */
      internal fun assertThat(
        actual: SubsequentBinaryOperatorsError
      ): SubsequentBinaryOperatorsSubject {
        return assertAbout(::SubsequentBinaryOperatorsSubject).that(actual)
      }
    }
  }

  /**
   * Truth subject for verifying properties of [NoVariableOrNumberBeforeBinaryOperatorError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class NoVariableOrNumberBeforeBinaryOperatorSubject private constructor(
    metadata: FailureMetadata,
    private val actual: NoVariableOrNumberBeforeBinaryOperatorError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [ComparableSubject] to test the value of
     * [NoVariableOrNumberBeforeBinaryOperatorError.operator] for the error being tested by this
     * subject.
     */
    fun hasOperatorThat(): ComparableSubject<MathBinaryOperation.Operator> =
      assertThat(actual.operator)

    /**
     * Returns a [StringSubject] to test the value of
     * [NoVariableOrNumberBeforeBinaryOperatorError.operatorSymbol] for the error being tested by
     * this subject.
     */
    fun hasOperatorSymbolThat(): StringSubject = assertThat(actual.operatorSymbol)

    companion object {
      /**
       * Returns a new [NoVariableOrNumberBeforeBinaryOperatorSubject] to verify aspects of the
       * specified [NoVariableOrNumberBeforeBinaryOperatorError] value.
       */
      internal fun assertThat(
        actual: NoVariableOrNumberBeforeBinaryOperatorError
      ): NoVariableOrNumberBeforeBinaryOperatorSubject {
        return assertAbout(::NoVariableOrNumberBeforeBinaryOperatorSubject).that(actual)
      }
    }
  }

  /**
   * Truth subject for verifying properties of [NoVariableOrNumberAfterBinaryOperatorError]s.
   *
   * Call [assertThat] to create the subject.
   */
  class NoVariableOrNumberAfterBinaryOperatorSubject private constructor(
    metadata: FailureMetadata,
    private val actual: NoVariableOrNumberAfterBinaryOperatorError
  ) : Subject(metadata, actual) {
    /**
     * Returns a [ComparableSubject] to test the value of
     * [NoVariableOrNumberAfterBinaryOperatorError.operator] for the error being tested by this
     * subject.
     */
    fun hasOperatorThat(): ComparableSubject<MathBinaryOperation.Operator> =
      assertThat(actual.operator)

    /**
     * Returns a [StringSubject] to test the value of
     * [NoVariableOrNumberAfterBinaryOperatorError.operatorSymbol] for the error being tested by
     * this subject.
     */
    fun hasOperatorSymbolThat(): StringSubject = assertThat(actual.operatorSymbol)

    companion object {
      /**
       * Returns a new [NoVariableOrNumberAfterBinaryOperatorSubject] to verify aspects of the
       * specified [NoVariableOrNumberAfterBinaryOperatorError] value.
       */
      internal fun assertThat(
        actual: NoVariableOrNumberAfterBinaryOperatorError
      ): NoVariableOrNumberAfterBinaryOperatorSubject {
        return assertAbout(::NoVariableOrNumberAfterBinaryOperatorSubject).that(actual)
      }
    }
  }

  companion object {
    /**
     * Returns a new [MathParsingErrorSubject] to verify aspects of the specified [MathParsingError]
     * value.
     */
    fun assertThat(actual: MathParsingError): MathParsingErrorSubject =
      assertAbout(::MathParsingErrorSubject).that(actual)
  }
}
