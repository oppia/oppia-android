package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Real

/** Tests for [PolynomialSubject]. */
@RunWith(JUnit4::class)
class PolynomialSubjectTest {

  /** Helper function to create a polynomial term. */
  fun createTerm(coefficient: Int, vararg variables: Pair<String, Int>): Polynomial.Term {
    return Polynomial.Term.newBuilder()
      .setCoefficient(Real.newBuilder().setInteger(coefficient))
      .apply {
        variables.forEach { (name, power) ->
          addVariable(Polynomial.Term.Variable.newBuilder().setName(name).setPower(power))
        }
      }
      .build()
  }

  /** Helper function to create a polynomial from multiple terms. */
  fun createPolynomial(vararg terms: Polynomial.Term): Polynomial {
    return Polynomial.newBuilder().apply {
      terms.forEach { addTerm(it) }
    }.build()
  }

  @Test
  fun testPolynomialSubject_withNullPolynomial_isNotValidPolynomial() {
    PolynomialSubject.assertThat(null).isNotValidPolynomial()
  }

  @Test
  fun testPolynomialSubject_withNonNullPolynomial_isNotValidPolynomial_fails() {
    val polynomial = createPolynomial(
      createTerm(5, "x" to 1)
    )
    assertThrows(AssertionError::class.java) {
      PolynomialSubject.assertThat(polynomial).isNotValidPolynomial()
    }
  }

  @Test
  fun testPolynomialSubject_withConstantPolynomial_isConstantThat() {
    val constantPolynomial = createPolynomial(
      createTerm(5)
    )
    PolynomialSubject.assertThat(constantPolynomial)
      .isConstantThat()
      .isIntegerThat()
      .isEqualTo(5)
  }

  @Test
  fun testPolynomialSubject_testIsConstantThat_withNonConstantPolynomial_fails() {
    val nonConstantPolynomial = createPolynomial(
      createTerm(5, "x" to 1)
    )

    assertThrows(AssertionError::class.java) {
      PolynomialSubject.assertThat(nonConstantPolynomial).isConstantThat()
    }
  }

  @Test
  fun testPolynomialSubject_withZeroTerms_hasTermCountThatIsEqualToZero() {
    val emptyPolynomial = Polynomial.newBuilder().build()
    PolynomialSubject.assertThat(emptyPolynomial)
      .hasTermCountThat()
      .isEqualTo(0)
  }

  @Test
  fun testPolynomialSubject_withTwoTerms_hasTermCountThatIsEqualToTwo() {
    val multiTermPolynomial = createPolynomial(
      createTerm(1),
      createTerm(2)
    )

    PolynomialSubject.assertThat(multiTermPolynomial)
      .hasTermCountThat()
      .isEqualTo(2)
  }

  @Test
  fun testPolynomialSubject_withValidIndex_termHasCoefficientAndvariable() {
    val polynomial = createPolynomial(
      createTerm(5, "x" to 2),
      createTerm(3, "y" to 1)
    )

    PolynomialSubject.assertThat(polynomial)
      .term(0)
      .hasCoefficientThat()
      .isIntegerThat()
      .isEqualTo(5)

    PolynomialSubject.assertThat(polynomial)
      .term(0)
      .variable(0)
      .hasNameThat()
      .isEqualTo("x")

    PolynomialSubject.assertThat(polynomial)
      .term(1)
      .hasCoefficientThat()
      .isIntegerThat()
      .isEqualTo(3)
  }

  @Test
  fun testPolynomialSubject_failsWithInvalidIndex() {
    val polynomial = Polynomial.newBuilder().build()
    assertThrows(IndexOutOfBoundsException::class.java) {
      PolynomialSubject.assertThat(polynomial).term(0)
    }
  }

  @Test
  fun testPolynomialSubject_withConstantPolynomial_evaluatesToPlainText() {
    val constantPolynomial = createPolynomial(
      createTerm(5)
    )
    PolynomialSubject.assertThat(constantPolynomial)
      .evaluatesToPlainTextThat()
      .isEqualTo("5")
  }

  @Test
  fun testPolynomialSubject_withComplexPolynomial_evaluatesToPlainText() {
    val polynomial = createPolynomial(
      createTerm(2, "x" to 2),
      createTerm(3, "x" to 1),
      createTerm(1)
    )

    PolynomialSubject.assertThat(polynomial)
      .evaluatesToPlainTextThat()
      .isEqualTo("2x^2 + 3x + 1")
  }

  @Test
  fun testPolynomialSubject_withTwoTerms_hasVariableCountThatIsEqualToTwo() {
    val polynomial = createPolynomial(
      createTerm(5, "x" to 2, "y" to 1)
    )

    PolynomialSubject.assertThat(polynomial)
      .term(0)
      .hasVariableCountThat()
      .isEqualTo(2)
  }

  @Test
  fun testPolynomialSubject_polynomialTermVariable_hasExpectedDetails() {
    val polynomial = createPolynomial(
      createTerm(1, "x" to 3)
    )

    PolynomialSubject.assertThat(polynomial)
      .term(0)
      .variable(0)
      .apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(3)
      }
  }
}
