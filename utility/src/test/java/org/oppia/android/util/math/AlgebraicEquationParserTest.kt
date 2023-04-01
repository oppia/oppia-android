package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/**
 * Tests for [MathExpressionParser].
 *
 * Note that this test suite specifically focuses on verifying that the parser can correctly parse
 * algebraic equations (i.e. via [MathExpressionParser.parseAlgebraicEquation]. This suite is not as
 * thorough as may be expected because:
 * 1. It relies heavily on [AlgebraicExpressionParserTest] for verifying that algebraic expressions
 *   can be correctly parsed. This suite is mainly geared toward verifying that the parser
 *   essentially just parses expressions for each side of the equation.
 * 2. Error cases are tested in [MathExpressionParserTest] instead of here, including for equations.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AlgebraicEquationParserTest {
  @Test
  fun testParseAlgEq_simpleVariableAssignment_correctlyParsesBothSidesStructures() {
    val equation = parseAlgebraicEquation("x = 1")

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testParseAlgEq_slopeInterceptForm_additionalVars_correctlyParsesBothSidesStructures() {
    val equation =
      parseAlgebraicEquation(
        "y = mx + b", allowedVariables = listOf("x", "y", "b", "m")
      )

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      addition {
        leftOperand {
          multiplication(isImplicit = true) {
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
  }

  @Test
  fun testParseAlgEq_binomialAssignedToY_correctlyParsesBothSidesStructures() {
    val equation = parseAlgebraicEquation("y = (x+1)^2")

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      exponentiation {
        leftOperand {
          group {
            addition {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(1)
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
  }

  @Test
  fun testParseAlgEq_factoredPolynomialAssignedToY_correctlyParsesBothSidesStructures() {
    val equation = parseAlgebraicEquation("y = (x+1)(x-1)")

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("y")
      }
    }
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      multiplication(isImplicit = true) {
        leftOperand {
          group {
            addition {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
                constant {
                  withValueThat().isIntegerThat().isEqualTo(1)
                }
              }
            }
          }
        }
        rightOperand {
          group {
            subtraction {
              leftOperand {
                variable {
                  withNameThat().isEqualTo("x")
                }
              }
              rightOperand {
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

  @Test
  fun testParseAlgEq_generalLineEquation_onLeftSide_correctlyParsesBothSidesStructures() {
    val equation =
      parseAlgebraicEquation(
        "a*x^2 + b*x + c = 0", allowedVariables = listOf("x", "a", "b", "c")
      )

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
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
                        withValueThat().isIntegerThat().isEqualTo(2)
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
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      constant {
        withValueThat().isIntegerThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testParseAlgEq_nonPolynomialEquation_correctlyParsesBothSidesStructures() {
    val equation = parseAlgebraicEquation("x = 2^sqrt(3)")

    assertThat(equation).hasLeftHandSideThat().hasStructureThatMatches {
      variable {
        withNameThat().isEqualTo("x")
      }
    }
    assertThat(equation).hasRightHandSideThat().hasStructureThatMatches {
      exponentiation {
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
  }

  private companion object {
    private fun parseAlgebraicEquation(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      val result = MathExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables, ErrorCheckingMode.ALL_ERRORS
      )
      assertThat(result).isInstanceOf(MathParsingResult.Success::class.java)
      return (result as MathParsingResult.Success<MathEquation>).result
    }
  }
}
