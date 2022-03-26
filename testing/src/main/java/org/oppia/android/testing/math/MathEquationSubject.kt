package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat

// TODO(#4097): Add tests for this class.

/**
 * Truth subject for verifying properties of [MathEquation]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
 * [MathEquation] proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class MathEquationSubject private constructor(
  metadata: FailureMetadata,
  private val actual: MathEquation
) : LiteProtoSubject(metadata, actual) {
  /**
   * Returns a [MathExpressionSubject] to test [MathEquation.getLeftSide]. This method never fails
   * since the underlying property defaults to a default proto if it's not defined in the equation.
   */
  fun hasLeftHandSideThat(): MathExpressionSubject = assertThat(actual.leftSide)

  /**
   * Returns a [MathExpressionSubject] to test [MathEquation.getRightSide]. This method never fails
   * since the underlying property defaults to a default proto if it's not defined in the equation.
   */
  fun hasRightHandSideThat(): MathExpressionSubject = assertThat(actual.rightSide)

  companion object {
    /**
     * Returns a new [MathEquationSubject] to verify aspects of the specified [MathEquation] value.
     */
    fun assertThat(actual: MathEquation): MathEquationSubject =
      assertAbout(::MathEquationSubject).that(actual)
  }
}
