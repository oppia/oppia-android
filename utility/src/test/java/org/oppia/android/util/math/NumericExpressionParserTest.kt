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
 * This test suite specifically focuses on ensuring that fundamental numeric expressions, when
 * parsed, yield the correct [MathExpression] structure that ensures proper order of operations (for
 * both operator associativity and precedence). This suite does not cover errors (see
 * [MathExpressionParserTest] for those tests), nor algebraic expressions (see
 * [AlgebraicExpressionParserTest]).
 *
 * Further, many of the tests also verify that the expression evaluates to the correct value. This
 * suite's goal is not to test that the evaluator works functionally but, rather, that it works
 * practically. There are targeted tests designed to fail for the evaluator if issues are
 * introduced (see [NumericExpressionEvaluatorTest]).
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionParserTest {

  @Test
  fun testParse_singleInteger_returnsExpressionWithConstant() {
    val expression = parseNumericExpressionWithAllErrors("1")

    assertThat(expression).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(1)
  }

  @Test
  fun testParse_singleInteger_withWhitespace_returnsExpressionWithConstant() {
    val expression = parseNumericExpressionWithAllErrors("   2 ")

    assertThat(expression).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(2)
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(2)
  }

  @Test
  fun testParse_singleInteger_multipleDigits_returnsExpressionWithConstant() {
    val expression = parseNumericExpressionWithAllErrors("732")

    assertThat(expression).hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(732)
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(732)
  }

  @Test
  fun testParse_singleRealNumber_withWhitespace_returnsExpressionWithConstant() {
    val expression = parseNumericExpressionWithAllErrors("   2.5 ")

    assertThat(expression).hasStructureThatMatches {
      constant {
        withValueThat().isIrrationalThat().isWithin(1e-5).of(2.5)
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(2.5)
  }

  @Test
  fun testParse_addition_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 + 2")

    assertThat(expression).hasStructureThatMatches {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(3)
  }

  @Test
  fun testParse_subtraction_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 - 2")

    assertThat(expression).hasStructureThatMatches {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testParse_subtraction_withMathSymbol_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 − 2")

    assertThat(expression).hasStructureThatMatches {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testParse_multiplication_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 * 2")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(2)
  }

  @Test
  fun testParse_multiplication_withMathSymbol_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 × 2")

    assertThat(expression).hasStructureThatMatches {
      multiplication {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(2)
  }

  @Test
  fun testParse_division_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 / 2")

    assertThat(expression).hasStructureThatMatches {
      division {
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
    assertThat(expression).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }
  }

  @Test
  fun testParse_division_withMathSymbol_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 ÷ 2")

    assertThat(expression).hasStructureThatMatches {
      division {
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
    assertThat(expression).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }
  }

  @Test
  fun testParse_exponentiation_returnsExpressionWithBinaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("1 ^ 2")

    assertThat(expression).hasStructureThatMatches {
      exponentiation {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(1)
  }

  @Test
  fun testParse_negation_returnsExpressionWithUnaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("-2")

    assertThat(expression).hasStructureThatMatches {
      negation {
        operand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-2)
  }

  @Test
  fun testParse_negation_withMathSymbol_returnsExpressionWithUnaryOperation() {
    val expression = parseNumericExpressionWithAllErrors("−2")

    assertThat(expression).hasStructureThatMatches {
      negation {
        operand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-2)
  }

  @Test
  fun testParse_positiveUnary_withoutOptionalErrors_returnsExpressionWithUnaryOperation() {
    val expression = parseNumericExpressionWithoutOptionalErrors("+2")

    assertThat(expression).hasStructureThatMatches {
      positive {
        operand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(2)
  }

  @Test
  fun testParse_integerInParentheses_withoutOptionalErrors_returnsExpressionWithGroup() {
    val expression = parseNumericExpressionWithoutOptionalErrors("(2)")

    assertThat(expression).hasStructureThatMatches {
      group {
        constant {
          withValueThat().isIntegerThat().isEqualTo(2)
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(2)
  }

  @Test
  fun testParse_inlineSquareRoot_returnsExpressionWithFunctionCall() {
    val expression = parseNumericExpressionWithAllErrors("√2")

    assertThat(expression).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(1.414213562)
  }

  @Test
  fun testParse_explicitSquareRoot_returnsExpressionWithFunctionCall() {
    val expression = parseNumericExpressionWithAllErrors("sqrt(2)")

    assertThat(expression).hasStructureThatMatches {
      functionCallTo(SQUARE_ROOT) {
        argument {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(1.414213562)
  }

  @Test
  fun testParse_multiplicationAndAddition_returnsExpWithMultResolvedFirst() {
    val expression = parseNumericExpressionWithAllErrors("1+2*3")

    // Multiplication is resolved first since it's higher precedence.
    assertThat(expression).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          multiplication {
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
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(7)
  }

  @Test
  fun testParse_exponentiationAndMultiplication_returnsExpWithExponentsResolvedFirst() {
    val expression = parseNumericExpressionWithAllErrors("2*3^4")

    // Exponentiation is resolved first since it's higher precedence.
    assertThat(expression).hasStructureThatMatches {
      multiplication {
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
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(162)
  }

  @Test
  fun testParse_groupAndExponentiation_returnsExpWithGroupResolvedFirst() {
    val expression = parseNumericExpressionWithAllErrors("(2*3)^4")

    // Exponentiation is resolved last since the group is higher precedence.
    assertThat(expression).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            multiplication {
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
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(1296)
  }

  @Test
  fun testParse_negationAndExponentiation_returnsExpWithNegationResolvedLast() {
    val expression = parseNumericExpressionWithAllErrors("-3^4")

    // Exponentiation is resolved first since negation is lower precedent.
    assertThat(expression).hasStructureThatMatches {
      negation {
        operand {
          exponentiation {
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
    // Note that this may differ from other calculators since the negation is applied last (others
    // may interpret it as (-3)^4).
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-81)
  }

  @Test
  fun testParse_inlineSquareRootAndExponentiation_returnsExpWithSquareRootResolvedFirst() {
    val expression = parseNumericExpressionWithAllErrors("√3^4")

    // The square root is resolved first since it's higher precedence.
    assertThat(expression).hasStructureThatMatches {
      exponentiation {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              constant {
                withValueThat().isIntegerThat().isEqualTo(3)
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(9.0)
  }

  @Test
  fun testParse_additionAndSubtraction_returnsExpWithBothAtSamePrecedenceAndLeftAssociative() {
    val expression = parseNumericExpressionWithAllErrors("1+2-3+4-5")

    // Addition and subtraction are resolved in-order since they're the same precedence, but they're
    // resolved with left associativity (that is, left-to-right). The above expression can have its
    // associativity made clearer with grouping: (((1+2)-3)+4)-5.
    assertThat(expression).hasStructureThatMatches {
      subtraction {
        leftOperand {
          addition {
            leftOperand {
              subtraction {
                leftOperand {
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
                rightOperand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(3)
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
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testParse_multiplicationAndDivision_returnsExpWithBothAtSamePrecedenceAndLeftAssociative() {
    val expression = parseNumericExpressionWithAllErrors("2*3/4*5/6")

    // Multiplication and division are resolved in-order since they're the same precedence, but
    // they're resolved with left associativity (that is, left-to-right). The above expression can
    // have its associativity made clearer with grouping: (((2*3)/4)*5)/6.
    assertThat(expression).hasStructureThatMatches {
      division {
        leftOperand {
          multiplication {
            leftOperand {
              division {
                leftOperand {
                  multiplication {
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
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(4)
                  }
                }
              }
            }
            rightOperand {
              constant {
                withValueThat().isIntegerThat().isEqualTo(5)
              }
            }
          }
        }
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
      }
    }
    assertThat(expression).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(1)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(4)
    }
  }

  @Test
  fun testParse_nestedExponents_returnsExpWithExponentsAsRightAssociative() {
    val expression = parseNumericExpressionWithoutOptionalErrors("2^3^1.5")

    // Exponentiation is resolved with right associativity, that is, from right to left. This is
    // made clearer by grouping: 2^(3^1.5). Note that this is a specific choice made by the
    // implementation as there's no broad consensus around exponentiation associativity for infix
    // exponentiation. Right associativity is ideal since it more closely matches written-out
    // exponentiation (where the nested exponent is resolved first).
    assertThat(expression).hasStructureThatMatches {
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
                withValueThat().isIrrationalThat().isWithin(1e-5).of(1.5)
              }
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(36.660445757)
  }

  @Test
  fun testParse_nestedExponents_withGroups_returnsExpWithForcedLeftAssociativeExponent() {
    val expression = parseNumericExpressionWithAllErrors("(2^3)^1.5")

    // Nested exponentiation can be "forced" to be left-associative by using a group to explicitly
    // change the order (since groups have higher precedence than exponents).
    assertThat(expression).hasStructureThatMatches {
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
            withValueThat().isIrrationalThat().isWithin(1e-5).of(1.5)
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(22.627416998)
  }

  @Test
  fun testParse_negationAndInlineSquareRoot_returnsExpWithBothResolvedWithRightAssociativity() {
    val expression = parseNumericExpressionWithAllErrors("√-13+-√17")

    // This expression demonstrates a few things:
    // 1. Combining binary and unary operators (to demonstrate relative precedence).
    // 2. That square roots are demonstrated with right associativity (i.e. the inner operator
    // happens first).
    assertThat(expression).hasStructureThatMatches {
      addition {
        leftOperand {
          functionCallTo(SQUARE_ROOT) {
            argument {
              negation {
                operand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(13)
                  }
                }
              }
            }
          }
        }
        rightOperand {
          negation {
            operand {
              functionCallTo(SQUARE_ROOT) {
                argument {
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
    // Cannot evaluate this expression in real numbers.
  }

  @Test
  fun testParse_multipleNegAndPosOps_noOptionalErrors_returnsExpWithRightAssociativeUnaryOps() {
    val expression = parseNumericExpressionWithoutOptionalErrors("+--++-3")

    // This demonstrates that unary operators are resolved with right associativity (i.e.
    // right-to-left with the innermost operator resolving first).
    assertThat(expression).hasStructureThatMatches {
      positive {
        operand {
          negation {
            operand {
              negation {
                operand {
                  positive {
                    operand {
                      positive {
                        operand {
                          negation {
                            operand {
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
              }
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(-3)
  }

  @Test
  fun testParse_explicitMultiplication_returnsExpressionThatDoesNotHaveImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("2 * 3")

    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = false) {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(6)
  }

  @Test
  fun testParse_twoAdjacentNumbers_withGroup_withoutOptionalErrors_returnsExpWithImplicitMult() {
    // This isn't valid without turning off extra error detecting since redundant parentheses (i.e.
    // the "(3)") trigger an error.
    val expression = parseNumericExpressionWithoutOptionalErrors("2(3)")

    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(6)
  }

  @Test
  fun testParse_numberNextToParentheses_returnsExpWithImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("2(1+2)")

    // The parentheses indicate implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(6)
  }

  @Test
  fun testParse_twoAdjacentParentheticalAdditions_returnsExpWithImplicitlyMultipliedSubExps() {
    val expression = parseNumericExpressionWithAllErrors("(1+2)(3+4)")

    // Two adjacent expressions may sometimes be considered implicitly multiplied.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(21)
  }

  @Test
  fun testParse_numberNextToInlineSquareRoot_returnsExpWithImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("2√3")

    // Square roots are treated as markers for implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(3.464101615)
  }

  @Test
  fun testParse_twoAdjacentInlineSquareRoots_returnsExpWithImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("√2√3")

    // Square roots are treated as markers for implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(2.449489743)
  }

  @Test
  fun testParse_numberNextToSquareRoot_returnsExpWithImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("2sqrt(2)")

    // Functions are treated as markers for implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(2.828427125)
  }

  @Test
  fun testParse_twoAdjacentSquareRoots_returnsExpWithImplicitMultiplication() {
    val expression = parseNumericExpressionWithAllErrors("sqrt(2)sqrt(2)")

    // Functions are treated as markers for implicit multiplication.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(2.0)
  }

  @Test
  fun testParse_squareRootNextToNumber_withoutOptionalErrors_returnsExpWithImplicitMult() {
    val expression = parseNumericExpressionWithoutOptionalErrors("sqrt(2)(3)")

    // The parser recognizes this case as implicit multiplication, but additional errors are
    // triggered since the "(3)" has redundant parentheses.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
              withValueThat().isIntegerThat().isEqualTo(3)
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(4.242640687)
  }

  @Test
  fun testParse_multipleAdjacentInlineSquareRoots_returnsExpWithLeftAssociativeImplicitMult() {
    val expression = parseNumericExpressionWithAllErrors("√2√3√4")

    // Implicit multiplication is left-associative, i.e.: (√2*√3)*√4.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(4.898979486)
  }

  @Test
  fun testParse_implicitMultiplicationAndAddition_returnsExpWithImplicitMultAtHigherPrecedence() {
    val expression = parseNumericExpressionWithAllErrors("1+3√2")

    // Implicit multiplication is higher precedence so it's evaluated first.
    assertThat(expression).hasStructureThatMatches {
      addition {
        leftOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        rightOperand {
          multiplication(isImplicit = true) {
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
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(5.242640687)
  }

  @Test
  fun testParse_implicitMultiplicationAndDivision_returnsExpWithSamePrecedence() {
    // "Hard" order of operation problem loosely based on & other problems that can often stump
    // people: https://www.basic-mathematics.com/hard-order-of-operations-problems.html, and that
    // can also break parsers that incorrectly set implicit multiplication to a higher precedence.
    val expression = parseNumericExpressionWithAllErrors("3÷2(3+4)*7")

    // The parser should ensure that implicit multiplication & explicit multiplication/division are
    // the same precedence to ensure that evaluation is predictable. If implicit multiplication is
    // higher precedence, then the expression above would be evaluated 0.0306 to rather than 73.5
    // (the correct value per multiple calculators). Below demonstrates this by showing that
    // implicit multiplication follows the same associative rules as explicit
    // multiplication/division (and not taking a higher-priority execution order). For simplicity,
    // the expression above can be thought of as the equivalent: ((3÷2)*(3+4))*7.
    assertThat(expression).hasStructureThatMatches {
      multiplication {
        leftOperand {
          multiplication(isImplicit = true) {
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
        rightOperand {
          constant {
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
      }
    }
    assertThat(expression).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(73)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }
  }

  @Test
  fun testParse_implicitMultAndExponents_noOptionalErrors_returnsExpWithImplicitMultEvaledSecond() {
    val expression = parseNumericExpressionWithoutOptionalErrors("2^(3)(4)")

    // Implicit multiplication is lower precedent than exponentiation (and thus evaluated last).
    // Note that the expression above violates the redundant parentheses check hence why optional
    // errors are disabled.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(32)
  }

  @Test
  fun testParse_adjacentExponentsWithGroup_returnsExpWithImplicitMult() {
    val expression = parseNumericExpressionWithAllErrors("2^3(4^5)")

    // Two adjacent exponentiations can be implicitly multiplied if one is grouped.
    assertThat(expression).hasStructureThatMatches {
      multiplication(isImplicit = true) {
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
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(8192)
  }

  @Test
  fun testParse_complexExpression_returnsExpWithCorrectOrderOfOperations() {
    val expression = parseNumericExpressionWithAllErrors("3*2-3+4^3*8/3*2+7")

    assertThat(expression).hasStructureThatMatches {
      // To better visualize the precedence & order of operations, see this grouped version:
      // (((3*2)-3)+((((4^3)*8)/3)*2))+7.
      addition {
        leftOperand {
          // ((3*2)-3)+((((4^3)*8)/3)*2)
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
              // (((4^3)*8)/3)*2
              multiplication {
                leftOperand {
                  // ((4^3)*8)/3
                  division {
                    leftOperand {
                      // (4^3)*8
                      multiplication {
                        leftOperand {
                          // 4^3
                          exponentiation {
                            leftOperand {
                              // 4
                              constant {
                                withValueThat().isIntegerThat().isEqualTo(4)
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
    assertThat(expression).evaluatesToRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(351)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(3)
    }
  }

  @Test
  fun testParse_compoundExpressionWithMultipleOperations_returnsExpWithCorrectOperationOrder() {
    val expression = parseNumericExpressionWithAllErrors("sqrt(1+2)(3-7^2)(5+-17)")

    assertThat(expression).hasStructureThatMatches {
      // sqrt(1+2)(3-7^2)(5+-17) -> (sqrt(1+2)*(3-7^2))*(5+-17)
      multiplication(isImplicit = true) {
        leftOperand {
          // sqrt(1+2)*(3-7^2)
          multiplication(isImplicit = true) {
            leftOperand {
              // sqrt(1+2)
              functionCallTo(SQUARE_ROOT) {
                argument {
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
            rightOperand {
              // (3-7^2)
              group {
                subtraction {
                  // 3
                  leftOperand {
                    constant {
                      withValueThat().isIntegerThat().isEqualTo(3)
                    }
                  }
                  rightOperand {
                    // 7^2
                    exponentiation {
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
            }
          }
        }
        rightOperand {
          // (5+-17)
          group {
            addition {
              leftOperand {
                // 5
                constant {
                  withValueThat().isIntegerThat().isEqualTo(5)
                }
              }
              rightOperand {
                // -17
                negation {
                  operand {
                    // 17
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
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(956.092045778)
  }

  @Test
  fun testParse_multiplicationOfNegations_returnsExpWithCorrectStructure() {
    val expression = parseNumericExpressionWithAllErrors("-2*-3")

    // Note that the following structure is not the same as (-2)*(-2) since unary negation has
    // lower precedence than multiplication, so it's computed as first with its operand being the
    // multiplication expression.
    assertThat(expression).hasStructureThatMatches {
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
                    withValueThat().isIntegerThat().isEqualTo(3)
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(6)
  }

  @Test
  fun testParse_multipleOperations_withSubsequentBinaryAndUnaryOps_returnsExpWithCorrectOpOrder() {
    val expression = parseNumericExpressionWithAllErrors("2*2/-4+7*2")

    assertThat(expression).hasStructureThatMatches {
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
    assertThat(expression).evaluatesToIntegerThat().isEqualTo(13)
  }

  @Test
  fun testParse_operationsWithNonIntegersAndGroups_returnsExpWithCorrectOperationOrder() {
    val expression = parseNumericExpressionWithAllErrors("7*(3.14/0.76+8.4)^(3.8+1/(2+2/(7.4+1)))")

    assertThat(expression).hasStructureThatMatches {
      // 7*(3.14/0.76+8.4)^(3.8+1/(2+2/(7.4+1))) -> 7*(((3.14/0.76)+8.4))^(3.8+(1/(2+(2/(7.4+1)))))
      multiplication {
        leftOperand {
          // 7
          constant {
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
        rightOperand {
          // (3.14/0.76)+8.4))^(3.8+(1/(2+(2/(7.4+1)))
          exponentiation {
            leftOperand {
              // ((3.14/0.76)+8.4)
              group {
                addition {
                  leftOperand {
                    // 3.14/0.76
                    division {
                      leftOperand {
                        // 3.14
                        constant {
                          withValueThat().isIrrationalThat().isWithin(1e-5).of(3.14)
                        }
                      }
                      rightOperand {
                        // 0.76
                        constant {
                          withValueThat().isIrrationalThat().isWithin(1e-5).of(0.76)
                        }
                      }
                    }
                  }
                  rightOperand {
                    // 8.4
                    constant {
                      withValueThat().isIrrationalThat().isWithin(1e-5).of(8.4)
                    }
                  }
                }
              }
            }
            rightOperand {
              // (3.8+(1/(2+(2/(7.4+1)))))
              group {
                addition {
                  leftOperand {
                    // 3.8
                    constant {
                      withValueThat().isIrrationalThat().isWithin(1e-5).of(3.8)
                    }
                  }
                  rightOperand {
                    // 1/(2+(2/(7.4+1)))
                    division {
                      leftOperand {
                        // 1
                        constant {
                          withValueThat().isIntegerThat().isEqualTo(1)
                        }
                      }
                      rightOperand {
                        // (2+(2/(7.4+1)))
                        group {
                          addition {
                            leftOperand {
                              // 2
                              constant {
                                withValueThat().isIntegerThat().isEqualTo(2)
                              }
                            }
                            rightOperand {
                              // 2/(7.4+1)
                              division {
                                leftOperand {
                                  // 2
                                  constant {
                                    withValueThat().isIntegerThat().isEqualTo(2)
                                  }
                                }
                                rightOperand {
                                  // (7.4+1)
                                  group {
                                    addition {
                                      leftOperand {
                                        // 7.4
                                        constant {
                                          withValueThat().isIrrationalThat().isWithin(1e-5).of(7.4)
                                        }
                                      }
                                      rightOperand {
                                        // 1
                                        constant {
                                          withValueThat().isIntegerThat().isEqualTo(1)
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
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertThat(expression).evaluatesToIrrationalThat().isWithin(1e-5).of(322194.700361352)
  }

  @Test
  fun testParse_twoSimilarExpressions_differedByWhitespace_areEqual() {
    val expression1 = parseNumericExpressionWithAllErrors("3*2-3+4^3*8/3*2+7")
    val expression2 = parseNumericExpressionWithAllErrors("  3   * 2  - 3   + 4^ 3* 8/ 3  *2 +  7 ")

    // The parser should ensure that no positional information is kept (which result in the two
    // expressions being equal). Note that not all expressions can be guaranteed to be exactly equal
    // (particularly if they contain real values since rounding errors during parsing may cause
    // inconsistencies).
    assertThat(expression1).isEqualTo(expression2)
  }

  private companion object {
    private fun parseNumericExpressionWithoutOptionalErrors(expression: String): MathExpression =
      parseNumericExpressionInternal(expression, ErrorCheckingMode.REQUIRED_ONLY)

    private fun parseNumericExpressionWithAllErrors(expression: String): MathExpression =
      parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)

    private fun parseNumericExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathExpression {
      val result = MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
      assertThat(result).isInstanceOf(MathParsingResult.Success::class.java)
      return (result as MathParsingResult.Success<MathExpression>).result
    }
  }
}
