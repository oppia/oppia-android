package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real
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
import org.oppia.android.util.math.MathParsingError.SingleRedundantParenthesesError
import org.oppia.android.util.math.MathParsingError.SpacesBetweenNumbersError
import org.oppia.android.util.math.MathParsingError.SubsequentBinaryOperatorsError
import org.oppia.android.util.math.MathParsingError.SubsequentUnaryOperatorsError
import org.oppia.android.util.math.MathParsingError.TermDividedByZeroError
import org.oppia.android.util.math.MathParsingError.UnbalancedParenthesesError
import org.oppia.android.util.math.MathParsingError.UnnecessarySymbolsError
import org.oppia.android.util.math.MathParsingError.VariableInNumericExpressionError

/** Tests for [MathParsingErrorSubject]. */
@RunWith(JUnit4::class)
class MathParsingErrorSubjectTest {

  @Test
  fun testMathParsingErrorSubject_hasSpaceBetweenNumbersError() {
    val error = SpacesBetweenNumbersError
    MathParsingErrorSubject.assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testMathParsingErrorSubject_hasSpaceBetweenNumbersError_fails() {
    val error = UnbalancedParenthesesError
    assertThrows(AssertionError::class.java) {
      MathParsingErrorSubject.assertThat(error).isSpacesBetweenNumbers()
    }
  }

  @Test
  fun testMathParsingErrorSubject_hasUnbalancedParenthesesError() {
    val error = UnbalancedParenthesesError
    MathParsingErrorSubject.assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testMathParsingErrorSubject_hasUnbalancedParenthesesError_fails() {
    val error = SpacesBetweenNumbersError
    assertThrows(AssertionError::class.java) {
      MathParsingErrorSubject.assertThat(error).isUnbalancedParentheses()
    }
  }

  @Test
  fun testMathParsingErrorSubject_hasSingleRedundantParentheses() {
    val constant = Real.newBuilder()
      .setInteger(5)
      .build()
    val expression = MathExpression.newBuilder()
      .setConstant(constant)
      .build()
    val group = MathExpression.newBuilder()
      .setGroup(expression)
      .build()
    val error = SingleRedundantParenthesesError("(5)", group)
    val subject = MathParsingErrorSubject.assertThat(error).isSingleRedundantParenthesesThat()
    subject.hasExpressionThat().evaluatesToIntegerThat().isEqualTo(5)
    subject.hasRawExpressionThat().isEqualTo("(5)")
  }

  @Test
  fun testMathParsingErrorSubject_hasMultipleRedundantParentheses() {
    val constant = Real.newBuilder()
      .setInteger(5)
      .build()
    val expression = MathExpression.newBuilder()
      .setConstant(constant)
      .build()
    val groupOne = MathExpression.newBuilder()
      .setGroup(expression)
      .build()
    val groupTwo = MathExpression.newBuilder()
      .setGroup(groupOne)
      .build()
    val error = MultipleRedundantParenthesesError("((5))", groupTwo)
    val subject = MathParsingErrorSubject.assertThat(error).isMultipleRedundantParenthesesThat()
    subject.hasExpressionThat().evaluatesToIntegerThat().isEqualTo(5)
    subject.hasRawExpressionThat().isEqualTo("((5))")
  }

  @Test
  fun testMathParsingErrorSubject_matchesUnnecessarySymbol() {
    val error = UnnecessarySymbolsError("@")
    MathParsingErrorSubject.assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("@")
  }

  @Test
  fun testMathParsingErrorSubject_matchesUnnecessarySymbol_fails() {
    val error = UnnecessarySymbolsError("@")
    assertThrows(AssertionError::class.java) {
      MathParsingErrorSubject.assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("#")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNumberAfterVariableError() {
    val number = Real.newBuilder().setInteger(5).build()
    val error = NumberAfterVariableError(number, "x")
    val subject = MathParsingErrorSubject.assertThat(error).isNumberAfterVariableThat()
    subject.hasNumberThat().isIntegerThat().isEqualTo(5)
    subject.hasVariableThat().isEqualTo("x")
  }

  @Test
  fun testMathParsingErrorSubject_isNumberAfterVariableError_fails() {
    val number = Real.newBuilder().setInteger(5).build()
    val error = NumberAfterVariableError(number, "x")
    assertThrows(AssertionError::class.java) {
      val subject = MathParsingErrorSubject.assertThat(error).isNumberAfterVariableThat()
      subject.hasNumberThat().isIntegerThat().isEqualTo(6)
      subject.hasVariableThat().isEqualTo("y")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentBinaryOperatorsError() {
    val error = SubsequentBinaryOperatorsError("x", "+")
    val subject = MathParsingErrorSubject.assertThat(error).isSubsequentBinaryOperatorsThat()
    subject.hasFirstOperatorThat().isEqualTo("x")
    subject.hasSecondOperatorThat().isEqualTo("+")
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentBinaryOperatorsError_fails() {
    val error = SubsequentBinaryOperatorsError("x", "+")
    assertThrows(AssertionError::class.java) {
      val subject = MathParsingErrorSubject.assertThat(error).isSubsequentBinaryOperatorsThat()
      subject.hasFirstOperatorThat().isEqualTo("y")
      subject.hasSecondOperatorThat().isEqualTo("-")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentUnaryOperatorsError() {
    val error = SubsequentUnaryOperatorsError
    MathParsingErrorSubject.assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testMathParsingErrorSubject_isNoVarOrNumBeforeBinaryOperator() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberBeforeBinaryOperatorError(operator, "+")
    val subject = MathParsingErrorSubject.assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat()
    subject.hasOperatorThat().isEqualTo(operator)
    subject.hasOperatorSymbolThat().isEqualTo("+")
  }

  @Test
  fun testMathParsingErrorSubject_isNoVarOrNumBeforeBinaryOperator_fails() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberBeforeBinaryOperatorError(operator, "+")
    assertThrows(AssertionError::class.java) {
      val subject = MathParsingErrorSubject.assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat()
      subject.hasOperatorThat().isEqualTo(MathBinaryOperation.Operator.SUBTRACT)
      subject.hasOperatorSymbolThat().isEqualTo("-")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNoVariableOrNumberAfterBinaryOperator() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberAfterBinaryOperatorError(operator, "+")
    val subject = MathParsingErrorSubject.assertThat(error)
      .isNoVariableOrNumberAfterBinaryOperatorThat()
    subject.hasOperatorThat().isEqualTo(operator)
    subject.hasOperatorSymbolThat().isEqualTo("+")
  }

  @Test
  fun testMathParsingErrorSubject_isNoVariableOrNumberAfterBinaryOperator_fails() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberAfterBinaryOperatorError(operator, "+")
    assertThrows(AssertionError::class.java) {
      val subject = MathParsingErrorSubject.assertThat(error)
        .isNoVariableOrNumberAfterBinaryOperatorThat()
      subject.hasOperatorThat().isEqualTo(MathBinaryOperation.Operator.SUBTRACT)
      subject.hasOperatorSymbolThat().isEqualTo("-")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isExponentIsVariableExpressionError() {
    val error = ExponentIsVariableExpressionError
    MathParsingErrorSubject.assertThat(error).isExponentIsVariableExpression()
  }

  @Test
  fun testMathParsingErrorSubject_isExponentTooLargeError() {
    val error = ExponentTooLargeError
    MathParsingErrorSubject.assertThat(error).isExponentTooLarge()
  }

  @Test
  fun testMathParsingErrorSubject_isNestedExponentsError() {
    val error = NestedExponentsError
    MathParsingErrorSubject.assertThat(error).isNestedExponents()
  }

  @Test
  fun testMathParsingErrorSubject_isHangingSquareRootError() {
    val error = HangingSquareRootError
    MathParsingErrorSubject.assertThat(error).isHangingSquareRoot()
  }

  @Test
  fun testMathParsingErrorSubject_isTermDividedByZeroError() {
    val error = TermDividedByZeroError
    MathParsingErrorSubject.assertThat(error).isTermDividedByZero()
  }

  @Test
  fun testMathParsingErrorSubject_isVariableInNumericExpressionError() {
    val error = VariableInNumericExpressionError
    MathParsingErrorSubject.assertThat(error).isVariableInNumericExpression()
  }

  @Test
  fun testMathParsingErrorSubject_isDisabledVariablesInUseWithVariablesError() {
    val error = DisabledVariablesInUseError(listOf("x", "y"))
    val subject = MathParsingErrorSubject.assertThat(error)
      .isDisabledVariablesInUseWithVariablesThat()
    subject.containsExactly("x", "y")
  }

  @Test
  fun testMathParsingErrorSubject_isDisabledVariablesInUseWithVariablesError_fails() {
    val error = DisabledVariablesInUseError(listOf("x", "y"))
    assertThrows(AssertionError::class.java) {
      val subject = MathParsingErrorSubject.assertThat(error)
        .isDisabledVariablesInUseWithVariablesThat()
      subject.containsExactly("x", "z")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isEquationIsMissingEqualsError() {
    val error = EquationIsMissingEqualsError
    MathParsingErrorSubject.assertThat(error).isEquationIsMissingEquals()
  }

  @Test
  fun testMathParsingErrorSubject_isEquationHasTooManyEqualsError() {
    val error = EquationHasTooManyEqualsError
    MathParsingErrorSubject.assertThat(error).isEquationHasTooManyEquals()
  }

  @Test
  fun testMathParsingErrorSubject_isEquationMissingLhsOrRhsError() {
    val error = EquationMissingLhsOrRhsError
    MathParsingErrorSubject.assertThat(error).isEquationMissingLhsOrRhs()
  }

  @Test
  fun testMathParsingErrorSubject_isInvalidFunctionInUseWithNameError() {
    val error = InvalidFunctionInUseError("sin")
    val subject = MathParsingErrorSubject.assertThat(error).isInvalidFunctionInUseWithNameThat()
    subject.isEqualTo("sin")
  }

  @Test
  fun testMathParsingErrorSubject_isFunctionNameIncompleteError() {
    val error = FunctionNameIncompleteError
    MathParsingErrorSubject.assertThat(error).isFunctionNameIncomplete()
  }

  @Test
  fun testMathParsingErrorSubject_isGenericError() {
    val error = GenericError
    MathParsingErrorSubject.assertThat(error).isGenericError()
  }
}
