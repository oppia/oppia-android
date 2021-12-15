package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.getConstant
import org.oppia.android.util.math.isConstant
import org.oppia.android.util.math.toPlainText

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

  fun isNotValidPolynomial() {
    // TODO: use toPlainText here.
    assertWithMessage(
      "Expected polynomial to be undefined, but was: ${actual?.toPlainText()}"
    ).that(actual).isNull()
  }

  fun isConstantThat(): RealSubject {
    // TODO: use toPlainText here.
    assertWithMessage("Expected polynomial to be constant, but was: $nonNullActual")
      .that(nonNullActual.isConstant())
      .isTrue()
    return assertThat(nonNullActual.getConstant())
  }

  fun hasTermCountThat(): IntegerSubject = assertThat(nonNullActual.termCount)

  fun term(index: Int): PolynomialTermSubject = assertThat(nonNullActual.termList[index])

  fun evaluatesToPlainTextThat(): StringSubject = assertThat(nonNullActual.toPlainText())

  companion object {
    fun assertThat(actual: Polynomial?): PolynomialSubject =
      assertAbout(::PolynomialSubject).that(actual)

    private fun assertThat(actual: Polynomial.Term): PolynomialTermSubject =
      assertAbout(::PolynomialTermSubject).that(actual)

    private fun assertThat(actual: Polynomial.Term.Variable): PolynomialTermVariableSubject =
      assertAbout(::PolynomialTermVariableSubject).that(actual)
  }

  class PolynomialTermSubject(
    metadata: FailureMetadata,
    private val actual: Polynomial.Term
  ) : LiteProtoSubject(metadata, actual) {
    fun hasCoefficientThat(): RealSubject = assertThat(actual.coefficient)

    fun hasVariableCountThat(): IntegerSubject = assertThat(actual.variableCount)

    fun variable(index: Int): PolynomialTermVariableSubject =
      assertThat(actual.variableList[index])
  }

  class PolynomialTermVariableSubject(
    metadata: FailureMetadata,
    private val actual: Polynomial.Term.Variable
  ) : LiteProtoSubject(metadata, actual) {
    fun hasNameThat(): StringSubject = assertThat(actual.name)

    fun hasPowerThat(): IntegerSubject = assertThat(actual.power)
  }
}
