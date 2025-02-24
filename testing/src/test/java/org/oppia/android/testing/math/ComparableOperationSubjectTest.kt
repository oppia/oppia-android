package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.ComparableOperationSubject.Companion.assertThat

/** Tests for [ComparableOperationSubject]. */
@RunWith(JUnit4::class)
class ComparableOperationSubjectTest {

  private fun createConstantOperation(value: Int): ComparableOperation {
    return ComparableOperation.newBuilder()
      .setConstantTerm(Real.newBuilder().setInteger(value))
      .build()
  }

  private fun createVariableOperation(name: String): ComparableOperation {
    return ComparableOperation.newBuilder()
      .setVariableTerm(name)
      .build()
  }

  private fun createCommutativeAccumulation(
    type: ComparableOperation.CommutativeAccumulation.AccumulationType,
    vararg operations: ComparableOperation
  ): ComparableOperation {
    val accumulation = ComparableOperation.CommutativeAccumulation.newBuilder()
      .setAccumulationType(type)
    operations.forEach { accumulation.addCombinedOperations(it) }
    return ComparableOperation.newBuilder()
      .setCommutativeAccumulation(accumulation)
      .build()
  }

  @Test
  fun testComparableOperationSubject_hasStructureThatMatches() {
    val operation = createConstantOperation(42)

    assertThat(operation).hasStructureThatMatches {
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(42)
      }
    }
  }

  @Test
  fun testComparableOperationSubject_failsWithInvalidStructure() {
    val operation = createConstantOperation(42)
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        variableTerm {
          withNameThat().isEqualTo("x")
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_hasNegatedProperty_matchesFalse() {
    val operation = createConstantOperation(42)

    assertThat(operation).hasStructureThatMatches {
      hasNegatedPropertyThat().isFalse()
    }
  }

  @Test
  fun testComparableOperationSubject_hasNegatedPropertyThat_matchesTrue() {
    val operation = ComparableOperation.newBuilder()
      .setConstantTerm(Real.newBuilder().setInteger(42))
      .setIsNegated(true)
      .build()

    assertThat(operation).hasStructureThatMatches {
      hasNegatedPropertyThat().isTrue()
    }
  }

  @Test
  fun testComparableOperationSubject_hasInvertedProperty_matchesFalse() {
    val operation = createConstantOperation(42)

    assertThat(operation).hasStructureThatMatches {
      hasInvertedPropertyThat().isFalse()
    }
  }

  @Test
  fun testComparableOperationSubject_hasInvertedProperty_matchesTrue() {
    val operation = ComparableOperation.newBuilder()
      .setConstantTerm(Real.newBuilder().setInteger(42))
      .setIsInverted(true)
      .build()

    assertThat(operation).hasStructureThatMatches {
      hasInvertedPropertyThat().isTrue()
    }
  }

  @Test
  fun testComparableOperationSubject_commutativeAccumulation_withValidSummation() {
    val operation = createCommutativeAccumulation(
      ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION,
      createConstantOperation(1),
      createConstantOperation(2)
    )

    assertThat(operation).hasStructureThatMatches {
      commutativeAccumulationWithType(
        ComparableOperation.CommutativeAccumulation
          .AccumulationType.SUMMATION
      ) {
        hasOperandCountThat().isEqualTo(2)
        index(0) {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(1)
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
  fun testComparableOperationSubject_withEmpty_commutativeAccumulation_hasCorrectStructre() {
    val operation = createCommutativeAccumulation(
      ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION
    )

    assertThat(operation).hasStructureThatMatches {
      commutativeAccumulationWithType(
        ComparableOperation.CommutativeAccumulation
          .AccumulationType.SUMMATION
      ) {
        hasOperandCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testComparableOperationSubject_commutativeAccumulation_failsWithInvalidType() {
    val operation = createConstantOperation(42)
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        commutativeAccumulationWithType(
          ComparableOperation.CommutativeAccumulation
            .AccumulationType.SUMMATION
        ) {
          hasOperandCountThat().isEqualTo(0)
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_commutativeAccumulation_failsWithInvalidIndex() {
    val operation = createCommutativeAccumulation(
      ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION,
      createConstantOperation(1)
    )
    assertThrows(IndexOutOfBoundsException::class.java) {
      assertThat(operation).hasStructureThatMatches {
        commutativeAccumulationWithType(
          ComparableOperation.CommutativeAccumulation
            .AccumulationType.SUMMATION
        ) {
          index(1) { }
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_matchesWithValidOperation() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setExponentiation(
            ComparableOperation.NonCommutativeOperation.BinaryOperation.newBuilder()
              .setLeftOperand(createConstantOperation(2))
              .setRightOperand(createConstantOperation(3))
          )
      )
      .build()

    assertThat(operation).hasStructureThatMatches {
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
  fun testComparableOperationSubject_exponentiation_failsWithInvalidOperation() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setSquareRoot(createConstantOperation(4))
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        nonCommutativeOperation {
          exponentiation { }
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_squareRoot_withValidOperation_hasCorrectStructure() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setSquareRoot(createConstantOperation(4))
      )
      .build()

    assertThat(operation).hasStructureThatMatches {
      nonCommutativeOperation {
        squareRootWithArgument {
          constantTerm {
            withValueThat().isIntegerThat().isEqualTo(4)
          }
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_squareRoot_failsWithInvalidOperation() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setExponentiation(
            ComparableOperation.NonCommutativeOperation.BinaryOperation.newBuilder()
              .setLeftOperand(createConstantOperation(2))
              .setRightOperand(createConstantOperation(3))
          )
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        nonCommutativeOperation {
          squareRootWithArgument { }
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_binaryOperation_failsWithInvalidLeftOperand() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setExponentiation(
            ComparableOperation.NonCommutativeOperation.BinaryOperation.newBuilder()
              .setRightOperand(createConstantOperation(3))
          )
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
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
    }
  }

  @Test
  fun testComparableOperationSubject_binaryOperation_failsWithInvalidRightOperand() {
    val operation = ComparableOperation.newBuilder()
      .setNonCommutativeOperation(
        ComparableOperation.NonCommutativeOperation.newBuilder()
          .setExponentiation(
            ComparableOperation.NonCommutativeOperation.BinaryOperation.newBuilder()
              .setLeftOperand(createConstantOperation(2))
          )
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        nonCommutativeOperation {
          exponentiation {
            rightOperand {
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
  fun testComparableOperationSubject_checksConstantTerm_withValidValue() {
    val operation = createConstantOperation(42)

    assertThat(operation).hasStructureThatMatches {
      constantTerm {
        withValueThat().isIntegerThat().isEqualTo(42)
      }
    }
  }

  @Test
  fun testComparableOperationSubject_constantTerm_failsWithInvalidType() {
    val operation = createVariableOperation("x")
    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        constantTerm {
          withValueThat().isIntegerThat().isEqualTo(42)
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_withVariableTerm_hasExpectedName() {
    val operation = createVariableOperation("x")

    assertThat(operation).hasStructureThatMatches {
      variableTerm {
        withNameThat().isEqualTo("x")
      }
    }
  }

  @Test
  fun testComparableOperationSubject_variableTerm_failWithInvalidType() {
    val operation = createConstantOperation(42)

    assertThrows(AssertionError::class.java) {
      assertThat(operation).hasStructureThatMatches {
        variableTerm {
          withNameThat().isEqualTo("x")
        }
      }
    }
  }

  @Test
  fun testComparableOperationSubject_complexExpression_withNestedOperations_hasCorrectStructure() {
    val operation = createCommutativeAccumulation(
      ComparableOperation.CommutativeAccumulation.AccumulationType.PRODUCT,
      ComparableOperation.newBuilder()
        .setNonCommutativeOperation(
          ComparableOperation.NonCommutativeOperation.newBuilder()
            .setExponentiation(
              ComparableOperation.NonCommutativeOperation.BinaryOperation.newBuilder()
                .setLeftOperand(createConstantOperation(2))
                .setRightOperand(createConstantOperation(3))
            )
        )
        .build(),
      ComparableOperation.newBuilder()
        .setNonCommutativeOperation(
          ComparableOperation.NonCommutativeOperation.newBuilder()
            .setSquareRoot(createConstantOperation(4))
        )
        .build(),
      createVariableOperation("x")
    )

    assertThat(operation).hasStructureThatMatches {
      commutativeAccumulationWithType(
        ComparableOperation.CommutativeAccumulation
          .AccumulationType.PRODUCT
      ) {
        hasOperandCountThat().isEqualTo(3)
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
          nonCommutativeOperation {
            squareRootWithArgument {
              constantTerm {
                withValueThat().isIntegerThat().isEqualTo(4)
              }
            }
          }
        }
        index(2) {
          variableTerm {
            withNameThat().isEqualTo("x")
          }
        }
      }
    }
  }
}
