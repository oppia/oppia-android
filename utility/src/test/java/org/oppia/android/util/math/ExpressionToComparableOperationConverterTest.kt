package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.toComparableOperation
import org.junit.runner.RunWith
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.PRODUCT
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.math.ComparableOperationSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/**
 * Tests for [ExpressionToComparableOperationConverter].
 *
 * Note that this suite is broken up into distinct sections (designated by block comments) to better
 * help organize the different behaviors being tested.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToComparableOperationConverterTest {
  @Parameter lateinit var op1: String
  @Parameter lateinit var op2: String

  /* Operation creation tests */

  @Test
  fun testConvert_integerConstantExpression_returnsConstantOperation() {
    val expression = parseNumericExpression("2")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testConvert_decimalConstantExpression_returnsConstantOperation() {
    val expression = parseNumericExpression("3.14")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      constantTerm {
        withValueThat().isIrrationalThat().isWithin(1e-5).of(3.14)
      }
    }
  }

  @Test
  fun testConvert_variableExpression_returnsVariableOperation() {
    val expression = parseAlgebraicExpression("x")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testConvert_addition_returnsSummation() {
    val expression = parseNumericExpression("1+2")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_addition_sameValues_returnsSummationWithBoth() {
    val expression = parseNumericExpression("1+1")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_subtraction_returnsSummationOfNegative() {
    val expression = parseNumericExpression("1-2")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplication_returnsProduct() {
    val expression = parseNumericExpression("2*3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_division_returnsProductOfInverted() {
    val expression = parseNumericExpression("2/3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_exponentiation_returnsNonCommutativeOperation() {
    val expression = parseNumericExpression("2^3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        exponentiation {
          leftOperand {
            constantTerm {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
          rightOperand {
            constantTerm {
              withValueThat().isIntegerThat().isEqualTo(3)
            }
          }
        }
      }
    }
  }

  @Test
  fun testConvert_squareRoot_returnsNonCommutativeOperation() {
    val expression = parseNumericExpression("sqrt(2)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        squareRootWithArgument {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_variableTerm_returnsNonNegativeOperation() {
    val expression = parseAlgebraicExpression("x")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
    }
  }

  @Test
  fun testConvert_negatedVariable_returnsNegativeVariableOperation() {
    val expression = parseAlgebraicExpression("-x")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testConvert_positiveVariable_returnsVariableOperation() {
    val expression = parseAlgebraicExpression("+x", errorCheckingMode = REQUIRED_ONLY)

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testConvert_positiveOfNegativeVariable_returnsNegativeVariableOperation() {
    val expression = parseAlgebraicExpression("+-x", errorCheckingMode = REQUIRED_ONLY)

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testConvert_subtractionOfNegative_returnsSummationWithPositives() {
    val expression = parseNumericExpression("1--2")

    val comparable = expression.toComparableOperation()

    // Verify that the subtraction & negation cancel out each other.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_negativePlusPositive_returnsSummationWithFirstTermNegative() {
    val expression = parseNumericExpression("-2+1")

    val comparable = expression.toComparableOperation()

    // Verify that the negative only applies to the 2, not to the whole expression.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multipleAdditions_returnsCombinedSummation() {
    val expression = parseNumericExpression("1+2+3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multipleSubtractions_returnsCombinedSummation() {
    val expression = parseNumericExpression("1-2-3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionsAndSubtractions_returnsCombinedSummation() {
    val expression = parseNumericExpression("1+2-3-4+5")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionsWithNestedAdds_returnsCompletelyCombinedSummation() {
    val expression = parseNumericExpression("1+((2+(3+4)+5)+6)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(6)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(5) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_subtractsWithNesting_returnsSummationWithDistributedNegation() {
    val expression = parseNumericExpression("1-(2+3-4)")

    val comparable = expression.toComparableOperation()

    // Both the 2 & 3 are negative since the subtraction distributes, and the 4 becomes positive.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(4)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_subtractsWithNestedSubs_returnsCompletelyCombinedSummation() {
    val expression = parseNumericExpression("1-((2-(3-4)-5)-6)")

    val comparable = expression.toComparableOperation()

    // Some of these are positive because of distribution.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(6)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(5) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionsAndSubtractionsWithNested_returnsCombinedSummation() {
    val expression =
      parseNumericExpression("1++(2-3)+-(4+5--(2+3-1))", errorCheckingMode = REQUIRED_ONLY)

    val comparable = expression.toComparableOperation()

    // This also verifies that negation distributes in the same way as subtraction.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(8)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(5) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(6) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(7) {
          hasNegatedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multipleMultiplications_returnsCombinedProduct() {
    val expression = parseNumericExpression("2*3*4")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multipleDivisions_returnsCombinedProduct() {
    val expression = parseNumericExpression("2/3/4")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationsAndDivisions_returnsCombinedProduct() {
    val expression = parseNumericExpression("2*3/4/5*6")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(5)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
        index(3) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(4) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationsWithNestedMults_returnsCompletelyCombinedProduct() {
    val expression = parseNumericExpression("2*((3*(4*5)*6)*7)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(6)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(3) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(4) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
        index(5) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_dividesWithNesting_returnsProductWithDistributedInversion() {
    val expression = parseNumericExpression("2/(3*4/5)")

    val comparable = expression.toComparableOperation()

    // Both the 3 & 5 become inverted, and the 5 becomes regular multiplication due to the division
    // distribution.
    //  2*5*inv(3)*inv(4)
    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(4)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(3) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_divisionsWithNestedDivs_returnsCompletelyCombinedProduct() {
    val expression = parseNumericExpression("2/((3/(4/5)/6)/7)")

    val comparable = expression.toComparableOperation()

    // Some of these are non-inverted because of distribution.
    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(6)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
        index(3) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
        index(4) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(5) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationsAndDivisionsWithNested_returnsCombinedProduct() {
    val expression = parseNumericExpression("1*(2/3)/(4*5*(2*3/1))")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(8)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(3) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(4) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(5) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(6) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(7) {
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationWithNoNegatives_returnsPositiveProduct() {
    val expression = parseNumericExpression("2*3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationWithOneNegative_returnsNegativeProduct() {
    val expression = parseNumericExpression("2*-3")

    val comparable = expression.toComparableOperation()

    // The entire accumulation is considered negative.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationWithTwoNegatives_returnsPositiveProduct() {
    val expression = parseNumericExpression("-2*-3")

    val comparable = expression.toComparableOperation()

    // The two negatives cancel out. This also verifies that negation can pipe up to top-level
    // negation.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationWithThreeNegatives_returnsNegativeProduct() {
    val expression = parseNumericExpression("-2*-3*-4")

    val comparable = expression.toComparableOperation()

    // 3 negative operands results in the overall product being negative.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_combinedMultDivWithNested_evenNegatives_returnsPositiveProduct() {
    val expression = parseNumericExpression("-2*-3/-(4/-(3*2))")

    val comparable = expression.toComparableOperation()

    // There are four negatives, so the overall expression is positive.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
    }
  }

  @Test
  fun testConvert_combinedMultDivWithNested_oddNegatives_returnsNegativeProduct() {
    val expression =
      parseNumericExpression("-2*-3/-(4/-(3*2*+(-3*7)))", errorCheckingMode = REQUIRED_ONLY)

    val comparable = expression.toComparableOperation()

    // There are five negatives, so the overall expression is negative. Note that this is also
    // verifying that the negation properly distributes with the group.
    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        // This is a side extra check to ensure that unary positive groups are correctly folded into
        // the product.
        hasOperandCountThat().isEqualTo(7)
      }
    }
  }

  @Test
  fun testConvert_additionAndExp_returnsSummationWithNonCommutative() {
    val expression = parseNumericExpression("1+2^3")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(2)
                }
              }
              rightOperand {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionAndSquareRoot_returnsSummationWithNonCommutative() {
    val expression = parseNumericExpression("1+sqrt(2)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionWithinExp_returnsSummationWithinNonCommutative() {
    val expression = parseNumericExpression("2^(1+3)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        exponentiation {
          leftOperand {
            constantTerm {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
          rightOperand {
            commutativeAccumulationWithType(SUMMATION) {
              hasOperandCountThat().isEqualTo(2)
              index(0) {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(1)
                }
              }
              index(1) {
                constantTerm {
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
  fun testConvert_additionWithinSquareRoot_returnsSummationWithinNonCommutative() {
    val expression = parseNumericExpression("sqrt(1+3)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        squareRootWithArgument {
          commutativeAccumulationWithType(SUMMATION) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(1)
              }
            }
            index(1) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationAndExp_returnsProductWithNonCommutative() {
    val expression = parseNumericExpression("2*3^4")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              rightOperand {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(4)
                }
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationAndSquareRoot_returnsProductWithNonCommutative() {
    val expression = parseNumericExpression("2*sqrt(3)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationWithinExp_returnsProductWithinNonCommutative() {
    val expression = parseNumericExpression("2^(3*4)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        exponentiation {
          leftOperand {
            constantTerm {
              withValueThat().isIntegerThat().isEqualTo(2)
            }
          }
          rightOperand {
            commutativeAccumulationWithType(PRODUCT) {
              hasOperandCountThat().isEqualTo(2)
              index(0) {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(3)
                }
              }
              index(1) {
                constantTerm {
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
  fun testConvert_multiplicationWithinSquareRoot_returnsProductWithinNonCommutative() {
    val expression = parseNumericExpression("sqrt(2*3)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      nonCommutativeOperation {
        squareRootWithArgument {
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            index(1) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
  }

  @Test
  fun testConvert_additionAndMultiplication_returnsSummationOfProduct() {
    val expression = parseNumericExpression("2*3+1")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
            index(1) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
      }
    }
  }

  @Test
  fun testConvert_multiplicationAndGroupedAddition_returnsProductOfSummation() {
    val expression = parseNumericExpression("2*(3+1)")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          commutativeAccumulationWithType(SUMMATION) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(1)
              }
            }
            index(1) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
      }
    }
  }

  /*
   * Top-level operation sorting. Note that negation & inversion can't be sorted relative to each
   * other since they'll never co-occur (though the underlying sorting is set up to prioritize
   * negative operations over inverted).
   *
   * Note also that accumulators can't be cross-verified for order since whether a summation or
   * product is first entirely depends on the expression itself (since multiplication and division
   * are higher precedence than addition and subtraction). Thus, these cases can't be tested for
   * sorting order.
   */

  @Test
  @RunParameterized(
    Iteration(name = "(1+2)*sqrt(3)", "op1=(1+2)", "op2=sqrt(3)"),
    Iteration(name = "sqrt(3)*(1+2)", "op1=sqrt(3)", "op2=(1+2)"),
    Iteration(name = "(1+2)*(3^4)", "op1=(1+2)", "op2=(3^4)"),
    Iteration(name = "(3^4)*(1+2)", "op1=(3^4)", "op2=(1+2)")
  )
  fun testConvert_additionAndNonCommutativeOp_samePrecedence_returnsOpWithSummationFirst() {
    val expression = parseNumericExpression("$op1 * $op2")

    val comparable = expression.toComparableOperation()

    // Verify that the summation is still first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          commutativeAccumulationWithType(SUMMATION) {}
        }
        index(1) {
          nonCommutativeOperation {}
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "2+sqrt(3)", "op1=2", "op2=sqrt(3)"),
    Iteration(name = "sqrt(3)+2", "op1=sqrt(3)", "op2=2"),
    Iteration(name = "2+3^4", "op1=2", "op2=3^4"),
    Iteration(name = "3^4+2", "op1=3^4", "op2=2")
  )
  fun testConvert_constantAndNonCommutativeOp_samePrecedence_returnsOpWithNonCommutativeFirst() {
    val expression = parseNumericExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // Verify that the non-commutative operation is first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {}
        }
        index(1) {
          constantTerm {}
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "2*x", "op1=2", "op2=x"),
    Iteration(name = "x*2", "op1=x", "op2=2")
  )
  fun testConvert_constantAndVariable_samePrecedence_returnsOpWithConstantFirst() {
    val expression = parseAlgebraicExpression("$op1 * $op2")

    val comparable = expression.toComparableOperation()

    // Verify that the constant is first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {}
        }
        index(1) {
          variableTerm {}
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "x+(-y)", "op1=x", "op2=(-y)"),
    Iteration(name = "(-y)+x", "op1=(-y)", "op2=x")
  )
  fun testConvert_positiveAndNegativeVariables_returnsOpWithNegatedLast() {
    val expression = parseAlgebraicExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // Verify that the positive term is first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          variableTerm {}
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          variableTerm {}
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "x*(1/y)", "op1=x", "op2=(1/y)"),
    Iteration(name = "(1/y)*x", "op1=(1/y)", "op2=x")
  )
  fun testConvert_invertedAndNonInvertedVariables_returnsOpWithInvertedLast() {
    val expression = parseAlgebraicExpression("$op1 * $op2")

    val comparable = expression.toComparableOperation()

    // Verify that the non-inverted term is first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasInvertedPropertyThat().isFalse()
          constantTerm {}
        }
        index(1) {
          hasInvertedPropertyThat().isFalse()
          variableTerm {}
        }
        index(2) {
          hasInvertedPropertyThat().isTrue()
          variableTerm {}
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "(1+2)*(2+3)", "op1=1+2", "op2=2+3"),
    Iteration(name = "(2+1)*(2+3)", "op1=2+1", "op2=2+3"),
    Iteration(name = "(1+2)*(3+2)", "op1=1+2", "op2=3+2"),
    Iteration(name = "(2+1)*(3+2)", "op1=2+1", "op2=3+2"),
    Iteration(name = "(2+3)*(1+2)", "op1=2+3", "op2=1+2"),
    Iteration(name = "(2+3)*(2+1)", "op1=2+3", "op2=2+1"),
    Iteration(name = "(3+2)*(1+2)", "op1=3+2", "op2=1+2"),
    Iteration(name = "(3+2)*(2+1)", "op1=3+2", "op2=2+1")
  )
  fun testConvert_twoAdditionsInProduct_smallerSumIsFirst() {
    val expression = parseNumericExpression("($op1)*($op2)")

    val comparable = expression.toComparableOperation()

    // Summations are deterministically sorted regardless of how the original expression structures
    // them.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          commutativeAccumulationWithType(SUMMATION) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(1)
              }
            }
          }
        }
        index(1) {
          commutativeAccumulationWithType(SUMMATION) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "(2*3)+(4*5)", "op1=2*3", "op2=4*5"),
    Iteration(name = "(3*2)+(4*5)", "op1=3*2", "op2=4*5"),
    Iteration(name = "(2*3)+(5*4)", "op1=2*3", "op2=5*4"),
    Iteration(name = "(3*2)+(5*4)", "op1=3*2", "op2=5*4"),
    Iteration(name = "(4*5)+(2*3)", "op1=4*5", "op2=2*3"),
    Iteration(name = "(4*5)+(3*2)", "op1=4*5", "op2=3*2"),
    Iteration(name = "(5*4)+(2*3)", "op1=5*4", "op2=2*3"),
    Iteration(name = "(5*4)+(3*2)", "op1=5*4", "op2=3*2")
  )
  fun testConvert_twoMultiplicationsInSum_smallerProductIsFirst() {
    val expression = parseNumericExpression("($op1)+($op2)")

    val comparable = expression.toComparableOperation()

    // Products are deterministically sorted regardless of how the original expression structures
    // them.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
        index(1) {
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
      }
    }
  }
  
  /* Non-commutative sorting */

  @Test
  @RunParameterized(
    Iteration(name = "(2^3)+sqrt(2)", "op1=(2^3)", "op2=sqrt(2)"),
    Iteration(name = "sqrt(2)+(2^3)", "op1=sqrt(2)", "op2=(2^3)")
  )
  fun testConvert_expAndSqrt_samePrecedence_returnsOpWithExpThenSqrt() {
    val expression = parseNumericExpression("$op1+$op2")

    val comparable = expression.toComparableOperation()

    // Verify that the exponentiation is first since it's higher priority during sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            exponentiation {}
          }
        }
        index(1) {
          nonCommutativeOperation {
            squareRootWithArgument {}
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    // const^const + const^const
    Iteration(name = "(2^3)+(4^5)", "op1=2^3", "op2=4^5"),
    Iteration(name = "(2^5)+(4^3)", "op1=2^5", "op2=4^3"),
    Iteration(name = "(4^3)+(2^5)", "op1=4^3", "op2=2^5"),
    Iteration(name = "(4^5)+(2^3)", "op1=4^5", "op2=2^3"),
    // const^var + const^var
    Iteration(name = "(2^x)+(4^5)", "op1=2^x", "op2=4^5"),
    Iteration(name = "(2^5)+(4^x)", "op1=2^5", "op2=4^x"),
    Iteration(name = "(4^x)+(2^5)", "op1=4^x", "op2=2^5"),
    Iteration(name = "(4^5)+(2^x)", "op1=4^5", "op2=2^x"),
    // const^(var or const) + const^(const or var)
    Iteration(name = "(2^x)+(4^y)", "op1=2^x", "op2=4^y"),
    Iteration(name = "(2^y)+(4^x)", "op1=2^y", "op2=4^x"),
    Iteration(name = "(4^x)+(2^y)", "op1=4^x", "op2=2^y"),
    Iteration(name = "(4^y)+(2^x)", "op1=4^y", "op2=2^x")
  )
  fun testConvert_addTwoExps_lhs1Const_rhs1Any_lhs2Const_rhs2Any_returnsOpWithLhsSizeBasedOrder() {
    // Note that optional errors need to be disabled as part of testing exponents as powers.
    val expression =
      parseAlgebraicExpression("($op1)+($op2)", errorCheckingMode = REQUIRED_ONLY)

    val comparable = expression.toComparableOperation()

    // Verify that the exponentiations are ordered based on the left-hand operand's size.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                constantTerm {
                  withValueThat().isIntegerThat().isEqualTo(2)
                }
              }
            }
          }
        }
        index(1) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                constantTerm {
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
  @RunParameterized(
    // var^const + var^const
    Iteration(name = "(u^3)+(v^5)", "op1=u^3", "op2=v^5"),
    Iteration(name = "(u^5)+(v^3)", "op1=u^5", "op2=v^3"),
    Iteration(name = "(v^3)+(u^5)", "op1=v^3", "op2=u^5"),
    Iteration(name = "(v^5)+(u^3)", "op1=v^5", "op2=u^3"),
    // var^var + var^var
    Iteration(name = "(u^x)+(v^5)", "op1=u^x", "op2=v^5"),
    Iteration(name = "(u^5)+(v^x)", "op1=u^5", "op2=v^x"),
    Iteration(name = "(v^x)+(u^5)", "op1=v^x", "op2=u^5"),
    Iteration(name = "(v^5)+(u^x)", "op1=v^5", "op2=u^x"),
    // var^(var or const) + var^(const or var)
    Iteration(name = "(u^x)+(v^y)", "op1=u^x", "op2=v^y"),
    Iteration(name = "(u^y)+(v^x)", "op1=u^y", "op2=v^x"),
    Iteration(name = "(v^x)+(u^y)", "op1=v^x", "op2=u^y"),
    Iteration(name = "(v^y)+(u^x)", "op1=v^y", "op2=u^x")
  )
  fun testConvert_addTwoExps_lhs1Var_rhs1Any_lhs2Var_rhs2Any_returnsOpWithLhsLetterBasedOrder() {
    // Note that optional errors need to be disabled as part of testing exponents as powers.
    val expression =
      parseAlgebraicExpression(
        "($op1)+($op2)",
        allowedVariables = listOf("u", "v", "x", "y"),
        errorCheckingMode = REQUIRED_ONLY
      )

    val comparable = expression.toComparableOperation()

    // Verify that the exponentiations are ordered based on the left-hand operand's lexicographical
    // ordering.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                variableTerm {
                  withNameThat().isEqualTo("u")
                }
              }
            }
          }
        }
        index(1) {
          nonCommutativeOperation {
            exponentiation {
              leftOperand {
                variableTerm {
                  withNameThat().isEqualTo("v")
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "sqrt(2)+sqrt(3)", "op1=2", "op2=3"),
    Iteration(name = "sqrt(3)+sqrt(2)", "op1=3", "op2=2")
  )
  fun testConvert_addTwoSqrts_leftConst_rightConst_returnsOpWithSqrtsByArgSize() {
    val expression = parseNumericExpression("sqrt($op1)+sqrt($op2)")

    val comparable = expression.toComparableOperation()

    // The square roots should be ordered based on their argument sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
        index(1) {
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(3)
              }
            }
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "sqrt(x)+sqrt(y)", "op1=x", "op2=y"),
    Iteration(name = "sqrt(y)+sqrt(x)", "op1=y", "op2=x")
  )
  fun testConvert_addTwoSqrts_leftVar_rightVar_returnsOpWithSqrtsByVariableOrder() {
    val expression = parseAlgebraicExpression("sqrt($op1)+sqrt($op2)")

    val comparable = expression.toComparableOperation()

    // The square roots should be ordered based on their argument lexicographical sorting.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            squareRootWithArgument {
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
          }
        }
        index(1) {
          nonCommutativeOperation {
            squareRootWithArgument {
              variableTerm {
                withNameThat().isEqualTo("y")
              }
            }
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "sqrt(2)+sqrt(x)", "op1=2", "op2=x"),
    Iteration(name = "sqrt(x)+sqrt(2)", "op1=x", "op2=2")
  )
  fun testConvert_addTwoSqrts_oneConst_oneVar_returnsOpWithSqrtsByConstFirst() {
    val expression = parseAlgebraicExpression("sqrt($op1)+sqrt($op2)")

    val comparable = expression.toComparableOperation()

    // Constant-before-variable ordering also affects peer square root orders.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
          }
        }
        index(1) {
          nonCommutativeOperation {
            squareRootWithArgument {
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
          }
        }
      }
    }
  }

  /* Constant & variable sorting */

  @Test
  @RunParameterized(
    Iteration(name = "2+3", "op1=2", "op2=3"),
    Iteration(name = "3+2", "op1=3", "op2=2")
  )
  fun testConvert_addTwoConstants_leftInteger_rightInteger_returnsOpSortedByValues() {
    val expression = parseNumericExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // The order of the summation should be based on the constants' values.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "3.2+6.3", "op1=3.2", "op2=6.3"),
    Iteration(name = "6.3+3.2", "op1=6.3", "op2=3.2")
  )
  fun testConvert_addTwoConstants_leftDouble_rightDouble_returnsOpSortedByValues() {
    val expression = parseNumericExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // The order of the summation should be based on the constants' values.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIrrationalThat().isWithin(1e-5).of(3.2)
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIrrationalThat().isWithin(1e-5).of(6.3)
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "3+6.3", "op1=3", "op2=6.3"),
    Iteration(name = "6.3+3", "op1=6.3", "op2=3")
  )
  fun testConvert_addTwoConstants_smallInt_largeDouble_returnsOpWithIntFirst() {
    val expression = parseNumericExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // The order of the summation should be based on the constants' values.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIrrationalThat().isWithin(1e-5).of(6.3)
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "8+6.3", "op1=8", "op2=6.3"),
    Iteration(name = "6.3+8", "op1=6.3", "op2=8")
  )
  fun testConvert_addTwoConstants_largeInt_smallDouble_returnsOpWithDoubleFirst() {
    val expression = parseNumericExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // The order of the summation should be based on the constants' values.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIrrationalThat().isWithin(1e-5).of(6.3)
          }
        }
        index(1) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(8)
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "x+6", "op1=x", "op2=6"),
    Iteration(name = "6+x", "op1=6", "op2=x")
  )
  fun testConvert_addVarAndIntConstant_returnsOpWithConstantFirst() {
    val expression = parseAlgebraicExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // Constants are always ordered before variables.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
        index(1) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "3.6+x", "op1=3.6", "op2=x"),
    Iteration(name = "x+3.6", "op1=x", "op2=3.6")
  )
  fun testConvert_addVarAndDoubleConstant_returnsOpWithConstantFirst() {
    val expression = parseAlgebraicExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // The order of the summation should be based on the constants' values.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIrrationalThat().isWithin(1e-5).of(3.6)
          }
        }
        index(1) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  fun testConvert_addTwoVariables_leftX_rightX_returnsOpBothXs() {
    val expression = parseAlgebraicExpression("x + x")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(1) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration(name = "x+y", "op1=x", "op2=y"),
    Iteration(name = "y+x", "op1=y", "op2=x")
  )
  fun testConvert_addTwoVariables_oneX_oneY_returnsOpWithXThenY() {
    val expression = parseAlgebraicExpression("$op1 + $op2")

    val comparable = expression.toComparableOperation()

    // Variables are sorted lexicographically.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(1) {
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun testConvert_addMultipleVars_returnsOpWithThemInOrder() {
    val expression = parseAlgebraicExpression("x + z + x + y + x")

    val comparable = expression.toComparableOperation()

    // Variables are sorted lexicographically.
    assertThat(comparable).hasStructureThatMatches {
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
        index(0) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(1) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(2) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(3) {
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
        index(4) {
          variableTerm {
            withNameThat().isEqualTo("z")
          }
        }
      }
    }
  }

  /* Combined operations */

  @Test
  fun testConvert_allOperations_withNestedGroups_returnsCorrectlyStructuredAndOrderedOperation() {
    val expression = parseAlgebraicExpression("√(1+2*3)+-2^3*4/7-(2yx+x^(2+1)*(17/3))/-(x+(y+1.2))")

    val comparable = expression.toComparableOperation()

    assertThat(comparable).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      // Sum of (ordered based on expected sorting criteria):
      // - -(2yx+x^(2+1)*(17/3))/-(x+(y+1.2)) -> evaluates to positive
      // - -2^3*4/7
      // - √(1+2*3)
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          // Product of (in sorted order):
          // - 2yx+x^(2+1)*(17/3)
          // - inverse of x+(y+1.2)
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              // Sum of (in sorted order):
              // - x^(2+1)*(17/3)
              // - 2yx
              commutativeAccumulationWithType(SUMMATION) {
                hasOperandCountThat().isEqualTo(2)
                index(0) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  // Product of (in sorted order):
                  // - x^(2+1)
                  // - 17
                  // - inverse of 3
                  commutativeAccumulationWithType(PRODUCT) {
                    hasOperandCountThat().isEqualTo(3)
                    index(0) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      // Exponentiation of: x and 2+1.
                      nonCommutativeOperation {
                        exponentiation {
                          leftOperand {
                            hasNegatedPropertyThat().isFalse()
                            hasInvertedPropertyThat().isFalse()
                            variableTerm {
                              withNameThat().isEqualTo("x")
                            }
                          }
                          rightOperand {
                            hasNegatedPropertyThat().isFalse()
                            hasInvertedPropertyThat().isFalse()
                            // Summation of (in sorted order): 1 and 2.
                            commutativeAccumulationWithType(SUMMATION) {
                              hasOperandCountThat().isEqualTo(2)
                              index(0) {
                                hasNegatedPropertyThat().isFalse()
                                hasInvertedPropertyThat().isFalse()
                                constantTerm {
                                  withValueThat().isIntegerThat().isEqualTo(1)
                                }
                              }
                              index(1) {
                                hasNegatedPropertyThat().isFalse()
                                hasInvertedPropertyThat().isFalse()
                                constantTerm {
                                  withValueThat().isIntegerThat().isEqualTo(2)
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                    index(1) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      constantTerm {
                        withValueThat().isIntegerThat().isEqualTo(17)
                      }
                    }
                    index(2) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isTrue()
                      constantTerm {
                        withValueThat().isIntegerThat().isEqualTo(3)
                      }
                    }
                  }
                }
                index(1) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  // Product of (in sorted order): 2, x, and y.
                  commutativeAccumulationWithType(PRODUCT) {
                    hasOperandCountThat().isEqualTo(3)
                    index(0) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      constantTerm {
                        withValueThat().isIntegerThat().isEqualTo(2)
                      }
                    }
                    index(1) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      variableTerm {
                        withNameThat().isEqualTo("x")
                      }
                    }
                    index(2) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      variableTerm {
                        withNameThat().isEqualTo("y")
                      }
                    }
                  }
                }
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isTrue()
              // Sum of (in sorted order): 1.2, x, and y.
              commutativeAccumulationWithType(SUMMATION) {
                hasOperandCountThat().isEqualTo(3)
                index(0) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  constantTerm {
                    withValueThat().isIrrationalThat().isWithin(1e-5).of(1.2)
                  }
                }
                index(1) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  variableTerm {
                    withNameThat().isEqualTo("x")
                  }
                }
                index(2) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  variableTerm {
                    withNameThat().isEqualTo("y")
                  }
                }
              }
            }
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          // Product of:
          // - 2^3
          // - 4**
          // - inverse of 7
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(3)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              // Exponentiation of: 2 and 3.
              nonCommutativeOperation {
                exponentiation {
                  leftOperand {
                    hasNegatedPropertyThat().isFalse()
                    hasInvertedPropertyThat().isFalse()
                    constantTerm {
                      withValueThat().isIntegerThat().isEqualTo(2)
                    }
                  }
                  rightOperand {
                    hasNegatedPropertyThat().isFalse()
                    hasInvertedPropertyThat().isFalse()
                    constantTerm {
                      withValueThat().isIntegerThat().isEqualTo(3)
                    }
                  }
                }
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
            index(2) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isTrue()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(7)
              }
            }
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          nonCommutativeOperation {
            squareRootWithArgument {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              // Sum of (in sorted order):
              // - 2*3
              // - 1
              commutativeAccumulationWithType(SUMMATION) {
                hasOperandCountThat().isEqualTo(2)
                index(0) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  // Product of: 2 and 3.
                  commutativeAccumulationWithType(PRODUCT) {
                    hasOperandCountThat().isEqualTo(2)
                    index(0) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      constantTerm {
                        withValueThat().isIntegerThat().isEqualTo(2)
                      }
                    }
                    index(1) {
                      hasNegatedPropertyThat().isFalse()
                      hasInvertedPropertyThat().isFalse()
                      constantTerm {
                        withValueThat().isIntegerThat().isEqualTo(3)
                      }
                    }
                  }
                }
                index(1) {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  constantTerm {
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

  /*
   * Equivalence checks. Note that these don't specifically verify doubles since they may not have
   * reliable equivalence checking (and may instead require threshold checking for approximated
   * equivalence).
   *
   * Further, these checks are using vanilla equivalence checking since they rely on the opreations
   * being properly sorted.
   */

  @Test
  fun testEquals_additionOps_differentByCommutativity_areEqual() {
    val comparable1 = parseNumericExpression("1 + 2").toComparableOperation()
    val comparable2 = parseNumericExpression("2 + 1").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_additionOps_differentByAssociativity_areEqual() {
    val comparable1 = parseNumericExpression("1 + (2 + 3)").toComparableOperation()
    val comparable2 = parseNumericExpression("(1 + 2) + 3").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_additionOps_differentByAssociativityAndCommutativity_areEqual() {
    val comparable1 = parseNumericExpression("1 + (2 + 3)").toComparableOperation()
    val comparable2 = parseNumericExpression("(2 + 1) + 3").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_additionOps_differentByValue_areNotEqual() {
    val comparable1 = parseNumericExpression("1 + 2").toComparableOperation()
    val comparable2 = parseNumericExpression("1 + 3").toComparableOperation()

    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_additionOps_sameOnlyByEvaluation_areNotEqual() {
    val comparable1 = parseNumericExpression("1 + 2 + 2 + 1").toComparableOperation()
    val comparable2 = parseNumericExpression("1 + 2 + 3").toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationOps_differentByCommutativity_areEqual() {
    val comparable1 = parseNumericExpression("2 * 3").toComparableOperation()
    val comparable2 = parseNumericExpression("3 * 2").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationOps_differentByAssociativity_areEqual() {
    val comparable1 = parseNumericExpression("2 * (3 * 4)").toComparableOperation()
    val comparable2 = parseNumericExpression("(2 * 3) * 4").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationOps_differentByAssociativityAndCommutativity_areEqual() {
    val comparable1 = parseNumericExpression("2 * (3 * 4)").toComparableOperation()
    val comparable2 = parseNumericExpression("(3 * 2) * 4").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationOps_differentByValue_areNotEqual() {
    val comparable1 = parseNumericExpression("2 * 3").toComparableOperation()
    val comparable2 = parseNumericExpression("2 * 4").toComparableOperation()

    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationOps_sameOnlyByEvaluation_areNotEqual() {
    val comparable1 = parseNumericExpression("2 * 3 * 4").toComparableOperation()
    val comparable2 = parseNumericExpression("2 * 2 * 2 * 3").toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_subtractionOps_same_areEqual() {
    val comparable1 = parseNumericExpression("1 - 2").toComparableOperation()
    val comparable2 = parseNumericExpression("1 - 2").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_subtractionOps_differentByOrder_areNotEqual() {
    val comparable1 = parseNumericExpression("1 - 2").toComparableOperation()
    val comparable2 = parseNumericExpression("2 - 1").toComparableOperation()

    // Subtraction is not commutative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_subtractionOps_differentByAssociativity_areNotEqual() {
    val comparable1 = parseNumericExpression("1 - (2 - 3)").toComparableOperation()
    val comparable2 = parseNumericExpression("(1 - 2) - 3").toComparableOperation()

    // Subtraction is not associative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_subtractionOps_sameOnlyByEvaluation_areNotEqual() {
    val comparable1 = parseNumericExpression("1 - 2 - 3").toComparableOperation()
    val comparable2 = parseNumericExpression("1 - 2 - 2 - 1").toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_divisionOps_same_areEqual() {
    val comparable1 = parseNumericExpression("2 / 3").toComparableOperation()
    val comparable2 = parseNumericExpression("2 / 3").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_divisionOps_differentByOrder_areNotEqual() {
    val comparable1 = parseNumericExpression("2 / 3").toComparableOperation()
    val comparable2 = parseNumericExpression("3 / 2").toComparableOperation()

    // Division is not commutative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_divisionOps_differentByAssociativity_areNotEqual() {
    val comparable1 = parseNumericExpression("2 / (3 / 4)").toComparableOperation()
    val comparable2 = parseNumericExpression("(2 / 3) / 4").toComparableOperation()

    // Division is not associative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_divisionOps_sameOnlyByEvaluation_areNotEqual() {
    val comparable1 = parseNumericExpression("2 / 3 / 4").toComparableOperation()
    val comparable2 = parseNumericExpression("2 / 3 / 2 / 2").toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_exponentiationOps_same_areEqual() {
    val comparable1 = parseNumericExpression("2 ^ 3").toComparableOperation()
    val comparable2 = parseNumericExpression("2 ^ 3").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_exponentiationOps_differentByOrder_areNotEqual() {
    val comparable1 = parseNumericExpression("2 ^ 3").toComparableOperation()
    val comparable2 = parseNumericExpression("3 ^ 2").toComparableOperation()

    // Exponentiation is not commutative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_exponentiationOps_differentByAssociativity_areNotEqual() {
    // Disable optional errors to allow nested exponentiation.
    val comparable1 =
      parseNumericExpression(
        "2 ^ (3 ^ 4)", errorCheckingMode = REQUIRED_ONLY
      ).toComparableOperation()
    val comparable2 =
      parseNumericExpression(
        "(2 ^ 3) ^ 4", errorCheckingMode = REQUIRED_ONLY
      ).toComparableOperation()

    // Exponentiation is not associative.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_exponentiationOps_differentByValue_areNotEqual() {
    val comparable1 = parseNumericExpression("2 ^ 3").toComparableOperation()
    val comparable2 = parseNumericExpression("2 ^ 4").toComparableOperation()

    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_exponentiationOps_sameOnlyByEvaluation_areNotEqual() {
    // Disable optional errors to allow nested exponentiation.
    val comparable1 = parseNumericExpression("2 ^ 4").toComparableOperation()
    val comparable2 =
      parseNumericExpression("2 ^ 2 ^ 2", errorCheckingMode = REQUIRED_ONLY).toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_squareRootOps_same_areEqual() {
    val comparable1 = parseNumericExpression("sqrt(2)").toComparableOperation()
    val comparable2 = parseNumericExpression("sqrt(2)").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_squareRootOps_differentByValue_areNotEqual() {
    val comparable1 = parseNumericExpression("sqrt(2)").toComparableOperation()
    val comparable2 = parseNumericExpression("sqrt(3)").toComparableOperation()

    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_squareRootOps_sameOnlyByEvaluation_areNotEqual() {
    val comparable1 = parseNumericExpression("sqrt(2)").toComparableOperation()
    val comparable2 = parseNumericExpression("sqrt(1 + 1)").toComparableOperation()

    // While the two expressions are numerically equivalent, they aren't comparable since there are
    // extra terms in one (more than trivial rearranging is required to determine that they're
    // equal).
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_additionsAndSubtractions_differentByOrder_areEqual() {
    val comparable1 = parseNumericExpression("1+2-3").toComparableOperation()
    val comparable2 = parseNumericExpression("-3+2+1").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_multiplicationsAndDivisions_differentByOrder_areEqual() {
    val comparable1 = parseNumericExpression("2*3/4*7").toComparableOperation()
    val comparable2 = parseNumericExpression("7*2*3/4").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_allAccumulationOperations_differentByOrder_areEqual() {
    val comparable1 = parseNumericExpression("1+2*3/4*7-8+3").toComparableOperation()
    val comparable2 = parseNumericExpression("-8+3+7*3/4*2+1").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_allOperations_differentByOrder_areEqual() {
    val comparable1 = parseNumericExpression("sqrt(1+2*3)*2^3/7-(2-2*3)").toComparableOperation()
    val comparable2 = parseNumericExpression("2^3*sqrt(3*2+1)/7-(-3*2+2)").toComparableOperation()

    assertThat(comparable1).isEqualTo(comparable2)
  }

  @Test
  fun testEquals_allOperations_oneNestedDifferentByValue_areNotEqual() {
    val comparable1 = parseNumericExpression("sqrt(1+2*3)*2^3/7-(2-2*3)").toComparableOperation()
    val comparable2 = parseNumericExpression("sqrt(1+2*3)*2^3/7-(2-4*3)").toComparableOperation()

    // Just one different term leads to the entire comparison failing.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  @Test
  fun testEquals_twoOps_allOperations_oneMissingTerm_areNotEqual() {
    val comparable1 = parseNumericExpression("sqrt(1+2*3)*2^3/7-(2-2*3)").toComparableOperation()
    val comparable2 = parseNumericExpression("sqrt(1+2*3)*2^3/7-(2-2)").toComparableOperation()

    // Just one missing term leads to the entire comparison failing.
    assertThat(comparable1).isNotEqualTo(comparable2)
  }

  private companion object {
    private fun parseNumericExpression(
      expression: String, errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseNumericExpression(
        expression, errorCheckingMode
      ).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z"),
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      ).retrieveExpectedSuccessfulResult()
    }

    private fun <T> MathParsingResult<T>.retrieveExpectedSuccessfulResult(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
