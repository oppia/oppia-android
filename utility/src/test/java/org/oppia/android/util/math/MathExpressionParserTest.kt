package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.MathParsingErrorSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/**
 * Tests for [MathExpressionParser].
 *
 * Note that while this verifies that, at a high level, numeric expressions, algebraic expressions,
 * and algebraic equations can be successfully parsed, it mainly focuses on errors (and some passing
 * cases that closely relate to possible errors).
 *
 * Further, this mainly relies on numeric expressions to ensure error detection works since it's
 * assumed that algebraic equations rely on algebraic expressions, and algebraic expressions rely on
 * numeric expressions.
 *
 * Finally, there are dedicated test suites for each of numeric expressions
 * [NumericExpressionParserTest], algebraic expressions [AlgebraicExpressionParserTest], and
 * algebraic equations [AlgebraicEquationParserTest].
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionParserTest {
  @Parameter lateinit var lhsOp: String
  @Parameter lateinit var rhsOp: String
  @Parameter lateinit var binOp: String
  @Parameter lateinit var subExp: String
  @Parameter lateinit var func: String

  @Test
  fun testParseNumExp_basicExpression_doesNotFail() {
    expectSuccessWhenParsingNumericExpression("1 + 2")
  }

  @Test
  fun testParseAlgExp_basicExpression_doesNotFail() {
    expectSuccessWhenParsingAlgebraicExpression("x + y")
  }

  @Test
  fun testParseAlgebraicEquation_basicEquation_doesNotFail() {
    expectSuccessWhenParsingAlgebraicEquation("y = 2x + 3")
  }

  @Test
  fun testParseNumExp_emptyExpression_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("")

    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_numbersWithSpaces_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("73 2")

    // Numbers cannot be implicitly multiplied unless they are in groups.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_spaceBetweenNegatedAndRegularNumber_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("-2 3")

    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_numberAndExponentSeparatedBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("2 2^2")

    // Unless a similar algebraic expression (e.g. 2x^2), this is not valid.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_squareRootAndExponentSeparatedBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("√2 2^2")

    // Ensure the square root special case doesn't change the implicit multiplication rule for
    // numbers.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_exponentAndNumberSeparatedBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("2^2 2")

    // Right implicit multiplication for numbers is never allowed.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_twoExponentsSeparatedBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingNumericExpression("2^2 3^2")

    // Subsequent exponents are never implicitly multiplied for numeric expressions.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseAlgExp_numberAndVariableBaseExponentSeparatedBySpace_doesNotFail() {
    // Implicit multiplication with numbers on the left is allowed if the right is a variable raised
    // to a power (in order to support polynomial syntax).
    expectSuccessWhenParsingAlgebraicExpression("2 x^2")
  }

  @Test
  fun testParseAlgExp_varBaseExponentAndNumberSeparatedBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingAlgebraicExpression("x^2 2")

    // Right implicit multiplication for numbers is never allowed.
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseAlgExp_twoAdjacentVariableBaseExponentsSeparatedBySpace_doesNotFail() {
    // Similarly, this is supported for polynomial syntax.
    expectSuccessWhenParsingAlgebraicExpression("x^2 y^2")
  }

  @Test
  fun testParseAlgExp_twoAdjacentNumericExponentsSepBySpace_returnsSpacesBetweenNumbersError() {
    val error = expectFailureWhenParsingAlgebraicExpression("2^2 3^2")

    // While the variable version of this works, the numeric does not (explicit multiplication is
    // required).
    assertThat(error).isSpacesBetweenNumbers()
  }

  @Test
  fun testParseNumExp_leftParenAndNumber_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("(73")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_numberAndRightParen_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("73)")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_numberGroup_extraLeftParen_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("((73)")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_number_hangingLeftParen_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("73 (")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_number_hangingRightParen_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("73 )")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_sqrt_missingRightParen_returnsUnbalancedParenthesesError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt(73")

    assertThat(error).isUnbalancedParentheses()
  }

  @Test
  fun testParseNumExp_outerExpressionGrouped_returnsRedundantParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("(7 * 2 + 4)")

    assertThat(error).isSingleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(7 * 2 + 4)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          addition {
            leftOperand {
              multiplication {
                leftOperand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(7)
                  }
                }
                rightOperand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_leftExpInRightNumericImplicitMult_returnsRedundantParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("(1 + 2)2")

    // Note that this error occurs because the '2' is considered an extra token in the token stream,
    // so only '(1 + 2)' is parsed.
    assertThat(error).isSingleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(1 + 2)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          addition {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(1)
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_rightExpInLeftNumericImplicitMult_doesNotFail() {
    // As compared with the above, implicit left multiplication is supported with numbers when
    // parentheses are used.
    expectSuccessWhenParsingNumericExpression("2(1 + 2)")
  }

  @Test
  fun testParseNumExp_singleVarTermInImplicitExpMult_returnsRedundantParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("(3) 2^2")

    // Note that this error occurs because the '2^2' is considered extra tokens in the token stream,
    // so only '(3)' is parsed.
    assertThat(error).isSingleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(3)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_outerExpressionGrouped_optionalErrorsDisabled_doesNotFail() {
    // This won't fail if optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("(7 * 2 + 4)", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_everythingDoubleParens_returnsMultiParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("((5 + 4))")

    assertThat(error).isMultipleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(5 + 4)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          addition {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(5)
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_everythingTripleParens_returnsMultiParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("(((5 + 4)))")

    assertThat(error).isMultipleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("((5 + 4))")
      hasExpressionThat().hasStructureThatMatches {
        group {
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(5)
                }
              }
              rightOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_expWithDoubleParens_returnsMultiParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("1+((5 + 4))")

    assertThat(error).isMultipleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(5 + 4)")
      hasExpressionThat().hasStructureThatMatches {
        group {
          addition {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(5)
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_expWithTripleParens_returnsMultiParensErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("1+(7*((( 9  + 3) )))")

    assertThat(error).isMultipleRedundantParenthesesThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("(( 9  + 3) )")
      hasExpressionThat().hasStructureThatMatches {
        group {
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(9)
                }
              }
              rightOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParseNumExp_expWithTripleParens_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger an error when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression(
      "1+(7*((( 9  + 3) )))", errorCheckingMode = REQUIRED_ONLY
    )
  }

  @Test
  fun testParseNumExp_innerExpWithSingleParens_onRight_doesNotFail() {
    // Succeeds because the right parenthetical term is complex and is part of an outer expression.
    expectSuccessWhenParsingNumericExpression("1+(5+4)")
  }

  @Test
  fun testParseNumExp_innerExpWithSingleParens_onLeft_doesNotFail() {
    // Succeeds because the left parenthetical term is complex and is part of an outer expression.
    expectSuccessWhenParsingNumericExpression("(5+4)+1")
  }

  @Test
  fun testParseNumExp_numberSingleParen_onLeft_returnsSingleTermParenErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("(5) + 4")

    assertThat(error).isRedundantIndividualTermsParensThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("5")
      hasExpressionThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(5)
        }
      }
    }
  }

  @Test
  fun testParseNumExp_numberSingleParen_onRight_returnsSingleTermParenErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("5^(2)")

    assertThat(error).isRedundantIndividualTermsParensThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("2")
      hasExpressionThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testParseNumExp_numberSingleParen_sqrt_returnsSingleTermParenErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("sqrt((2))")

    assertThat(error).isRedundantIndividualTermsParensThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("2")
      hasExpressionThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testParseNumExp_numSingleParen_betweenImplicitMult_returnsSingleTermParenErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("2^3(4)2^3")

    // While parentheses can enable implicit multiplication with numbers, due to exponents never
    // being valid right implicit multiplication operands (for numeric expressions) the above is
    // considered invalid (plus the '4' is by itself without anything else being in the group).
    assertThat(error).isRedundantIndividualTermsParensThat().apply {
      // The valid sub-expression should be captured as part of the error.
      hasRawExpressionThat().isEqualTo("4")
      hasExpressionThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(4)
        }
      }
    }
  }

  @Test
  fun testParseNumExp_numberSingleParen_sqrt_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger an error when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("sqrt((2))", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_dollarSign_returnsUnnecessarySymbolErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("$2")

    assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("$")
  }

  @Test
  fun testParseNumExp_exclamation_returnsUnnecessarySymbolErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("5!")

    assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("!")
  }

  @Test
  fun testParseAlgExp_unexpectedAmpersand_returnsUnnecessarySymbolErrorWithDetails() {
    val error = expectFailureWhenParsingAlgebraicExpression("1+2 &xyz")

    assertThat(error).isUnnecessarySymbolWithSymbolThat().isEqualTo("&")
  }

  @Test
  fun testParseAlgExp_numberRightOfVar_returnsNumberAfterVariableErrorWithDetails() {
    val error = expectFailureWhenParsingAlgebraicExpression("x5")

    assertThat(error).isNumberAfterVariableThat().apply {
      hasNumberThat().isIntegerThat().isEqualTo(5)
      hasVariableThat().isEqualTo("x")
    }
  }

  @Test
  fun testParseAlgExp_expRightOfVar_returnsNumberAfterVariableErrorWithDetails() {
    val error = expectFailureWhenParsingAlgebraicExpression("2+y 3.14*7")

    assertThat(error).isNumberAfterVariableThat().apply {
      hasNumberThat().isIrrationalThat().isWithin(1e-5).of(3.14)
      hasVariableThat().isEqualTo("y")
    }
  }

  @Test
  fun testParseAlgEq_numberRightOfVar_leftSide_returnsNumberAfterVariableErrorWithDetails() {
    val error = expectFailureWhenParsingAlgebraicEquation("y 2 = (x+1)(x-1)")

    assertThat(error).isNumberAfterVariableThat().apply {
      hasNumberThat().isIntegerThat().isEqualTo(2)
      hasVariableThat().isEqualTo("y")
    }
  }

  // Note that these parameters are intentionally set up to avoid double unary operators (such as
  // -- or ++) since those result in different errors due to unary operations being higher
  // precedence. In general, unary operators can't appear on the right since they'll be treated as
  // such.
  @Test
  @Iteration("**", "lhsOp=*", "rhsOp=*")
  @Iteration("×*", "lhsOp=×", "rhsOp=*")
  @Iteration("/*", "lhsOp=/", "rhsOp=*")
  @Iteration("÷*", "lhsOp=÷", "rhsOp=*")
  @Iteration("^*", "lhsOp=^", "rhsOp=*")
  @Iteration("+*", "lhsOp=+", "rhsOp=*")
  @Iteration("-*", "lhsOp=-", "rhsOp=*")
  @Iteration("−*", "lhsOp=−", "rhsOp=*")
  @Iteration("–*", "lhsOp=–", "rhsOp=*")
  @Iteration("*×", "lhsOp=*", "rhsOp=×")
  @Iteration("××", "lhsOp=×", "rhsOp=×")
  @Iteration("/×", "lhsOp=/", "rhsOp=×")
  @Iteration("÷×", "lhsOp=÷", "rhsOp=×")
  @Iteration("^×", "lhsOp=^", "rhsOp=×")
  @Iteration("+×", "lhsOp=+", "rhsOp=×")
  @Iteration("-×", "lhsOp=-", "rhsOp=×")
  @Iteration("−×", "lhsOp=−", "rhsOp=×")
  @Iteration("–×", "lhsOp=–", "rhsOp=×")
  @Iteration("*/", "lhsOp=*", "rhsOp=/")
  @Iteration("×/", "lhsOp=×", "rhsOp=/")
  @Iteration("//", "lhsOp=/", "rhsOp=/")
  @Iteration("÷/", "lhsOp=÷", "rhsOp=/")
  @Iteration("^/", "lhsOp=^", "rhsOp=/")
  @Iteration("+/", "lhsOp=+", "rhsOp=/")
  @Iteration("-/", "lhsOp=-", "rhsOp=/")
  @Iteration("−/", "lhsOp=−", "rhsOp=/")
  @Iteration("–/", "lhsOp=–", "rhsOp=/")
  @Iteration("*÷", "lhsOp=*", "rhsOp=÷")
  @Iteration("×÷", "lhsOp=×", "rhsOp=÷")
  @Iteration("/÷", "lhsOp=/", "rhsOp=÷")
  @Iteration("÷÷", "lhsOp=÷", "rhsOp=÷")
  @Iteration("^÷", "lhsOp=^", "rhsOp=÷")
  @Iteration("+÷", "lhsOp=+", "rhsOp=÷")
  @Iteration("-÷", "lhsOp=-", "rhsOp=÷")
  @Iteration("−÷", "lhsOp=−", "rhsOp=÷")
  @Iteration("–÷", "lhsOp=–", "rhsOp=÷")
  @Iteration("*^", "lhsOp=*", "rhsOp=^")
  @Iteration("×^", "lhsOp=×", "rhsOp=^")
  @Iteration("/^", "lhsOp=/", "rhsOp=^")
  @Iteration("÷^", "lhsOp=÷", "rhsOp=^")
  @Iteration("^^", "lhsOp=^", "rhsOp=^")
  @Iteration("+^", "lhsOp=+", "rhsOp=^")
  @Iteration("-^", "lhsOp=-", "rhsOp=^")
  @Iteration("−^", "lhsOp=−", "rhsOp=^")
  @Iteration("–^", "lhsOp=–", "rhsOp=^")
  fun testParseNumExp_adjacentBinaryOps_returnsSubsequentBinaryOperatorsErrorWithDetails() {
    val expression = "1 $lhsOp$rhsOp 2"

    val error = expectFailureWhenParsingNumericExpression(expression)

    // Generally, two adjacent binary operators is an error since a value is expected between them.
    assertThat(error).isSubsequentBinaryOperatorsThat().apply {
      hasFirstOperatorThat().isEqualTo(lhsOp)
      hasSecondOperatorThat().isEqualTo(rhsOp)
    }
  }

  @Test
  fun testParseNumExp_doubleUnaryPlus_returnsSubsequentUnaryOperatorsError() {
    val error = expectFailureWhenParsingNumericExpression("++2")

    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testParseNumExp_doubleUnaryMinus_returnsSubsequentUnaryOperatorsError() {
    val error = expectFailureWhenParsingAlgebraicExpression("--x")

    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testParseNumExp_unaryMinusPlus_returnsSubsequentUnaryOperatorsError() {
    val error = expectFailureWhenParsingAlgebraicExpression("-+x")

    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testParseNumExp_unaryPlusMinus_returnsSubsequentUnaryOperatorsError() {
    val error = expectFailureWhenParsingNumericExpression("+-2")

    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testParseNumExp_twoMinuses_doesNotFail() {
    expectSuccessWhenParsingNumericExpression("2--3") // Will succeed since it's 2 - (-2).
  }

  @Test
  fun testParseNumExp_threeMinuses_returnsSubsequentUnaryOperatorsError() {
    val error = expectFailureWhenParsingNumericExpression("2---3")

    // The above results in this error since it's effectively "2 - (--3)", where the "--3" is
    // invalid.
    assertThat(error).isSubsequentUnaryOperators()
  }

  @Test
  fun testParseNumExp_threeMinuses_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("2---3", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  // Note that unary operators like '+' and '-' are excluded here since they may result in valid
  // unary operations.
  @Iteration("nothing_times_something_asterisk", "binOp=*")
  @Iteration("nothing_times_something", "binOp=×")
  @Iteration("nothing_divides_something_slash", "binOp=/")
  @Iteration("nothing_divides_something", "binOp=÷")
  @Iteration("nothing_to_power_of_something", "binOp=^")
  fun testParseNumExp_binOnlyOps_noLeftValue_returnsNoVarOrNumBeforeBinOperatorErrorWithDetails() {
    val expression = "$binOp 2"
    val operator = BINARY_SYMBOL_TO_OPERATOR_MAP.getValue(binOp)

    val error = expectFailureWhenParsingNumericExpression(expression)

    // A binary operator with no left-hand side is invalid.
    assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo(binOp)
    }
  }

  @Test
  fun testParseNumExp_unaryPlus_returnsNoVarOrNumBeforeBinOperatorErrorWithDetails() {
    val error = expectFailureWhenParsingNumericExpression("+2")

    // While '+2' is a valid unary expression, it's treated as an error (since it's more likely to
    // be a mistyped binary operation than a no-side effect unary operation).
    assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(ADD)
      hasOperatorSymbolThat().isEqualTo("+")
    }
  }

  @Test
  fun testParseNumExp_unaryPlus_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("+2", errorCheckingMode = REQUIRED_ONLY)
  }

  // Note that unary operators like '+' and '-' are excluded here since they may result in valid
  // unary operations.
  @Test
  @Iteration("nothing_times_something_asterisk", "binOp=*")
  @Iteration("nothing_times_something", "binOp=×")
  @Iteration("nothing_divides_something_slash", "binOp=/")
  @Iteration("nothing_divides_something", "binOp=÷")
  @Iteration("nothing_to_power_of_something", "binOp=^")
  fun testParseAlgExp_binOnlyOps_noLeftValue_returnsNoVarOrNumBeforeBinOperatorErrorWithDetails() {
    val expression = "$binOp x"
    val operator = BINARY_SYMBOL_TO_OPERATOR_MAP.getValue(binOp)

    val error = expectFailureWhenParsingAlgebraicExpression(expression)

    // A binary operator with no left-hand side is invalid.
    assertThat(error).isNoVarOrNumBeforeBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo(binOp)
    }
  }

  @Test
  @Iteration("something_times_nothing_asterisk", "binOp=*")
  @Iteration("something_times_nothing", "binOp=×")
  @Iteration("something_divides_nothing_slash", "binOp=/")
  @Iteration("something_divides_nothing", "binOp=÷")
  @Iteration("something_to_power_of_nothing", "binOp=^")
  @Iteration("something_adds_nothing", "binOp=+")
  @Iteration("something_subtracts_nothing_hyphen", "binOp=-")
  @Iteration("something_subtracts_nothing_en_dash", "binOp=–")
  @Iteration("something_subtracts_nothing", "binOp=−")
  fun testParseNumExp_binaryOps_noRightValue_returnsNoVarOrNumAfterBinOperatorErrorWithDetails() {
    val expression = "2 $binOp"
    val operator = BINARY_SYMBOL_TO_OPERATOR_MAP.getValue(binOp)

    val error = expectFailureWhenParsingNumericExpression(expression)

    // A binary operator with no right-hand side is invalid.
    assertThat(error).isNoVariableOrNumberAfterBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo(binOp)
    }
  }

  @Test
  @Iteration("something_times_nothing_asterisk", "binOp=*")
  @Iteration("something_times_nothing", "binOp=×")
  @Iteration("something_divides_nothing_slash", "binOp=/")
  @Iteration("something_divides_nothing", "binOp=÷")
  @Iteration("something_to_power_of_nothing", "binOp=^")
  @Iteration("something_adds_nothing", "binOp=+")
  @Iteration("something_subtracts_nothing_hyphen", "binOp=-")
  @Iteration("something_subtracts_nothing_en_dash", "binOp=–")
  @Iteration("something_subtracts_nothing", "binOp=−")
  fun testParseAlgExp_binaryOps_noRightValue_returnsNoVarOrNumAfterBinOperatorErrorWithDetails() {
    val expression = "x $binOp"
    val operator = BINARY_SYMBOL_TO_OPERATOR_MAP.getValue(binOp)

    val error = expectFailureWhenParsingAlgebraicExpression(expression)

    // A binary operator with no right-hand side is invalid.
    assertThat(error).isNoVariableOrNumberAfterBinaryOperatorThat().apply {
      hasOperatorThat().isEqualTo(operator)
      hasOperatorSymbolThat().isEqualTo(binOp)
    }
  }

  @Test
  @Iteration("var_directly_in_exp", "subExp=x")
  @Iteration("var_directly_in_sub_exp", "subExp=(1+x)")
  @Iteration("var_directly_in_nested_exp", "subExp=3^x")
  @Iteration("var_directly_in_sqrt", "subExp=sqrt(x)")
  @Iteration("var_in_unary", "subExp=-x")
  fun testParseAlgExp_powersWithVariableExpressions_returnsExponentIsVariableExpressionError() {
    val expression = "2^$subExp"

    val error = expectFailureWhenParsingAlgebraicExpression(expression)

    // Regardless of how a variable is within an exponent's power, it's always invalid.
    assertThat(error).isExponentIsVariableExpression()
  }

  @Test
  fun testParseAlgExp_powersWithVariableExpression_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingAlgebraicExpression("2^x", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_largeIntegerExponent_returnsExponentTooLargeError() {
    val error = expectFailureWhenParsingNumericExpression("2^7")

    assertThat(error).isExponentTooLarge()
  }

  @Test
  fun testParseNumExp_largeRealExponent_returnsExponentTooLargeError() {
    val error = expectFailureWhenParsingNumericExpression("2^30.12")

    assertThat(error).isExponentTooLarge()
  }

  @Test
  fun testParseNumExp_largeRealExponent_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("2^30.12", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_smallIntegerExponent_doesNotFail() {
    // Smaller exponents are fine.
    expectSuccessWhenParsingNumericExpression("2^3")
  }

  @Test
  fun testParseNumExp_nestedExponents_returnsNestedExponentsError() {
    val error = expectFailureWhenParsingNumericExpression("2^3^2")

    assertThat(error).isNestedExponents()
  }

  @Test
  fun testParseAlgExp_nestedExponents_returnsNestedExponentsError() {
    val error = expectFailureWhenParsingAlgebraicExpression("x^2^5")

    assertThat(error).isNestedExponents()
  }

  @Test
  fun testParseNumExp_nestedExponents_withUnary_returnsNestedExponentsError() {
    val error = expectFailureWhenParsingAlgebraicExpression("2^-3^4")

    // This covers a slightly different case than the above.
    assertThat(error).isNestedExponents()
  }

  @Test
  fun testParseAlgExp_nestedExponents_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingAlgebraicExpression("x^2^5", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_noSquareRootArgumentForSymbol_returnsHangingSquareRootError() {
    val error = expectFailureWhenParsingNumericExpression("2√")

    assertThat(error).isHangingSquareRoot()
  }

  @Test
  fun testParseNumExp_integerDividedByZero_returnsTermDividedByZeroError() {
    val error = expectFailureWhenParsingNumericExpression("2/0")

    assertThat(error).isTermDividedByZero()
  }

  @Test
  fun testParseAlgExp_variableDividedByZero_returnsTermDividedByZeroError() {
    val error = expectFailureWhenParsingAlgebraicExpression("x/0")

    assertThat(error).isTermDividedByZero()
  }

  @Test
  fun testParseNumExp_integerDividedByZeroInSqrt_returnsTermDividedByZeroError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt(2+7/0.0)")

    assertThat(error).isTermDividedByZero()
  }

  @Test
  fun testParseNumExp_integerDividedByZeroInSqrt_optionalErrorsDisabled_doesNotFail() {
    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingNumericExpression("sqrt(2+7/0.0)", errorCheckingMode = REQUIRED_ONLY)
  }

  @Test
  fun testParseNumExp_addVariables_returnsVariableInNumericExpressionError() {
    val error = expectFailureWhenParsingNumericExpression("x+y")

    assertThat(error).isVariableInNumericExpression()
  }

  @Test
  fun testParseNumExp_addVariable_afterNumber_returnsVariableInNumericExpressionError() {
    val error = expectFailureWhenParsingNumericExpression("2x")

    // This covers a slightly different case than the above.
    assertThat(error).isVariableInNumericExpression()
  }

  @Test
  fun testParseAlgExp_addUnsupportedVariable_returnsDisabledVariablesInUseErrorWithDetails() {
    val allowedVariables = listOf("x", "y")

    val error = expectFailureWhenParsingAlgebraicExpression("x+y+a", allowedVariables)

    // 'a' isn't an allowed variable.
    assertThat(error).isDisabledVariablesInUseWithVariablesThat().containsExactly("a")
  }

  @Test
  fun testParseAlgExp_multUnsupportedVariables_returnsDisabledVariablesInUseErrorWithDetails() {
    val allowedVariables = listOf("x", "y")

    val error = expectFailureWhenParsingAlgebraicExpression("apple", allowedVariables)

    // All disabled variables should be considered.
    assertThat(error)
      .isDisabledVariablesInUseWithVariablesThat()
      .containsExactly("a", "p", "l", "e")
  }

  @Test
  fun testParseAlgExp_multUnsupportedVariables_optionalErrorsDisabled_doesNotFail() {
    val allowedVariables = listOf("x", "y")

    // This doesn't trigger a failure when optional errors are disabled.
    expectSuccessWhenParsingAlgebraicExpression(
      "apple", allowedVariables, errorCheckingMode = REQUIRED_ONLY
    )
  }

  @Test
  fun testParseAlgExp_addSupportedVariables_doesNotFail() {
    val allowedVariables = listOf("x", "y", "a")

    // If only allowed variables are used, no errors should be reported.
    expectSuccessWhenParsingAlgebraicExpression("x+y+a", allowedVariables)
  }

  @Test
  fun testParseAlgExp_addSupportedVariables_noneSupported_returnsDisabledVarsErrorWithDetails() {
    val allowedVariables = listOf<String>()

    val error = expectFailureWhenParsingAlgebraicExpression("x+y+z", allowedVariables)

    // No allowed variables essentially results in variables no longer being supported (though with
    // less targeted errors than when using numeric expressions).
    assertThat(error).isDisabledVariablesInUseWithVariablesThat().containsExactly("x", "y", "z")
  }

  @Test
  fun testParseAlgEq_twoEquals_returnsEquationHasTooManyEqualsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("x==2")

    assertThat(error).isEquationHasTooManyEquals()
  }

  @Test
  fun testParseAlgEq_doubleEquivalence_returnsEquationHasTooManyEqualsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("x=2=y")

    assertThat(error).isEquationHasTooManyEquals()
  }

  @Test
  fun testParseAlgEq_doubleEquivalence_missingSecondRhs_returnsEquationHasTooManyEqualsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("x=2=")

    assertThat(error).isEquationHasTooManyEquals()
  }

  @Test
  fun testParseAlgEq_noEquals_returnsEquationIsMissingEqualsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("x")

    assertThat(error).isEquationIsMissingEquals()
  }

  @Test
  fun testParseAlgEq_somethingEqualsNothing_returnsEquationMissingLhsOrRhsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("x=")

    assertThat(error).isEquationMissingLhsOrRhs()
  }

  @Test
  fun testParseAlgEq_nothingEqualsSomething_returnsEquationMissingLhsOrRhsError() {
    val error = expectFailureWhenParsingAlgebraicEquation("=x")

    assertThat(error).isEquationMissingLhsOrRhs()
  }

  @Test
  @Iteration("exp", "func=exp")
  @Iteration("log", "func=log")
  @Iteration("log10", "func=log10")
  @Iteration("ln", "func=ln")
  @Iteration("sin", "func=sin")
  @Iteration("cos", "func=cos")
  @Iteration("tan", "func=tan")
  @Iteration("cot", "func=cot")
  @Iteration("csc", "func=csc")
  @Iteration("sec", "func=sec")
  @Iteration("atan", "func=atan")
  @Iteration("asin", "func=asin")
  @Iteration("acos", "func=acos")
  @Iteration("abs", "func=abs")
  fun testParseNumExp_prohibitedFunctionInUse_returnsInvalidFunctionInUseErrorWithDetails() {
    val expression = "$func(0.5+1)"

    val error = expectFailureWhenParsingAlgebraicEquation(expression)

    // Usage of detected unsupported functions should result in failures.
    assertThat(error).isInvalidFunctionInUseWithNameThat().isEqualTo(func)
  }

  @Test
  fun testParseAlgExp_unknownFunction_doesNotFail() {
    val allowedVariables = LOWERCASE_LATIN_ALPHABET

    // An unknown function won't fail since, so long as it's not similar to known functions, it will
    // be treated as implicit variable multiplication. This will fail if the letters composing the
    // name are unsupported variables or if attempted in a numeric expression (since variables
    // aren't supported). Finally, the '+1' avoids redundant parentheses errors.
    expectSuccessWhenParsingAlgebraicExpression("round(2+1)", allowedVariables)
  }

  @Test
  @Iteration("ex", "func=ex")
  @Iteration("lo", "func=lo")
  @Iteration("log1", "func=log1")
  @Iteration("si", "func=si")
  @Iteration("co", "func=co")
  @Iteration("ta", "func=ta")
  @Iteration("cs", "func=cs")
  @Iteration("se", "func=se")
  @Iteration("at", "func=at")
  @Iteration("ata", "func=ata")
  @Iteration("as", "func=as")
  @Iteration("asi", "func=asi")
  @Iteration("ac", "func=ac")
  @Iteration("aco", "func=aco")
  @Iteration("ab", "func=ab")
  @Iteration("sq", "func=sq")
  @Iteration("sqr", "func=sqr")
  fun testParseAlgExp_startOfKnownFunction_returnsFunctionNameIncompleteError() {
    val expression = "$func(0.5+1)"
    val error = expectFailureWhenParsingAlgebraicExpression(expression)

    // Starting a detected function but not completing it should result in an incomplete name error.
    assertThat(error).isFunctionNameIncomplete()
  }

  @Test
  @Iteration("a", "func=a")
  @Iteration("c", "func=c")
  @Iteration("e", "func=e")
  @Iteration("l", "func=l")
  @Iteration("s", "func=s")
  @Iteration("t", "func=t")
  fun testParseAlgExp_firstLetterOfKnownFunctions_areValidExpressions() {
    val expression = "$func(0.5+1)"
    val allowedVariables = LOWERCASE_LATIN_ALPHABET

    // The first letter of a function is just a variable (it's never treated as the start of a
    // function name unless more letters are provided).
    expectSuccessWhenParsingAlgebraicExpression(expression, allowedVariables)
  }

  @Test
  fun testParseNumExp_sqrtFunc_missingArgumentAndRightParen_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt(")

    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_sqrtFunc_missingArgument_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt()")

    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_sqrtFunc_missingParensWithFloatingArgument_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt 2")

    assertThat(error).isGenericError()
  }

  @Test
  fun testParseAlgEq_extraNumberAtEnd_returnsGenericError() {
    val error = expectFailureWhenParsingAlgebraicEquation("y = (x+1)(x-1) 2")

    // The trailing '2' isn't used in the expression.
    assertThat(error).isGenericError()
  }

  @Test
  fun testParseAlgExp_hasEquals_returnsGenericError() {
    val error = expectFailureWhenParsingAlgebraicExpression("x = √2 × 7 ÷ 4")

    // '=' is not allowed in algebraic expressions.
    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_trailingNumber_afterSqrt_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt(2)3")

    // Right implicit multiplication of numbers isn't allowed. In this case, it's likely the '3' is
    // being interpreted as an additional token that's unused.
    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_trailingImplicitlyMultipliedExponent_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("sqrt(3) 2^2")

    // Right implicit multiplication of numeric exponents isn't allowed, though in this case it's
    // likely that the '2^2' is being interpreted as additional, unused tokens.
    assertThat(error).isGenericError()
  }

  @Test
  fun testParseNumExp_trailingEquals_returnsGenericError() {
    val error = expectFailureWhenParsingNumericExpression("+=")

    // Expressions can't end with an equals sign.
    assertThat(error).isGenericError()
  }

  private companion object {
    private val BINARY_SYMBOL_TO_OPERATOR_MAP = mapOf(
      "*" to MULTIPLY,
      "×" to MULTIPLY,
      "/" to DIVIDE,
      "÷" to DIVIDE,
      "^" to EXPONENTIATE,
      "+" to ADD,
      "-" to SUBTRACT,
      "−" to SUBTRACT,
      "–" to SUBTRACT
    )
    private val LOWERCASE_LATIN_ALPHABET = listOf(
      "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
      "t", "u", "v", "w", "x", "y", "z"
    )

    private fun expectSuccessWhenParsingNumericExpression(
      expression: String,
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ) {
      expectSuccessfulParsingResult(parseNumericExpression(expression, errorCheckingMode))
    }

    private fun expectFailureWhenParsingNumericExpression(expression: String): MathParsingError {
      return expectFailingParsingResult(parseNumericExpression(expression))
    }

    private fun expectSuccessWhenParsingAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z"),
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ) {
      expectSuccessfulParsingResult(
        parseAlgebraicExpression(expression, allowedVariables, errorCheckingMode)
      )
    }

    private fun expectFailureWhenParsingAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingError {
      return expectFailingParsingResult(parseAlgebraicExpression(expression, allowedVariables))
    }

    private fun expectSuccessWhenParsingAlgebraicEquation(expression: String) {
      expectSuccessfulParsingResult(parseAlgebraicEquation(expression))
    }

    private fun expectFailureWhenParsingAlgebraicEquation(expression: String): MathParsingError {
      return expectFailingParsingResult(parseAlgebraicEquation(expression))
    }

    private fun parseNumericExpression(
      expression: String,
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }

    private fun parseAlgebraicExpression(
      expression: String,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      )
    }

    private fun parseAlgebraicEquation(expression: String): MathParsingResult<MathEquation> {
      return MathExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables = listOf("x", "y", "z"), ALL_ERRORS
      )
    }

    private fun <T> expectSuccessfulParsingResult(result: MathParsingResult<T>) {
      assertThat(result).isInstanceOf(MathParsingResult.Success::class.java)
    }

    private fun <T> expectFailingParsingResult(result: MathParsingResult<T>): MathParsingError {
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<T>).error
    }
  }
}
