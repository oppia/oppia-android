package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.util.math.toRawLatex

class MathEquationSubject(
  metadata: FailureMetadata,
  val actual: MathEquation
) : LiteProtoSubject(metadata, actual) {
  fun hasLeftHandSideThat(): MathExpressionSubject = assertThat(actual.leftSide)

  fun hasRightHandSideThat(): MathExpressionSubject = assertThat(actual.rightSide)

  fun convertsToLatexStringThat(): StringSubject =
    assertThat(convertToLatex(divAsFraction = false))

  fun convertsWithFractionsToLatexStringThat(): StringSubject =
    assertThat(convertToLatex(divAsFraction = true))

  private fun convertToLatex(divAsFraction: Boolean): String = actual.toRawLatex(divAsFraction)

  companion object {
    fun assertThat(actual: MathEquation): MathEquationSubject =
      assertAbout(::MathEquationSubject).that(actual)
  }
}
