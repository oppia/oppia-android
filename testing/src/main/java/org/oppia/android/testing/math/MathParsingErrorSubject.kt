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

// TODO: file issue to add tests.

class MathParsingErrorSubject(
  metadata: FailureMetadata,
  private val actual: MathParsingError
) : Subject(metadata, actual) {
  fun isSpacesBetweenNumbers() {
    assertThat(actual).isEqualTo(SpacesBetweenNumbersError)
  }

  fun isUnbalancedParentheses() {
    assertThat(actual).isEqualTo(UnbalancedParenthesesError)
  }

  fun isSingleRedundantParenthesesThat(): SingleRedundantParenthesesSubject {
    return SingleRedundantParenthesesSubject.assertThat(verifyAsType())
  }

  fun isMultipleRedundantParenthesesThat(): MultipleRedundantParenthesesSubject {
    return MultipleRedundantParenthesesSubject.assertThat(verifyAsType())
  }

  fun isRedundantIndividualTermsParensThat(): RedundantParenthesesForIndividualTermsSubject {
    return RedundantParenthesesForIndividualTermsSubject.assertThat(verifyAsType())
  }

  fun isUnnecessarySymbolWithSymbolThat(): StringSubject {
    return assertThat(verifyAsType<UnnecessarySymbolsError>().invalidSymbol)
  }

  fun isNumberAfterVariableThat(): NumberAfterVariableSubject {
    return NumberAfterVariableSubject.assertThat(verifyAsType())
  }

  fun isSubsequentBinaryOperatorsThat(): SubsequentBinaryOperatorsSubject {
    return SubsequentBinaryOperatorsSubject.assertThat(verifyAsType())
  }

  fun isSubsequentUnaryOperators() {
    assertThat(actual).isEqualTo(SubsequentUnaryOperatorsError)
  }

  fun isNoVarOrNumBeforeBinaryOperatorThat(): NoVariableOrNumberBeforeBinaryOperatorSubject {
    return NoVariableOrNumberBeforeBinaryOperatorSubject.assertThat(verifyAsType())
  }

  fun isNoVariableOrNumberAfterBinaryOperatorThat(): NoVariableOrNumberAfterBinaryOperatorSubject {
    return NoVariableOrNumberAfterBinaryOperatorSubject.assertThat(verifyAsType())
  }

  fun isExponentIsVariableExpression() {
    assertThat(actual).isEqualTo(ExponentIsVariableExpressionError)
  }

  fun isExponentTooLarge() {
    assertThat(actual).isEqualTo(ExponentTooLargeError)
  }

  fun isNestedExponents() {
    assertThat(actual).isEqualTo(NestedExponentsError)
  }

  fun isHangingSquareRoot() {
    assertThat(actual).isEqualTo(HangingSquareRootError)
  }

  fun isTermDividedByZero() {
    assertThat(actual).isEqualTo(TermDividedByZeroError)
  }

  fun isVariableInNumericExpression() {
    assertThat(actual).isEqualTo(VariableInNumericExpressionError)
  }

  fun isDisabledVariablesInUseWithVariablesThat(): IterableSubject {
    return assertThat(verifyAsType<DisabledVariablesInUseError>().variables)
  }

  fun isEquationIsMissingEquals() {
    assertThat(actual).isEqualTo(EquationIsMissingEqualsError)
  }

  fun isEquationHasTooManyEquals() {
    assertThat(actual).isEqualTo(EquationHasTooManyEqualsError)
  }

  fun isEquationMissingLhsOrRhs() {
    assertThat(actual).isEqualTo(EquationMissingLhsOrRhsError)
  }

  fun isInvalidFunctionInUseWithNameThat(): StringSubject {
    return assertThat(verifyAsType<InvalidFunctionInUseError>().functionName)
  }

  fun isFunctionNameIncomplete() {
    assertThat(actual).isEqualTo(FunctionNameIncompleteError)
  }

  fun isGenericError() {
    assertThat(actual).isEqualTo(GenericError)
  }

  private inline fun <reified T : MathParsingError> verifyAsType(): T {
    assertThat(actual).isInstanceOf(T::class.java)
    return actual as T
  }

  class SingleRedundantParenthesesSubject(
    metadata: FailureMetadata,
    private val actual: SingleRedundantParenthesesError
  ) : Subject(metadata, actual) {
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      internal fun assertThat(
        actual: SingleRedundantParenthesesError
      ): SingleRedundantParenthesesSubject {
        return assertAbout(::SingleRedundantParenthesesSubject).that(actual)
      }
    }
  }

  class MultipleRedundantParenthesesSubject(
    metadata: FailureMetadata,
    private val actual: MultipleRedundantParenthesesError
  ) : Subject(metadata, actual) {
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      internal fun assertThat(
        actual: MultipleRedundantParenthesesError
      ): MultipleRedundantParenthesesSubject {
        return assertAbout(::MultipleRedundantParenthesesSubject).that(actual)
      }
    }
  }

  class RedundantParenthesesForIndividualTermsSubject(
    metadata: FailureMetadata,
    private val actual: RedundantParenthesesForIndividualTermsError
  ) : Subject(metadata, actual) {
    fun hasRawExpressionThat(): StringSubject = assertThat(actual.rawExpression)

    fun hasExpressionThat(): MathExpressionSubject = assertThat(actual.expression)

    companion object {
      internal fun assertThat(
        actual: RedundantParenthesesForIndividualTermsError
      ): RedundantParenthesesForIndividualTermsSubject {
        return assertAbout(::RedundantParenthesesForIndividualTermsSubject).that(actual)
      }
    }
  }

  class NumberAfterVariableSubject(
    metadata: FailureMetadata,
    private val actual: NumberAfterVariableError
  ) : Subject(metadata, actual) {
    fun hasNumberThat(): RealSubject = assertThat(actual.number)

    fun hasVariableThat(): StringSubject = assertThat(actual.variable)

    companion object {
      internal fun assertThat(actual: NumberAfterVariableError): NumberAfterVariableSubject =
        assertAbout(::NumberAfterVariableSubject).that(actual)
    }
  }

  class SubsequentBinaryOperatorsSubject(
    metadata: FailureMetadata,
    private val actual: SubsequentBinaryOperatorsError
  ) : Subject(metadata, actual) {
    fun hasFirstOperatorThat(): StringSubject = assertThat(actual.operator1)

    fun hasSecondOperatorThat(): StringSubject = assertThat(actual.operator2)

    companion object {
      internal fun assertThat(
        actual: SubsequentBinaryOperatorsError
      ): SubsequentBinaryOperatorsSubject {
        return assertAbout(::SubsequentBinaryOperatorsSubject).that(actual)
      }
    }
  }

  class NoVariableOrNumberBeforeBinaryOperatorSubject(
    metadata: FailureMetadata,
    private val actual: NoVariableOrNumberBeforeBinaryOperatorError
  ) : Subject(metadata, actual) {
    fun hasOperatorThat(): ComparableSubject<MathBinaryOperation.Operator> =
      assertThat(actual.operator)

    fun hasOperatorSymbolThat(): StringSubject = assertThat(actual.operatorSymbol)

    companion object {
      internal fun assertThat(
        actual: NoVariableOrNumberBeforeBinaryOperatorError
      ): NoVariableOrNumberBeforeBinaryOperatorSubject {
        return assertAbout(::NoVariableOrNumberBeforeBinaryOperatorSubject).that(actual)
      }
    }
  }

  class NoVariableOrNumberAfterBinaryOperatorSubject(
    metadata: FailureMetadata,
    private val actual: NoVariableOrNumberAfterBinaryOperatorError
  ) : Subject(metadata, actual) {
    fun hasOperatorThat(): ComparableSubject<MathBinaryOperation.Operator> =
      assertThat(actual.operator)

    fun hasOperatorSymbolThat(): StringSubject = assertThat(actual.operatorSymbol)

    companion object {
      internal fun assertThat(
        actual: NoVariableOrNumberAfterBinaryOperatorError
      ): NoVariableOrNumberAfterBinaryOperatorSubject {
        return assertAbout(::NoVariableOrNumberAfterBinaryOperatorSubject).that(actual)
      }
    }
  }

  companion object {
    fun assertThat(actual: MathParsingError): MathParsingErrorSubject =
      assertAbout(::MathParsingErrorSubject).that(actual)
  }
}
