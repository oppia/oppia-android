package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.toComparableOperation
import org.junit.runner.RunWith
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.PRODUCT
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.ComparableOperationSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/** Tests for [ExpressionToComparableOperationConverter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToComparableOperationConverterTest {
  // TODO: add tests for comparator/sorting & negation simplification?

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
    val expression = parseNumericExpression("1++(2-3)+-(4+5--(2+3-1))", REQUIRED_ONLY)

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
    val expression = parseNumericExpression("-2*-3/-(4/-(3*2*+(-3*7)))", REQUIRED_ONLY)

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

  /* Top-level operation sorting */
  // testConvert_additionThenSquareRoot_samePrecedence_returnsOpWithSummationFirst
  // testConvert_squareRootThenAddition_samePrecedence_returnsOpWithSummationFirst
  // testConvert_additionThenExp_samePrecedence_returnsOpWithSummationFirst
  // testConvert_exponentiationThenAddition_samePrecedence_returnsOpWithSummationFirst
  // testConvert_constantThenSquareRoot_samePrecedence_returnsOpWithNonCommutativeFirst
  // testConvert_squareRootThenConstant_samePrecedence_returnsOpWithNonCommutativeFirst
  // testConvert_constantThenExp_samePrecedence_returnsOpWithNonCommutativeFirst
  // testConvert_exponentiationThenConstant_samePrecedence_returnsOpWithNonCommutativeFirst
  // testConvert_constantThenVariable_samePrecedence_returnsOpWithConstantFirst
  // testConvert_variableThenConstant_samePrecedence_returnsOpWithConstantFirst
  // testConvert_twoVariables_negatedThenInverted_returnsOpWithNegatedFirst
  // testConvert_twoVariables_invertedThenNegated_returnsOpWithNegatedFirst

  /* Accumulator sorting */
  // TODO: add sorting for negatives & inverteds.
  // TODO: mention no tiebreakers since there can't be summations or products adjacent with others
  //  of the same type.
  // testConvert_additionAndMult_samePrecedence_returnsOpWithSummationFirst
  // testConvert_multiplicationAndAddition_samePrecedence_returnsOpWithSummationFirst
  // testConvert_additionAndMult_samePrecedenceAsNested_returnsOpWithSummationFirst
  // testConvert_multiplicationAndAddition_samePrecedenceAsNested_returnsOpWithSummationFirst

  /* Non-commutative sorting */
  // testConvert_addExpThenSqrt_samePrecedence_returnsOpWithExpThenSqrt
  // testConvert_addSqrtThenExp_samePrecedence_returnsOpWithExpThenSqrt
  // testConvert_addTwoExps_lhs1Const_rhs1Const_lhs2Const_rhs2Const_returnsOpWithExp1Then2
  // ... parameterized:
  // const^const  const^const
  // var^const    const^const
  // const^var    const^const
  // var^var      const^const
  //
  // const^const  var^const
  // var^const    var^const
  // const^var    var^const
  // var^var      var^const
  //
  // const^const  const^var
  // var^const    const^var
  // const^var    const^var
  // var^var      const^var
  //
  // const^const  var^var
  // var^const    var^var
  // const^var    var^var
  // var^var      var^var
  // ...
  // testConvert_addTwoSqrts_leftConst_rightConst_returnsOpWithSqrt1ThenSqrt2
  // testConvert_addTwoSqrts_leftVar_rightConst_returnsOpWithSqrt2ThenSqrt1
  // testConvert_addTwoSqrts_leftConst_rightVar_returnsOpWithSqrt1ThenSqrt2
  // testConvert_addTwoSqrts_leftVar_rightVar_returnsOpWithSqrt1ThenSqrt2

  // testConvert_addTwoExps_lhs1Var_rhs1Const_lhs2Const_rhs2Const_returnsOpWithExp2Then1
  // testConvert_addTwoExps_lhs1Const_rhs1Var_lhs2Const_rhs2Const_returnsOpWithExp2Then1
  // testConvert_addTwoExps_lhs1Const_rhs1Var_lhs2Const_rhs2Var_returnsOpWithExp1Then2
  // testConvert_addTwoExps_lhs1Const_rhs1Var_lhs2Var_rhs2Const_returnsOpWithExp1Then2

  /* Constant sorting */
  // testConvert_addTwoConstants_leftInteger2_rightInteger3_returnsOpWith2Then3
  // ... parameterized:
  // left: 2  right: 3
  // left: 3  right: 2
  //
  // left: 2  right: 3.14
  // left: 3.14  right: 2
  //
  // left: 4  right: 3.14
  // left: 3.14  right: 4
  //
  // left: 3.14  right: 6.28
  // left: 6.28  right: 3.14
  // ...

  /* Variable sorting */
  // testConvert_addTwoVariables_leftX_rightX_returnsOpBothXs
  // testConvert_addTwoVariables_leftX_rightY_returnsOpWithXThenY
  // ... parameterized:
  // x, y; y, x; y, z; z, y; x, y, z; z, y, x
  // ...

  /* Combined operations */
  // testConvert_allOperations_withNestedGroups_returnsCorrectlyStructuredAndOrderedOperation

  /* Equivalence checks */
  // testEquals_twoAdditionOps_differentByCommutativity_areEqual
  // testEquals_twoAdditionOps_differentByAssociativity_areEqual
  // testEquals_twoAdditionOps_differentByAssociativityAndCommutativity_areEqual
  // testEquals_twoAdditionOps_differentByValue_areNotEqual
  // testEquals_twoAdditionOps_differentByEvaluation_areNotEqual
  // testEquals_twoMultOps_differentByCommutativity_areEqual
  // testEquals_twoMultOps_differentByAssociativity_areEqual
  // testEquals_twoMultOps_differentByAssociativityAndCommutativity_areEqual
  // testEquals_twoMultOps_differentByValue_areNotEqual
  // testEquals_twoMultOps_differentByEvaluation_areNotEqual
  // TODO: for this & the next one, test with three operations (e.g. 2 / 3 / 4).
  // testEquals_twoSubOps_same_areEqual
  // testEquals_twoSubOps_differentByOrder_areNotEqual
  // testEquals_twoSubOps_differentByValue_areNotEqual
  // testEquals_twoDivOps_same_areEqual
  // testEquals_twoDivOps_differentByOrder_areNotEqual
  // testEquals_twoDivOps_differentByValue_areNotEqual
  // testEquals_twoExps_same_areEqual
  // testEquals_twoExps_differentByOrder_areNotEqual
  // testEquals_twoExps_differentByValue_areNotEqual
  // testEquals_twoSqrts_same_areEqual
  // testEquals_twoSqrts_differentByValue_areNotEqual
  // testEquals_twoOps_addsAndSubs_differentByOrder_areEqual
  // testEquals_twoOps_multsAndDivs_differentByOrder_areEqual
  // testEquals_twoOps_addsSubsMultsAndDivs_differentByOrder_areEqual
  // testEquals_twoOps_allOperations_differentByOrder_areEqual
  // testEquals_twoOps_allOperations_oneNestedDifferentByValue_areNotEqual
  // testEquals_twoOps_allOperations_oneMissingTerm_areNotEqual

  @Test
  fun test1() {
    // TODO: do something with this
    val exp = parseNumericExpression("1")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun test2() {
    // TODO: do something with this
    val exp = parseNumericExpression("-1")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun test3() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+3+4")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
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
  fun test4() {
    // TODO: do something with this
    val exp = parseNumericExpression("-1-2-3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(2)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun test5() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+2-3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
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
        index(2) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
      }
    }
  }

  @Test
  fun test6() {
    // TODO: do something with this
    val exp = parseNumericExpression("2*3*4")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
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
  fun test7() {
    // TODO: do something with this
    val exp = parseNumericExpression("1-2*3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
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

  @Test
  fun test8() {
    // TODO: do something with this
    val exp = parseNumericExpression("2*3-4")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
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
        index(1) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun test9() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+2*3-4+8*7*6-9")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
        index(0) {
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
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(3)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(6)
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(7)
              }
            }
            index(2) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(8)
              }
            }
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(9)
          }
        }
      }
    }
  }

  @Test
  fun test10() {
    // TODO: do something with this
    val exp = parseNumericExpression("2/3/4")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
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
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun test11() {
    // TODO: do something with this
    val exp = parseNumericExpression("2^3^4", REQUIRED_ONLY)
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
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
            nonCommutativeOperation {
              exponentiation {
                leftOperand {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  constantTerm {
                    withValueThat().isIntegerThat().isEqualTo(3)
                  }
                }
                rightOperand {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
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
  }

  @Test
  fun test12() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+2/3+3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
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
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
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
  fun test13() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+(2/3)+3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
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
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
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
  fun test14() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+2^3+3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
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
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
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
  fun test15() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+(2^3)+3")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
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
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(2) {
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
  fun test16() {
    // TODO: do something with this
    val exp = parseNumericExpression("2*3/4*7")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(4)
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
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun test17() {
    // TODO: do something with this
    val exp = parseNumericExpression("2*(3/4)*7")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(4)
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
            withValueThat().isIntegerThat().isEqualTo(7)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun test18() {
    // TODO: do something with this
    val exp = parseNumericExpression("-3*sqrt(2)")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          nonCommutativeOperation {
            squareRootWithArgument {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(2)
              }
            }
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
  fun test19() {
    // TODO: do something with this
    val exp = parseNumericExpression("1+(2+(3+(4+5)))")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
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
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
      }
    }
  }

  @Test
  fun test20() {
    // TODO: do something with this
    val exp = parseNumericExpression("2*(3*(4*(5*6)))")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(5)
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
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(5)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(6)
          }
        }
      }
    }
  }

  @Test
  fun test21() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("x")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun test22() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("-x")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun test23() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+x+y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
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

  @Test
  fun test24() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("-1-x-y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(2) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun test25() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+x-y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
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
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(2) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun test26() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("2xy")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
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

  @Test
  fun test27() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1-xy")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("y")
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

  @Test
  fun test28() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("xy-4")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("y")
              }
            }
          }
        }
        index(1) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun test29() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+xy-4+yz-9")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("y")
              }
            }
          }
        }
        index(1) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("y")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("z")
              }
            }
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
          }
        }
        index(3) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(4) {
          hasNegatedPropertyThat().isTrue()
          hasInvertedPropertyThat().isFalse()
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(9)
          }
        }
      }
    }
  }

  @Test
  fun test30() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("2/x/y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
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
          hasInvertedPropertyThat().isTrue()
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun test31() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("x^3^4", REQUIRED_ONLY)
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
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
            nonCommutativeOperation {
              exponentiation {
                leftOperand {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
                  constantTerm {
                    withValueThat().isIntegerThat().isEqualTo(3)
                  }
                }
                rightOperand {
                  hasNegatedPropertyThat().isFalse()
                  hasInvertedPropertyThat().isFalse()
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
  }

  @Test
  fun test32() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+x/y+z")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isTrue()
              variableTerm {
                withNameThat().isEqualTo("y")
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
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("z")
          }
        }
      }
    }
  }

  @Test
  fun test33() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+(x/y)+z")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          commutativeAccumulationWithType(PRODUCT) {
            hasOperandCountThat().isEqualTo(2)
            index(0) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
            index(1) {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isTrue()
              variableTerm {
                withNameThat().isEqualTo("y")
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
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("z")
          }
        }
      }
    }
  }

  @Test
  fun test34() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+x^3+y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
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
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun test35() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+(x^3)+y")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(3)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
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
            withValueThat().isIntegerThat().isEqualTo(1)
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

  @Test
  fun test36() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("2*x/y*z")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(4)
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
            withNameThat().isEqualTo("z")
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun test37() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("2*(x/y)*z")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(4)
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
            withNameThat().isEqualTo("z")
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isTrue()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
      }
    }
  }

  @Test
  fun test38() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("-2*sqrt(x)")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          nonCommutativeOperation {
            squareRootWithArgument {
              hasNegatedPropertyThat().isFalse()
              hasInvertedPropertyThat().isFalse()
              variableTerm {
                withNameThat().isEqualTo("x")
              }
            }
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

  @Test
  fun test39() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("1+(x+(3+(z+y)))")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(SUMMATION) {
        hasOperandCountThat().isEqualTo(5)
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
            withValueThat().isIntegerThat().isEqualTo(3)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
        index(4) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("z")
          }
        }
      }
    }
  }

  @Test
  fun test40() {
    // TODO: do something with this
    val exp = parseAlgebraicExpression("2*(x*(4*(zy)))")
    assertThat(exp.toComparableOperation()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      commutativeAccumulationWithType(PRODUCT) {
        hasOperandCountThat().isEqualTo(5)
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
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
        index(2) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
        index(3) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("y")
          }
        }
        index(4) {
          hasNegatedPropertyThat().isFalse()
          hasInvertedPropertyThat().isFalse()
          variableTerm {
            withNameThat().isEqualTo("z")
          }
        }
      }
    }
  }

  // TODO: Equality tests:
  @Test
  fun test41() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("(1+2)+3")
    val secondList = createComparableOperationListFromNumericExpression("1+(2+3)")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test42() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("1+2+3")
    val secondList = createComparableOperationListFromNumericExpression("3+2+1")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test43() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("1-2-3")
    val secondList = createComparableOperationListFromNumericExpression("-3 + -2 + 1")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test44() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("1-2-3")
    val secondList = createComparableOperationListFromNumericExpression("-3-2+1")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test45() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("1-2-3")
    val secondList = createComparableOperationListFromNumericExpression("-3-2+1")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test46() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("1-2-3")
    val secondList = createComparableOperationListFromNumericExpression("3-2-1")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test47() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3*4")
    val secondList = createComparableOperationListFromNumericExpression("4*3*2")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test48() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*(3/4)")
    val secondList = createComparableOperationListFromNumericExpression("3/4*2")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test49() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4")
    val secondList = createComparableOperationListFromNumericExpression("3/4*2")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test50() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4")
    val secondList = createComparableOperationListFromNumericExpression("2*3*4")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test51() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4")
    val secondList = createComparableOperationListFromNumericExpression("2*4/3")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test52() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4*7")
    val secondList = createComparableOperationListFromNumericExpression("3/4*7*2")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test53() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4*7")
    val secondList = createComparableOperationListFromNumericExpression("7*(3*2/4)")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test54() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2*3/4*7")
    val secondList = createComparableOperationListFromNumericExpression("7*3*2/4")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test55() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("-2*3")
    val secondList = createComparableOperationListFromNumericExpression("3*-2")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test56() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("2^3")
    val secondList = createComparableOperationListFromNumericExpression("3^2")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test57() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("-(1+2)")
    val secondList = createComparableOperationListFromNumericExpression("-1+2")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test58() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromNumericExpression("-(1+2)")
    val secondList = createComparableOperationListFromNumericExpression("-1-2")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test59() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromAlgebraicExpression("x(x+1)")
    val secondList = createComparableOperationListFromAlgebraicExpression("(1+x)x")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test60() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromAlgebraicExpression("x(x+1)")
    val secondList = createComparableOperationListFromAlgebraicExpression("x^2+x")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test61() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromAlgebraicExpression("x^2*sqrt(x)")
    val secondList = createComparableOperationListFromAlgebraicExpression("x")
    assertThat(firstList).isNotEqualTo(secondList)
  }

  @Test
  fun test62() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromAlgebraicExpression("xyz")
    val secondList = createComparableOperationListFromAlgebraicExpression("zyx")
    assertThat(firstList).isEqualTo(secondList)
  }

  @Test
  fun test63() {
    // TODO: do something with this
    val firstList = createComparableOperationListFromAlgebraicExpression("1+xy-2")
    val secondList = createComparableOperationListFromAlgebraicExpression("-2+1+yx")
    assertThat(firstList).isEqualTo(secondList)
  }

  private fun createComparableOperationListFromNumericExpression(expression: String) =
    parseNumericExpression(expression).toComparableOperation()

  private fun createComparableOperationListFromAlgebraicExpression(expression: String) =
    parseAlgebraicExpression(expression).toComparableOperation()

  private companion object {
    private fun parseNumericExpression(
      expression: String, errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseNumericExpression(
        expression, errorCheckingMode
      ).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicExpression(
      expression: String, errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables = listOf("x", "y", "z"), errorCheckingMode
      ).retrieveExpectedSuccessfulResult()
    }

    private fun <T> MathParsingResult<T>.retrieveExpectedSuccessfulResult(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
