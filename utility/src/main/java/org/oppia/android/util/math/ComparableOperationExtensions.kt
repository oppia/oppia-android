package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.COMMUTATIVE_ACCUMULATION
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.COMPARISONTYPE_NOT_SET
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.CONSTANT_TERM
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.NON_COMMUTATIVE_OPERATION
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.VARIABLE_TERM
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation.OperationTypeCase.EXPONENTIATION
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation.OperationTypeCase.OPERATIONTYPE_NOT_SET
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation.OperationTypeCase.SQUARE_ROOT

/**
 * Returns whether this [ComparableOperation] is approximately equal to another, that is,
 * whether it exactly matches the other except for constants (which instead utilize
 * [org.oppia.android.app.model.Real.isApproximatelyEqualTo]).
 *
 * This function assumes that both this [ComparableOperation] and [other] are sorted prior to
 * equality checking.
 */
fun ComparableOperation.isApproximatelyEqualTo(other: ComparableOperation): Boolean {
  return when {
    isNegated != other.isNegated -> false
    isInverted != other.isInverted -> false
    comparisonTypeCase != other.comparisonTypeCase -> false
    else -> when (comparisonTypeCase) {
      COMMUTATIVE_ACCUMULATION ->
        commutativeAccumulation.isApproximatelyEqualTo(other.commutativeAccumulation)
      NON_COMMUTATIVE_OPERATION ->
        nonCommutativeOperation.isApproximatelyEqualTo(other.nonCommutativeOperation)
      CONSTANT_TERM -> constantTerm.isApproximatelyEqualTo(other.constantTerm)
      VARIABLE_TERM -> variableTerm == other.variableTerm
      COMPARISONTYPE_NOT_SET, null -> true
    }
  }
}

private fun CommutativeAccumulation.isApproximatelyEqualTo(
  other: CommutativeAccumulation
): Boolean {
  if (accumulationType != other.accumulationType) return false
  if (combinedOperationsCount != other.combinedOperationsCount) return false
  return combinedOperationsList.zip(other.combinedOperationsList).all { (first, second) ->
    first.isApproximatelyEqualTo(second)
  }
}

private fun NonCommutativeOperation.isApproximatelyEqualTo(
  other: NonCommutativeOperation
): Boolean {
  if (operationTypeCase != other.operationTypeCase) return false
  return when (operationTypeCase) {
    EXPONENTIATION -> {
      exponentiation.leftOperand.isApproximatelyEqualTo(other.exponentiation.leftOperand) &&
        exponentiation.rightOperand.isApproximatelyEqualTo(other.exponentiation.rightOperand)
    }
    SQUARE_ROOT -> squareRoot.isApproximatelyEqualTo(other.squareRoot)
    OPERATIONTYPE_NOT_SET, null -> true
  }
}
