package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathParsingError.DisabledVariablesInUseError
import org.oppia.android.util.math.MathParsingError.EquationHasWrongNumberOfEqualsError
import org.oppia.android.util.math.MathParsingError.EquationMissingLhsOrRhsError
import org.oppia.android.util.math.MathParsingError.ExponentIsVariableExpressionError
import org.oppia.android.util.math.MathParsingError.ExponentTooLargeError
import org.oppia.android.util.math.MathParsingError.FunctionNameIncompleteError
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
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionParserTest {
  // TODO: add high-level checks for the three types, but don't test in detail since there are
  //  separate suites. Also, document the separate suites' existence in this suites's KDoc.

  @Test
  fun testErrorCases() {
    // TODO: split up.
    val failure1 = expectFailureWhenParsingNumericExpression("73 2")
    assertThat(failure1).isEqualTo(SpacesBetweenNumbersError)

    val failure2 = expectFailureWhenParsingNumericExpression("(73")
    assertThat(failure2).isEqualTo(UnbalancedParenthesesError)

    val failure3 = expectFailureWhenParsingNumericExpression("73)")
    assertThat(failure3).isEqualTo(UnbalancedParenthesesError)

    val failure4 = expectFailureWhenParsingNumericExpression("((73)")
    assertThat(failure4).isEqualTo(UnbalancedParenthesesError)

    val failure5 = expectFailureWhenParsingNumericExpression("73 (")
    assertThat(failure5).isEqualTo(UnbalancedParenthesesError)

    val failure6 = expectFailureWhenParsingNumericExpression("73 )")
    assertThat(failure6).isEqualTo(UnbalancedParenthesesError)

    val failure7 = expectFailureWhenParsingNumericExpression("sqrt(73")
    assertThat(failure7).isEqualTo(UnbalancedParenthesesError)

    // TODO: test properties on errors (& add better testing library for errors, or at least helpers).
    val failure8 = expectFailureWhenParsingNumericExpression("(7 * 2 + 4)")
    assertThat(failure8).isInstanceOf(SingleRedundantParenthesesError::class.java)

    val failure9 = expectFailureWhenParsingNumericExpression("((5 + 4))")
    assertThat(failure9).isInstanceOf(MultipleRedundantParenthesesError::class.java)

    val failure13 = expectFailureWhenParsingNumericExpression("(((5 + 4)))")
    assertThat(failure13).isInstanceOf(MultipleRedundantParenthesesError::class.java)

    val failure14 = expectFailureWhenParsingNumericExpression("1+((5 + 4))")
    assertThat(failure14).isInstanceOf(MultipleRedundantParenthesesError::class.java)

    val failure15 = expectFailureWhenParsingNumericExpression("1+(7*((( 9  + 3) )))")
    assertThat(failure15).isInstanceOf(MultipleRedundantParenthesesError::class.java)
    assertThat((failure15 as MultipleRedundantParenthesesError).rawExpression)
      .isEqualTo("(( 9  + 3) )")

    parseNumericExpressionSuccessfully("1+(5+4)")
    parseNumericExpressionSuccessfully("(5+4)+1")

    val failure10 = expectFailureWhenParsingNumericExpression("(5) + 4")
    assertThat(failure10).isInstanceOf(RedundantParenthesesForIndividualTermsError::class.java)

    val failure11 = expectFailureWhenParsingNumericExpression("5^(2)")
    assertThat(failure11).isInstanceOf(RedundantParenthesesForIndividualTermsError::class.java)
    assertThat((failure11 as RedundantParenthesesForIndividualTermsError).rawExpression)
      .isEqualTo("2")

    val failure12 = expectFailureWhenParsingNumericExpression("sqrt((2))")
    assertThat(failure12).isInstanceOf(RedundantParenthesesForIndividualTermsError::class.java)

    val failure16 = expectFailureWhenParsingNumericExpression("$2")
    assertThat(failure16).isInstanceOf(UnnecessarySymbolsError::class.java)
    assertThat((failure16 as UnnecessarySymbolsError).invalidSymbol).isEqualTo("$")

    val failure17 = expectFailureWhenParsingNumericExpression("5%")
    assertThat(failure17).isInstanceOf(UnnecessarySymbolsError::class.java)
    assertThat((failure17 as UnnecessarySymbolsError).invalidSymbol).isEqualTo("%")

    val failure18 = expectFailureWhenParsingAlgebraicExpression("x5")
    assertThat(failure18).isInstanceOf(NumberAfterVariableError::class.java)
    assertThat((failure18 as NumberAfterVariableError).number.integer).isEqualTo(5)
    assertThat(failure18.variable).isEqualTo("x")

    val failure19 = expectFailureWhenParsingAlgebraicExpression("2+y 3.14*7")
    assertThat(failure19).isInstanceOf(NumberAfterVariableError::class.java)
    assertThat((failure19 as NumberAfterVariableError).number.irrational).isWithin(1e-5).of(3.14)
    assertThat(failure19.variable).isEqualTo("y")

    // TODO: expand to multiple tests or use parametrized tests.
    // RHS operators don't result in unary operations (which are valid in the grammar).
    val rhsOperators = listOf("*", "×", "/", "÷", "^")
    val lhsOperators = rhsOperators + listOf("+", "-", "−")
    val operatorCombinations = lhsOperators.flatMap { op1 -> rhsOperators.map { op1 to it } }
    for ((op1, op2) in operatorCombinations) {
      val failure22 = expectFailureWhenParsingNumericExpression(expression = "1 $op1$op2 2")
      assertThat(failure22).isInstanceOf(SubsequentBinaryOperatorsError::class.java)
      assertThat((failure22 as SubsequentBinaryOperatorsError).operator1).isEqualTo(op1)
      assertThat(failure22.operator2).isEqualTo(op2)
    }

    val failure37 = expectFailureWhenParsingNumericExpression("++2")
    assertThat(failure37).isInstanceOf(SubsequentUnaryOperatorsError::class.java)

    val failure38 = expectFailureWhenParsingAlgebraicExpression("--x")
    assertThat(failure38).isInstanceOf(SubsequentUnaryOperatorsError::class.java)

    val failure39 = expectFailureWhenParsingAlgebraicExpression("-+x")
    assertThat(failure39).isInstanceOf(SubsequentUnaryOperatorsError::class.java)

    val failure40 = expectFailureWhenParsingNumericExpression("+-2")
    assertThat(failure40).isInstanceOf(SubsequentUnaryOperatorsError::class.java)

    parseNumericExpressionSuccessfully("2++3") // Will succeed since it's 2 + (+2).
    val failure41 = expectFailureWhenParsingNumericExpression("2+++3")
    assertThat(failure41).isInstanceOf(SubsequentUnaryOperatorsError::class.java)

    val failure23 = expectFailureWhenParsingNumericExpression("/2")
    assertThat(failure23).isInstanceOf(NoVariableOrNumberBeforeBinaryOperatorError::class.java)
    assertThat((failure23 as NoVariableOrNumberBeforeBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.DIVIDE)

    val failure24 = expectFailureWhenParsingAlgebraicExpression("*x")
    assertThat(failure24).isInstanceOf(NoVariableOrNumberBeforeBinaryOperatorError::class.java)
    assertThat((failure24 as NoVariableOrNumberBeforeBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.MULTIPLY)

    val failure27 = expectFailureWhenParsingNumericExpression("2^")
    assertThat(failure27).isInstanceOf(NoVariableOrNumberAfterBinaryOperatorError::class.java)
    assertThat((failure27 as NoVariableOrNumberAfterBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.EXPONENTIATE)

    val failure25 = expectFailureWhenParsingNumericExpression("2/")
    assertThat(failure25).isInstanceOf(NoVariableOrNumberAfterBinaryOperatorError::class.java)
    assertThat((failure25 as NoVariableOrNumberAfterBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.DIVIDE)

    val failure26 = expectFailureWhenParsingAlgebraicExpression("x*")
    assertThat(failure26).isInstanceOf(NoVariableOrNumberAfterBinaryOperatorError::class.java)
    assertThat((failure26 as NoVariableOrNumberAfterBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.MULTIPLY)

    val failure28 = expectFailureWhenParsingAlgebraicExpression("x+")
    assertThat(failure28).isInstanceOf(NoVariableOrNumberAfterBinaryOperatorError::class.java)
    assertThat((failure28 as NoVariableOrNumberAfterBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.ADD)

    val failure29 = expectFailureWhenParsingAlgebraicExpression("x-")
    assertThat(failure29).isInstanceOf(NoVariableOrNumberAfterBinaryOperatorError::class.java)
    assertThat((failure29 as NoVariableOrNumberAfterBinaryOperatorError).operator)
      .isEqualTo(MathBinaryOperation.Operator.SUBTRACT)

    val failure42 = expectFailureWhenParsingAlgebraicExpression("2^x")
    assertThat(failure42).isInstanceOf(ExponentIsVariableExpressionError::class.java)

    val failure43 = expectFailureWhenParsingAlgebraicExpression("2^(1+x)")
    assertThat(failure43).isInstanceOf(ExponentIsVariableExpressionError::class.java)

    val failure44 = expectFailureWhenParsingAlgebraicExpression("2^3^x")
    assertThat(failure44).isInstanceOf(ExponentIsVariableExpressionError::class.java)

    val failure45 = expectFailureWhenParsingAlgebraicExpression("2^sqrt(x)")
    assertThat(failure45).isInstanceOf(ExponentIsVariableExpressionError::class.java)

    val failure46 = expectFailureWhenParsingNumericExpression("2^7")
    assertThat(failure46).isInstanceOf(ExponentTooLargeError::class.java)

    val failure47 = expectFailureWhenParsingNumericExpression("2^30.12")
    assertThat(failure47).isInstanceOf(ExponentTooLargeError::class.java)

    parseNumericExpressionSuccessfully("2^3")

    val failure48 = expectFailureWhenParsingNumericExpression("2^3^2")
    assertThat(failure48).isInstanceOf(NestedExponentsError::class.java)

    val failure49 = expectFailureWhenParsingAlgebraicExpression("x^2^5")
    assertThat(failure49).isInstanceOf(NestedExponentsError::class.java)

    val failure20 = expectFailureWhenParsingNumericExpression("2√")
    assertThat(failure20).isInstanceOf(HangingSquareRootError::class.java)

    val failure50 = expectFailureWhenParsingNumericExpression("2/0")
    assertThat(failure50).isInstanceOf(TermDividedByZeroError::class.java)

    val failure51 = expectFailureWhenParsingAlgebraicExpression("x/0")
    assertThat(failure51).isInstanceOf(TermDividedByZeroError::class.java)

    val failure52 = expectFailureWhenParsingNumericExpression("sqrt(2+7/0.0)")
    assertThat(failure52).isInstanceOf(TermDividedByZeroError::class.java)

    val failure21 = expectFailureWhenParsingNumericExpression("x+y")
    assertThat(failure21).isInstanceOf(VariableInNumericExpressionError::class.java)

    val failure53 = expectFailureWhenParsingAlgebraicExpression("x+y+a")
    assertThat(failure53).isInstanceOf(DisabledVariablesInUseError::class.java)
    assertThat((failure53 as DisabledVariablesInUseError).variables).containsExactly("a")

    val failure54 = expectFailureWhenParsingAlgebraicExpression("apple")
    assertThat(failure54).isInstanceOf(DisabledVariablesInUseError::class.java)
    assertThat((failure54 as DisabledVariablesInUseError).variables)
      .containsExactly("a", "p", "l", "e")

    val failure55 =
      expectFailureWhenParsingAlgebraicExpression("apple", allowedVariables = listOf("a", "p", "l"))
    assertThat(failure55).isInstanceOf(DisabledVariablesInUseError::class.java)
    assertThat((failure55 as DisabledVariablesInUseError).variables).containsExactly("e")

    parseAlgebraicExpressionSuccessfully("x+y+z")

    val failure56 =
      expectFailureWhenParsingAlgebraicExpression("x+y+z", allowedVariables = listOf())
    assertThat(failure56).isInstanceOf(DisabledVariablesInUseError::class.java)
    assertThat((failure56 as DisabledVariablesInUseError).variables).containsExactly("x", "y", "z")

    val failure30 = expectFailureWhenParsingAlgebraicEquation("x==2")
    assertThat(failure30).isInstanceOf(EquationHasWrongNumberOfEqualsError::class.java)

    val failure31 = expectFailureWhenParsingAlgebraicEquation("x=2=y")
    assertThat(failure31).isInstanceOf(EquationHasWrongNumberOfEqualsError::class.java)

    val failure32 = expectFailureWhenParsingAlgebraicEquation("x=2=")
    assertThat(failure32).isInstanceOf(EquationHasWrongNumberOfEqualsError::class.java)

    val failure33 = expectFailureWhenParsingAlgebraicEquation("x=")
    assertThat(failure33).isInstanceOf(EquationMissingLhsOrRhsError::class.java)

    val failure34 = expectFailureWhenParsingAlgebraicEquation("=x")
    assertThat(failure34).isInstanceOf(EquationMissingLhsOrRhsError::class.java)

    val failure35 = expectFailureWhenParsingAlgebraicEquation("=x")
    assertThat(failure35).isInstanceOf(EquationMissingLhsOrRhsError::class.java)

    // TODO: expand to multiple tests or use parametrized tests.
    val prohibitedFunctionNames =
      listOf(
        "exp", "log", "log10", "ln", "sin", "cos", "tan", "cot", "csc", "sec", "atan", "asin",
        "acos", "abs"
      )
    for (functionName in prohibitedFunctionNames) {
      val failure36 = expectFailureWhenParsingAlgebraicEquation("$functionName(0.5)")
      assertThat(failure36).isInstanceOf(InvalidFunctionInUseError::class.java)
      assertThat((failure36 as InvalidFunctionInUseError).functionName).isEqualTo(functionName)
    }

    val failure57 = expectFailureWhenParsingAlgebraicExpression("sq")
    assertThat(failure57).isInstanceOf(FunctionNameIncompleteError::class.java)

    val failure58 = expectFailureWhenParsingAlgebraicExpression("sqr")
    assertThat(failure58).isInstanceOf(FunctionNameIncompleteError::class.java)

    // TODO: Other cases: sqrt(, sqrt(), sqrt 2, +2
  }

  private companion object {
    // TODO: fix helper API.

    private fun expectFailureWhenParsingNumericExpression(expression: String): MathParsingError {
      val result = parseNumericExpressionWithAllErrors(expression)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathExpression>).error
    }

    private fun parseNumericExpressionSuccessfully(expression: String): MathExpression {
      val result = parseNumericExpressionWithAllErrors(expression)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionWithAllErrors(
      expression: String
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, ErrorCheckingMode.ALL_ERRORS)
    }

    private fun expectFailureWhenParsingAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingError {
      val result =
        parseAlgebraicExpressionWithAllErrors(expression, allowedVariables)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathExpression>).error
    }

    private fun parseAlgebraicExpressionSuccessfully(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result = parseAlgebraicExpressionWithAllErrors(expression, allowedVariables)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, ErrorCheckingMode.ALL_ERRORS
      )
    }

    private fun expectFailureWhenParsingAlgebraicEquation(expression: String): MathParsingError {
      val result = parseAlgebraicEquationWithAllErrors(expression)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathEquation>).error
    }

    private fun parseAlgebraicEquationWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathEquation> {
      return MathExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables, ErrorCheckingMode.ALL_ERRORS
      )
    }
  }
}
