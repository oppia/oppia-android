package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
import org.oppia.android.testing.math.MathEquationSubject
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
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

  @Test
  fun testHumanReadableString() {
    // TODO: split up & move to separate test suites. Finish test cases (if anymore are needed).

    val exp1 = parseNumericExpressionSuccessfully("1")
    assertThat(exp1).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    val exp2 = parseAlgebraicExpressionSuccessfully("x")
    assertThat(exp2).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    val eq1 = parseAlgebraicEquationSuccessfully("x=1")
    assertThat(eq1).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    // specific cases (from rules & other cases):
    val exp3 = parseNumericExpressionSuccessfully("1")
    assertThat(exp3).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
    assertThat(exp3).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp49 = parseNumericExpressionSuccessfully("-1")
    assertThat(exp49).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative 1")

    val exp50 = parseNumericExpressionSuccessfully("+1")
    assertThat(exp50).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive 1")

    val exp4 = parseNumericExpressionWithoutOptionalErrors("((1))")
    assertThat(exp4).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp5 = parseNumericExpressionSuccessfully("1+2")
    assertThat(exp5).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus 2")

    val exp6 = parseNumericExpressionSuccessfully("1-2")
    assertThat(exp6).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus 2")

    val exp7 = parseNumericExpressionSuccessfully("1*2")
    assertThat(exp7).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times 2")

    val exp8 = parseNumericExpressionSuccessfully("1/2")
    assertThat(exp8).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by 2")

    val exp9 = parseNumericExpressionSuccessfully("1+(1-2)")
    assertThat(exp9)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus 2 close parenthesis")

    val exp10 = parseNumericExpressionSuccessfully("2^3")
    assertThat(exp10)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of 3")

    val exp11 = parseNumericExpressionSuccessfully("2^(1+2)")
    assertThat(exp11)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of open parenthesis 1 plus 2 close parenthesis")

    val exp12 = parseNumericExpressionSuccessfully("100000*2")
    assertThat(exp12).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")

    val exp13 = parseNumericExpressionSuccessfully("sqrt(2)")
    assertThat(exp13).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp14 = parseNumericExpressionSuccessfully("√2")
    assertThat(exp14).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp15 = parseNumericExpressionSuccessfully("sqrt(1+2)")
    assertThat(exp15)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")

    val singularOrdinalNames = mapOf(
      1 to "oneth",
      2 to "half",
      3 to "third",
      4 to "fourth",
      5 to "fifth",
      6 to "sixth",
      7 to "seventh",
      8 to "eighth",
      9 to "ninth",
      10 to "tenth",
    )
    val pluralOrdinalNames = mapOf(
      1 to "oneths",
      2 to "halves",
      3 to "thirds",
      4 to "fourths",
      5 to "fifths",
      6 to "sixths",
      7 to "sevenths",
      8 to "eighths",
      9 to "ninths",
      10 to "tenths",
    )
    for (denominatorToCheck in 1..10) {
      for (numeratorToCheck in 0..denominatorToCheck) {
        val exp16 = parseNumericExpressionSuccessfully("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp17 = parseNumericExpressionSuccessfully("-1/3")
    assertThat(exp17)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 1 third")

    val exp18 = parseNumericExpressionSuccessfully("-2/3")
    assertThat(exp18)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 2 thirds")

    val exp19 = parseNumericExpressionSuccessfully("10/11")
    assertThat(exp19)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("10 over 11")

    val exp20 = parseNumericExpressionSuccessfully("121/7986")
    assertThat(exp20)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("121 over 7,986")

    val exp21 = parseNumericExpressionSuccessfully("8/7")
    assertThat(exp21)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("8 over 7")

    val exp22 = parseNumericExpressionSuccessfully("-10/-30")
    assertThat(exp22)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative the fraction with numerator 10 and denominator negative 30")

    val exp23 = parseAlgebraicExpressionSuccessfully("1")
    assertThat(exp23).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp24 = parseAlgebraicExpressionWithoutOptionalErrors("((1))")
    assertThat(exp24).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp25 = parseAlgebraicExpressionSuccessfully("x")
    assertThat(exp25).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")

    val exp26 = parseAlgebraicExpressionWithoutOptionalErrors("((x))")
    assertThat(exp26).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")

    val exp51 = parseAlgebraicExpressionSuccessfully("-x")
    assertThat(exp51).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative x")

    val exp52 = parseAlgebraicExpressionSuccessfully("+x")
    assertThat(exp52).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive x")

    val exp27 = parseAlgebraicExpressionSuccessfully("1+x")
    assertThat(exp27).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus x")

    val exp28 = parseAlgebraicExpressionSuccessfully("1-x")
    assertThat(exp28).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus x")

    val exp29 = parseAlgebraicExpressionSuccessfully("1*x")
    assertThat(exp29).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times x")

    val exp30 = parseAlgebraicExpressionSuccessfully("1/x")
    assertThat(exp30).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by x")

    val exp31 = parseAlgebraicExpressionSuccessfully("1/x")
    assertThat(exp31)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("the fraction with numerator 1 and denominator x")

    val exp32 = parseAlgebraicExpressionSuccessfully("1+(1-x)")
    assertThat(exp32)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus x close parenthesis")

    val exp33 = parseAlgebraicExpressionSuccessfully("2x")
    assertThat(exp33).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x")

    val exp34 = parseAlgebraicExpressionSuccessfully("xy")
    assertThat(exp34).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x times y")

    val exp35 = parseAlgebraicExpressionSuccessfully("z")
    assertThat(exp35).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("zed")

    val exp36 = parseAlgebraicExpressionSuccessfully("2xz")
    assertThat(exp36).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x times zed")

    val exp37 = parseAlgebraicExpressionSuccessfully("x^2")
    assertThat(exp37)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of 2")

    val exp38 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1+x)")
    assertThat(exp38)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of open parenthesis 1 plus x close parenthesis")

    val exp39 = parseAlgebraicExpressionSuccessfully("100000*2")
    assertThat(exp39).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")

    val exp40 = parseAlgebraicExpressionSuccessfully("sqrt(2)")
    assertThat(exp40).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp41 = parseAlgebraicExpressionSuccessfully("sqrt(x)")
    assertThat(exp41).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")

    val exp42 = parseAlgebraicExpressionSuccessfully("√2")
    assertThat(exp42).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp43 = parseAlgebraicExpressionSuccessfully("√x")
    assertThat(exp43).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")

    val exp44 = parseAlgebraicExpressionSuccessfully("sqrt(1+2)")
    assertThat(exp44)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")

    val exp45 = parseAlgebraicExpressionSuccessfully("sqrt(1+x)")
    assertThat(exp45)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus x end square root")

    val exp46 = parseAlgebraicExpressionSuccessfully("√(1+x)")
    assertThat(exp46)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis 1 plus x close parenthesis end square root")

    for (denominatorToCheck in 1..10) {
      for (numeratorToCheck in 0..denominatorToCheck) {
        val exp16 = parseAlgebraicExpressionSuccessfully("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp47 = parseAlgebraicExpressionSuccessfully("1")
    assertThat(exp47).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp48 = parseAlgebraicExpressionSuccessfully("x(5-y)")
    assertThat(exp48)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x times open parenthesis 5 minus y close parenthesis")

    val eq2 = parseAlgebraicEquationSuccessfully("x=1/y")
    assertThat(eq2)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by y")

    val eq3 = parseAlgebraicEquationSuccessfully("x=1/2")
    assertThat(eq3)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by 2")

    val eq4 = parseAlgebraicEquationSuccessfully("x=1/y")
    assertThat(eq4)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals the fraction with numerator 1 and denominator y")

    val eq5 = parseAlgebraicEquationSuccessfully("x=1/2")
    assertThat(eq5)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals 1 half")

    // Tests from examples in the PRD
    val eq6 = parseAlgebraicEquationSuccessfully("3x^2+4y=62")
    assertThat(eq6)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 x raised to the power of 2 plus 4 y equals 62")

    val exp53 = parseAlgebraicExpressionSuccessfully("(x+6)/(x-4)")
    assertThat(exp53)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo(
        "the fraction with numerator open parenthesis x plus 6 close parenthesis and denominator" +
          " open parenthesis x minus 4 close parenthesis"
      )

    val exp54 = parseAlgebraicExpressionWithoutOptionalErrors("4*(x)^(2)+20x")
    assertThat(exp54)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("4 times x raised to the power of 2 plus 20 x")

    val exp55 = parseAlgebraicExpressionSuccessfully("3+x-5")
    assertThat(exp55).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("3 plus x minus 5")

    val exp56 = parseAlgebraicExpressionSuccessfully("Z+A-Z", allowedVariables = listOf("A", "Z"))
    assertThat(exp56).forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("Zed plus A minus Zed")

    val exp57 =
      parseAlgebraicExpressionSuccessfully("6C-5A-1", allowedVariables = listOf("A", "C"))
    assertThat(exp57)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("6 C minus 5 A minus 1")

    val exp58 = parseAlgebraicExpressionSuccessfully("5*Z-w", allowedVariables = listOf("Z", "w"))
    assertThat(exp58)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("5 times Zed minus w")

    val exp59 =
      parseAlgebraicExpressionSuccessfully("L*S-3S+L", allowedVariables = listOf("L", "S"))
    assertThat(exp59)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("L times S minus 3 S plus L")

    val exp60 = parseAlgebraicExpressionSuccessfully("2*(2+6+3+4)")
    assertThat(exp60)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 times open parenthesis 2 plus 6 plus 3 plus 4 close parenthesis")

    val exp61 = parseAlgebraicExpressionSuccessfully("sqrt(64)")
    assertThat(exp61)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("square root of 64")

    val exp62 = parseAlgebraicExpressionSuccessfully("√(a+b)", allowedVariables = listOf("a", "b"))
    assertThat(exp62)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis a plus b close parenthesis end square root")

    val exp63 = parseAlgebraicExpressionSuccessfully("3*10^-5")
    assertThat(exp63)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 times 10 raised to the power of negative 5")

    val exp64 =
      parseAlgebraicExpressionWithoutOptionalErrors(
        "((x+2y)+5*(a-2b)+z)", allowedVariables = listOf("x", "y", "a", "b", "z")
      )
    assertThat(exp64)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo(
        "open parenthesis open parenthesis x plus 2 y close parenthesis plus 5 times open" +
          " parenthesis a minus 2 b close parenthesis plus zed close parenthesis"
      )
  }

  // TODO: move these to MathExpressionSubject
  fun MathExpressionSubject.forHumanReadable(language: OppiaLanguage): HumanReadableStringChecker =
    HumanReadableStringChecker(language, actual::toHumanReadableString)

  // TODO: move these to MathEquationSubject
  fun MathEquationSubject.forHumanReadable(language: OppiaLanguage): HumanReadableStringChecker =
    HumanReadableStringChecker(language, actual::toHumanReadableString)

  class HumanReadableStringChecker(
    private val language: OppiaLanguage,
    private val maybeConvertToHumanReadableString: (OppiaLanguage, Boolean) -> String?
  ) {
    fun convertsToStringThat(): StringSubject =
      assertThat(convertToHumanReadableString(language, /* divAsFraction= */ false))

    fun convertsWithFractionsToStringThat(): StringSubject =
      assertThat(convertToHumanReadableString(language, /* divAsFraction= */ true))

    fun doesNotConvertToString() {
      assertWithMessage("Expected to not convert to: $language")
        .that(maybeConvertToHumanReadableString(language, /* divAsFraction= */ false))
        .isNull()
    }

    private fun convertToHumanReadableString(
      language: OppiaLanguage,
      divAsFraction: Boolean
    ): String {
      val readableString = maybeConvertToHumanReadableString(language, divAsFraction)
      assertWithMessage("Expected to convert to: $language").that(readableString).isNotNull()
      return checkNotNull(readableString) // Verified in the above assertion check.
    }
  }

  private companion object {
    // TODO: fix helper API.

    private fun parseNumericExpressionWithoutOptionalErrors(expression: String): MathExpression {
      return (
        parseNumericExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY
        ) as MathParsingResult.Success<MathExpression>
        ).result
    }

    private fun parseNumericExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }

    private fun parseAlgebraicExpressionWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return (
        parseAlgebraicExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables
        ) as MathParsingResult.Success<MathExpression>
        ).result
    }

    private fun parseAlgebraicExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      )
    }

    // TODO: ...

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

    private fun parseAlgebraicEquationSuccessfully(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      val result = parseAlgebraicEquationWithAllErrors(expression, allowedVariables)
      return (result as MathParsingResult.Success<MathEquation>).result
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
