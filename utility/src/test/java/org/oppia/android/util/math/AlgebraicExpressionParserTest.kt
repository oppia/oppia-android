package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/**
 * Tests for [MathExpressionParser].
 *
 * This test suite specifically focuses on verifying expressions that include variables. It largely
 * assumes that core properties around precedence and associativity, and general handling of numeric
 * expressions are correct through the tests managed by [NumericExpressionParserTest]. This is a
 * valid approach since these tests are aware that an implementation is shared between the two. In
 * the event the implementations are forked in the future, this test suite should correspondingly be
 * updated to include tests for the core portions of parsing. For the most part, this suite assumes
 * that its tests properly verify that variables can be a step-in replacement for numbers when
 * considering numeric expression tests (with some special exceptions around implicit multiplication
 * to enable polynomial syntax).
 *
 * This suite does not test errors--see [MathExpressionParserTest] for those tests. Further, it does
 * not test algebraic equations (see [AlgebraicEquationParserTest] for those tests).
 *
 * This test suite largely focuses on demonstrating polynomial syntax since that's the expected
 * principal use of algebraic expressions and equations.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AlgebraicExpressionParserTest {
  @Test
  fun testParse_variable_returnsExpWithVariable() {
    val expression = parseAlgebraicExpressionWithAllErrors("x")

    assertThat(expression).hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testParse_variablePlusInteger_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x+1")

    assertThat(expression).hasStructureThatMatches {
      addition {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testParse_integerPlusVariable_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("1+x")

    assertThat(expression).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun testParse_variableMinusInteger_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x-1")

    assertThat(expression).hasStructureThatMatches {
      subtraction {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testParse_variableMinusInteger_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x−1")

    assertThat(expression).hasStructureThatMatches {
      subtraction {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testParse_integerMinusVariable_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("1-x")

    assertThat(expression).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun testParse_integerMinusVariable_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("1−x")

    assertThat(expression).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun testParse_variableTimesInteger_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x*2")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_variableTimesInteger_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x×2")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_integerTimesVariable_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("2*x")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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

  @Test
  fun testParse_integerTimesVariable_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("2×x")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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

  @Test
  fun testParse_variableDividedByInteger_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x/2")

    assertThat(expression).hasStructureThatMatches {
      division {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_variableDividedByInteger_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x÷2")

    assertThat(expression).hasStructureThatMatches {
      division {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_integerDividedByVariable_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("1/x")

    assertThat(expression).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun testParse_integerDividedByVariable_mathSymbol_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("1÷x")

    assertThat(expression).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun testParse_variableRaisedToInteger_returnsExpWithVariableBasedBinaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("x^2")

    // This demonstrates basic quadratic polynomial syntax.
    assertThat(expression).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_intRaisedToVariable_noOptionalErrors_returnsExpWithVariableBasedBinaryOperation() {
    // Note that optional errors prohibit variables in exponents.
    val expression = parseAlgebraicExpressionWithoutOptionalErrors("2^x")

    assertThat(expression).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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

  @Test
  fun testParse_negatedVariable_returnsExpWithVariableBasedUnaryOperation() {
    val expression = parseAlgebraicExpressionWithAllErrors("-x")

    assertThat(expression).hasStructureThatMatches {
      negation {
        operand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  fun testParse_positiveVariable_withoutOptionalErrors_returnsExpWithVariableBasedUnaryOperation() {
    // Note that optional errors prohibit unary positive operations.
    val expression = parseAlgebraicExpressionWithoutOptionalErrors("+x")

    assertThat(expression).hasStructureThatMatches {
      positive {
        operand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  fun testParse_variableInGroup_returnsExpWithVariableInGroup() {
    val expression = parseAlgebraicExpressionWithAllErrors("2*(1+x)")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(1)
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
    }
  }

  @Test
  fun testParse_inlineSquareRootOfX_returnsExpWithVariableInFunctionArgument() {
    val expression = parseAlgebraicExpressionWithAllErrors("√x")

    assertThat(expression).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  fun testParse_squareRootOfX_returnsExpWithVariableInFunctionArgument() {
    val expression = parseAlgebraicExpressionWithAllErrors("sqrt(x)")

    assertThat(expression).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  fun testParse_integerAndVariable_returnsExpWithImplicitMultiplication() {
    val expression = parseAlgebraicExpressionWithAllErrors("2x")

    // This also demonstrates basic polynomial syntax for linear equations.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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

  @Test
  fun testParse_negatedIntegerAndVariable_returnsExpWithImplicitMultiplicationWithUnary() {
    val expression = parseAlgebraicExpressionWithAllErrors("-2x")

    // Similar to the previous test, but this ensures negation ordering (relative to variables &
    // implicit multiplication).
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          negation {
            operand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
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

  @Test
  fun testParse_xInlineSquareRootOfInteger_returnsExpWithImplicitMultiplication() {
    val expression = parseAlgebraicExpressionWithAllErrors("x√2")

    // A variable next to a square root indicates implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
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
  fun testParse_xSquareRootOfInteger_returnsExpWithImplicitMultiplication() {
    val expression = parseAlgebraicExpressionWithAllErrors("xsqrt(2)")

    // Even with 'sqrt' and x being tightly together, the parser still sees a distinct variable and
    // function.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
          }
        }
        rightOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
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
  fun testParse_variableTimesVariable_same_returnsExpWithMultiedVariables() {
    val expression = parseAlgebraicExpressionWithAllErrors("x*x")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          variable {
            withNameThat().isEqualTo("x")
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

  @Test
  fun testParse_variableTimesVariable_returnsExpWithMultipleVariables() {
    val expression = parseAlgebraicExpressionWithAllErrors("x*y")

    assertThat(expression).hasStructureThatMatches {
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
  }

  @Test
  fun testParse_variableNextToVariable_returnsExpWithImplicitMultiplication() {
    val expression = parseAlgebraicExpressionWithAllErrors("xy")

    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
  }

  @Test
  fun testParse_threeSubsequentVariables_returnsExpWithImplicitMultAndLeftAssociativity() {
    val expression = parseAlgebraicExpressionWithAllErrors("xyz")

    // 'xyz' results in a right-associative implicit multiplication (i.e. (x*y)*z).
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          multiplication(isImplicit = true) {
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
  }

  @Test
  fun testParse_threeSubsequentVariables_customVariables_returnsExpWithImplicitMult() {
    val allowedVariables = listOf("i", "j", "k")

    val expression = parseAlgebraicExpressionWithAllErrors("ijk", allowedVariables)

    // Other variables can be used, too.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          multiplication(isImplicit = true) {
            leftOperand {
              variable {
                withNameThat().isEqualTo("i")
              }
            }
            rightOperand {
              variable {
                withNameThat().isEqualTo("j")
              }
            }
          }
        }
        rightOperand {
          variable {
            withNameThat().isEqualTo("k")
          }
        }
      }
    }
  }

  @Test
  fun testParse_fullQuadraticExpression_returnsExpWithCorrectValuesAndOrders() {
    val expression = parseAlgebraicExpressionWithAllErrors("-8x^3-7.4x^2+x-12/√2")

    // This combines all of the distinct pieces tested earlier to demonstrate full polynomial
    // syntax.
    assertThat(expression).hasStructureThatMatches {
      // -8x^3-7.4x^2+x-12/√2 -> ((((-8) * (x^3)) - (7.4 * (x^2))) + x) - (12/√2)
      subtraction {
        leftOperand {
          // (((-8) * (x^3)) - (7.4 * (x^2))) + x
          addition {
            leftOperand {
              // ((-8) * (x^3)) - (7.4 * (x^2))
              subtraction {
                leftOperand {
                  // (-8) * (x^3)
                  multiplication(isImplicit = true) {
                    leftOperand {
                      // -8
                      negation {
                        operand {
                          // 8
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(8)
                          }
                        }
                      }
                    }
                    rightOperand {
                      // x^3
                      exponentiation {
                        leftOperand {
                          // x
                          variable {
                            withNameThat().isEqualTo("x")
                          }
                        }
                        rightOperand {
                          // 3
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(3)
                          }
                        }
                      }
                    }
                  }
                }
                rightOperand {
                  // 7.4 * (x^2)
                  multiplication(isImplicit = true) {
                    leftOperand {
                      // 7.4
                      constant {
                        withValueThat().isIrrationalThat().isWithin(1e-5).of(7.4)
                      }
                    }
                    rightOperand {
                      // x^2
                      exponentiation {
                        leftOperand {
                          // x
                          variable {
                            withNameThat().isEqualTo("x")
                          }
                        }
                        rightOperand {
                          // 2
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(2)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            rightOperand {
              // x
              variable {
                withNameThat().isEqualTo("x")
              }
            }
          }
        }
        rightOperand {
          // 12/√2
          division {
            leftOperand {
              // 12
              constant {
                withValueThat().isIntegerThat().isEqualTo(12)
              }
            }
            rightOperand {
              // √2
              functionCallTo(SQUARE_ROOT) {
                argument {
                  // 2
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testParse_expressionWithMultipleVariableQuadraticTerms_returnsExpWithCorrectValsAndOrders() {
    val expression = parseAlgebraicExpressionWithAllErrors("12x^2y^2-yz^2+yzx-731z")

    // This builds on the polynomial syntax demonstrated above by demonstrating multi-variable
    // implicit multiplication for multi-dimensional polynomials. Note that this also demonstrates
    // that exponents can imply multiplication when the base is a variable (as part of polynomial
    // syntax) which is a functional difference from numeric-only expressions (where subsequent
    // numeric exponents never imply multiplication).
    assertThat(expression).hasStructureThatMatches {
      // 12x^2y^2-yz^2+yzx-731z -> ((((12*(x^2))*(y^2))-(y*(z^2)))+((y*z)*x))-(731*z)
      subtraction {
        leftOperand {
          // (((12*(x^2))*(y^2))-(y*(z^2)))+((y*z)*x)
          addition {
            leftOperand {
              // ((12*(x^2))*(y^2))-(y*(z^2))
              subtraction {
                leftOperand {
                  // (12*(x^2))*(y^2)
                  multiplication(isImplicit = true) {
                    leftOperand {
                      // 12*(x^2)
                      multiplication(isImplicit = true) {
                        leftOperand {
                          // 12
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(12)
                          }
                        }
                        rightOperand {
                          // x^2
                          exponentiation {
                            leftOperand {
                              // x
                              variable {
                                withNameThat().isEqualTo("x")
                              }
                            }
                            rightOperand {
                              // 2
                              constant {
                                withValueThat().isIntegerThat().isEqualTo(2)
                              }
                            }
                          }
                        }
                      }
                    }
                    rightOperand {
                      // y^2
                      exponentiation {
                        leftOperand {
                          // y
                          variable {
                            withNameThat().isEqualTo("y")
                          }
                        }
                        rightOperand {
                          // 2
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(2)
                          }
                        }
                      }
                    }
                  }
                }
                rightOperand {
                  // y*(z^2)
                  multiplication(isImplicit = true) {
                    leftOperand {
                      // y
                      variable {
                        withNameThat().isEqualTo("y")
                      }
                    }
                    rightOperand {
                      // z^2
                      exponentiation {
                        leftOperand {
                          // z
                          variable {
                            withNameThat().isEqualTo("z")
                          }
                        }
                        rightOperand {
                          // 2
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(2)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            rightOperand {
              // (y*z)*x
              multiplication(isImplicit = true) {
                leftOperand {
                  // y*z
                  multiplication(isImplicit = true) {
                    leftOperand {
                      // y
                      variable {
                        withNameThat().isEqualTo("y")
                      }
                    }
                    rightOperand {
                      // z
                      variable {
                        withNameThat().isEqualTo("z")
                      }
                    }
                  }
                }
                rightOperand {
                  // x
                  variable {
                    withNameThat().isEqualTo("x")
                  }
                }
              }
            }
          }
        }
        rightOperand {
          // 731*z
          multiplication(isImplicit = true) {
            leftOperand {
              // 731
              constant {
                withValueThat().isIntegerThat().isEqualTo(731)
              }
            }
            rightOperand {
              // z
              variable {
                withNameThat().isEqualTo("z")
              }
            }
          }
        }
      }
    }
  }

  private companion object {
    private fun parseAlgebraicExpressionWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return parseAlgebraicExpressionInternal(
        expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables
      )
    }

    private fun parseAlgebraicExpressionWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return parseAlgebraicExpressionInternal(
        expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables
      )
    }

    private fun parseAlgebraicExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode,
      allowedVariables: List<String>
    ): MathExpression {
      val result = MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      )
      assertThat(result).isInstanceOf(MathParsingResult.Success::class.java)
      return (result as MathParsingResult.Success<MathExpression>).result
    }
  }
}
