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

/** Tests for [MathExpressionParser]. */
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToComparableOperationListTest {
  // TODO: add high-level checks for the three types, but don't test in detail since there are
  //  separate suites. Also, document the separate suites' existence in this suites's KDoc.

  @Test
  fun testToComparableOperation() {
    // TODO: split up & move to separate test suites. Finish test cases (if anymore are needed).

    val exp1 = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp1.toComparableOperationList()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }

    val exp2 = parseNumericExpressionSuccessfullyWithAllErrors("-1")
    assertThat(exp2.toComparableOperationList()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }

    val exp3 = parseNumericExpressionSuccessfullyWithAllErrors("1+3+4")
    assertThat(exp3.toComparableOperationList()).hasStructureThatMatches {
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

    val exp4 = parseNumericExpressionSuccessfullyWithAllErrors("-1-2-3")
    assertThat(exp4.toComparableOperationList()).hasStructureThatMatches {
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

    val exp5 = parseNumericExpressionSuccessfullyWithAllErrors("1+2-3")
    assertThat(exp5.toComparableOperationList()).hasStructureThatMatches {
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

    val exp6 = parseNumericExpressionSuccessfullyWithAllErrors("2*3*4")
    assertThat(exp6.toComparableOperationList()).hasStructureThatMatches {
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

    val exp7 = parseNumericExpressionSuccessfullyWithAllErrors("1-2*3")
    assertThat(exp7.toComparableOperationList()).hasStructureThatMatches {
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

    val exp8 = parseNumericExpressionSuccessfullyWithAllErrors("2*3-4")
    assertThat(exp8.toComparableOperationList()).hasStructureThatMatches {
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

    val exp9 = parseNumericExpressionSuccessfullyWithAllErrors("1+2*3-4+8*7*6-9")
    assertThat(exp9.toComparableOperationList()).hasStructureThatMatches {
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

    val exp10 = parseNumericExpressionSuccessfullyWithAllErrors("2/3/4")
    assertThat(exp10.toComparableOperationList()).hasStructureThatMatches {
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

    val exp11 = parseNumericExpressionSuccessfullyWithoutOptionalErrors("2^3^4")
    assertThat(exp11.toComparableOperationList()).hasStructureThatMatches {
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

    val exp12 = parseNumericExpressionSuccessfullyWithAllErrors("1+2/3+3")
    assertThat(exp12.toComparableOperationList()).hasStructureThatMatches {
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

    val exp13 = parseNumericExpressionSuccessfullyWithAllErrors("1+(2/3)+3")
    assertThat(exp13.toComparableOperationList()).hasStructureThatMatches {
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

    val exp14 = parseNumericExpressionSuccessfullyWithAllErrors("1+2^3+3")
    assertThat(exp14.toComparableOperationList()).hasStructureThatMatches {
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

    val exp15 = parseNumericExpressionSuccessfullyWithAllErrors("1+(2^3)+3")
    assertThat(exp15.toComparableOperationList()).hasStructureThatMatches {
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

    val exp16 = parseNumericExpressionSuccessfullyWithAllErrors("2*3/4*7")
    assertThat(exp16.toComparableOperationList()).hasStructureThatMatches {
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

    val exp17 = parseNumericExpressionSuccessfullyWithAllErrors("2*(3/4)*7")
    assertThat(exp17.toComparableOperationList()).hasStructureThatMatches {
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

    val exp18 = parseNumericExpressionSuccessfullyWithAllErrors("-3*sqrt(2)")
    assertThat(exp18.toComparableOperationList()).hasStructureThatMatches {
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

    val exp19 = parseNumericExpressionSuccessfullyWithAllErrors("1+(2+(3+(4+5)))")
    assertThat(exp19.toComparableOperationList()).hasStructureThatMatches {
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

    val exp20 = parseNumericExpressionSuccessfullyWithAllErrors("2*(3*(4*(5*6)))")
    assertThat(exp20.toComparableOperationList()).hasStructureThatMatches {
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

    val exp21 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp21.toComparableOperationList()).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
      hasInvertedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }

    val exp22 = parseAlgebraicExpressionSuccessfullyWithAllErrors("-x")
    assertThat(exp22.toComparableOperationList()).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
      hasInvertedPropertyThat().isFalse()
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }

    val exp23 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x+y")
    assertThat(exp23.toComparableOperationList()).hasStructureThatMatches {
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

    val exp24 = parseAlgebraicExpressionSuccessfullyWithAllErrors("-1-x-y")
    assertThat(exp24.toComparableOperationList()).hasStructureThatMatches {
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

    val exp25 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x-y")
    assertThat(exp25.toComparableOperationList()).hasStructureThatMatches {
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

    val exp26 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2xy")
    assertThat(exp26.toComparableOperationList()).hasStructureThatMatches {
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

    val exp27 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1-xy")
    assertThat(exp27.toComparableOperationList()).hasStructureThatMatches {
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

    val exp28 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy-4")
    assertThat(exp28.toComparableOperationList()).hasStructureThatMatches {
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

    val exp29 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+xy-4+yz-9")
    assertThat(exp29.toComparableOperationList()).hasStructureThatMatches {
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

    val exp30 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2/x/y")
    assertThat(exp30.toComparableOperationList()).hasStructureThatMatches {
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

    val exp31 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("x^3^4")
    assertThat(exp31.toComparableOperationList()).hasStructureThatMatches {
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

    val exp32 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x/y+z")
    assertThat(exp32.toComparableOperationList()).hasStructureThatMatches {
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

    val exp33 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x/y)+z")
    assertThat(exp33.toComparableOperationList()).hasStructureThatMatches {
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

    val exp34 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x^3+y")
    assertThat(exp34.toComparableOperationList()).hasStructureThatMatches {
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

    val exp35 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x^3)+y")
    assertThat(exp35.toComparableOperationList()).hasStructureThatMatches {
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

    val exp36 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*x/y*z")
    assertThat(exp36.toComparableOperationList()).hasStructureThatMatches {
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

    val exp37 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(x/y)*z")
    assertThat(exp37.toComparableOperationList()).hasStructureThatMatches {
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

    val exp38 = parseAlgebraicExpressionSuccessfullyWithAllErrors("-2*sqrt(x)")
    assertThat(exp38.toComparableOperationList()).hasStructureThatMatches {
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

    val exp39 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(x+(3+(z+y)))")
    assertThat(exp39.toComparableOperationList()).hasStructureThatMatches {
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

    val exp40 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(x*(4*(zy)))")
    assertThat(exp40.toComparableOperationList()).hasStructureThatMatches {
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

    // Equality tests:
    val list1 = createComparableOperationListFromNumericExpression("(1+2)+3")
    val list2 = createComparableOperationListFromNumericExpression("1+(2+3)")
    assertThat(list1).isEqualTo(list2)

    val list3 = createComparableOperationListFromNumericExpression("1+2+3")
    val list4 = createComparableOperationListFromNumericExpression("3+2+1")
    assertThat(list3).isEqualTo(list4)

    val list5 = createComparableOperationListFromNumericExpression("1-2-3")
    val list6 = createComparableOperationListFromNumericExpression("-3 + -2 + 1")
    assertThat(list5).isEqualTo(list6)

    val list7 = createComparableOperationListFromNumericExpression("1-2-3")
    val list8 = createComparableOperationListFromNumericExpression("-3-2+1")
    assertThat(list7).isEqualTo(list8)

    val list9 = createComparableOperationListFromNumericExpression("1-2-3")
    val list10 = createComparableOperationListFromNumericExpression("-3-2+1")
    assertThat(list9).isEqualTo(list10)

    val list11 = createComparableOperationListFromNumericExpression("1-2-3")
    val list12 = createComparableOperationListFromNumericExpression("3-2-1")
    assertThat(list11).isNotEqualTo(list12)

    val list13 = createComparableOperationListFromNumericExpression("2*3*4")
    val list14 = createComparableOperationListFromNumericExpression("4*3*2")
    assertThat(list13).isEqualTo(list14)

    val list15 = createComparableOperationListFromNumericExpression("2*(3/4)")
    val list16 = createComparableOperationListFromNumericExpression("3/4*2")
    assertThat(list15).isEqualTo(list16)

    val list17 = createComparableOperationListFromNumericExpression("2*3/4")
    val list18 = createComparableOperationListFromNumericExpression("3/4*2")
    assertThat(list17).isEqualTo(list18)

    val list45 = createComparableOperationListFromNumericExpression("2*3/4")
    val list46 = createComparableOperationListFromNumericExpression("2*3*4")
    assertThat(list45).isNotEqualTo(list46)

    val list19 = createComparableOperationListFromNumericExpression("2*3/4")
    val list20 = createComparableOperationListFromNumericExpression("2*4/3")
    assertThat(list19).isNotEqualTo(list20)

    val list21 = createComparableOperationListFromNumericExpression("2*3/4*7")
    val list22 = createComparableOperationListFromNumericExpression("3/4*7*2")
    assertThat(list21).isEqualTo(list22)

    val list23 = createComparableOperationListFromNumericExpression("2*3/4*7")
    val list24 = createComparableOperationListFromNumericExpression("7*(3*2/4)")
    assertThat(list23).isEqualTo(list24)

    val list25 = createComparableOperationListFromNumericExpression("2*3/4*7")
    val list26 = createComparableOperationListFromNumericExpression("7*3*2/4")
    assertThat(list25).isEqualTo(list26)

    val list27 = createComparableOperationListFromNumericExpression("-2*3")
    val list28 = createComparableOperationListFromNumericExpression("3*-2")
    assertThat(list27).isEqualTo(list28)

    val list29 = createComparableOperationListFromNumericExpression("2^3")
    val list30 = createComparableOperationListFromNumericExpression("3^2")
    assertThat(list29).isNotEqualTo(list30)

    val list31 = createComparableOperationListFromNumericExpression("-(1+2)")
    val list32 = createComparableOperationListFromNumericExpression("-1+2")
    assertThat(list31).isNotEqualTo(list32)

    val list33 = createComparableOperationListFromNumericExpression("-(1+2)")
    val list34 = createComparableOperationListFromNumericExpression("-1-2")
    assertThat(list33).isNotEqualTo(list34)

    val list35 = createComparableOperationListFromAlgebraicExpression("x(x+1)")
    val list36 = createComparableOperationListFromAlgebraicExpression("(1+x)x")
    assertThat(list35).isEqualTo(list36)

    val list37 = createComparableOperationListFromAlgebraicExpression("x(x+1)")
    val list38 = createComparableOperationListFromAlgebraicExpression("x^2+x")
    assertThat(list37).isNotEqualTo(list38)

    val list39 = createComparableOperationListFromAlgebraicExpression("x^2*sqrt(x)")
    val list40 = createComparableOperationListFromAlgebraicExpression("x")
    assertThat(list39).isNotEqualTo(list40)

    val list41 = createComparableOperationListFromAlgebraicExpression("xyz")
    val list42 = createComparableOperationListFromAlgebraicExpression("zyx")
    assertThat(list41).isEqualTo(list42)

    val list43 = createComparableOperationListFromAlgebraicExpression("1+xy-2")
    val list44 = createComparableOperationListFromAlgebraicExpression("-2+1+yx")
    assertThat(list43).isEqualTo(list44)

    // TODO: add tests for comparator/sorting & negation simplification?
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
