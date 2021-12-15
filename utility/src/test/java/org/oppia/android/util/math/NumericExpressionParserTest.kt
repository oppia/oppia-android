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
import kotlin.math.sqrt

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionParserTest {

  @Test
  fun testLotsOfCasesForNumericExpression() {
    // TODO: split this up
    // TODO: add log string generation for expressions.
    expectFailureWhenParsingNumericExpression("")

    val expression1 = parseNumericExpressionWithAllErrors("1")
    assertThat(expression1).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }
    assertThat(expression1).evaluatesToIntegerThat().isEqualTo(1)

    expectFailureWhenParsingNumericExpression("x")

    val expression2 = parseNumericExpressionWithAllErrors("   2 ")
    assertThat(expression2).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(2)
      }
    }
    assertThat(expression2).evaluatesToIntegerThat().isEqualTo(2)

    val expression3 = parseNumericExpressionWithAllErrors("   2.5 ")
    assertThat(expression3).hasStructureThatMatches {
      constant {
        withValueThat().isIrrationalThat().isWithin(1e-5).of(2.5)
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
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(3)
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
    assertThat(expression4).evaluatesToIntegerThat().isEqualTo(512)

    val expression23 = parseNumericExpressionWithAllErrors("(2^3)^2")
    assertThat(expression23).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            exponentiation {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(2)
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
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression23).evaluatesToIntegerThat().isEqualTo(64)

    val expression24 = parseNumericExpressionWithAllErrors("512/32/4")
    assertThat(expression24).hasStructureThatMatches {
      division {
        leftOperand {
          division {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(512)
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(32)
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
    assertThat(expression24).evaluatesToIntegerThat().isEqualTo(4)

    val expression25 = parseNumericExpressionWithAllErrors("512/(32/4)")
    assertThat(expression25).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(512)
          }
        }
        rightOperand {
          group {
            division {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(32)
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
    assertThat(expression25).evaluatesToIntegerThat().isEqualTo(64)

    val expression5 = parseNumericExpressionWithAllErrors("sqrt(2)")
    assertThat(expression5).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression5).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0))

    expectFailureWhenParsingNumericExpression("sqr(2)")

    expectFailureWhenParsingNumericExpression("xyz(2)")

    val expression6 = parseNumericExpressionWithAllErrors("732")
    assertThat(expression6).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(732)
      }
    }
    assertThat(expression6).evaluatesToIntegerThat().isEqualTo(732)

    // Verify order of operations between higher & lower precedent operators.
    val expression32 = parseNumericExpressionWithAllErrors("3+4^5")
    assertThat(expression32).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(5)
              }
            }
          }
        }
      }
    }
    assertThat(expression32).evaluatesToIntegerThat().isEqualTo(1027)

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
                        withValueThat().isIntegerThat().isEqualTo(3)
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
                rightOperand {
                  // 3
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(3)
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
                                withValueThat().isIntegerThat().isEqualTo(4)
                              }
                            }
                            rightOperand {
                              // 7
                              constant {
                                withValueThat().isIntegerThat().isEqualTo(7)
                              }
                            }
                          }
                        }
                        rightOperand {
                          // 8
                          constant {
                            withValueThat().isIntegerThat().isEqualTo(8)
                          }
                        }
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
          // 7
          constant {
            withValueThat().isIntegerThat().isEqualTo(7)
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

    val expression8 = parseNumericExpressionWithAllErrors("(1+2)(3+4)")
    assertThat(expression8).hasStructureThatMatches {
      multiplication {
        leftOperand {
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
        rightOperand {
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
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
    assertThat(expression8).evaluatesToIntegerThat().isEqualTo(21)

    // Right implicit multiplication of numbers isn't allowed.
    expectFailureWhenParsingNumericExpression("(1+2)2")

    val expression10 = parseNumericExpressionWithAllErrors("2(1+2)")
    assertThat(expression10).hasStructureThatMatches {
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
                constant {
                  withValueThat().isIntegerThat().isEqualTo(2)
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

    val expression12 = parseNumericExpressionWithAllErrors("3sqrt(2)")
    assertThat(expression12).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
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
    assertThat(expression12).evaluatesToIrrationalThat().isWithin(1e-5).of(3.0 * sqrt(2.0))

    expectFailureWhenParsingNumericExpression("xsqrt(2)")

    val expression13 = parseNumericExpressionWithAllErrors("sqrt(2)*(1+2)*(3-2^5)")
    assertThat(expression13).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
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
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(2)
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
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(5)
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

    val expression58 = parseNumericExpressionWithAllErrors("sqrt(2)(1+2)(3-2^5)")
    assertThat(expression58).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
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
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(2)
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
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              rightOperand {
                exponentiation {
                  leftOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(5)
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
            withValueThat().isIntegerThat().isEqualTo(3)
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
                withValueThat().isIntegerThat().isEqualTo(3)
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
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression16).evaluatesToIntegerThat().isEqualTo(4)

    val expression17 = parseNumericExpressionWithAllErrors("1+-4")
    assertThat(expression17).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression17).evaluatesToIntegerThat().isEqualTo(-3)

    val expression18 = parseNumericExpressionWithAllErrors("1++4")
    assertThat(expression18).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          positive {
            operand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression18).evaluatesToIntegerThat().isEqualTo(5)

    val expression19 = parseNumericExpressionWithAllErrors("1--4")
    assertThat(expression19).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression19).evaluatesToIntegerThat().isEqualTo(5)

    expectFailureWhenParsingNumericExpression("1-^-4")

    val expression20 = parseNumericExpressionWithAllErrors("√2 × 7 ÷ 4")
    assertThat(expression20).hasStructureThatMatches {
      division {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(7)
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
    assertThat(expression20).evaluatesToIrrationalThat().isWithin(1e-5).of((sqrt(2.0) * 7.0) / 4.0)

    expectFailureWhenParsingNumericExpression("1+2 &asdf")

    val expression21 = parseNumericExpressionWithAllErrors("sqrt(2)sqrt(3)sqrt(4)")
    // Note that this tree demonstrates left associativity.
    assertThat(expression21).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(3)
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
                withValueThat().isIntegerThat().isEqualTo(4)
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

    val expression22 = parseNumericExpressionWithAllErrors("(1+2)(3-7^2)(5+-17)")
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
            rightOperand {
              // 3-7^2
              group {
                subtraction {
                  leftOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(3)
                    }
                  }
                  rightOperand {
                    exponentiation {
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
                  withValueThat().isIntegerThat().isEqualTo(5)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(17)
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

    val expression26 = parseNumericExpressionWithAllErrors("3^-2")
    assertThat(expression26).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        rightOperand {
          negation {
            operand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
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
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(2)
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
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              rightOperand {
                negation {
                  operand {
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
    assertThat(expression27).evaluatesToIrrationalThat().isWithin(1e-5).of(0.78338103693)

    val expression28 = parseNumericExpressionWithAllErrors("1-3^sqrt(4)")
    assertThat(expression28).hasStructureThatMatches {
      subtraction {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
            rightOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
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
    assertThat(expression28).evaluatesToIntegerThat().isEqualTo(-8)

    // "Hard" order of operation problems loosely based on & other problems that can often stump
    // people: https://www.basic-mathematics.com/hard-order-of-operations-problems.html.
    val expression29 = parseNumericExpressionWithAllErrors("3÷2*(3+4)")
    assertThat(expression29).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(3)
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
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
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
    assertThat(expression29).evaluatesToRationalThat().evaluatesToRealThat().isWithin(1e-5).of(10.5)

    val expression59 = parseNumericExpressionWithAllErrors("3÷2(3+4)")
    assertThat(expression59).hasStructureThatMatches {
      multiplication {
        leftOperand {
          division {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(3)
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
          group {
            addition {
              leftOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
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
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(4)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(5)
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
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(3)
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
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(4)
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
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
            }
          }
        }
        rightOperand {
          exponentiation {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(expression35).evaluatesToIntegerThat().isEqualTo(32)

    // An exponentiation can be a right operand of an implicit mult if it's grouped.
    val expression36 = parseNumericExpressionWithoutOptionalErrors("2^(3)(2^2)")
    assertThat(expression36).hasStructureThatMatches {
      multiplication {
        leftOperand {
          exponentiation {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(3)
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
                  withValueThat().isIntegerThat().isEqualTo(2)
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
                    withValueThat().isIntegerThat().isEqualTo(2)
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
            rightOperand {
              // 4
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(4)
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
                withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(expression38).evaluatesToIntegerThat().isEqualTo(256)

    expectFailureWhenParsingNumericExpression("2^2 2^2")
    expectFailureWhenParsingNumericExpression("(3) 2^2")
    expectFailureWhenParsingNumericExpression("sqrt(3) 2^2")
    expectFailureWhenParsingNumericExpression("√2 2^2")
    expectFailureWhenParsingNumericExpression("2^2 3")

    expectFailureWhenParsingNumericExpression("-2 3")

    val expression39 = parseNumericExpressionWithAllErrors("-(1+2)")
    assertThat(expression39).hasStructureThatMatches {
      negation {
        operand {
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
    assertThat(expression39).evaluatesToIntegerThat().isEqualTo(-3)

    // Should pass for algebra.
    expectFailureWhenParsingNumericExpression("-2 x")

    val expression40 = parseNumericExpressionWithAllErrors("-2 (1+2)")
    assertThat(expression40).hasStructureThatMatches {
      // The negation happens last for parity with other common calculators.
      negation {
        operand {
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
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
                rightOperand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(3)
                  }
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(4)
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
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
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
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(3)
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
                      withValueThat().isIntegerThat().isEqualTo(2)
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
                group {
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
    assertThat(expression60).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(12.0))

    val expression42 = parseNumericExpressionWithAllErrors("-2*-2")
    // Note that the following structure is not the same as (-2)*(-2) since unary negation has
    // higher precedence than multiplication, so it's first & recurses to include the entire
    // multiplication expression.
    assertThat(expression42).hasStructureThatMatches {
      negation {
        operand {
          multiplication {
            leftOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            rightOperand {
              negation {
                operand {
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
    assertThat(expression42).evaluatesToIntegerThat().isEqualTo(4)

    // TODO: Here & elsewhere, fix the fact that this is actually a valid use of single-term
    //  parentheses (there's a bug in the current error detection logic).
    val expression44 = parseNumericExpressionWithoutOptionalErrors("2(2)")
    assertThat(expression44).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression44).evaluatesToIntegerThat().isEqualTo(4)

    val expression45 = parseNumericExpressionWithAllErrors("2sqrt(2)")
    assertThat(expression45).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(expression45).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression46 = parseNumericExpressionWithAllErrors("2√2")
    assertThat(expression46).hasStructureThatMatches {
      multiplication {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(expression46).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression47 = parseNumericExpressionWithoutOptionalErrors("(2)(2)")
    assertThat(expression47).hasStructureThatMatches {
      multiplication {
        leftOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(2)
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
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression48).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0 * sqrt(2.0))

    val expression49 = parseNumericExpressionWithAllErrors("sqrt(2)sqrt(2)")
    assertThat(expression49).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
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
    assertThat(expression49).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression50 = parseNumericExpressionWithAllErrors("√2√2")
    assertThat(expression50).hasStructureThatMatches {
      multiplication {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
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
    assertThat(expression50).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt(2.0) * sqrt(2.0))

    val expression51 = parseNumericExpressionWithoutOptionalErrors("(2)(2)(2)")
    assertThat(expression51).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(2)
                }
              }
            }
            rightOperand {
              group {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(2)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
        }
      }
    }
    assertThat(expression51).evaluatesToIntegerThat().isEqualTo(8)

    val expression52 = parseNumericExpressionWithAllErrors("sqrt(2)sqrt(2)sqrt(2)")
    assertThat(expression52).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
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
    val sqrt2 = sqrt(2.0)
    assertThat(expression52).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    val expression53 = parseNumericExpressionWithAllErrors("√2√2√2")
    assertThat(expression53).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication {
            leftOperand {
              functionCallTo(SQUARE_ROOT) {
                argument {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(2)
                  }
                }
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
    assertThat(expression53).evaluatesToIrrationalThat().isWithin(1e-5).of(sqrt2 * sqrt2 * sqrt2)

    // Should fail for algebra.
    expectFailureWhenParsingNumericExpression("x7")

    // Should pass for algebra.
    expectFailureWhenParsingNumericExpression("2x^2")

    val expression54 = parseNumericExpressionWithAllErrors("2*2/-4+7*2")
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
                    withValueThat().isIntegerThat().isEqualTo(2)
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
            rightOperand {
              // -4
              negation {
                // 4
                operand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(4)
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
                withValueThat().isIntegerThat().isEqualTo(7)
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
    assertThat(expression54).evaluatesToIntegerThat().isEqualTo(13)

    val expression55 = parseNumericExpressionWithAllErrors("3/(1-2)")
    assertThat(expression55).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            subtraction {
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
    assertThat(expression55).evaluatesToIntegerThat().isEqualTo(-3)

    val expression56 = parseNumericExpressionWithoutOptionalErrors("(3)/(1-2)")
    assertThat(expression56).hasStructureThatMatches {
      division {
        leftOperand {
          group {
            constant {
              withValueThat().isIntegerThat().isEqualTo(3)
            }
          }
        }
        rightOperand {
          group {
            subtraction {
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
    assertThat(expression56).evaluatesToIntegerThat().isEqualTo(-3)

    val expression57 = parseNumericExpressionWithoutOptionalErrors("3/((1-2))")
    assertThat(expression57).hasStructureThatMatches {
      division {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        rightOperand {
          group {
            group {
              subtraction {
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
    }
    assertThat(expression57).evaluatesToIntegerThat().isEqualTo(-3)

    // TODO: add others, including tests for malformed expressions throughout the parser &
    //  tokenizer.
  }

  private companion object {
    // TODO: fix helper API.

    private fun expectFailureWhenParsingNumericExpression(expression: String): MathParsingError {
      val result = parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)
      assertThat(result).isInstanceOf(MathParsingResult.Failure::class.java)
      return (result as MathParsingResult.Failure<MathExpression>).error
    }

    private fun parseNumericExpressionWithoutOptionalErrors(expression: String): MathExpression {
      val result =
        parseNumericExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionWithAllErrors(expression: String): MathExpression {
      val result =
        parseNumericExpressionInternal(
          expression, ErrorCheckingMode.ALL_ERRORS
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }
  }
}
