package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.BooleanSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real
import org.robolectric.annotation.LooperMode
import kotlin.math.sqrt
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
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
import org.oppia.android.util.math.NumericExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.NumericExpressionParser.Companion.MathParsingResult

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionParserTest {
  @Test
  fun testErrorCases() {
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

    parseNumericExpressionWithAllErrors("1+(5+4)")
    parseNumericExpressionWithAllErrors("(5+4)+1")

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

    parseNumericExpressionWithAllErrors("2++3") // Will succeed since it's 2 + (+2).
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

    parseNumericExpressionWithAllErrors("2^3")

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

    parseAlgebraicExpressionWithAllErrors("x+y+z")

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
  fun testLotsOfCasesForNumericExpression() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsingNumericExpression("")

    val expression1 = parseNumericExpressionWithoutOptionalErrors("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    expectFailureWhenParsingNumericExpression("x")

    val expression2 = parseNumericExpressionWithoutOptionalErrors("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseNumericExpressionWithoutOptionalErrors("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withIrrationalValueThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression3).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)

    expectFailureWhenParsingNumericExpression("   x ")

    expectFailureWhenParsingNumericExpression(" z  x ")

    val expression4 = parseNumericExpressionWithoutOptionalErrors("2^3^2")
    assertThat(expression4).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression4).evaluatesToIntegerThat().isEqualTo(512)

    val expression23 = parseNumericExpressionWithoutOptionalErrors("(2^3)^2")
    assertThat(expression23).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression23).evaluatesToIntegerThat().isEqualTo(64)

    val expression24 = parseNumericExpressionWithoutOptionalErrors("512/32/4")
    assertThat(expression24).hasStructureThatMatches {
      division {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(512)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(32)
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(4)
          }
        }
      }
    }
    assertThat(expression24).evaluatesToIntegerThat().isEqualTo(4)

    val expression25 = parseNumericExpressionWithoutOptionalErrors("512/(32/4)")
    assertThat(expression25).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(512)
          }
        }
        rightOperand {
          group {
            division {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(32)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression25).evaluatesToIntegerThat().isEqualTo(64)

    val expression5 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)")
    assertThat(expression5).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression5).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0))

    expectFailureWhenParsingNumericExpression("sqr(2)")

    expectFailureWhenParsingNumericExpression("xyz(2)")

    val expression6 = parseNumericExpressionWithoutOptionalErrors("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    // Verify order of operations between higher & lower precedent operators.
    val expression32 = parseNumericExpressionWithoutOptionalErrors("3+4^7")
    assertThat(expression32).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
          }
        }
      }
    }
    assertThat(expression32).evaluatesToIntegerThat().isEqualTo(16387)

    val expression7 = parseNumericExpressionWithoutOptionalErrors("3*2-3+4^7*8/3*2+7")
    assertThat(expression7).hasStructureThatMatches {
      // To better visualize the precedence & order of operations, see this grouped version:
      // (((3*2)-3)+((((4^7)*8)/3)*2))+7.
      addition {
        leftOperand {
          // ((3*2)-3)+((((4^7)*8)/3)*2)
          addition {
            leftOperand {
              // (1*2)-3
              subtraction {
                leftOperand {
                  // 3*2
                  multiplication {
                    leftOperand {
                      // 3
                      constant {
                        withIntegerValueThat().isEqualTo(3)
                      }
                    }
                    rightOperand {
                      // 2
                      constant {
                        withIntegerValueThat().isEqualTo(2)
                      }
                    }
                  }
                }
                rightOperand {
                  // 3
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              // (((4^7)*8)/3)*2
              multiplication {
                leftOperand {
                  // ((4^7)*8)/3
                  division {
                    leftOperand {
                      // (4^7)*8
                      multiplication {
                        leftOperand {
                          // 4^7
                          exponentiation {
                            leftOperand {
                              // 4
                              constant {
                                withIntegerValueThat().isEqualTo(4)
                              }
                            }
                            rightOperand {
                              // 7
                              constant {
                                withIntegerValueThat().isEqualTo(7)
                              }
                            }
                          }
                        }
                        rightOperand {
                          // 8
                          constant {
                            withIntegerValueThat().isEqualTo(8)
                          }
                        }
                      }
                    }
                    rightOperand {
                      // 3
                      constant {
                        withIntegerValueThat().isEqualTo(3)
                      }
                    }
                  }
                }
                rightOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 7
          constant {
            withIntegerValueThat().isEqualTo(7)
          }
        }
      }
    }
    assertThat(expression7)
      .evaluatesToRationalThat()
      .evaluatesToRealThat()
      .isWithin(1e-5)
      .of(87391.333333333)

    expectFailureWhenParsingNumericExpression("x = √2 × 7 ÷ 4")

    val expression8 = parseNumericExpressionWithoutOptionalErrors("(1+2)(3+4)")
    assertThat(expression8).hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression8).evaluatesToIntegerThat().isEqualTo(21)

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingNumericExpression("(1+2)2")

    val expression10 = parseNumericExpressionWithoutOptionalErrors("2(1+2)")
    assertThat(expression10).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression10).evaluatesToIntegerThat().isEqualTo(6)

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingNumericExpression("sqrt(2)3")

    val expression12 = parseNumericExpressionWithoutOptionalErrors("3sqrt(2)")
    assertThat(expression12).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression12).evaluatesToIrrationalThat().isWithin(1e-5).of(3.0 * sqrt(2.0))

    expectFailureWhenParsingNumericExpression("xsqrt(2)")

    val expression13 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)*(1+2)*(3-2^5)")
    assertThat(expression13).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(5)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression13).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression58 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)(1+2)(3-2^5)")
    assertThat(expression58).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(5)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression58).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression14 = parseNumericExpressionWithoutOptionalErrors("((3))")
    assertThat(expression14).hasStructureThatMatches {
      group {
        group {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
      }
    }
    assertThat(expression14).evaluatesToIntegerThat().isEqualTo(3)

    val expression15 = parseNumericExpressionWithoutOptionalErrors("++3")
    assertThat(expression15).hasStructureThatMatches {
      positive {
        operand {
          positive {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression15).evaluatesToIntegerThat().isEqualTo(3)

    val expression16 = parseNumericExpressionWithoutOptionalErrors("--4")
    assertThat(expression16).hasStructureThatMatches {
      negation {
        operand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression16).evaluatesToIntegerThat().isEqualTo(4)

    val expression17 = parseNumericExpressionWithoutOptionalErrors("1+-4")
    assertThat(expression17).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression17).evaluatesToIntegerThat().isEqualTo(-3)

    val expression18 = parseNumericExpressionWithoutOptionalErrors("1++4")
    assertThat(expression18).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          positive {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression18).evaluatesToIntegerThat().isEqualTo(5)

    val expression19 = parseNumericExpressionWithoutOptionalErrors("1--4")
    assertThat(expression19).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression19).evaluatesToIntegerThat().isEqualTo(5)

    expectFailureWhenParsingNumericExpression("1-^-4")

    val expression20 = parseNumericExpressionWithoutOptionalErrors("√2 × 7 ÷ 4")
    assertThat(expression20).hasStructureThatMatches {
      division {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(4)
          }
        }
      }
    }
    assertThat(expression20).evaluatesToIrrationalThat().isWithin(1e-5).of((sqrt(2.0) * 7.0) / 4.0)

    expectFailureWhenParsingNumericExpression("1+2 &asdf")

    val expression21 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)sqrt(3)sqrt(4)")
    // Note that this tree demonstrates left associativity.
    assertThat(expression21).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression21)
      .evaluatesToIrrationalThat()
      .isWithin(1e-5)
      .of(sqrt(2.0) * sqrt(3.0) * sqrt(4.0))

    val expression22 = parseNumericExpressionWithoutOptionalErrors("(1+2)(3-7^2)(5+-17)")
    assertThat(expression22).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              // 1+2
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
            rightOperand {
              // 3-7^2
              group {
                subtraction {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(3)
                    }
                  }
                  rightOperand {
                    exponentiation {
                      leftOperand {
                        constant {
                          withIntegerValueThat().isEqualTo(7)
                        }
                      }
                      rightOperand {
                        constant {
                          withIntegerValueThat().isEqualTo(2)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 5+-17
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(5)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(17)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression22).evaluatesToIntegerThat().isEqualTo(1656)

    val expression26 = parseNumericExpressionWithoutOptionalErrors("3^-2")
    assertThat(expression26).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression26).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(9)
    }

    val expression27 = parseNumericExpressionWithoutOptionalErrors("(3^-2)^(3^-2)")
    assertThat(expression27).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression27).evaluatesToIrrationalThat().isWithin(1e-5).of(0.78338103693)

    val expression28 = parseNumericExpressionWithoutOptionalErrors("1-3^sqrt(4)")
    assertThat(expression28).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(4)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression28).evaluatesToIntegerThat().isEqualTo(-8)

    // "Hard" order of operation problems loosely based on & other problems that can often stump
    // people: https://www.basic-mathematics.com/hard-order-of-operations-problems.html.
    val expression29 = parseNumericExpressionWithoutOptionalErrors("3÷2*(3+4)")
    assertThat(expression29).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression29).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    val expression59 = parseNumericExpressionWithoutOptionalErrors("3÷2(3+4)")
    assertThat(expression59).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression59).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    // Numbers cannot have implicit multiplication unless they are in groups.
    expectFailureWhenParsingNumericExpression("2 2")

    expectFailureWhenParsingNumericExpression("2 2^2")

    expectFailureWhenParsingNumericExpression("2^2 2")

    val expression31 = parseNumericExpressionWithoutOptionalErrors("(3)(4)(5)")
    assertThat(expression31).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(5)
            }
          }
        }
      }
    }
    assertThat(expression31).evaluatesToIntegerThat().isEqualTo(60)

    val expression33 = parseNumericExpressionWithoutOptionalErrors("2^(3)")
    assertThat(expression33).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
      }
    }
    assertThat(expression33).evaluatesToIntegerThat().isEqualTo(8)

    // Verify that implicit multiple has lower precedence than exponentiation.
    val expression34 = parseNumericExpressionWithoutOptionalErrors("2^(3)(4)")
    assertThat(expression34).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(4)
            }
          }
        }
      }
    }
    assertThat(expression34).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingNumericExpression("2^(3)2^2")

    val expression35 = parseNumericExpressionWithoutOptionalErrors("2^(3)*2^2")
    assertThat(expression35).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression35).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can be a right operand of an implicit mult if it's grouped.
    val expression36 = parseNumericExpressionWithoutOptionalErrors("2^(3)(2^2)")
    assertThat(expression36).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression36).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingNumericExpression("2^3(4)2^3")

    val expression38 = parseNumericExpressionWithoutOptionalErrors("2^3(4)*2^3")
    assertThat(expression38).hasStructureThatMatches {
      // 2^3(4)*2^3
      multiplication {
        leftOperand {
          // 2^3(4)
          multiplication {
            leftOperand {
              // 2^3
              exponentiation {
                leftOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  // 3
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              // 4
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
        rightOperand {
          // 2^3
          exponentiation {
            leftOperand {
              // 2
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              // 3
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression38).evaluatesToIntegerThat().isEqualTo(256)

    expectFailureWhenParsingNumericExpression("2^2 2^2")
    expectFailureWhenParsingNumericExpression("(3) 2^2")
    expectFailureWhenParsingNumericExpression("sqrt(3) 2^2")
    expectFailureWhenParsingNumericExpression("√2 2^2")
    expectFailureWhenParsingNumericExpression("2^2 3")

    expectFailureWhenParsingNumericExpression("-2 3")

    val expression39 = parseNumericExpressionWithoutOptionalErrors("-(1+2)")
    assertThat(expression39).hasStructureThatMatches {
      negation {
        operand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression39).evaluatesToIntegerThat().isEqualTo(-3)

    // Should pass for algebra.
    expectFailureWhenParsingNumericExpression("-2 x")

    val expression40 = parseNumericExpressionWithoutOptionalErrors("-2 (1+2)")
    assertThat(expression40).hasStructureThatMatches {
      // The negation happens last for parity with other common calculators.
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression40).evaluatesToIntegerThat().isEqualTo(-6)

    val expression41 = parseNumericExpressionWithoutOptionalErrors("-2^3(4)")
    assertThat(expression41).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              exponentiation {
                leftOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression41).evaluatesToIntegerThat().isEqualTo(-32)

    val expression43 = parseNumericExpressionWithoutOptionalErrors("√2^2(3)")
    assertThat(expression43).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
      }
    }
    assertThat(expression43).evaluatesToIrrationalThat().isWithin(1e-5).of(6.0)

    val expression60 = parseNumericExpressionWithoutOptionalErrors("√(2^2(3))")
    assertThat(expression60).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          group {
            multiplication {
              leftOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
              rightOperand {
                group {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression60).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(12.0))

    val expression42 = parseNumericExpressionWithoutOptionalErrors("-2*-2")
    // Note that the following structure is not the same as (-2)*(-2) since unary negation has
    // higher precedence than multiplication, so it's first & recurses to include the entire
    // multiplication expression.
    assertThat(expression42).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              negation {
                operand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression42).evaluatesToIntegerThat().isEqualTo(4)

    val expression44 = parseNumericExpressionWithoutOptionalErrors("2(2)")
    assertThat(expression44).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression44).evaluatesToIntegerThat().isEqualTo(4)

    val expression45 = parseNumericExpressionWithoutOptionalErrors("2sqrt(2)")
    assertThat(expression45).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression45).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression46 = parseNumericExpressionWithoutOptionalErrors("2√2")
    assertThat(expression46).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression46).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression47 = parseNumericExpressionWithoutOptionalErrors("(2)(2)")
    assertThat(expression47).hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression47).evaluatesToIntegerThat().isEqualTo(4)

    val expression48 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)(2)")
    assertThat(expression48).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression48).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression49 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)sqrt(2)")
    assertThat(expression49).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression49).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression50 = parseNumericExpressionWithoutOptionalErrors("√2√2")
    assertThat(expression50).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression50).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression51 = parseNumericExpressionWithoutOptionalErrors("(2)(2)(2)")
    assertThat(expression51).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression51).evaluatesToIntegerThat().isEqualTo(8)

    val expression52 = parseNumericExpressionWithoutOptionalErrors("sqrt(2)sqrt(2)sqrt(2)")
    assertThat(expression52).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    val sqrt2 = sqrt(2.0)
    assertThat(expression52).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    val expression53 = parseNumericExpressionWithoutOptionalErrors("√2√2√2")
    assertThat(expression53).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression53).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    // Should fail for algebra.
    expectFailureWhenParsingNumericExpression("x7")

    // Should pass for algebra.
    expectFailureWhenParsingNumericExpression("2x^2")

    val expression54 = parseNumericExpressionWithoutOptionalErrors("2*2/-4+7*2")
    assertThat(expression54).hasStructureThatMatches {
      // 2*2/-4+7*2 -> ((2*2)/(-4))+(7*2)
      addition {
        leftOperand {
          // 2*2/-4
          division {
            leftOperand {
              // 2*2
              multiplication {
                leftOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              // -4
              negation {
                // 4
                operand {
                  constant {
                    withIntegerValueThat().isEqualTo(4)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 7*2
          multiplication {
            leftOperand {
              // 7
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
            rightOperand {
              // 2
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression54).evaluatesToIntegerThat().isEqualTo(13)

    val expression55 = parseNumericExpressionWithoutOptionalErrors("3/(1-2)")
    assertThat(expression55).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression55).evaluatesToIntegerThat().isEqualTo(-3)

    val expression56 = parseNumericExpressionWithoutOptionalErrors("(3)/(1-2)")
    assertThat(expression56).hasStructureThatMatches {
      division {
        leftOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression56).evaluatesToIntegerThat().isEqualTo(-3)

    val expression57 = parseNumericExpressionWithoutOptionalErrors("3/((1-2))")
    assertThat(expression57).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            group {
              subtraction {
                leftOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(1)
                  }
                }
                rightOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression57).evaluatesToIntegerThat().isEqualTo(-3)

    // TODO: add others, including tests for malformed expressions throughout the parser &
    //  tokenizer.
  }

  @Test
  fun testLotsOfCasesForAlgebraicExpression() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsingAlgebraicExpression("")

    val expression1 = parseAlgebraicExpressionWithoutOptionalErrors("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    val expression61 = parseAlgebraicExpressionWithoutOptionalErrors("x")
    assertThat(expression61).hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }

    val expression2 = parseAlgebraicExpressionWithoutOptionalErrors("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseAlgebraicExpressionWithoutOptionalErrors("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withIrrationalValueThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression3).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)

    val expression62 = parseAlgebraicExpressionWithoutOptionalErrors("   y ")
    assertThat(expression62).hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }

    val expression63 = parseAlgebraicExpressionWithoutOptionalErrors(" z  x ")
    assertThat(expression63).hasStructureThatMatches {
      multiplication {
        leftOperand {
          variable {
            withNameThat().isEqualTo("z")
          }
        }
        rightOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }

    val expression4 = parseAlgebraicExpressionWithoutOptionalErrors("2^3^2")
    assertThat(expression4).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression4).evaluatesToIntegerThat().isEqualTo(512)

    val expression23 = parseAlgebraicExpressionWithoutOptionalErrors("(2^3)^2")
    assertThat(expression23).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression23).evaluatesToIntegerThat().isEqualTo(64)

    val expression24 = parseAlgebraicExpressionWithoutOptionalErrors("512/32/4")
    assertThat(expression24).hasStructureThatMatches {
      division {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(512)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(32)
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(4)
          }
        }
      }
    }
    assertThat(expression24).evaluatesToIntegerThat().isEqualTo(4)

    val expression25 = parseAlgebraicExpressionWithoutOptionalErrors("512/(32/4)")
    assertThat(expression25).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(512)
          }
        }
        rightOperand {
          group {
            division {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(32)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression25).evaluatesToIntegerThat().isEqualTo(64)

    val expression5 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)")
    assertThat(expression5).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression5).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0))

    expectFailureWhenParsingAlgebraicExpression("sqr(2)")

    val expression64 = parseAlgebraicExpressionWithoutOptionalErrors("xyz(2)")
    assertThat(expression64).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              multiplication {
                leftOperand {
                  variable {
                    withNameThat().isEqualTo("x")
                  }
                }
                rightOperand {
                  variable {
                    withNameThat().isEqualTo("y")
                  }
                }
              }
            }
            rightOperand {
              variable {
                withNameThat().isEqualTo("z")
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }

    val expression6 = parseAlgebraicExpressionWithoutOptionalErrors("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    expectFailureWhenParsingAlgebraicExpression("73 2")

    // Verify order of operations between higher & lower precedent operators.
    val expression32 = parseAlgebraicExpressionWithoutOptionalErrors("3+4^7")
    assertThat(expression32).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
          }
        }
      }
    }
    assertThat(expression32).evaluatesToIntegerThat().isEqualTo(16387)

    val expression7 = parseAlgebraicExpressionWithoutOptionalErrors("3*2-3+4^7*8/3*2+7")
    assertThat(expression7).hasStructureThatMatches {
      // To better visualize the precedence & order of operations, see this grouped version:
      // (((3*2)-3)+((((4^7)*8)/3)*2))+7.
      addition {
        leftOperand {
          // ((3*2)-3)+((((4^7)*8)/3)*2)
          addition {
            leftOperand {
              // (1*2)-3
              subtraction {
                leftOperand {
                  // 3*2
                  multiplication {
                    leftOperand {
                      // 3
                      constant {
                        withIntegerValueThat().isEqualTo(3)
                      }
                    }
                    rightOperand {
                      // 2
                      constant {
                        withIntegerValueThat().isEqualTo(2)
                      }
                    }
                  }
                }
                rightOperand {
                  // 3
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              // (((4^7)*8)/3)*2
              multiplication {
                leftOperand {
                  // ((4^7)*8)/3
                  division {
                    leftOperand {
                      // (4^7)*8
                      multiplication {
                        leftOperand {
                          // 4^7
                          exponentiation {
                            leftOperand {
                              // 4
                              constant {
                                withIntegerValueThat().isEqualTo(4)
                              }
                            }
                            rightOperand {
                              // 7
                              constant {
                                withIntegerValueThat().isEqualTo(7)
                              }
                            }
                          }
                        }
                        rightOperand {
                          // 8
                          constant {
                            withIntegerValueThat().isEqualTo(8)
                          }
                        }
                      }
                    }
                    rightOperand {
                      // 3
                      constant {
                        withIntegerValueThat().isEqualTo(3)
                      }
                    }
                  }
                }
                rightOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 7
          constant {
            withIntegerValueThat().isEqualTo(7)
          }
        }
      }
    }
    assertThat(expression7)
      .evaluatesToRationalThat()
      .evaluatesToRealThat()
      .isWithin(1e-5)
      .of(87391.333333333)

    expectFailureWhenParsingAlgebraicExpression("x = √2 × 7 ÷ 4")

    val expression8 = parseAlgebraicExpressionWithoutOptionalErrors("(1+2)(3+4)")
    assertThat(expression8).hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression8).evaluatesToIntegerThat().isEqualTo(21)

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingAlgebraicExpression("(1+2)2")

    val expression10 = parseAlgebraicExpressionWithoutOptionalErrors("2(1+2)")
    assertThat(expression10).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression10).evaluatesToIntegerThat().isEqualTo(6)

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingAlgebraicExpression("sqrt(2)3")

    val expression12 = parseAlgebraicExpressionWithoutOptionalErrors("3sqrt(2)")
    assertThat(expression12).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression12).evaluatesToIrrationalThat().isWithin(1e-5).of(3.0 * sqrt(2.0))

    val expression65 = parseAlgebraicExpressionWithoutOptionalErrors("xsqrt(2)")
    assertThat(expression65).hasStructureThatMatches {
      multiplication {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }

    val expression13 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)*(1+2)*(3-2^5)")
    assertThat(expression13).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(5)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression13).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression58 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)(1+2)(3-2^5)")
    assertThat(expression58).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(5)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression58).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression14 = parseAlgebraicExpressionWithoutOptionalErrors("((3))")
    assertThat(expression14).hasStructureThatMatches {
      group {
        group {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
      }
    }
    assertThat(expression14).evaluatesToIntegerThat().isEqualTo(3)

    val expression15 = parseAlgebraicExpressionWithoutOptionalErrors("++3")
    assertThat(expression15).hasStructureThatMatches {
      positive {
        operand {
          positive {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression15).evaluatesToIntegerThat().isEqualTo(3)

    val expression16 = parseAlgebraicExpressionWithoutOptionalErrors("--4")
    assertThat(expression16).hasStructureThatMatches {
      negation {
        operand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression16).evaluatesToIntegerThat().isEqualTo(4)

    val expression17 = parseAlgebraicExpressionWithoutOptionalErrors("1+-4")
    assertThat(expression17).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression17).evaluatesToIntegerThat().isEqualTo(-3)

    val expression18 = parseAlgebraicExpressionWithoutOptionalErrors("1++4")
    assertThat(expression18).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          positive {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression18).evaluatesToIntegerThat().isEqualTo(5)

    val expression19 = parseAlgebraicExpressionWithoutOptionalErrors("1--4")
    assertThat(expression19).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression19).evaluatesToIntegerThat().isEqualTo(5)

    expectFailureWhenParsingAlgebraicExpression("1-^-4")

    val expression20 = parseAlgebraicExpressionWithoutOptionalErrors("√2 × 7 ÷ 4")
    assertThat(expression20).hasStructureThatMatches {
      division {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(4)
          }
        }
      }
    }
    assertThat(expression20).evaluatesToIrrationalThat().isWithin(1e-5).of((sqrt(2.0) * 7.0) / 4.0)

    expectFailureWhenParsingAlgebraicExpression("1+2 &asdf")

    val expression21 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)sqrt(3)sqrt(4)")
    // Note that this tree demonstrates left associativity.
    assertThat(expression21).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression21)
      .evaluatesToIrrationalThat()
      .isWithin(1e-5)
      .of(sqrt(2.0) * sqrt(3.0) * sqrt(4.0))

    val expression22 = parseAlgebraicExpressionWithoutOptionalErrors("(1+2)(3-7^2)(5+-17)")
    assertThat(expression22).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              // 1+2
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
            rightOperand {
              // 3-7^2
              group {
                subtraction {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(3)
                    }
                  }
                  rightOperand {
                    exponentiation {
                      leftOperand {
                        constant {
                          withIntegerValueThat().isEqualTo(7)
                        }
                      }
                      rightOperand {
                        constant {
                          withIntegerValueThat().isEqualTo(2)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 5+-17
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(5)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(17)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression22).evaluatesToIntegerThat().isEqualTo(1656)

    val expression26 = parseAlgebraicExpressionWithoutOptionalErrors("3^-2")
    assertThat(expression26).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression26).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(9)
    }

    val expression27 = parseAlgebraicExpressionWithoutOptionalErrors("(3^-2)^(3^-2)")
    assertThat(expression27).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
        rightOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression27).evaluatesToIrrationalThat().isWithin(1e-5).of(0.78338103693)

    val expression28 = parseAlgebraicExpressionWithoutOptionalErrors("1-3^sqrt(4)")
    assertThat(expression28).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(1)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(4)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression28).evaluatesToIntegerThat().isEqualTo(-8)

    // "Hard" order of operation problems loosely based on & other problems that can often stump
    // people: https://www.basic-mathematics.com/hard-order-of-operations-problems.html.
    val expression29 = parseAlgebraicExpressionWithoutOptionalErrors("3÷2*(3+4)")
    assertThat(expression29).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression29).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    val expression59 = parseAlgebraicExpressionWithoutOptionalErrors("3÷2(3+4)")
    assertThat(expression59).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression59).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    // Numbers cannot have implicit multiplication unless they are in groups.
    expectFailureWhenParsingAlgebraicExpression("2 2")

    expectFailureWhenParsingAlgebraicExpression("2 2^2")

    expectFailureWhenParsingAlgebraicExpression("2^2 2")

    val expression31 = parseAlgebraicExpressionWithoutOptionalErrors("(3)(4)(5)")
    assertThat(expression31).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(5)
            }
          }
        }
      }
    }
    assertThat(expression31).evaluatesToIntegerThat().isEqualTo(60)

    val expression33 = parseAlgebraicExpressionWithoutOptionalErrors("2^(3)")
    assertThat(expression33).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
      }
    }
    assertThat(expression33).evaluatesToIntegerThat().isEqualTo(8)

    // Verify that implicit multiple has lower precedence than exponentiation.
    val expression34 = parseAlgebraicExpressionWithoutOptionalErrors("2^(3)(4)")
    assertThat(expression34).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(4)
            }
          }
        }
      }
    }
    assertThat(expression34).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingAlgebraicExpression("2^(3)2^2")

    val expression35 = parseAlgebraicExpressionWithoutOptionalErrors("2^(3)*2^2")
    assertThat(expression35).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression35).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can be a right operand of an implicit mult if it's grouped.
    val expression36 = parseAlgebraicExpressionWithoutOptionalErrors("2^(3)(2^2)")
    assertThat(expression36).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression36).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingAlgebraicExpression("2^3(4)2^3")

    val expression38 = parseAlgebraicExpressionWithoutOptionalErrors("2^3(4)*2^3")
    assertThat(expression38).hasStructureThatMatches {
      // 2^3(4)*2^3
      multiplication {
        leftOperand {
          // 2^3(4)
          multiplication {
            leftOperand {
              // 2^3
              exponentiation {
                leftOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  // 3
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              // 4
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
        rightOperand {
          // 2^3
          exponentiation {
            leftOperand {
              // 2
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              // 3
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression38).evaluatesToIntegerThat().isEqualTo(256)

    expectFailureWhenParsingAlgebraicExpression("2^2 2^2")
    expectFailureWhenParsingAlgebraicExpression("(3) 2^2")
    expectFailureWhenParsingAlgebraicExpression("sqrt(3) 2^2")
    expectFailureWhenParsingAlgebraicExpression("√2 2^2")
    expectFailureWhenParsingAlgebraicExpression("2^2 3")

    expectFailureWhenParsingAlgebraicExpression("-2 3")

    val expression39 = parseAlgebraicExpressionWithoutOptionalErrors("-(1+2)")
    assertThat(expression39).hasStructureThatMatches {
      negation {
        operand {
          group {
            addition {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression39).evaluatesToIntegerThat().isEqualTo(-3)

    // Should pass for algebra.
    val expression66 = parseAlgebraicExpressionWithoutOptionalErrors("-2 x")
    assertThat(expression66).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              variable {
                withNameThat().isEqualTo("x")
              }
            }
          }
        }
      }
    }

    val expression40 = parseAlgebraicExpressionWithoutOptionalErrors("-2 (1+2)")
    assertThat(expression40).hasStructureThatMatches {
      // The negation happens last for parity with other common calculators.
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                addition {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(1)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression40).evaluatesToIntegerThat().isEqualTo(-6)

    val expression41 = parseAlgebraicExpressionWithoutOptionalErrors("-2^3(4)")
    assertThat(expression41).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              exponentiation {
                leftOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(4)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression41).evaluatesToIntegerThat().isEqualTo(-32)

    val expression43 = parseAlgebraicExpressionWithoutOptionalErrors("√2^2(3)")
    assertThat(expression43).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
      }
    }
    assertThat(expression43).evaluatesToIrrationalThat().isWithin(1e-5).of(6.0)

    val expression60 = parseAlgebraicExpressionWithoutOptionalErrors("√(2^2(3))")
    assertThat(expression60).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          group {
            multiplication {
              leftOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withIntegerValueThat().isEqualTo(2)
                    }
                  }
                }
              }
              rightOperand {
                group {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression60).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(12.0))

    val expression42 = parseAlgebraicExpressionWithoutOptionalErrors("-2*-2")
    // Note that the following structure is not the same as (-2)*(-2) since unary negation has
    // higher precedence than multiplication, so it's first & recurses to include the entire
    // multiplication expression.
    assertThat(expression42).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            rightOperand {
              negation {
                operand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression42).evaluatesToIntegerThat().isEqualTo(4)

    val expression44 = parseAlgebraicExpressionWithoutOptionalErrors("2(2)")
    assertThat(expression44).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression44).evaluatesToIntegerThat().isEqualTo(4)

    val expression45 = parseAlgebraicExpressionWithoutOptionalErrors("2sqrt(2)")
    assertThat(expression45).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression45).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression46 = parseAlgebraicExpressionWithoutOptionalErrors("2√2")
    assertThat(expression46).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression46).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression47 = parseAlgebraicExpressionWithoutOptionalErrors("(2)(2)")
    assertThat(expression47).hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression47).evaluatesToIntegerThat().isEqualTo(4)

    val expression48 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)(2)")
    assertThat(expression48).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression48).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression49 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)sqrt(2)")
    assertThat(expression49).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression49).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression50 = parseAlgebraicExpressionWithoutOptionalErrors("√2√2")
    assertThat(expression50).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression50).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression51 = parseAlgebraicExpressionWithoutOptionalErrors("(2)(2)(2)")
    assertThat(expression51).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression51).evaluatesToIntegerThat().isEqualTo(8)

    val expression52 = parseAlgebraicExpressionWithoutOptionalErrors("sqrt(2)sqrt(2)sqrt(2)")
    assertThat(expression52).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    val sqrt2 = sqrt(2.0)
    assertThat(expression52).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    val expression53 = parseAlgebraicExpressionWithoutOptionalErrors("√2√2√2")
    assertThat(expression53).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression53).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    // Should fail for algebra.
    expectFailureWhenParsingAlgebraicExpression("x7")

    // Should pass for algebra.
    val expression67 = parseAlgebraicExpressionWithoutOptionalErrors("2x^2y^-3")
    assertThat(expression67).hasStructureThatMatches {
      // 2x^2y^-3 -> (2*(x^2))*(y^(-3))
      multiplication {
        // 2x^2
        leftOperand {
          multiplication {
            // 2
            leftOperand {
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
            // x^2
            rightOperand {
              exponentiation {
                // x
                leftOperand {
                  variable {
                    withNameThat().isEqualTo("x")
                  }
                }
                // 2
                rightOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
        // y^-3
        rightOperand {
          exponentiation {
            // y
            leftOperand {
              variable {
                withNameThat().isEqualTo("y")
              }
            }
            // -3
            rightOperand {
              negation {
                // 3
                operand {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
      }
    }

    val expression54 = parseAlgebraicExpressionWithoutOptionalErrors("2*2/-4+7*2")
    assertThat(expression54).hasStructureThatMatches {
      // 2*2/-4+7*2 -> ((2*2)/(-4))+(7*2)
      addition {
        leftOperand {
          // 2*2/-4
          division {
            leftOperand {
              // 2*2
              multiplication {
                leftOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  // 2
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              // -4
              negation {
                // 4
                operand {
                  constant {
                    withIntegerValueThat().isEqualTo(4)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 7*2
          multiplication {
            leftOperand {
              // 7
              constant {
                withIntegerValueThat().isEqualTo(7)
              }
            }
            rightOperand {
              // 2
              constant {
                withIntegerValueThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
    assertThat(expression54).evaluatesToIntegerThat().isEqualTo(13)

    val expression55 = parseAlgebraicExpressionWithoutOptionalErrors("3/(1-2)")
    assertThat(expression55).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression55).evaluatesToIntegerThat().isEqualTo(-3)

    val expression56 = parseAlgebraicExpressionWithoutOptionalErrors("(3)/(1-2)")
    assertThat(expression56).hasStructureThatMatches {
      division {
        leftOperand {
          group {
            constant {
              withIntegerValueThat().isEqualTo(3)
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(2)
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression56).evaluatesToIntegerThat().isEqualTo(-3)

    val expression57 = parseAlgebraicExpressionWithoutOptionalErrors("3/((1-2))")
    assertThat(expression57).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            group {
              subtraction {
                leftOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(1)
                  }
                }
                rightOperand {
                  constant {
                    withIntegerValueThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression57).evaluatesToIntegerThat().isEqualTo(-3)

    // TODO: add others, including tests for malformed expressions throughout the parser &
    //  tokenizer.
  }

  @Test
  fun testLotsOfCasesForAlgebraicEquation() {
    expectFailureWhenParsingAlgebraicEquation(" x =")
    expectFailureWhenParsingAlgebraicEquation(" = y")

    val equation1 = parseAlgebraicEquationWithAllErrors("x = 1")
    assertThat(equation1).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }
    assertThat(equation1).hasRightHandSideThat().evaluatesToIntegerThat().isEqualTo(1)

    val equation2 =
      parseAlgebraicEquationWithAllErrors("y = mx + b", allowedVariables = listOf("x", "y", "b", "m"))
    assertThat(equation2).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation2).hasRightHandSideThat().hasStructureThatMatches {
      addition {
        leftOperand {
          multiplication {
            leftOperand {
              variable {
                withNameThat().isEqualTo("m")
              }
            }
            rightOperand {
              variable {
                withNameThat().isEqualTo("x")
              }
            }
          }
        }
        rightOperand {
          variable {
            withNameThat().isEqualTo("b")
          }
        }
      }
    }

    val equation3 = parseAlgebraicEquationWithAllErrors("y = (x+1)^2")
    assertThat(equation3).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation3).hasRightHandSideThat().hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            addition {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
            }
          }
        }
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }

    val equation4 = parseAlgebraicEquationWithAllErrors("y = (x+1)(x-1)")
    assertThat(equation4).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation4).hasRightHandSideThat().hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            addition {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
                constant {
                  withIntegerValueThat().isEqualTo(1)
                }
              }
            }
          }
        }
      }
    }

    expectFailureWhenParsingAlgebraicEquation("y = (x+1)(x-1) 2")
    expectFailureWhenParsingAlgebraicEquation("y 2 = (x+1)(x-1)")

    val equation5 =
      parseAlgebraicEquationWithAllErrors("a*x^2 + b*x + c = 0", allowedVariables = listOf("x", "a", "b", "c"))
    assertThat(equation5).hasLeftHandSideThat().hasStructureThatMatches {
      addition {
        leftOperand {
          addition {
            leftOperand {
              multiplication {
                leftOperand {
                  variable {
                    withNameThat().isEqualTo("a")
                  }
                }
                rightOperand {
                  exponentiation {
                    leftOperand {
                      variable {
                        withNameThat().isEqualTo("x")
                      }
                    }
                    rightOperand {
                      constant {
                        withIntegerValueThat().isEqualTo(2)
                      }
                    }
                  }
                }
              }
            }
            rightOperand {
              multiplication {
                leftOperand {
                  variable {
                    withNameThat().isEqualTo("b")
                  }
                }
                rightOperand {
                  variable {
                    withNameThat().isEqualTo("x")
                  }
                }
              }
            }
          }
        }
        rightOperand {
          variable {
            withNameThat().isEqualTo("c")
          }
        }
      }
    }
    assertThat(equation5).hasRightHandSideThat().hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testLatex() {
    // TODO: split up & move to separate test suites. Finish test cases.

    val exp1 = parseNumericExpressionWithAllErrors("1")
    assertThat(exp1.toRawLatex()).isEqualTo("1")

    val exp2 = parseNumericExpressionWithAllErrors("1+2")
    assertThat(exp2.toRawLatex()).isEqualTo("1 + 2")

    val exp3 = parseNumericExpressionWithAllErrors("1*2")
    assertThat(exp3.toRawLatex()).isEqualTo("1 \\times 2")

    val exp4 = parseNumericExpressionWithAllErrors("1/2")
    assertThat(exp4.toRawLatex()).isEqualTo("1 \\div 2")

    val exp5 = parseNumericExpressionWithAllErrors("1/2")
    assertThat(exp5.toRawLatex(divAsFraction = true)).isEqualTo("\\frac{1}{2}")

    val exp10 = parseNumericExpressionWithAllErrors("√2")
    assertThat(exp10.toRawLatex()).isEqualTo("\\sqrt{2}")

    val exp11 = parseNumericExpressionWithAllErrors("√(1/2)")
    assertThat(exp11.toRawLatex()).isEqualTo("\\sqrt{(1 \\div 2)}")

    val exp6 = parseAlgebraicExpressionWithAllErrors("x+y")
    assertThat(exp6.toRawLatex()).isEqualTo("x + y")

    val exp7 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1/y)")
    assertThat(exp7.toRawLatex()).isEqualTo("x ^ {(1 \\div y)}")

    val exp8 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1/y)")
    assertThat(exp8.toRawLatex(divAsFraction = true)).isEqualTo("x ^ {(\\frac{1}{y})}")

    val exp9 = parseAlgebraicExpressionWithoutOptionalErrors("x^y^z")
    assertThat(exp9.toRawLatex(divAsFraction = true)).isEqualTo("x ^ {y ^ {z}}")

    val eq1 =
      parseAlgebraicEquationWithAllErrors(
        "7a^2+b^2+c^2=0", allowedVariables = listOf("a", "b", "c")
      )
    assertThat(eq1.toRawLatex()).isEqualTo("7a ^ {2} + b ^ {2} + c ^ {2} = 0")

    val eq2 = parseAlgebraicEquationWithAllErrors("sqrt(1+x)/x=1")
    assertThat(eq2.toRawLatex()).isEqualTo("\\sqrt{1 + x} \\div x = 1")

    val eq3 = parseAlgebraicEquationWithAllErrors("sqrt(1+x)/x=1")
    assertThat(eq3.toRawLatex(divAsFraction = true)).isEqualTo("\\frac{\\sqrt{1 + x}}{x} = 1")
  }

  @Test
  fun testHumanReadableString() {
    // TODO: split up & move to separate test suites. Finish test cases (if anymore are needed).

    val exp1 = parseNumericExpressionWithAllErrors("1")
    assertThat(exp1.toHumanReadableString(ARABIC)).isNull()

    assertThat(exp1.toHumanReadableString(HINDI)).isNull()

    assertThat(exp1.toHumanReadableString(HINGLISH)).isNull()

    assertThat(exp1.toHumanReadableString(PORTUGUESE)).isNull()

    assertThat(exp1.toHumanReadableString(BRAZILIAN_PORTUGUESE)).isNull()

    assertThat(exp1.toHumanReadableString(LANGUAGE_UNSPECIFIED)).isNull()

    assertThat(exp1.toHumanReadableString(UNRECOGNIZED)).isNull()

    val exp2 = parseAlgebraicExpressionWithAllErrors("x")
    assertThat(exp2.toHumanReadableString(ARABIC)).isNull()

    assertThat(exp2.toHumanReadableString(HINDI)).isNull()

    assertThat(exp2.toHumanReadableString(HINGLISH)).isNull()

    assertThat(exp2.toHumanReadableString(PORTUGUESE)).isNull()

    assertThat(exp2.toHumanReadableString(BRAZILIAN_PORTUGUESE)).isNull()

    assertThat(exp2.toHumanReadableString(LANGUAGE_UNSPECIFIED)).isNull()

    assertThat(exp2.toHumanReadableString(UNRECOGNIZED)).isNull()

    val eq1 = parseAlgebraicEquationWithAllErrors("x=1")
    assertThat(eq1.toHumanReadableString(ARABIC)).isNull()

    assertThat(eq1.toHumanReadableString(HINDI)).isNull()

    assertThat(eq1.toHumanReadableString(HINGLISH)).isNull()

    assertThat(eq1.toHumanReadableString(PORTUGUESE)).isNull()

    assertThat(eq1.toHumanReadableString(BRAZILIAN_PORTUGUESE)).isNull()

    assertThat(eq1.toHumanReadableString(LANGUAGE_UNSPECIFIED)).isNull()

    assertThat(eq1.toHumanReadableString(UNRECOGNIZED)).isNull()

    // specific cases (from rules & other cases):
    val exp3 = parseNumericExpressionWithAllErrors("1")
    assertThat(exp3.toHumanReadableString(ENGLISH)).isEqualTo("1")

    val exp49 = parseNumericExpressionWithAllErrors("-1")
    assertThat(exp49.toHumanReadableString(ENGLISH)).isEqualTo("negative 1")

    val exp50 = parseNumericExpressionWithAllErrors("+1")
    assertThat(exp50.toHumanReadableString(ENGLISH)).isEqualTo("positive 1")

    val exp4 = parseNumericExpressionWithoutOptionalErrors("((1))")
    assertThat(exp4.toHumanReadableString(ENGLISH)).isEqualTo("1")

    val exp5 = parseNumericExpressionWithAllErrors("1+2")
    assertThat(exp5.toHumanReadableString(ENGLISH)).isEqualTo("1 plus 2")

    val exp6 = parseNumericExpressionWithAllErrors("1-2")
    assertThat(exp6.toHumanReadableString(ENGLISH)).isEqualTo("1 minus 2")

    val exp7 = parseNumericExpressionWithAllErrors("1*2")
    assertThat(exp7.toHumanReadableString(ENGLISH)).isEqualTo("1 times 2")

    val exp8 = parseNumericExpressionWithAllErrors("1/2")
    assertThat(exp8.toHumanReadableString(ENGLISH)).isEqualTo("1 divided by 2")

    val exp9 = parseNumericExpressionWithAllErrors("1+(1-2)")
    assertThat(exp9.toHumanReadableString(ENGLISH))
      .isEqualTo("1 plus open parenthesis 1 minus 2 close parenthesis")

    val exp10 = parseNumericExpressionWithAllErrors("2^3")
    assertThat(exp10.toHumanReadableString(ENGLISH)).isEqualTo("2 raised to the power of 3")

    val exp11 = parseNumericExpressionWithAllErrors("2^(1+2)")
    assertThat(exp11.toHumanReadableString(ENGLISH))
      .isEqualTo("2 raised to the power of open parenthesis 1 plus 2 close parenthesis")

    val exp12 = parseNumericExpressionWithAllErrors("100000*2")
    assertThat(exp12.toHumanReadableString(ENGLISH)).isEqualTo("100,000 times 2")

    val exp13 = parseNumericExpressionWithAllErrors("sqrt(2)")
    assertThat(exp13.toHumanReadableString(ENGLISH)).isEqualTo("square root of 2")

    val exp14 = parseNumericExpressionWithAllErrors("√2")
    assertThat(exp14.toHumanReadableString(ENGLISH)).isEqualTo("square root of 2")

    val exp15 = parseNumericExpressionWithAllErrors("sqrt(1+2)")
    assertThat(exp15.toHumanReadableString(ENGLISH))
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
        val exp16 = parseNumericExpressionWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16.toHumanReadableString(ENGLISH, divAsFraction = true))
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp17 = parseNumericExpressionWithAllErrors("-1/3")
    assertThat(exp17.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("negative 1 third")

    val exp18 = parseNumericExpressionWithAllErrors("-2/3")
    assertThat(exp18.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("negative 2 thirds")

    val exp19 = parseNumericExpressionWithAllErrors("10/11")
    assertThat(exp19.toHumanReadableString(ENGLISH, divAsFraction = true)).isEqualTo("10 over 11")

    val exp20 = parseNumericExpressionWithAllErrors("121/7986")
    assertThat(exp20.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("121 over 7,986")

    val exp21 = parseNumericExpressionWithAllErrors("8/7")
    assertThat(exp21.toHumanReadableString(ENGLISH, divAsFraction = true)).isEqualTo("8 over 7")

    val exp22 = parseNumericExpressionWithAllErrors("-10/-30")
    assertThat(exp22.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("negative the fraction with numerator 10 and denominator negative 30")

    val exp23 = parseAlgebraicExpressionWithAllErrors("1")
    assertThat(exp23.toHumanReadableString(ENGLISH)).isEqualTo("1")

    val exp24 = parseAlgebraicExpressionWithoutOptionalErrors("((1))")
    assertThat(exp24.toHumanReadableString(ENGLISH)).isEqualTo("1")

    val exp25 = parseAlgebraicExpressionWithAllErrors("x")
    assertThat(exp25.toHumanReadableString(ENGLISH)).isEqualTo("x")

    val exp26 = parseAlgebraicExpressionWithoutOptionalErrors("((x))")
    assertThat(exp26.toHumanReadableString(ENGLISH)).isEqualTo("x")

    val exp51 = parseAlgebraicExpressionWithAllErrors("-x")
    assertThat(exp51.toHumanReadableString(ENGLISH)).isEqualTo("negative x")

    val exp52 = parseAlgebraicExpressionWithAllErrors("+x")
    assertThat(exp52.toHumanReadableString(ENGLISH)).isEqualTo("positive x")

    val exp27 = parseAlgebraicExpressionWithAllErrors("1+x")
    assertThat(exp27.toHumanReadableString(ENGLISH)).isEqualTo("1 plus x")

    val exp28 = parseAlgebraicExpressionWithAllErrors("1-x")
    assertThat(exp28.toHumanReadableString(ENGLISH)).isEqualTo("1 minus x")

    val exp29 = parseAlgebraicExpressionWithAllErrors("1*x")
    assertThat(exp29.toHumanReadableString(ENGLISH)).isEqualTo("1 times x")

    val exp30 = parseAlgebraicExpressionWithAllErrors("1/x")
    assertThat(exp30.toHumanReadableString(ENGLISH)).isEqualTo("1 divided by x")

    val exp31 = parseAlgebraicExpressionWithAllErrors("1/x")
    assertThat(exp31.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("the fraction with numerator 1 and denominator x")

    val exp32 = parseAlgebraicExpressionWithAllErrors("1+(1-x)")
    assertThat(exp32.toHumanReadableString(ENGLISH))
      .isEqualTo("1 plus open parenthesis 1 minus x close parenthesis")

    val exp33 = parseAlgebraicExpressionWithAllErrors("2x")
    assertThat(exp33.toHumanReadableString(ENGLISH)).isEqualTo("2 x")

    val exp34 = parseAlgebraicExpressionWithAllErrors("xy")
    assertThat(exp34.toHumanReadableString(ENGLISH)).isEqualTo("x times y")

    val exp35 = parseAlgebraicExpressionWithAllErrors("z")
    assertThat(exp35.toHumanReadableString(ENGLISH)).isEqualTo("zed")

    val exp36 = parseAlgebraicExpressionWithAllErrors("2xz")
    assertThat(exp36.toHumanReadableString(ENGLISH)).isEqualTo("2 x times zed")

    val exp37 = parseAlgebraicExpressionWithAllErrors("x^2")
    assertThat(exp37.toHumanReadableString(ENGLISH)).isEqualTo("x raised to the power of 2")

    val exp38 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1+x)")
    assertThat(exp38.toHumanReadableString(ENGLISH))
      .isEqualTo("x raised to the power of open parenthesis 1 plus x close parenthesis")

    val exp39 = parseAlgebraicExpressionWithAllErrors("100000*2")
    assertThat(exp39.toHumanReadableString(ENGLISH)).isEqualTo("100,000 times 2")

    val exp40 = parseAlgebraicExpressionWithAllErrors("sqrt(2)")
    assertThat(exp40.toHumanReadableString(ENGLISH)).isEqualTo("square root of 2")

    val exp41 = parseAlgebraicExpressionWithAllErrors("sqrt(x)")
    assertThat(exp41.toHumanReadableString(ENGLISH)).isEqualTo("square root of x")

    val exp42 = parseAlgebraicExpressionWithAllErrors("√2")
    assertThat(exp42.toHumanReadableString(ENGLISH)).isEqualTo("square root of 2")

    val exp43 = parseAlgebraicExpressionWithAllErrors("√x")
    assertThat(exp43.toHumanReadableString(ENGLISH)).isEqualTo("square root of x")

    val exp44 = parseAlgebraicExpressionWithAllErrors("sqrt(1+2)")
    assertThat(exp44.toHumanReadableString(ENGLISH))
      .isEqualTo("start square root 1 plus 2 end square root")

    val exp45 = parseAlgebraicExpressionWithAllErrors("sqrt(1+x)")
    assertThat(exp45.toHumanReadableString(ENGLISH))
      .isEqualTo("start square root 1 plus x end square root")

    val exp46 = parseAlgebraicExpressionWithAllErrors("√(1+x)")
    assertThat(exp46.toHumanReadableString(ENGLISH))
      .isEqualTo("start square root open parenthesis 1 plus x close parenthesis end square root")

    for (denominatorToCheck in 1..10) {
      for (numeratorToCheck in 0..denominatorToCheck) {
        val exp16 = parseAlgebraicExpressionWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16.toHumanReadableString(ENGLISH, divAsFraction = true))
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp47 = parseAlgebraicExpressionWithAllErrors("1")
    assertThat(exp47.toHumanReadableString(ENGLISH)).isEqualTo("1")

    val exp48 = parseAlgebraicExpressionWithAllErrors("x(5-y)")
    assertThat(exp48.toHumanReadableString(ENGLISH))
      .isEqualTo("x times open parenthesis 5 minus y close parenthesis")

    val eq2 = parseAlgebraicEquationWithAllErrors("x=1/y")
    assertThat(eq2.toHumanReadableString(ENGLISH)).isEqualTo("x equals 1 divided by y")

    val eq3 = parseAlgebraicEquationWithAllErrors("x=1/2")
    assertThat(eq3.toHumanReadableString(ENGLISH)).isEqualTo("x equals 1 divided by 2")

    val eq4 = parseAlgebraicEquationWithAllErrors("x=1/y")
    assertThat(eq4.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("x equals the fraction with numerator 1 and denominator y")

    val eq5 = parseAlgebraicEquationWithAllErrors("x=1/2")
    assertThat(eq5.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo("x equals 1 half")

    // Tests from examples in the PRD
    val eq6 = parseAlgebraicEquationWithAllErrors("3x^2+4y=62")
    assertThat(eq6.toHumanReadableString(ENGLISH))
      .isEqualTo("3 x raised to the power of 2 plus 4 y equals 62")

    val exp53 = parseAlgebraicExpressionWithAllErrors("(x+6)/(x-4)")
    assertThat(exp53.toHumanReadableString(ENGLISH, divAsFraction = true))
      .isEqualTo(
        "the fraction with numerator open parenthesis x plus 6 close parenthesis and denominator" +
          " open parenthesis x minus 4 close parenthesis"
      )

    val exp54 = parseAlgebraicExpressionWithoutOptionalErrors("4*(x)^(2)+20x")
    assertThat(exp54.toHumanReadableString(ENGLISH))
      .isEqualTo("4 times x raised to the power of 2 plus 20 x")

    val exp55 = parseAlgebraicExpressionWithAllErrors("3+x-5")
    assertThat(exp55.toHumanReadableString(ENGLISH)).isEqualTo("3 plus x minus 5")

    val exp56 = parseAlgebraicExpressionWithAllErrors("Z+A-Z", allowedVariables = listOf("A", "Z"))
    assertThat(exp56.toHumanReadableString(ENGLISH)).isEqualTo("Zed plus A minus Zed")

    val exp57 =
      parseAlgebraicExpressionWithAllErrors("6C-5A-1", allowedVariables = listOf("A", "C"))
    assertThat(exp57.toHumanReadableString(ENGLISH)).isEqualTo("6 C minus 5 A minus 1")

    val exp58 = parseAlgebraicExpressionWithAllErrors("5*Z-w", allowedVariables = listOf("Z", "w"))
    assertThat(exp58.toHumanReadableString(ENGLISH)).isEqualTo("5 times Zed minus w")

    val exp59 =
      parseAlgebraicExpressionWithAllErrors("L*S-3S+L", allowedVariables = listOf("L", "S"))
    assertThat(exp59.toHumanReadableString(ENGLISH)).isEqualTo("L times S minus 3 S plus L")

    val exp60 = parseAlgebraicExpressionWithAllErrors("2*(2+6+3+4)")
    assertThat(exp60.toHumanReadableString(ENGLISH))
      .isEqualTo("2 times open parenthesis 2 plus 6 plus 3 plus 4 close parenthesis")

    val exp61 = parseAlgebraicExpressionWithAllErrors("sqrt(64)")
    assertThat(exp61.toHumanReadableString(ENGLISH)).isEqualTo("square root of 64")

    val exp62 = parseAlgebraicExpressionWithAllErrors("√(a+b)", allowedVariables = listOf("a", "b"))
    assertThat(exp62.toHumanReadableString(ENGLISH))
      .isEqualTo("start square root open parenthesis a plus b close parenthesis end square root")

    val exp63 = parseAlgebraicExpressionWithAllErrors("3*10^-5")
    assertThat(exp63.toHumanReadableString(ENGLISH))
      .isEqualTo("3 times 10 raised to the power of negative 5")

    val exp64 =
      parseAlgebraicExpressionWithoutOptionalErrors(
        "((x+2y)+5*(a-2b)+z)", allowedVariables = listOf("x", "y", "a", "b", "z")
      )
    assertThat(exp64.toHumanReadableString(ENGLISH))
      .isEqualTo(
        "open parenthesis open parenthesis x plus 2 y close parenthesis plus 5 times open" +
          " parenthesis a minus 2 b close parenthesis plus zed close parenthesis"
      )
  }

  @DslMarker
  private annotation class ExpressionComparatorMarker

  // See: https://kotlinlang.org/docs/type-safe-builders.html.
  private class MathExpressionSubject(
    metadata: FailureMetadata,
    private val actual: MathExpression
  ) : Subject(metadata, actual) {
    fun hasStructureThatMatches(init: ExpressionComparator.() -> Unit): ExpressionComparator {
      // TODO: maybe verify that all aspects are verified?
      return ExpressionComparator.createFromExpression(actual).also(init)
    }

    fun evaluatesToRationalThat(): FractionSubject =
      assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.RATIONAL).rational)

    fun evaluatesToIrrationalThat(): DoubleSubject =
      assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.IRRATIONAL).irrational)

    fun evaluatesToIntegerThat(): IntegerSubject =
      assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.INTEGER).integer)

    private fun evaluateAsReal(expectedType: Real.RealTypeCase): Real {
      val real = actual.evaluateAsNumericExpression()
      assertWithMessage("Failed to evaluate numeric expression").that(real).isNotNull()
      assertWithMessage("Expected constant to evaluate to $expectedType")
        .that(real?.realTypeCase)
        .isEqualTo(expectedType)
      return checkNotNull(real) // Just to remove the nullable operator; the actual check is above.
    }

    // TODO: update DSL to not have return values (since it's unnecessary).
    @ExpressionComparatorMarker
    class ExpressionComparator private constructor(private val expression: MathExpression) {
      // TODO: convert to constant comparator?
      fun constant(init: ConstantComparator.() -> Unit): ConstantComparator =
        ConstantComparator.createFromExpression(expression).also(init)

      fun variable(init: VariableComparator.() -> Unit): VariableComparator =
        VariableComparator.createFromExpression(expression).also(init)

      fun addition(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
        return BinaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathBinaryOperation.Operator.ADD
        ).also(init)
      }

      fun subtraction(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
        return BinaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathBinaryOperation.Operator.SUBTRACT
        ).also(init)
      }

      fun multiplication(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
        return BinaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathBinaryOperation.Operator.MULTIPLY
        ).also(init)
      }

      fun division(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
        return BinaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathBinaryOperation.Operator.DIVIDE
        ).also(init)
      }

      fun exponentiation(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
        return BinaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathBinaryOperation.Operator.EXPONENTIATE
        ).also(init)
      }

      fun negation(init: UnaryOperationComparator.() -> Unit): UnaryOperationComparator {
        return UnaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathUnaryOperation.Operator.NEGATE
        ).also(init)
      }

      fun positive(init: UnaryOperationComparator.() -> Unit): UnaryOperationComparator {
        return UnaryOperationComparator.createFromExpression(
          expression,
          expectedOperator = MathUnaryOperation.Operator.POSITIVE
        ).also(init)
      }

      fun functionCallTo(
        type: MathFunctionCall.FunctionType,
        init: FunctionCallComparator.() -> Unit
      ): FunctionCallComparator {
        return FunctionCallComparator.createFromExpression(
          expression,
          expectedFunctionType = type
        ).also(init)
      }

      fun group(init: ExpressionComparator.() -> Unit): ExpressionComparator {
        return createFromExpression(expression.group).also(init)
      }

      internal companion object {
        fun createFromExpression(expression: MathExpression): ExpressionComparator =
          ExpressionComparator(expression)
      }
    }

    @ExpressionComparatorMarker
    class ConstantComparator private constructor(private val constant: Real) {
      fun withIntegerValueThat(): IntegerSubject {
        assertThat(constant.realTypeCase).isEqualTo(Real.RealTypeCase.INTEGER)
        return assertThat(constant.integer)
      }

      fun withIrrationalValueThat(): DoubleSubject {
        assertThat(constant.realTypeCase).isEqualTo(Real.RealTypeCase.IRRATIONAL)
        return assertThat(constant.irrational)
      }

      internal companion object {
        fun createFromExpression(expression: MathExpression): ConstantComparator {
          assertThat(expression.expressionTypeCase).isEqualTo(CONSTANT)
          return ConstantComparator(expression.constant)
        }
      }
    }

    @ExpressionComparatorMarker
    class VariableComparator private constructor(private val variableName: String) {
      fun withNameThat(): StringSubject = assertThat(variableName)

      internal companion object {
        fun createFromExpression(expression: MathExpression): VariableComparator {
          assertThat(expression.expressionTypeCase).isEqualTo(VARIABLE)
          return VariableComparator(expression.variable)
        }
      }
    }

    @ExpressionComparatorMarker
    class BinaryOperationComparator private constructor(
      private val operation: MathBinaryOperation
    ) {
      fun leftOperand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
        ExpressionComparator.createFromExpression(operation.leftOperand).also(init)

      fun rightOperand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
        ExpressionComparator.createFromExpression(operation.rightOperand).also(init)

      internal companion object {
        fun createFromExpression(
          expression: MathExpression,
          expectedOperator: MathBinaryOperation.Operator
        ): BinaryOperationComparator {
          assertThat(expression.expressionTypeCase).isEqualTo(BINARY_OPERATION)
          assertWithMessage("Expected binary operation with operator: $expectedOperator")
            .that(expression.binaryOperation.operator)
            .isEqualTo(expectedOperator)
          return BinaryOperationComparator(expression.binaryOperation)
        }
      }
    }

    @ExpressionComparatorMarker
    class UnaryOperationComparator private constructor(
      private val operation: MathUnaryOperation
    ) {
      fun operand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
        ExpressionComparator.createFromExpression(operation.operand).also(init)

      internal companion object {
        fun createFromExpression(
          expression: MathExpression,
          expectedOperator: MathUnaryOperation.Operator
        ): UnaryOperationComparator {
          assertThat(expression.expressionTypeCase).isEqualTo(UNARY_OPERATION)
          assertWithMessage("Expected unary operation with operator: $expectedOperator")
            .that(expression.unaryOperation.operator)
            .isEqualTo(expectedOperator)
          return UnaryOperationComparator(expression.unaryOperation)
        }
      }
    }

    @ExpressionComparatorMarker
    class FunctionCallComparator private constructor(
      private val functionCall: MathFunctionCall
    ) {
      fun argument(init: ExpressionComparator.() -> Unit): ExpressionComparator =
        ExpressionComparator.createFromExpression(functionCall.argument).also(init)

      internal companion object {
        fun createFromExpression(
          expression: MathExpression,
          expectedFunctionType: MathFunctionCall.FunctionType
        ): FunctionCallComparator {
          assertThat(expression.expressionTypeCase).isEqualTo(FUNCTION_CALL)
          assertWithMessage("Expected function call to: $expectedFunctionType")
            .that(expression.functionCall.functionType)
            .isEqualTo(expectedFunctionType)
          return FunctionCallComparator(expression.functionCall)
        }
      }
    }
  }

  private class MathEquationSubject(
    metadata: FailureMetadata,
    private val actual: MathEquation
  ) : Subject(metadata, actual) {
    fun hasLeftHandSideThat(): MathExpressionSubject = assertThat(actual.leftSide)

    fun hasRightHandSideThat(): MathExpressionSubject = assertThat(actual.rightSide)
  }

  // TODO: move this to a common location.
  private class FractionSubject(
    metadata: FailureMetadata,
    private val actual: Fraction
  ) : Subject(metadata, actual) {
    fun hasNegativePropertyThat(): BooleanSubject = assertThat(actual.isNegative)

    fun hasWholeNumberThat(): IntegerSubject = assertThat(actual.wholeNumber)

    fun hasNumeratorThat(): IntegerSubject = assertThat(actual.numerator)

    fun hasDenominatorThat(): IntegerSubject = assertThat(actual.denominator)

    fun evaluatesToRealThat(): DoubleSubject = assertThat(actual.toDouble())
  }

  private companion object {
    // TODO: fix helper API.

    private fun expectFailureWhenParsingNumericExpression(expression: String): MathParsingError {
      val result = parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathExpression>).error
    }

    private fun parseNumericExpressionWithoutOptionalErrors(expression: String): MathExpression {
      return (parseNumericExpressionInternal(expression, ErrorCheckingMode.REQUIRED_ONLY) as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionWithAllErrors(expression: String): MathExpression {
      return (parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS) as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionInternal(
      expression: String, errorCheckingMode: ErrorCheckingMode
    ): MathParsingResult<MathExpression> {
      return NumericExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }

    private fun expectFailureWhenParsingAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingError {
      val result =
        parseAlgebraicExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathExpression>).error
    }

    private fun parseAlgebraicExpressionWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return (parseAlgebraicExpressionInternal(expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables) as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return (parseAlgebraicExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables) as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathExpression> {
      return NumericExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      )
    }

    private fun expectFailureWhenParsingAlgebraicEquation(expression: String): MathParsingError {
      val result = parseAlgebraicEquationInternal(expression, ErrorCheckingMode.ALL_ERRORS)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathEquation>).error
    }

    private fun parseAlgebraicEquationWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      return (parseAlgebraicEquationInternal(expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables) as MathParsingResult.Success<MathEquation>).result
    }

    private fun parseAlgebraicEquationInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathEquation> {
      return NumericExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables, errorCheckingMode
      )
    }

    private fun assertThat(actual: MathExpression): MathExpressionSubject =
      assertAbout(::MathExpressionSubject).that(actual)

    private fun assertThat(actual: MathEquation): MathEquationSubject =
      assertAbout(::MathEquationSubject).that(actual)

    private fun assertThat(actual: Fraction): FractionSubject =
      assertAbout(::FractionSubject).that(actual)
  }
}
