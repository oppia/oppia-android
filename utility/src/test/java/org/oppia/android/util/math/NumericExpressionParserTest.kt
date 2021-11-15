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
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.LooperMode
import kotlin.math.sqrt
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionParserTest {
  @Test
  fun testLotsOfCasesForNumericExpression() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsingNumericExpression("")

    val expression1 = parseNumericExpression("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    expectFailureWhenParsingNumericExpression("x")

    val expression2 = parseNumericExpression("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseNumericExpression("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withIrrationalValueThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression3).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)

    expectFailureWhenParsingNumericExpression("   x ")

    expectFailureWhenParsingNumericExpression(" z  x ")

    val expression4 = parseNumericExpression("2^3^2")
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

    val expression23 = parseNumericExpression("(2^3)^2")
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

    val expression24 = parseNumericExpression("512/32/4")
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

    val expression25 = parseNumericExpression("512/(32/4)")
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

    val expression5 = parseNumericExpression("sqrt(2)")
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

    val expression6 = parseNumericExpression("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    expectFailureWhenParsingNumericExpression("73 2")

    // Verify order of operations between higher & lower precedent operators.
    val expression32 = parseNumericExpression("3+4^7")
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

    val expression7 = parseNumericExpression("3*2-3+4^7*8/3*2+7")
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

    val expression8 = parseNumericExpression("(1+2)(3+4)")
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

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingNumericExpression("(1+2)2")

    val expression10 = parseNumericExpression("2(1+2)")
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

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingNumericExpression("sqrt(2)3")

    val expression12 = parseNumericExpression("3sqrt(2)")
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

    val expression13 = parseNumericExpression("sqrt(2)*(1+2)*(3-2^5)")
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

    val expression58 = parseNumericExpression("sqrt(2)(1+2)(3-2^5)")
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
    assertThat(expression58).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression14 = parseNumericExpression("((3))")
    assertThat(expression14).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(3)
      }
    }
    assertThat(expression14).evaluatesToIntegerThat().isEqualTo(3)

    val expression15 = parseNumericExpression("++3")
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

    val expression16 = parseNumericExpression("--4")
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

    val expression17 = parseNumericExpression("1+-4")
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

    val expression18 = parseNumericExpression("1++4")
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

    val expression19 = parseNumericExpression("1--4")
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

    val expression20 = parseNumericExpression("√2 × 7 ÷ 4")
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

    val expression21 = parseNumericExpression("sqrt(2)sqrt(3)sqrt(4)")
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

    val expression22 = parseNumericExpression("(1+2)(3-7^2)(5+-17)")
    assertThat(expression22).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              // 1+2
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
              // 3-7^2
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
        rightOperand {
          // 5+-17
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
    assertThat(expression22).evaluatesToIntegerThat().isEqualTo(1656)

    val expression26 = parseNumericExpression("3^-2")
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

    val expression27 = parseNumericExpression("(3^-2)^(3^-2)")
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

    val expression28 = parseNumericExpression("1-3^sqrt(4)")
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
    val expression29 = parseNumericExpression("3÷2*(3+4)")
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

    val expression59 = parseNumericExpression("3÷2(3+4)")
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
    assertThat(expression59).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    // Numbers cannot have implicit multiplication unless they are in groups.
    expectFailureWhenParsingNumericExpression("2 2")

    expectFailureWhenParsingNumericExpression("2 2^2")

    expectFailureWhenParsingNumericExpression("2^2 2")

    val expression31 = parseNumericExpression("(3)(4)(5)")
    assertThat(expression31).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
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
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(5)
          }
        }
      }
    }
    assertThat(expression31).evaluatesToIntegerThat().isEqualTo(60)

    val expression33 = parseNumericExpression("2^(3)")
    assertThat(expression33).hasStructureThatMatches {
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
    assertThat(expression33).evaluatesToIntegerThat().isEqualTo(8)

    // Verify that implicit multiple has lower precedence than exponentiation.
    val expression34 = parseNumericExpression("2^(3)(4)")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    assertThat(expression34).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingNumericExpression("2^(3)2^2")

    val expression35 = parseNumericExpression("2^(3)*2^2")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    val expression36 = parseNumericExpression("2^(3)(2^2)")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    assertThat(expression36).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingNumericExpression("2^3(4)2^3")

    val expression38 = parseNumericExpression("2^3(4)*2^3")
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
              constant {
                withIntegerValueThat().isEqualTo(4)
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

    val expression39 = parseNumericExpression("-(1+2)")
    assertThat(expression39).hasStructureThatMatches {
      negation {
        operand {
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
    assertThat(expression39).evaluatesToIntegerThat().isEqualTo(-3)

    // Should pass for algebra.
    expectFailureWhenParsingNumericExpression("-2 x")

    val expression40 = parseNumericExpression("-2 (1+2)")
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
    assertThat(expression40).evaluatesToIntegerThat().isEqualTo(-6)

    val expression41 = parseNumericExpression("-2^3(4)")
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
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression41).evaluatesToIntegerThat().isEqualTo(-32)

    val expression43 = parseNumericExpression("√2^2(3)")
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
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
      }
    }
    assertThat(expression43).evaluatesToIrrationalThat().isWithin(1e-5).of(6.0)

    val expression60 = parseNumericExpression("√(2^2(3))")
    assertThat(expression60).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
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
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression60).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(12.0))

    val expression42 = parseNumericExpression("-2*-2")
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

    val expression44 = parseNumericExpression("2(2)")
    assertThat(expression44).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression44).evaluatesToIntegerThat().isEqualTo(4)

    val expression45 = parseNumericExpression("2sqrt(2)")
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

    val expression46 = parseNumericExpression("2√2")
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

    val expression47 = parseNumericExpression("(2)(2)")
    assertThat(expression47).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression47).evaluatesToIntegerThat().isEqualTo(4)

    val expression48 = parseNumericExpression("sqrt(2)(2)")
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression48).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression49 = parseNumericExpression("sqrt(2)sqrt(2)")
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

    val expression50 = parseNumericExpression("√2√2")
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

    val expression51 = parseNumericExpression("(2)(2)(2)")
    assertThat(expression51).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression51).evaluatesToIntegerThat().isEqualTo(8)

    val expression52 = parseNumericExpression("sqrt(2)sqrt(2)sqrt(2)")
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

    val expression53 = parseNumericExpression("√2√2√2")
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

    val expression54 = parseNumericExpression("2*2/-4+7*2")
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

    val expression55 = parseNumericExpression("(3/(1-2))")
    assertThat(expression55).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression55).evaluatesToIntegerThat().isEqualTo(-3)

    val expression56 = parseNumericExpression("(3)/(1-2)")
    assertThat(expression56).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression56).evaluatesToIntegerThat().isEqualTo(-3)

    val expression57 = parseNumericExpression("3/((1-2))")
    assertThat(expression57).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression57).evaluatesToIntegerThat().isEqualTo(-3)

    // TODO: add others, including tests for malformed expressions throughout the parser &
    //  tokenizer.
  }

  @Test
  fun testLotsOfCasesForAlgebraicExpression() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsingAlgebraicExpression("")

    val expression1 = parseAlgebraicExpression("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    val expression61 = parseAlgebraicExpression("x")
    assertThat(expression61).hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }

    val expression2 = parseAlgebraicExpression("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseAlgebraicExpression("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withIrrationalValueThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression3).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)

    val expression62 = parseAlgebraicExpression("   y ")
    assertThat(expression62).hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }

    val expression63 = parseAlgebraicExpression(" z  x ")
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

    val expression4 = parseAlgebraicExpression("2^3^2")
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

    val expression23 = parseAlgebraicExpression("(2^3)^2")
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

    val expression24 = parseAlgebraicExpression("512/32/4")
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

    val expression25 = parseAlgebraicExpression("512/(32/4)")
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

    val expression5 = parseAlgebraicExpression("sqrt(2)")
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

    val expression64 = parseAlgebraicExpression("xyz(2)")
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }

    val expression6 = parseAlgebraicExpression("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    expectFailureWhenParsingAlgebraicExpression("73 2")

    // Verify order of operations between higher & lower precedent operators.
    val expression32 = parseAlgebraicExpression("3+4^7")
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

    val expression7 = parseAlgebraicExpression("3*2-3+4^7*8/3*2+7")
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

    val expression8 = parseAlgebraicExpression("(1+2)(3+4)")
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

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingAlgebraicExpression("(1+2)2")

    val expression10 = parseAlgebraicExpression("2(1+2)")
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

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingAlgebraicExpression("sqrt(2)3")

    val expression12 = parseAlgebraicExpression("3sqrt(2)")
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

    val expression65 = parseAlgebraicExpression("xsqrt(2)")
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

    val expression13 = parseAlgebraicExpression("sqrt(2)*(1+2)*(3-2^5)")
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

    val expression58 = parseAlgebraicExpression("sqrt(2)(1+2)(3-2^5)")
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
    assertThat(expression58).evaluatesToIrrationalThat().isWithin(1e-5).of(-123.036579926)

    val expression14 = parseAlgebraicExpression("((3))")
    assertThat(expression14).hasStructureThatMatches {
      constant {
        withIntegerValueThat().isEqualTo(3)
      }
    }
    assertThat(expression14).evaluatesToIntegerThat().isEqualTo(3)

    val expression15 = parseAlgebraicExpression("++3")
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

    val expression16 = parseAlgebraicExpression("--4")
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

    val expression17 = parseAlgebraicExpression("1+-4")
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

    val expression18 = parseAlgebraicExpression("1++4")
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

    val expression19 = parseAlgebraicExpression("1--4")
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

    val expression20 = parseAlgebraicExpression("√2 × 7 ÷ 4")
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

    val expression21 = parseAlgebraicExpression("sqrt(2)sqrt(3)sqrt(4)")
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

    val expression22 = parseAlgebraicExpression("(1+2)(3-7^2)(5+-17)")
    assertThat(expression22).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              // 1+2
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
              // 3-7^2
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
        rightOperand {
          // 5+-17
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
    assertThat(expression22).evaluatesToIntegerThat().isEqualTo(1656)

    val expression26 = parseAlgebraicExpression("3^-2")
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

    val expression27 = parseAlgebraicExpression("(3^-2)^(3^-2)")
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

    val expression28 = parseAlgebraicExpression("1-3^sqrt(4)")
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
    val expression29 = parseAlgebraicExpression("3÷2*(3+4)")
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

    val expression59 = parseAlgebraicExpression("3÷2(3+4)")
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
    assertThat(expression59).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    // Numbers cannot have implicit multiplication unless they are in groups.
    expectFailureWhenParsingAlgebraicExpression("2 2")

    expectFailureWhenParsingAlgebraicExpression("2 2^2")

    expectFailureWhenParsingAlgebraicExpression("2^2 2")

    val expression31 = parseAlgebraicExpression("(3)(4)(5)")
    assertThat(expression31).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
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
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(5)
          }
        }
      }
    }
    assertThat(expression31).evaluatesToIntegerThat().isEqualTo(60)

    val expression33 = parseAlgebraicExpression("2^(3)")
    assertThat(expression33).hasStructureThatMatches {
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
    assertThat(expression33).evaluatesToIntegerThat().isEqualTo(8)

    // Verify that implicit multiple has lower precedence than exponentiation.
    val expression34 = parseAlgebraicExpression("2^(3)(4)")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    assertThat(expression34).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingAlgebraicExpression("2^(3)2^2")

    val expression35 = parseAlgebraicExpression("2^(3)*2^2")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    val expression36 = parseAlgebraicExpression("2^(3)(2^2)")
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
              constant {
                withIntegerValueThat().isEqualTo(3)
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
    assertThat(expression36).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can never be an implicit right operand.
    expectFailureWhenParsingAlgebraicExpression("2^3(4)2^3")

    val expression38 = parseAlgebraicExpression("2^3(4)*2^3")
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
              constant {
                withIntegerValueThat().isEqualTo(4)
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

    val expression39 = parseAlgebraicExpression("-(1+2)")
    assertThat(expression39).hasStructureThatMatches {
      negation {
        operand {
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
    assertThat(expression39).evaluatesToIntegerThat().isEqualTo(-3)

    // Should pass for algebra.
    val expression66 = parseAlgebraicExpression("-2 x")
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

    val expression40 = parseAlgebraicExpression("-2 (1+2)")
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
    assertThat(expression40).evaluatesToIntegerThat().isEqualTo(-6)

    val expression41 = parseAlgebraicExpression("-2^3(4)")
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
              constant {
                withIntegerValueThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression41).evaluatesToIntegerThat().isEqualTo(-32)

    val expression43 = parseAlgebraicExpression("√2^2(3)")
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
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
      }
    }
    assertThat(expression43).evaluatesToIrrationalThat().isWithin(1e-5).of(6.0)

    val expression60 = parseAlgebraicExpression("√(2^2(3))")
    assertThat(expression60).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
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
              constant {
                withIntegerValueThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
    assertThat(expression60).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(12.0))

    val expression42 = parseAlgebraicExpression("-2*-2")
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

    val expression44 = parseAlgebraicExpression("2(2)")
    assertThat(expression44).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression44).evaluatesToIntegerThat().isEqualTo(4)

    val expression45 = parseAlgebraicExpression("2sqrt(2)")
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

    val expression46 = parseAlgebraicExpression("2√2")
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

    val expression47 = parseAlgebraicExpression("(2)(2)")
    assertThat(expression47).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression47).evaluatesToIntegerThat().isEqualTo(4)

    val expression48 = parseAlgebraicExpression("sqrt(2)(2)")
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression48).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression49 = parseAlgebraicExpression("sqrt(2)sqrt(2)")
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

    val expression50 = parseAlgebraicExpression("√2√2")
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

    val expression51 = parseAlgebraicExpression("(2)(2)(2)")
    assertThat(expression51).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
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
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression51).evaluatesToIntegerThat().isEqualTo(8)

    val expression52 = parseAlgebraicExpression("sqrt(2)sqrt(2)sqrt(2)")
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

    val expression53 = parseAlgebraicExpression("√2√2√2")
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
    val expression67 = parseAlgebraicExpression("2x^2y^-3")
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

    val expression54 = parseAlgebraicExpression("2*2/-4+7*2")
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

    val expression55 = parseAlgebraicExpression("(3/(1-2))")
    assertThat(expression55).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression55).evaluatesToIntegerThat().isEqualTo(-3)

    val expression56 = parseAlgebraicExpression("(3)/(1-2)")
    assertThat(expression56).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression56).evaluatesToIntegerThat().isEqualTo(-3)

    val expression57 = parseAlgebraicExpression("3/((1-2))")
    assertThat(expression57).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withIntegerValueThat().isEqualTo(3)
          }
        }
        rightOperand {
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
    assertThat(expression57).evaluatesToIntegerThat().isEqualTo(-3)

    // TODO: add others, including tests for malformed expressions throughout the parser &
    //  tokenizer.
  }

  @Test
  fun testLotsOfCasesForAlgebraicEquation() {
    expectFailureWhenParsingAlgebraicEquation(" x =")
    expectFailureWhenParsingAlgebraicEquation(" = y")

    val equation1 = parseAlgebraicEquation("x = 1")
    assertThat(equation1).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }
    assertThat(equation1).hasRightHandSideThat().evaluatesToIntegerThat().isEqualTo(1)

    val equation2 =
      parseAlgebraicEquation("y = mx + b", allowedVariables = listOf("x", "y", "b", "m"))
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

    val equation3 = parseAlgebraicEquation("y = (x+1)^2")
    assertThat(equation3).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation3).hasRightHandSideThat().hasStructureThatMatches {
      exponentiation {
        leftOperand {
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
        rightOperand {
          constant {
            withIntegerValueThat().isEqualTo(2)
          }
        }
      }
    }

    val equation4 = parseAlgebraicEquation("y = (x+1)(x-1)")
    assertThat(equation4).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation4).hasRightHandSideThat().hasStructureThatMatches {
      multiplication {
        leftOperand {
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
        rightOperand {
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

    expectFailureWhenParsingAlgebraicEquation("y = (x+1)(x-1) 2")
    expectFailureWhenParsingAlgebraicEquation("y 2 = (x+1)(x-1)")

    val equation5 =
      parseAlgebraicEquation("a*x^2 + b*x + c = 0", allowedVariables = listOf("x", "a", "b", "c"))
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
    private fun expectFailureWhenParsingNumericExpression(expression: String) {
      assertThrows(NumericExpressionParser.ParseException::class) {
        parseNumericExpression(expression)
      }
    }

    private fun parseNumericExpression(expression: String): MathExpression {
      return NumericExpressionParser.parseNumericExpression(expression)
    }

    private fun expectFailureWhenParsingAlgebraicExpression(expression: String) {
      assertThrows(NumericExpressionParser.ParseException::class) {
        parseAlgebraicExpression(expression)
      }
    }

    private fun parseAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return NumericExpressionParser.parseAlgebraicExpression(expression, allowedVariables)
    }

    private fun parseAlgebraicEquation(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      return NumericExpressionParser.parseAlgebraicEquation(expression, allowedVariables)
    }

    private fun expectFailureWhenParsingAlgebraicEquation(expression: String) {
      assertThrows(NumericExpressionParser.ParseException::class) {
        parseAlgebraicEquation(expression)
      }
    }

    private fun assertThat(actual: MathExpression): MathExpressionSubject =
      assertAbout(::MathExpressionSubject).that(actual)

    private fun assertThat(actual: MathEquation): MathEquationSubject =
      assertAbout(::MathEquationSubject).that(actual)

    private fun assertThat(actual: Fraction): FractionSubject =
      assertAbout(::FractionSubject).that(actual)
  }
}
