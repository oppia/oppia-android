package org.oppia.android.testing.math

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real

/** Tests for [MathEquationSubject]. */
@RunWith(JUnit4::class)
class MathEquationSubjectTest {

  @Test
  fun testHasLeftHandSide_withValidExpression_matchesExpression() {
    val equation = createEquation(
      leftSide = createConstantExpression(5),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(5)
      }
    }
  }

  @Test
  fun testHasLeftHandSide_withDefaultExpression_hasNoExpressionType() {
    val equation = MathEquation.getDefaultInstance()

    MathEquationSubject.assertThat(equation).hasLeftHandSideThat().isEqualTo(
      MathExpression.getDefaultInstance()
    )
  }

  @Test
  fun testHasRightHandSide_withValidExpression_matchesExpression() {
    val equation = createEquation(
      leftSide = createConstantExpression(0),
      rightSide = createConstantExpression(10)
    )

    MathEquationSubject.assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(10)
      }
    }
  }

  @Test
  fun testHasRightHandSide_withDefaultExpression_hasNoExpressionType() {
    val equation = MathEquation.getDefaultInstance()

    MathEquationSubject.assertThat(equation).hasRightHandSideThat().isEqualTo(
      MathExpression.getDefaultInstance()
    )
  }

  @Test
  fun testConvertsToLatex_simpleEquation_producesCorrectString() {
    val equation = createEquation(
      leftSide = createConstantExpression(5),
      rightSide = createConstantExpression(10)
    )

    MathEquationSubject.assertThat(equation)
      .convertsToLatexStringThat()
      .isEqualTo("5 = 10")
  }

  @Test
  fun testConvertsToLatex_withDivision_retainsDivisionOperator() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.DIVIDE,
        createConstantExpression(10),
        createConstantExpression(2)
      ),
      rightSide = createConstantExpression(5)
    )

    MathEquationSubject.assertThat(equation)
      .convertsToLatexStringThat()
      .isEqualTo("10 \\div 2 = 5")
  }

  @Test
  fun testConvertsToLatexWithFractions_withDivision_producesFractionNotation() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.DIVIDE,
        createConstantExpression(10),
        createConstantExpression(2)
      ),
      rightSide = createConstantExpression(5)
    )

    MathEquationSubject.assertThat(equation)
      .convertsWithFractionsToLatexStringThat()
      .isEqualTo("\\frac{10}{2} = 5")
  }

  @Test
  fun testConvertsToLatex_complexExpression_producesCorrectString() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.ADD,
        createConstantExpression(3),
        createBinaryOperation(
          MathBinaryOperation.Operator.MULTIPLY,
          createConstantExpression(4),
          createVariableExpression("x")
        )
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation)
      .convertsToLatexStringThat()
      .isEqualTo("3 + 4 \\times x = 0")
  }

  @Test
  fun testLeftHandSide_wrongExpression_failsWithAppropriateMessage() {
    val equation = createEquation(
      leftSide = createConstantExpression(5),
      rightSide = createConstantExpression(0)
    )

    val exception = assertThrows(AssertionError::class.java) {
      MathEquationSubject.assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(6)
        }
      }
    }
    assertThat(exception).hasMessageThat().contains("expected: 6")
  }

  @Test
  fun testRightHandSide_wrongExpression_failsWithAppropriateMessage() {
    val equation = createEquation(
      leftSide = createConstantExpression(0),
      rightSide = createConstantExpression(10)
    )

    val exception = assertThrows(AssertionError::class.java) {
      MathEquationSubject.assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
        constant {
          withValueThat().isIntegerThat().isEqualTo(11)
        }
      }
    }
    assertThat(exception).hasMessageThat().contains("expected: 11")
  }

  @Test
  fun testConvertsToLatex_withNestedOperations_producesCorrectString() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.ADD,
        createUnaryOperation(
          MathUnaryOperation.Operator.NEGATE,
          createConstantExpression(2)
        ),
        createBinaryOperation(
          MathBinaryOperation.Operator.MULTIPLY,
          createConstantExpression(3),
          createVariableExpression("x")
        )
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation)
      .convertsToLatexStringThat()
      .isEqualTo("-2 + 3 \\times x = 0")
  }

  @Test
  fun testConvertsToLatexWithFractions_nestedFractions_producesCorrectString() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.DIVIDE,
        createConstantExpression(1),
        createBinaryOperation(
          MathBinaryOperation.Operator.DIVIDE,
          createConstantExpression(2),
          createVariableExpression("x")
        )
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation)
      .convertsWithFractionsToLatexStringThat()
      .isEqualTo("\\frac{1}{\\frac{2}{x}} = 0")
  }

  @Test
  fun testConvertsToLatex_withUnaryOperationInFraction_producesCorrectString() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.DIVIDE,
        createUnaryOperation(
          MathUnaryOperation.Operator.NEGATE,
          createConstantExpression(1)
        ),
        createConstantExpression(2)
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation)
      .convertsWithFractionsToLatexStringThat()
      .isEqualTo("\\frac{-1}{2} = 0")
  }

  @Test
  fun testConvertsToLatex_withFunctionCallInComplexExpression_producesCorrectString() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.ADD,
        createConstantExpression(1),
        createFunctionCall(
          MathFunctionCall.FunctionType.SQUARE_ROOT,
          createBinaryOperation(
            MathBinaryOperation.Operator.ADD,
            createConstantExpression(4),
            createVariableExpression("x")
          )
        )
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation)
      .convertsToLatexStringThat()
      .isEqualTo("1 + \\sqrt{4 + x} = 0")
  }

  @Test
  fun testConvertsToLatex_withInvalidExpression_fails() {
    val equation = MathEquation.getDefaultInstance()

    val exception = assertThrows(AssertionError::class.java) {
      MathEquationSubject.assertThat(equation)
        .convertsToLatexStringThat()
        .isEqualTo("5 = 0")
    }
    assertThat(exception).hasMessageThat().contains(
      "expected: 5 = 0\n" +
        "but was :  ="
    )
  }

  @Test
  fun testConvertsWithFractionsToLatex_withInvalidExpression_fails() {
    val equation = MathEquation.getDefaultInstance()

    val exception = assertThrows(AssertionError::class.java) {
      MathEquationSubject.assertThat(equation)
        .convertsWithFractionsToLatexStringThat()
        .isEqualTo("\\frac{1}{2} = 0")
    }
    assertThat(exception).hasMessageThat().contains(
      "expected: \\frac{1}{2} = 0\n" +
        "but was :  ="
    )
  }

  @Test
  fun testHasLeftHandSide_withComplexNestedExpression_matchesExpression() {
    val equation = createEquation(
      leftSide = createBinaryOperation(
        MathBinaryOperation.Operator.ADD,
        createFunctionCall(
          MathFunctionCall.FunctionType.SQUARE_ROOT,
          createBinaryOperation(
            MathBinaryOperation.Operator.MULTIPLY,
            createConstantExpression(4),
            createVariableExpression("x")
          )
        ),
        createUnaryOperation(
          MathUnaryOperation.Operator.NEGATE,
          createConstantExpression(3)
        )
      ),
      rightSide = createConstantExpression(0)
    )

    MathEquationSubject.assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      addition {
        leftOperand {
          functionCallTo(MathFunctionCall.FunctionType.SQUARE_ROOT) {
            argument {
              multiplication {
                leftOperand {
                  constant {
                    withValueThat().isIntegerThat().isEqualTo(4)
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

  private fun createFunctionCall(
    functionType: MathFunctionCall.FunctionType,
    argument: MathExpression
  ): MathExpression {
    return MathExpression.newBuilder()
      .setFunctionCall(
        MathFunctionCall.newBuilder()
          .setFunctionType(functionType)
          .setArgument(argument)
      )
      .build()
  }

  private fun createUnaryOperation(
    operator: MathUnaryOperation.Operator,
    operand: MathExpression
  ): MathExpression {
    return MathExpression.newBuilder()
      .setUnaryOperation(
        MathUnaryOperation.newBuilder()
          .setOperator(operator)
          .setOperand(operand)
      )
      .build()
  }

  private fun createEquation(
    leftSide: MathExpression,
    rightSide: MathExpression
  ): MathEquation {
    return MathEquation.newBuilder()
      .setLeftSide(leftSide)
      .setRightSide(rightSide)
      .build()
  }

  private fun createConstantExpression(value: Int): MathExpression {
    return MathExpression.newBuilder()
      .setConstant(Real.newBuilder().setInteger(value))
      .build()
  }

  private fun createVariableExpression(name: String): MathExpression {
    return MathExpression.newBuilder()
      .setVariable(name)
      .build()
  }

  private fun createBinaryOperation(
    operator: MathBinaryOperation.Operator,
    left: MathExpression,
    right: MathExpression
  ): MathExpression {
    return MathExpression.newBuilder()
      .setBinaryOperation(
        MathBinaryOperation.newBuilder()
          .setOperator(operator)
          .setLeftOperand(left)
          .setRightOperand(right)
      )
      .build()
  }
}
