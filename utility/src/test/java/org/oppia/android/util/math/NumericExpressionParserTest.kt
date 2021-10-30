package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.BooleanSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
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
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.LooperMode
import kotlin.math.sqrt

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionParserTest {
  @Test
  fun testLotsOfCases() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsing("")

    val expression1 = parseExpression("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    expectFailureWhenParsing("x")

    val expression2 = parseExpression("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseExpression("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withIrrationalValueThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression3).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)

    expectFailureWhenParsing("   x ")

    expectFailureWhenParsing(" z  x ")

    val expression4 = parseExpression("2^3^2")
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

    val expression23 = parseExpression("(2^3)^2")
    assertThat(expression23).hasStructureThatMatches {
      exponentiation {
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression23).evaluatesToIntegerThat().isEqualTo(64)

    val expression24 = parseExpression("512/32/4")
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

    val expression25 = parseExpression("512/(32/4)")
    assertThat(expression25).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(512)
          }
        }
        rightOperand {
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
    assertThat(expression25).evaluatesToIntegerThat().isEqualTo(64)

    val expression5 = parseExpression("sqrt(2)")
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

    expectFailureWhenParsing("sqr(2)")

    expectFailureWhenParsing("xyz(2)")

    val expression6 = parseExpression("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    expectFailureWhenParsing("73 2")

    val expression7 = parseExpression("3*2-3+4^7*8/3*2+7")
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

    expectFailureWhenParsing("x = √2 × 7 ÷ 4")

    val expression8 = parseExpression("(1+2)(3+4)")
    assertThat(expression8).hasStructureThatMatches {
      multiplication {
        leftOperand {
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
        rightOperand {
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
    assertThat(expression8).evaluatesToIntegerThat().isEqualTo(21)

    val expression9 = parseExpression("(1+2)2")
    assertThat(expression9).hasStructureThatMatches {
      multiplication {
        leftOperand {
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
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression9).evaluatesToIntegerThat().isEqualTo(6)

    val expression10 = parseExpression("2(1+2)")
    assertThat(expression10).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
        rightOperand {
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
    assertThat(expression10).evaluatesToIntegerThat().isEqualTo(6)

    val expression11 = parseExpression("sqrt(2)3")
    assertThat(expression11).hasStructureThatMatches {
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
            withIntegerValueThat().isEqualTo(3)
          }
        }
      }
    }
    assertThat(expression11).evaluatesToIrrationalThat().isWithin(1e-5).of(3.0 * sqrt(2.0))

    val expression12 = parseExpression("3sqrt(2)")
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

    expectFailureWhenParsing("xsqrt(2)")

    // TODO: add version with implicit multiplication (has wrong associativity today).
    val expression13 = parseExpression("sqrt(2)*(1+2)*(3-2^5)")
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
        rightOperand {
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
    assertThat(expression13).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression14 = parseExpression("((3))")
    assertThat(expression14).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(3)
      }
    }
    assertThat(expression14).evaluatesToIntegerThat().isEqualTo(3)

    val expression15 = parseExpression("++3")
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

    val expression16 = parseExpression("--4")
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

    val expression17 = parseExpression("1+-4")
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

    val expression18 = parseExpression("1++4")
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

    val expression19 = parseExpression("1--4")
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

    expectFailureWhenParsing("1-^-4")

    val expression20 = parseExpression("√2 × 7 ÷ 4")
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

    expectFailureWhenParsing("1+2 asdf")

    val expression21 = parseExpression("sqrt(2)sqrt(3)sqrt(4)")
    assertThat(expression21).hasStructureThatMatches {
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
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withIntegerValueThat().isEqualTo(3)
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
      }
    }
    assertThat(expression21)
      .evaluatesToIrrationalThat()
      .isWithin(1e-5)
      .of(sqrt(2.0) * sqrt(3.0) * sqrt(4.0))

    val expression22 = parseExpression("(1+2)(3-7^2)(5+-17)")
    assertThat(expression22).hasStructureThatMatches {
      multiplication {
        leftOperand {
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
        rightOperand {
          multiplication {
            leftOperand {
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
            rightOperand {
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
    }
    assertThat(expression22).evaluatesToIntegerThat().isEqualTo(1656)

    val expression26 = parseExpression("3^-2")
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

    val expression27 = parseExpression("(3^-2)^(3^-2)")
    assertThat(expression27).hasStructureThatMatches {
      exponentiation {
        leftOperand {
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
        rightOperand {
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
    assertThat(expression27).evaluatesToIrrationalThat().isWithin(1e-5).of(0.78338103693)

    val expression28 = parseExpression("1-3^sqrt(4)")
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
    // TODO: test implicit version (currently broken in parser)
    val expression29 = parseExpression("3÷2*(3+4)")
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
    assertThat(expression29).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    // TODO: add others
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

    @ExpressionComparatorMarker
    class ExpressionComparator private constructor(private val expression: MathExpression) {
      // TODO: convert to constant comparator?
      fun constant(init: ConstantComparator.() -> Unit): ConstantComparator =
        ConstantComparator.createFromExpression(expression).also(init)

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
    private fun expectFailureWhenParsing(expression: String) {
      assertThrows(NumericExpressionParser.ParseException::class) { parseExpression(expression) }
    }

    private fun parseExpression(expression: String): MathExpression {
      return NumericExpressionParser(expression).parse()
    }

    private fun assertThat(actual: MathExpression): MathExpressionSubject =
      assertAbout(::MathExpressionSubject).that(actual)

    private fun assertThat(actual: Fraction): FractionSubject =
      assertAbout(::FractionSubject).that(actual)
  }
}
