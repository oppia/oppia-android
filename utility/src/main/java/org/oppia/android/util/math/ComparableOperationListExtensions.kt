package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.ComparableOperationList.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation
import org.oppia.android.app.model.ComparableOperationList.NonCommutativeOperation

/**
 * Returns whether this [ComparableOperationList] is approximately equal to another, that is,
 * whether it exactly matches the other except for constants (which instead utilize
 * [Real.approximatelyEquals]).
 */
fun ComparableOperationList.approximatelyEquals(other: ComparableOperationList): Boolean {
  return rootOperation.approximatelyEquals(other.rootOperation)
}

private fun ComparableOperation.approximatelyEquals(other: ComparableOperation): Boolean {
  if (isNegated != other.isNegated) return false
  if (isInverted != other.isInverted) return false
  if (comparisonTypeCase != other.comparisonTypeCase) return false
  return when (comparisonTypeCase) {
    ComparableOperation.ComparisonTypeCase.COMMUTATIVE_ACCUMULATION ->
      commutativeAccumulation.approximatelyEquals(other.commutativeAccumulation)
    ComparableOperation.ComparisonTypeCase.NON_COMMUTATIVE_OPERATION ->
      nonCommutativeOperation.approximatelyEquals(other.nonCommutativeOperation)
    ComparableOperation.ComparisonTypeCase.CONSTANT_TERM ->
      constantTerm.approximatelyEquals(other.constantTerm)
    ComparableOperation.ComparisonTypeCase.VARIABLE_TERM -> variableTerm == other.variableTerm
    ComparableOperation.ComparisonTypeCase.COMPARISONTYPE_NOT_SET, null -> true
  }
}

private fun CommutativeAccumulation.approximatelyEquals(other: CommutativeAccumulation): Boolean {
  if (accumulationType != other.accumulationType) return false
  if (combinedOperationsCount != other.combinedOperationsCount) return false
  return combinedOperationsList.zip(other.combinedOperationsList).all { (first, second) ->
    first.approximatelyEquals(second)
  }
}

private fun NonCommutativeOperation.approximatelyEquals(other: NonCommutativeOperation): Boolean {
  if (operationTypeCase != other.operationTypeCase) return false
  return when (operationTypeCase) {
    NonCommutativeOperation.OperationTypeCase.EXPONENTIATION -> {
      exponentiation.leftOperand.approximatelyEquals(other.exponentiation.leftOperand)
        && exponentiation.rightOperand.approximatelyEquals(other.exponentiation.rightOperand)
    }
    NonCommutativeOperation.OperationTypeCase.SQUARE_ROOT ->
      squareRoot.approximatelyEquals(other.squareRoot)
    NonCommutativeOperation.OperationTypeCase.OPERATIONTYPE_NOT_SET, null -> true
  }
}
