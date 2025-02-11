package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.testing.math.PolynomialSubject.Companion.assertThat
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.getConstant
import org.oppia.android.util.math.isConstant
import org.oppia.android.util.math.toPlainText

/**
 * Truth subject for verifying properties of [Polynomial]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying [Polynomial]
 * proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class PolynomialSubject(
  metadata: FailureMetadata,
  private val actual: Polynomial?
) : LiteProtoSubject(metadata, actual) {
  private val nonNullActual by lazy {
    checkNotNull(actual) {
      "Expected polynomial to be defined, not null (is the expression/equation not a valid" +
        " polynomial?)"
    }
  }

  /** Verifies that the represented [Polynomial] is null (i.e. not a valid polynomial). */
  fun isNotValidPolynomial() {
    assertWithMessage(
      "Expected polynomial to be undefined, but was: ${actual?.toPlainText()}"
    ).that(actual).isNull()
  }

  /**
   * Verifies that the represented [Polynomial] is a constant (i.e. [Polynomial.isConstant] and
   * returns a [RealSubject] to verify the value of the constant polynomial.
   */
  fun isConstantThat(): RealSubject {
    assertWithMessage("Expected polynomial to be constant, but was: ${nonNullActual.toPlainText()}")
      .that(nonNullActual.isConstant())
      .isTrue()
    return assertThat(nonNullActual.getConstant())
  }

  /**
   * Returns an [IntegerSubject] to test [Polynomial.getTermCount].
   *
   * This method never fails since the underlying property defaults to 0 if there are no terms
   * defined in the polynomial (unless the polynomial is null).
   */
  fun hasTermCountThat(): IntegerSubject = assertThat(nonNullActual.termCount)

  /**
   * Returns a [PolynomialTermSubject] to test [Polynomial.getTerm] for the specified index.
   *
   * This method throws if the index doesn't correspond to a valid term. Callers should first verify
   * the term count using [hasTermCountThat].
   */
  fun term(index: Int): PolynomialTermSubject = assertThat(nonNullActual.termList[index])

  /**
   * Returns a [StringSubject] to test the plain-text representation of the [Polynomial] (i.e. via
   * [Polynomial.toPlainText]).
   */
  fun evaluatesToPlainTextThat(): StringSubject = assertThat(nonNullActual.toPlainText())

  companion object {
    /** Returns a new [PolynomialSubject] to verify aspects of the specified [Polynomial] value. */
    fun assertThat(actual: Polynomial?): PolynomialSubject =
      assertAbout(::PolynomialSubject).that(actual)

    private fun assertThat(actual: Polynomial.Term): PolynomialTermSubject =
      assertAbout(::PolynomialTermSubject).that(actual)

    private fun assertThat(actual: Polynomial.Term.Variable): PolynomialTermVariableSubject =
      assertAbout(::PolynomialTermVariableSubject).that(actual)
  }

  /**
   * Truth subject for verifying properties of [Polynomial.Term]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [Polynomial.Term] proto can be verified through inherited methods.
   */
  class PolynomialTermSubject(
    metadata: FailureMetadata,
    private val actual: Polynomial.Term
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [RealSubject] to test [Polynomial.Term.getCoefficient] for the represented term.
     *
     * This method never fails since the underlying property defaults to a default instance if it's
     * not defined in the term.
     */
    fun hasCoefficientThat(): RealSubject = assertThat(actual.coefficient)

    /**
     * Returns an [IntegerSubject] to test [Polynomial.Term.getVariableCount] for the represented
     * term.
     *
     * This method never fails since the underlying property defaults to 0 if there are no variables
     * in the represented term.
     */
    fun hasVariableCountThat(): IntegerSubject = assertThat(actual.variableCount)

    /**
     * Returns a [PolynomialTermVariableSubject] to test [Polynomial.Term.getVariable] for the
     * specified index.
     *
     * This method throws if the index doesn't correspond to a valid variable. Callers should first
     * verify the variable count using [hasVariableCountThat].
     */
    fun variable(index: Int): PolynomialTermVariableSubject =
      assertThat(actual.variableList[index])
  }

  /**
   * Truth subject for verifying properties of [Polynomial.Term.Variable]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [Polynomial.Term.Variable] proto can be verified through inherited methods.
   */
  class PolynomialTermVariableSubject(
    metadata: FailureMetadata,
    private val actual: Polynomial.Term.Variable
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [Polynomial.Term.Variable.getName] for the represented
     * variable.
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the variable.
     */
    fun hasNameThat(): StringSubject = assertThat(actual.name)

    /**
     * Returns an [IntegerSubject] to test [Polynomial.Term.Variable.getPower] for the represented
     * variable.
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the variable.
     */
    fun hasPowerThat(): IntegerSubject = assertThat(actual.power)
  }
}
