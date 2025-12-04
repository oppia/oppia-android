package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.MathParsingErrorSubject.Companion.assertThat
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
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testMathParsingErrorSubject_hasSpaceBetweenNumbersError_fails() {
    val error = UnbalancedParenthesesError
    assertThrows(AssertionError::class.java) {
      assertThat(error).isSpacesBetweenNumbers()
    }
  }

  @Test
  fun testMathParsingErrorSubject_hasUnbalancedParenthesesError() {
    val error = UnbalancedParenthesesError
    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testMathParsingErrorSubject_hasUnbalancedParenthesesError_fails() {
    val error = SpacesBetweenNumbersError
    assertThrows(AssertionError::class.java) {
      assertThat(error).isUnbalancedParentheses()
    }
  }

  @Test
  fun testMathParsingErrorSubject_hasSingleRedundantParenthesesWitDetails() {
    val constant = Real.newBuilder().setInteger(5).build()
    val expression = MathExpression.newBuilder().setConstant(constant).build()
    val group = MathExpression.newBuilder().setGroup(expression).build()
    val error = SingleRedundantParenthesesError("(5)", group)

    assertThat(error).isSingleRedundantParenthesesThat().apply {
      hasRawExpressionThat().isEqualTo("(5)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          constant {
            hasExpressionThat().evaluatesToIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_hasMultipleRedundantParenthesesWithDetails() {
    val constant = Real.newBuilder().setInteger(5).build()
    val expression = MathExpression.newBuilder().setConstant(constant).build()
    val groupOne = MathExpression.newBuilder().setGroup(expression).build()
    val groupTwo = MathExpression.newBuilder().setGroup(groupOne).build()
    val error = MultipleRedundantParenthesesError("((5))", groupTwo)

    assertThat(error).isMultipleRedundantParenthesesThat().apply {
      hasRawExpressionThat().isEqualTo("((5))")
      hasExpressionThat().hasStructureThatMatches {
        group {
          group {
            constant {
              hasExpressionThat().evaluatesToIntegerThat().isEqualTo(5)
            }
          }
        }
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_matchesUnnecessarySymbol() {
    val error = UnnecessarySymbolsError("@")
    assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("@")
  }

  @Test
  fun testMathParsingErrorSubject_matchesUnnecessarySymbol_fails() {
    val error = UnnecessarySymbolsError("@")
    assertThrows(AssertionError::class.java) {
      assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("#")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNumberAfterVariableError() {
    val number = Real.newBuilder().setInteger(5).build()
    val error = NumberAfterVariableError(number, "x")

    assertThat(error).isNumberAfterVariableThat().apply {
      hasNumberThat().isIntegerThat().isEqualTo(5)
      hasVariableThat().isEqualTo("x")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNumberAfterVariableError_fails() {
    val number = Real.newBuilder().setInteger(5).build()
    val error = NumberAfterVariableError(number, "x")
    assertThrows(AssertionError::class.java) {
      assertThat(error).isNumberAfterVariableThat().apply {
        hasNumberThat().isIntegerThat().isEqualTo(5)
        hasVariableThat().isEqualTo("y")
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentBinaryOperatorsError() {
    val error = SubsequentBinaryOperatorsError("x", "+")
    assertThat(error).isSubsequentBinaryOperatorsThat().apply {
      hasFirstOperatorThat().isEqualTo("x")
      hasSecondOperatorThat().isEqualTo("+")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentBinaryOperatorsError_fails() {
    val error = SubsequentBinaryOperatorsError("x", "+")
    assertThrows(AssertionError::class.java) {
      assertThat(error).isSubsequentBinaryOperatorsThat().apply {
        hasFirstOperatorThat().isEqualTo("y")
        hasSecondOperatorThat().isEqualTo("-")
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_isSubsequentUnaryOperatorsError() {
    val error = SubsequentUnaryOperatorsError
    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testMathParsingErrorSubject_isNoVarOrNumBeforeBinaryOperator() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberBeforeBinaryOperatorError(operator, "+")
    assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo("+")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNoVarOrNumBeforeBinaryOperator_fails() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberBeforeBinaryOperatorError(operator, "+")
    assertThrows(AssertionError::class.java) {
      assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat().apply {
        hasOperatorThat().isEqualTo(MathBinaryOperation.Operator.SUBTRACT)
        hasOperatorSymbolThat().isEqualTo("-")
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNoVariableOrNumberAfterBinaryOperator() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberAfterBinaryOperatorError(operator, "+")
    assertThat(error).isNoVariableOrNumberAfterBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo("+")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isNoVariableOrNumberAfterBinaryOperator_fails() {
    val operator = MathBinaryOperation.Operator.ADD
    val error = NoVariableOrNumberAfterBinaryOperatorError(operator, "+")
    assertThrows(AssertionError::class.java) {
      assertThat(error).isNoVariableOrNumberAfterBinaryOperatorThat().apply {
        hasOperatorThat().isEqualTo(MathBinaryOperation.Operator.SUBTRACT)
        hasOperatorSymbolThat().isEqualTo("-")
      }
    }
  }

  @Test
  fun testMathParsingErrorSubject_isExponentIsVariableExpressionError() {
    val error = ExponentIsVariableExpressionError
    assertThat(error).isExponentIsVariableExpression()
  }

  @Test
  fun testMathParsingErrorSubject_isExponentTooLargeError() {
    val error = ExponentTooLargeError
    assertThat(error).isExponentTooLarge()
  }

  @Test
  fun testMathParsingErrorSubject_isNestedExponentsError() {
    val error = NestedExponentsError
    assertThat(error).isNestedExponents()
  }

  @Test
  fun testMathParsingErrorSubject_isHangingSquareRootError() {
    val error = HangingSquareRootError
    assertThat(error).isHangingSquareRoot()
  }

  @Test
  fun testMathParsingErrorSubject_isTermDividedByZeroError() {
    val error = TermDividedByZeroError
    assertThat(error).isTermDividedByZero()
  }

  @Test
  fun testMathParsingErrorSubject_isVariableInNumericExpressionError() {
    val error = VariableInNumericExpressionError
    assertThat(error).isVariableInNumericExpression()
  }

  @Test
  fun testMathParsingErrorSubject_isDisabledVariablesInUseWithVariablesError() {
    val error = DisabledVariablesInUseError(listOf("x", "y"))
    assertThat(error).isDisabledVariablesInUseWithVariablesThat().containsExactly("x", "y")
  }

  @Test
  fun testMathParsingErrorSubject_isDisabledVariablesInUseWithVariablesError_fails() {
    val error = DisabledVariablesInUseError(listOf("x", "y"))
    assertThrows(AssertionError::class.java) {
      assertThat(error).isDisabledVariablesInUseWithVariablesThat().containsExactly("x", "z")
    }
  }

  @Test
  fun testMathParsingErrorSubject_isEquationIsMissingEqualsError() {
    val error = EquationIsMissingEqualsError
    assertThat(error).isEquationIsMissingEquals()
  }

  @Test
  fun testMathParsingErrorSubject_isEquationHasTooManyEqualsError() {
    val error = EquationHasTooManyEqualsError
    assertThat(error).isEquationHasTooManyEquals()
  }

  @Test
  fun testMathParsingErrorSubject_isEquationMissingLhsOrRhsError() {
    val error = EquationMissingLhsOrRhsError
    assertThat(error).isEquationMissingLhsOrRhs()
  }

  @Test
  fun testMathParsingErrorSubject_isInvalidFunctionInUseWithNameError() {
    val error = InvalidFunctionInUseError("sin")
    assertThat(error).isInvalidFunctionInUseWithNameThat().isEqualTo("sin")
  }

  @Test
  fun testMathParsingErrorSubject_isFunctionNameIncompleteError() {
    val error = FunctionNameIncompleteError
    assertThat(error).isFunctionNameIncomplete()
  }

  @Test
  fun testMathParsingErrorSubject_isGenericError() {
    val error = GenericError
    assertThat(error).isGenericError()
  }
}
