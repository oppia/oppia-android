package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.PolynomialSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToPolynomialTest {
  // TODO: add high-level checks for the three types, but don't test in detail since there are
  //  separate suites. Also, document the separate suites' existence in this suites's KDoc.

  @Test
  fun testPolynomials() {
    // TODO: split up & move to separate test suites. Finish test cases (if anymore are needed).

    val poly1 = parseNumericExpressionSuccessfully("1").toPolynomial()
    assertThat(poly1).evaluatesToPlainTextThat().isEqualTo("1")
    assertThat(poly1).isConstantThat().isIntegerThat().isEqualTo(1)

    val poly13 = parseNumericExpressionSuccessfully("1-1").toPolynomial()
    assertThat(poly13).evaluatesToPlainTextThat().isEqualTo("0")
    assertThat(poly13).isConstantThat().isIntegerThat().isEqualTo(0)

    val poly2 = parseNumericExpressionSuccessfully("3 + 4 * 2 / (1 - 5) ^ 2").toPolynomial()
    assertThat(poly2).evaluatesToPlainTextThat().isEqualTo("7/2")
    assertThat(poly2).isConstantThat().isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(3)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }

    val poly3 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors("133+3.14*x/(11-15)^2").toPolynomial()
    assertThat(poly3).evaluatesToPlainTextThat().isEqualTo("0.19625x + 133")
    assertThat(poly3).hasTermCountThat().isEqualTo(2)
    assertThat(poly3).term(0).hasCoefficientThat().isIrrationalThat().isWithin(1e-5).of(0.19625)
    assertThat(poly3).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly3).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly3).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly3).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(133)
    assertThat(poly3).term(1).hasVariableCountThat().isEqualTo(0)

    val poly4 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2").toPolynomial()
    assertThat(poly4).evaluatesToPlainTextThat().isEqualTo("x^2")
    assertThat(poly4).hasTermCountThat().isEqualTo(1)
    assertThat(poly4).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly4).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly4).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly4).term(0).variable(0).hasPowerThat().isEqualTo(2)

    val poly5 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy+x").toPolynomial()
    assertThat(poly5).evaluatesToPlainTextThat().isEqualTo("xy + x")
    assertThat(poly5).hasTermCountThat().isEqualTo(2)
    assertThat(poly5).term(0).hasVariableCountThat().isEqualTo(2)
    assertThat(poly5).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly5).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly5).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly5).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly5).term(0).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly5).term(1).hasVariableCountThat().isEqualTo(1)
    assertThat(poly5).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly5).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly5).term(1).variable(0).hasPowerThat().isEqualTo(1)

    val poly6 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2x").toPolynomial()
    assertThat(poly6).evaluatesToPlainTextThat().isEqualTo("2x")
    assertThat(poly6).hasTermCountThat().isEqualTo(1)
    assertThat(poly6).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly6).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(2)
    assertThat(poly6).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly6).term(0).variable(0).hasPowerThat().isEqualTo(1)

    val poly30 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x+2").toPolynomial()
    assertThat(poly30).evaluatesToPlainTextThat().isEqualTo("x + 2")
    assertThat(poly30).hasTermCountThat().isEqualTo(2)
    assertThat(poly30).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly30).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(2)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly29 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2-3*x-10").toPolynomial()
    assertThat(poly29).evaluatesToPlainTextThat().isEqualTo("x^2 - 3x - 10")
    assertThat(poly29).hasTermCountThat().isEqualTo(3)
    assertThat(poly29).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly29).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-3)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly29).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-10)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly31 = parseAlgebraicExpressionSuccessfullyWithAllErrors("4*(x+2)").toPolynomial()
    assertThat(poly31).evaluatesToPlainTextThat().isEqualTo("4x + 8")
    assertThat(poly31).hasTermCountThat().isEqualTo(2)
    assertThat(poly31).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(4)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly31).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(8)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly7 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2xy^2z^3").toPolynomial()
    assertThat(poly7).evaluatesToPlainTextThat().isEqualTo("2xy^2z^3")
    assertThat(poly7).hasTermCountThat().isEqualTo(1)
    assertThat(poly7).term(0).hasVariableCountThat().isEqualTo(3)
    assertThat(poly7).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(2)
    assertThat(poly7).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly7).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly7).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly7).term(0).variable(1).hasPowerThat().isEqualTo(2)
    assertThat(poly7).term(0).variable(2).hasNameThat().isEqualTo("z")
    assertThat(poly7).term(0).variable(2).hasPowerThat().isEqualTo(3)

    // Show that 7+xy+yz-3-xz-yz+3xy-4 combines into 4xy-xz (the eliminated terms should be gone).
    val poly8 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy+yz-xz-yz+3xy").toPolynomial()
    assertThat(poly8).evaluatesToPlainTextThat().isEqualTo("4xy - xz")
    assertThat(poly8).hasTermCountThat().isEqualTo(2)
    assertThat(poly8).term(0).hasVariableCountThat().isEqualTo(2)
    assertThat(poly8).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(4)
    assertThat(poly8).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly8).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly8).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly8).term(0).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly8).term(1).hasVariableCountThat().isEqualTo(2)
    assertThat(poly8).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(-1)
    assertThat(poly8).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly8).term(1).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly8).term(1).variable(1).hasNameThat().isEqualTo("z")
    assertThat(poly8).term(1).variable(1).hasPowerThat().isEqualTo(1)

    // x+2x should become 3x since like terms are combined.
    val poly9 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x+2x").toPolynomial()
    assertThat(poly9).evaluatesToPlainTextThat().isEqualTo("3x")
    assertThat(poly9).hasTermCountThat().isEqualTo(1)
    assertThat(poly9).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly9).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(3)
    assertThat(poly9).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly9).term(0).variable(0).hasPowerThat().isEqualTo(1)

    // xx^2 should become x^3 since like terms are combined.
    val poly10 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xx^2").toPolynomial()
    assertThat(poly10).evaluatesToPlainTextThat().isEqualTo("x^3")
    assertThat(poly10).hasTermCountThat().isEqualTo(1)
    assertThat(poly10).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly10).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly10).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly10).term(0).variable(0).hasPowerThat().isEqualTo(3)

    // No terms in this polynomial should be combined.
    val poly11 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2+x+1").toPolynomial()
    assertThat(poly11).evaluatesToPlainTextThat().isEqualTo("x^2 + x + 1")
    assertThat(poly11).hasTermCountThat().isEqualTo(3)
    assertThat(poly11).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly11).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly11).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly11).term(0).variable(0).hasPowerThat().isEqualTo(2)
    assertThat(poly11).term(1).hasVariableCountThat().isEqualTo(1)
    assertThat(poly11).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly11).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly11).term(1).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly11).term(2).hasVariableCountThat().isEqualTo(0)
    assertThat(poly11).term(2).hasCoefficientThat().isIntegerThat().isEqualTo(1)

    // No terms in this polynomial should be combined.
    val poly12 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2 + x^2y").toPolynomial()
    assertThat(poly12).evaluatesToPlainTextThat().isEqualTo("x^2y + x^2")
    assertThat(poly12).hasTermCountThat().isEqualTo(2)
    assertThat(poly12).term(0).hasVariableCountThat().isEqualTo(2)
    assertThat(poly12).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly12).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly12).term(0).variable(0).hasPowerThat().isEqualTo(2)
    assertThat(poly12).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly12).term(0).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly12).term(1).hasVariableCountThat().isEqualTo(1)
    assertThat(poly12).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly12).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly12).term(1).variable(0).hasPowerThat().isEqualTo(2)

    // Ordering tests. Verify that ordering matches
    // https://en.wikipedia.org/wiki/Polynomial#Definition (where multiple variables are sorted
    // lexicographically).

    // The order of the terms in this polynomial should be reversed.
    val poly14 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x+x^2+x^3").toPolynomial()
    assertThat(poly14).evaluatesToPlainTextThat().isEqualTo("x^3 + x^2 + x + 1")
    assertThat(poly14).hasTermCountThat().isEqualTo(4)
    assertThat(poly14).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly14).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly14).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly14).term(0).variable(0).hasPowerThat().isEqualTo(3)
    assertThat(poly14).term(1).hasVariableCountThat().isEqualTo(1)
    assertThat(poly14).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly14).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly14).term(1).variable(0).hasPowerThat().isEqualTo(2)
    assertThat(poly14).term(2).hasVariableCountThat().isEqualTo(1)
    assertThat(poly14).term(2).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly14).term(2).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly14).term(2).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly14).term(3).hasVariableCountThat().isEqualTo(0)
    assertThat(poly14).term(3).hasCoefficientThat().isIntegerThat().isEqualTo(1)

    // The order of the terms in this polynomial should be preserved.
    val poly15 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^3+x^2+x+1").toPolynomial()
    assertThat(poly15).evaluatesToPlainTextThat().isEqualTo("x^3 + x^2 + x + 1")
    assertThat(poly15).hasTermCountThat().isEqualTo(4)
    assertThat(poly15).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(poly15).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly15).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly15).term(0).variable(0).hasPowerThat().isEqualTo(3)
    assertThat(poly15).term(1).hasVariableCountThat().isEqualTo(1)
    assertThat(poly15).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly15).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly15).term(1).variable(0).hasPowerThat().isEqualTo(2)
    assertThat(poly15).term(2).hasVariableCountThat().isEqualTo(1)
    assertThat(poly15).term(2).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly15).term(2).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly15).term(2).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly15).term(3).hasVariableCountThat().isEqualTo(0)
    assertThat(poly15).term(3).hasCoefficientThat().isIntegerThat().isEqualTo(1)

    // The order of the terms in this polynomial should be reversed.
    val poly16 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy+xz+yz").toPolynomial()
    assertThat(poly16).evaluatesToPlainTextThat().isEqualTo("xy + xz + yz")
    assertThat(poly16).hasTermCountThat().isEqualTo(3)
    assertThat(poly16).term(0).hasVariableCountThat().isEqualTo(2)
    assertThat(poly16).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly16).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly16).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly16).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly16).term(0).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly16).term(1).hasVariableCountThat().isEqualTo(2)
    assertThat(poly16).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly16).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly16).term(1).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly16).term(1).variable(1).hasNameThat().isEqualTo("z")
    assertThat(poly16).term(1).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly16).term(2).hasVariableCountThat().isEqualTo(2)
    assertThat(poly16).term(2).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly16).term(2).variable(0).hasNameThat().isEqualTo("y")
    assertThat(poly16).term(2).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly16).term(2).variable(1).hasNameThat().isEqualTo("z")
    assertThat(poly16).term(2).variable(1).hasPowerThat().isEqualTo(1)

    // The order of the terms in this polynomial should be preserved.
    val poly17 = parseAlgebraicExpressionSuccessfullyWithAllErrors("yz+xz+xy").toPolynomial()
    assertThat(poly17).evaluatesToPlainTextThat().isEqualTo("xy + xz + yz")
    assertThat(poly17).hasTermCountThat().isEqualTo(3)
    assertThat(poly17).term(0).hasVariableCountThat().isEqualTo(2)
    assertThat(poly17).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly17).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly17).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly17).term(0).variable(1).hasNameThat().isEqualTo("y")
    assertThat(poly17).term(0).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly17).term(1).hasVariableCountThat().isEqualTo(2)
    assertThat(poly17).term(1).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly17).term(1).variable(0).hasNameThat().isEqualTo("x")
    assertThat(poly17).term(1).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly17).term(1).variable(1).hasNameThat().isEqualTo("z")
    assertThat(poly17).term(1).variable(1).hasPowerThat().isEqualTo(1)
    assertThat(poly17).term(2).hasVariableCountThat().isEqualTo(2)
    assertThat(poly17).term(2).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(poly17).term(2).variable(0).hasNameThat().isEqualTo("y")
    assertThat(poly17).term(2).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(poly17).term(2).variable(1).hasNameThat().isEqualTo("z")
    assertThat(poly17).term(2).variable(1).hasPowerThat().isEqualTo(1)

    val poly18 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors("3+x+y+xy+x^2y+xy^2+x^2y^2").toPolynomial()
    assertThat(poly18).evaluatesToPlainTextThat().isEqualTo("x^2y^2 + x^2y + xy^2 + xy + x + y + 3")
    assertThat(poly18).hasTermCountThat().isEqualTo(7)
    assertThat(poly18).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly18).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly18).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly18).term(3).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly18).term(4).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly18).term(5).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly18).term(6).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(3)
      hasVariableCountThat().isEqualTo(0)
    }

    // Ensure variables of coefficient and power of 0 are removed.
    val poly22 = parseAlgebraicExpressionSuccessfullyWithAllErrors("0x").toPolynomial()
    assertThat(poly22).evaluatesToPlainTextThat().isEqualTo("0")
    assertThat(poly22).hasTermCountThat().isEqualTo(1)
    assertThat(poly22).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(0)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly23 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x-x").toPolynomial()
    assertThat(poly23).evaluatesToPlainTextThat().isEqualTo("0")
    assertThat(poly23).hasTermCountThat().isEqualTo(1)
    assertThat(poly23).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(0)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly24 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^0").toPolynomial()
    assertThat(poly24).evaluatesToPlainTextThat().isEqualTo("1")
    assertThat(poly24).hasTermCountThat().isEqualTo(1)
    assertThat(poly24).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly25 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x/x").toPolynomial()
    assertThat(poly25).evaluatesToPlainTextThat().isEqualTo("1")
    assertThat(poly25).hasTermCountThat().isEqualTo(1)
    assertThat(poly25).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly26 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^(2-2)").toPolynomial()
    assertThat(poly26).evaluatesToPlainTextThat().isEqualTo("1")
    assertThat(poly26).hasTermCountThat().isEqualTo(1)
    assertThat(poly26).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly28 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x+1)/2").toPolynomial()
    assertThat(poly28).evaluatesToPlainTextThat().isEqualTo("(1/2)x + 1/2")
    assertThat(poly28).hasTermCountThat().isEqualTo(2)
    assertThat(poly28).term(0).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly28).term(1).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasVariableCountThat().isEqualTo(0)
    }

    // Ensure like terms are combined after polynomial multiplication.
    val poly20 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x-5)(x+2)").toPolynomial()
    assertThat(poly20).evaluatesToPlainTextThat().isEqualTo("x^2 - 3x - 10")
    assertThat(poly20).hasTermCountThat().isEqualTo(3)
    assertThat(poly20).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly20).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-3)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly20).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-10)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly21 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(1+x)^3").toPolynomial()
    assertThat(poly21).evaluatesToPlainTextThat().isEqualTo("x^3 + 3x^2 + 3x + 1")
    assertThat(poly21).hasTermCountThat().isEqualTo(4)
    assertThat(poly21).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(3)
      }
    }
    assertThat(poly21).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(3)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly21).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(3)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly21).term(3).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly27 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2*y^2 + 2").toPolynomial()
    assertThat(poly27).evaluatesToPlainTextThat().isEqualTo("x^2y^2 + 2")
    assertThat(poly27).hasTermCountThat().isEqualTo(2)
    assertThat(poly27).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly27).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(2)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly32 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors("(x^2-3*x-10)*(x+2)").toPolynomial()
    assertThat(poly32).evaluatesToPlainTextThat().isEqualTo("x^3 - x^2 - 16x - 20")
    assertThat(poly32).hasTermCountThat().isEqualTo(4)
    assertThat(poly32).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(3)
      }
    }
    assertThat(poly32).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly32).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-16)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly32).term(3).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-20)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly33 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x-y)^3").toPolynomial()
    assertThat(poly33).evaluatesToPlainTextThat().isEqualTo("x^3 - 3x^2y + 3xy^2 - y^3")
    assertThat(poly33).hasTermCountThat().isEqualTo(4)
    assertThat(poly33).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(3)
      }
    }
    assertThat(poly33).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-3)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly33).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(3)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly33).term(3).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(3)
      }
    }

    // Ensure polynomial division works.
    val poly19 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors("(x^2-3*x-10)/(x+2)").toPolynomial()
    assertThat(poly19).evaluatesToPlainTextThat().isEqualTo("x - 5")
    assertThat(poly19).hasTermCountThat().isEqualTo(2)
    assertThat(poly19).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly19).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-5)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly35 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(xy-5y)/y").toPolynomial()
    assertThat(poly35).evaluatesToPlainTextThat().isEqualTo("x - 5")
    assertThat(poly35).hasTermCountThat().isEqualTo(2)
    assertThat(poly35).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly35).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-5)
      hasVariableCountThat().isEqualTo(0)
    }

    val poly36 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors("(x^2-2xy+y^2)/(x-y)").toPolynomial()
    assertThat(poly36).evaluatesToPlainTextThat().isEqualTo("x - y")
    assertThat(poly36).hasTermCountThat().isEqualTo(2)
    assertThat(poly36).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly36).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }

    // Example from https://www.kristakingmath.com/blog/predator-prey-systems-ghtcp-5e2r4-427ab.
    val poly37 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x^3-y^3)/(x-y)").toPolynomial()
    assertThat(poly37).evaluatesToPlainTextThat().isEqualTo("x^2 + xy + y^2")
    assertThat(poly37).hasTermCountThat().isEqualTo(3)
    assertThat(poly37).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly37).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly37).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }

    // Multi-variable & more complex division.
    val poly34 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "(x^3-3x^2y+3xy^2-y^3)/(x-y)^2"
      ).toPolynomial()
    assertThat(poly34).evaluatesToPlainTextThat().isEqualTo("x - y")
    assertThat(poly34).hasTermCountThat().isEqualTo(2)
    assertThat(poly34).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly34).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }

    val poly38 = parseNumericExpressionSuccessfully("2^-4").toPolynomial()
    assertThat(poly38).evaluatesToPlainTextThat().isEqualTo("1/16")
    assertThat(poly38).hasTermCountThat().isEqualTo(1)
    assertThat(poly38).term(0).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(16)
      }
      hasVariableCountThat().isEqualTo(0)
    }

    val poly39 = parseNumericExpressionSuccessfully("2^(3-6)").toPolynomial()
    assertThat(poly39).evaluatesToPlainTextThat().isEqualTo("1/8")
    assertThat(poly39).hasTermCountThat().isEqualTo(1)
    assertThat(poly39).term(0).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(8)
      }
      hasVariableCountThat().isEqualTo(0)
    }

    // x^-3 is not a valid polynomial (since polynomials can't have negative powers).
    val poly40 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^(3-6)").toPolynomial()
    assertThat(poly40).isNotValidPolynomial()

    // 2^x is not a polynomial.
    val poly41 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("2^x").toPolynomial()
    assertThat(poly41).isNotValidPolynomial()

    // 1/x is not a polynomial.
    val poly42 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("1/x").toPolynomial()
    assertThat(poly42).isNotValidPolynomial()

    val poly43 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x/2").toPolynomial()
    assertThat(poly43).evaluatesToPlainTextThat().isEqualTo("(1/2)x")
    assertThat(poly43).hasTermCountThat().isEqualTo(1)
    assertThat(poly43).term(0).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }

    val poly44 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x-3)/2").toPolynomial()
    assertThat(poly44).evaluatesToPlainTextThat().isEqualTo("(1/2)x - 3/2")
    assertThat(poly44).hasTermCountThat().isEqualTo(2)
    assertThat(poly44).term(0).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isFalse()
        hasWholeNumberThat().isEqualTo(0)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(poly44).term(1).apply {
      hasCoefficientThat().isRationalThat().apply {
        hasNegativePropertyThat().isTrue()
        hasWholeNumberThat().isEqualTo(1)
        hasNumeratorThat().isEqualTo(1)
        hasDenominatorThat().isEqualTo(2)
      }
      hasVariableCountThat().isEqualTo(0)
    }

    val poly45 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x-1)(x+1)").toPolynomial()
    assertThat(poly45).evaluatesToPlainTextThat().isEqualTo("x^2 - 1")
    assertThat(poly45).hasTermCountThat().isEqualTo(2)
    assertThat(poly45).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(poly45).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(-1)
      hasVariableCountThat().isEqualTo(0)
    }

    // √x is not a polynomial.
    val poly46 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(x)").toPolynomial()
    assertThat(poly46).isNotValidPolynomial()

    val poly47 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√(x^2)").toPolynomial()
    assertThat(poly47).evaluatesToPlainTextThat().isEqualTo("x")
    assertThat(poly47).hasTermCountThat().isEqualTo(1)
    assertThat(poly47).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
    }

    val poly51 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√(x^2y^2)").toPolynomial()
    assertThat(poly51).evaluatesToPlainTextThat().isEqualTo("xy")
    assertThat(poly51).hasTermCountThat().isEqualTo(1)
    assertThat(poly51).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }

    // A limitation in the current polynomial conversion is that sqrt(x) will fail due to it not
    // have any polynomial representation.
    val poly48 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√x^2").toPolynomial()
    assertThat(poly48).isNotValidPolynomial()

    // √(x^2+2) may evaluate to a polynomial, but it requires factoring (which isn't yet supported).
    val poly50 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√(x^2+2)").toPolynomial()
    assertThat(poly50).isNotValidPolynomial()

    // Division by zero is undefined, so a polynomial can't be constructed.
    val poly49 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("(x+2)/0").toPolynomial()
    assertThat(poly49).isNotValidPolynomial()

    val poly52 = parsePolynomialFromNumericExpression("1")
    val poly53 = parsePolynomialFromNumericExpression("0")
    assertThat(poly52).isNotEqualTo(poly53)

    val poly54 = parsePolynomialFromNumericExpression("1+2")
    val poly55 = parsePolynomialFromNumericExpression("3")
    assertThat(poly54).isEqualTo(poly55)

    val poly56 = parsePolynomialFromNumericExpression("1-2")
    val poly57 = parsePolynomialFromNumericExpression("-1")
    assertThat(poly56).isEqualTo(poly57)

    val poly58 = parsePolynomialFromNumericExpression("2*3")
    val poly59 = parsePolynomialFromNumericExpression("6")
    assertThat(poly58).isEqualTo(poly59)

    val poly60 = parsePolynomialFromNumericExpression("2^3")
    val poly61 = parsePolynomialFromNumericExpression("8")
    assertThat(poly60).isEqualTo(poly61)

    val poly62 = parsePolynomialFromAlgebraicExpression("1+x")
    val poly63 = parsePolynomialFromAlgebraicExpression("x+1")
    assertThat(poly62).isEqualTo(poly63)

    val poly64 = parsePolynomialFromAlgebraicExpression("y+x")
    val poly65 = parsePolynomialFromAlgebraicExpression("x+y")
    assertThat(poly64).isEqualTo(poly65)

    val poly66 = parsePolynomialFromAlgebraicExpression("(x+1)^2")
    val poly67 = parsePolynomialFromAlgebraicExpression("x^2+2x+1")
    assertThat(poly66).isEqualTo(poly67)

    val poly68 = parsePolynomialFromAlgebraicExpression("(x+1)/2")
    val poly69 = parsePolynomialFromAlgebraicExpression("x/2+(1/2)")
    assertThat(poly68).isEqualTo(poly69)

    val poly70 = parsePolynomialFromAlgebraicExpression("x*2")
    val poly71 = parsePolynomialFromAlgebraicExpression("2x")
    assertThat(poly70).isEqualTo(poly71)

    val poly72 = parsePolynomialFromAlgebraicExpression("x(x+1)")
    val poly73 = parsePolynomialFromAlgebraicExpression("x^2+x")
    assertThat(poly72).isEqualTo(poly73)
  }

  private fun parsePolynomialFromNumericExpression(expression: String) =
    parseNumericExpressionSuccessfully(expression).toPolynomial()

  private fun parsePolynomialFromAlgebraicExpression(expression: String) =
    parseAlgebraicExpressionSuccessfullyWithAllErrors(expression).toPolynomial()

  private companion object {
    // TODO: fix helper API.

    private fun parseNumericExpressionSuccessfully(expression: String): MathExpression {
      val result = parseNumericExpressionWithAllErrors(expression)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionWithAllErrors(
      expression: String
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, ErrorCheckingMode.ALL_ERRORS)
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
