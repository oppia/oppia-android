package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ComparableOperationList.CommutativeAccumulation.AccumulationType.PRODUCT
import org.oppia.android.app.model.ComparableOperationList.CommutativeAccumulation.AccumulationType.SUMMATION
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.ComparableOperationListSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/** Tests for [ExpressionToComparableOperationConverter]. */
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToComparableOperationConverterTest {
  // TODO: add high-level checks for the three types, but don't test in detail since there are
  //  separate suites. Also, document the separate suites' existence in this suites's KDoc.

  // TODO: use utility directly
  // TODO: finish tests.
  // TODO: add tests for comparator/sorting & negation simplification?

  /* Operation creation */
  // testConvert_constantExpression_returnsConstantOperation
  // testConvert_variableExpression_returnsVariableOperation
  // testConvert_addition_returnsSummation
  // testConvert_subtraction_returnsSummationOfNegative
  // testConvert_multiplication_returnsProduct
  // testConvert_division_returnsProductOfInverted
  // testConvert_exponentiation_returnsNonCommutativeOperation
  // testConvert_squareRoot_returnsNonCommutativeOperation
  // testConvert_negatedVariable_returnsNegativeVariableOperation
  // testConvert_positiveVariable_returnsVariableOperation
  // testConvert_positiveOfNegativeVariable_returnsNegativeVariableOperation
  // testConvert_subtractionOfNegative_returnsSummationWithPositives
  // testConvert_multipleAdditions_returnsCombinedSummation
  // testConvert_multipleSubtractions_returnsCombinedSummation
  // testConvert_additionsAndSubtractions_returnsCombinedSummation
  // testConvert_additionsWithNestedAdds_returnsCompletelyCombinedSummation
  // testConvert_subtractsWithNestedSubs_returnsCompletelyCombinedSummation
  // testConvert_additionsAndSubtractionsWithNested_returnsCombinedSummation
  // testConvert_multipleMultiplications_returnsCombinedProduct
  // testConvert_multipleDivisions_returnsCombinedProduct
  // testConvert_multiplicationsAndDivisions_returnsCombinedProduct
  // testConvert_multiplicationsWithNestedMults_returnsCompletelyCombinedProduct
  // testConvert_divisionsWithNestedDivs_returnsCompletelyCombinedProduct
  // testConvert_multiplicationsAndDivisionsWithNested_returnsCombinedProduct
  // testConvert_multiplicationWithOneNegative_returnsNegativeProduct
  // testConvert_multiplicationWithTwoNegatives_returnsPositiveProduct
  // testConvert_multiplicationWithThreeNegatives_returnsNegativeProduct
  // testConvert_combinedMultDivWithNested_evenNegatives_returnsPositiveProduct
  // testConvert_combinedMultDivWithNested_oddNegatives_returnsNegativeProduct
  // testConvert_additionAndExp_returnsSummationWithNonCommutative
  // testConvert_additionAndSquareRoot_returnsSummationWithNonCommutative
  // testConvert_additionWithinExp_returnsSummationWithinNonCommutative
  // testConvert_additionWithinSquareRoot_returnsSummationWithinNonCommutative
  // testConvert_multiplicationAndExp_returnsProductWithNonCommutative
  // testConvert_multiplicationAndSquareRoot_returnsProductWithNonCommutative
  // testConvert_multiplicationWithinExp_returnsProductWithinNonCommutative
  // testConvert_multiplicationWithinSquareRoot_returnsProductWithinNonCommutative
  // testConvert_additionAndMultiplication_returnsSummationOfProduct
  // testConvert_multiplicationAndGroupedAddition_returnsProductOfSummation

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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-1")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+3+4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-1-2-3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+2-3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2*3*4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1-2*3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2*3-4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+2*3-4+8*7*6-9")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2/3/4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithoutOptionalErrors("2^3^4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+2/3+3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+(2/3)+3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+2^3+3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+(2^3)+3")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2*3/4*7")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2*(3/4)*7")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-3*sqrt(2)")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+(2+(3+(4+5)))")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2*(3*(4*(5*6)))")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("-x")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x+y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("-1-x-y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x-y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2xy")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1-xy")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy-4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+xy-4+yz-9")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2/x/y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("x^3^4")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x/y+z")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x/y)+z")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x^3+y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x^3)+y")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*x/y*z")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(x/y)*z")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("-2*sqrt(x)")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x+(3+(z+y)))")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(x*(4*(zy)))")
    assertThat(exp.toComparableOperationList()).hasStructureThatMatches {
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
    parseNumericExpressionSuccessfullyWithAllErrors(expression).toComparableOperationList()

  private fun createComparableOperationListFromAlgebraicExpression(expression: String) =
    parseAlgebraicExpressionSuccessfullyWithAllErrors(expression).toComparableOperationList()

  private companion object {
    // TODO: fix helper API.

    private fun parseNumericExpressionSuccessfullyWithAllErrors(
      expression: String
    ): MathExpression {
      val result = parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionSuccessfullyWithoutOptionalErrors(
      expression: String
    ): MathExpression {
      val result =
        parseNumericExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }

    private fun parseAlgebraicExpressionSuccessfullyWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables
        )
      return (result as MathParsingResult.Success<MathExpression>).result
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
  }
}
