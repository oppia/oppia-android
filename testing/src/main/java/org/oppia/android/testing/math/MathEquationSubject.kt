package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat

class MathEquationSubject(
  metadata: FailureMetadata,
  // TODO: restrict visibility.
  val actual: MathEquation
) : LiteProtoSubject(metadata, actual) {
  fun hasLeftHandSideThat(): MathExpressionSubject = assertThat(actual.leftSide)

  fun hasRightHandSideThat(): MathExpressionSubject = assertThat(actual.rightSide)

  companion object {
    fun assertThat(actual: MathEquation): MathEquationSubject =
      assertAbout(::MathEquationSubject).that(actual)
  }
}
