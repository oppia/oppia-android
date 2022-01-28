package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.PRODUCT
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.CONSTANT_TERM
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.NON_COMMUTATIVE_OPERATION
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase.VARIABLE_TERM
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation.OperationTypeCase
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.invertNegation
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.toComparableOperation

class ExpressionToComparableOperationConverter private constructor() {
  companion object {
    // TODO: consider eliminating the comparator extensions. Probably should verify full test suite
    //  & the old tests before deleting the old tests.

    private val COMPARABLE_OPERATION_COMPARATOR: Comparator<ComparableOperation> by lazy {
      // Some of the comparators must be deferred since they indirectly reference this comparator
      // (which isn't valid until it's fully assembled).
      Comparator.comparing(ComparableOperation::getComparisonTypeCase)
        .thenComparing(ComparableOperation::getIsNegated)
        .thenComparing(ComparableOperation::getIsInverted)
        .thenSelectAmong(
          ComparableOperation::getComparisonTypeCase,
          ComparableOperation.ComparisonTypeCase.COMMUTATIVE_ACCUMULATION to comparingDeferred(
            ComparableOperation::getCommutativeAccumulation
          ) { COMMUTATIVE_ACCUMULATION_COMPARATOR },
          NON_COMMUTATIVE_OPERATION to comparingDeferred(
            ComparableOperation::getNonCommutativeOperation
          ) { NON_COMMUTATIVE_OPERATION_COMPARATOR },
          CONSTANT_TERM to Comparator.comparing(
            ComparableOperation::getConstantTerm, REAL_COMPARATOR
          ),
          VARIABLE_TERM to Comparator.comparing(ComparableOperation::getVariableTerm)
        )
    }

    private val COMMUTATIVE_ACCUMULATION_COMPARATOR: Comparator<CommutativeAccumulation> by lazy {
      Comparator.comparing(CommutativeAccumulation::getAccumulationType)
        .thenComparing(
          { accumulation ->
            accumulation.combinedOperationsList.toSortedSet(COMPARABLE_OPERATION_COMPARATOR)
          },
          COMPARABLE_OPERATION_COMPARATOR.toSetComparator()
        )
    }

    private val NON_COMMUTATIVE_BINARY_OPERATION_COMPARATOR by lazy {
      Comparator.comparing(
        NonCommutativeOperation.BinaryOperation::getLeftOperand, COMPARABLE_OPERATION_COMPARATOR
      ).thenComparing(
        NonCommutativeOperation.BinaryOperation::getRightOperand, COMPARABLE_OPERATION_COMPARATOR
      )
    }

    private val NON_COMMUTATIVE_OPERATION_COMPARATOR: Comparator<NonCommutativeOperation> by lazy {
      Comparator.comparing(NonCommutativeOperation::getOperationTypeCase)
        .thenSelectAmong(
          NonCommutativeOperation::getOperationTypeCase,
          OperationTypeCase.EXPONENTIATION to Comparator.comparing(
            NonCommutativeOperation::getExponentiation, NON_COMMUTATIVE_BINARY_OPERATION_COMPARATOR
          ),
          OperationTypeCase.SQUARE_ROOT to Comparator.comparing(
            NonCommutativeOperation::getSquareRoot, COMPARABLE_OPERATION_COMPARATOR
          ),
        )
    }

    fun MathExpression.toComparableOperation(): ComparableOperation {
      return when (expressionTypeCase) {
        CONSTANT -> ComparableOperation.newBuilder().apply {
          constantTerm = constant
        }.build()
        VARIABLE -> ComparableOperation.newBuilder().apply {
          variableTerm = variable
        }.build()
        BINARY_OPERATION -> when (binaryOperation.operator) {
          ADD -> toSummation(isRhsNegative = false)
          SUBTRACT -> toSummation(isRhsNegative = true)
          MULTIPLY -> toProduct(isRhsInverted = false)
          DIVIDE -> toProduct(isRhsInverted = true)
          EXPONENTIATE ->
            toNonCommutativeOperation(NonCommutativeOperation.Builder::setExponentiation)
          BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null ->
            ComparableOperation.getDefaultInstance()
        }
        UNARY_OPERATION -> when (unaryOperation.operator) {
          NEGATE -> unaryOperation.operand.toComparableOperation().invertNegation()
          POSITIVE -> unaryOperation.operand.toComparableOperation()
          UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null ->
            ComparableOperation.getDefaultInstance()
        }
        FUNCTION_CALL -> when (functionCall.functionType) {
          SQUARE_ROOT -> ComparableOperation.newBuilder().apply {
            nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
              squareRoot = functionCall.argument.toComparableOperation()
            }.build()
          }.build()
          FunctionType.FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null ->
            ComparableOperation.getDefaultInstance()
        }
        GROUP -> group.toComparableOperation()
        EXPRESSIONTYPE_NOT_SET, null -> ComparableOperation.getDefaultInstance()
      }
    }

    private fun MathExpression.toSummation(isRhsNegative: Boolean): ComparableOperation {
      return ComparableOperation.newBuilder().apply {
        commutativeAccumulation = CommutativeAccumulation.newBuilder().apply {
          accumulationType = SUMMATION
          addOperationToSum(binaryOperation.leftOperand, forceNegative = false)
          addOperationToSum(binaryOperation.rightOperand, forceNegative = isRhsNegative)
          sort()
        }.build()
      }.build()
    }

    private fun MathExpression.toProduct(isRhsInverted: Boolean): ComparableOperation {
      return ComparableOperation.newBuilder().apply {
        commutativeAccumulation = CommutativeAccumulation.newBuilder().apply {
          accumulationType = PRODUCT
          val negativeCount =
            addOperationToProduct(
              binaryOperation.leftOperand, forceInverse = false, invertNegation = false
            ) + addOperationToProduct(
              binaryOperation.rightOperand, forceInverse = isRhsInverted, invertNegation = false
            )
          // If an odd number of terms were negative then the overall product is negative.
          isNegated = (negativeCount % 2) != 0
          sort()
        }.build()
      }.build()
    }

    private fun CommutativeAccumulation.Builder.addOperationToSum(
      expression: MathExpression,
      forceNegative: Boolean
    ) {
      when {
        expression.binaryOperation.operator == ADD -> {
          // The whole operation being negative distributes to both sides of the addition.
          addOperationToSum(expression.binaryOperation.leftOperand, forceNegative)
          addOperationToSum(expression.binaryOperation.rightOperand, forceNegative)
        }
        expression.binaryOperation.operator == SUBTRACT -> {
          // Similar to addition, negation distributes but is inverted by this subtraction for the
          // right-hand operand.
          addOperationToSum(expression.binaryOperation.leftOperand, forceNegative)
          addOperationToSum(expression.binaryOperation.rightOperand, !forceNegative)
        }
        expression.unaryOperation.operator == NEGATE ->
          addOperationToSum(expression.unaryOperation.operand, !forceNegative)
        // Positive unary can be treated similarly to groups (inline for nesting).
        expression.unaryOperation.operator == POSITIVE ->
          addOperationToSum(expression.unaryOperation.operand, forceNegative)
        // Skip groups so that nested operations can be properly combined.
        expression.expressionTypeCase == GROUP -> addOperationToSum(expression.group, forceNegative)
        forceNegative -> addCombinedOperations(expression.toComparableOperation().invertNegation())
        else -> addCombinedOperations(expression.toComparableOperation())
      }
    }

    /**
     * Recursively adds [expression] tp the ongoing product [CommutativeAccumulation.Builder] by
     * collapsing subsequent products into a single list.
     *
     * @param forceInverse whether this expression is being divided rather than multiplied
     * @param invertNegation whether to invert the negation sign for immediate constituent
     *     operations
     * @return the number of negative operations that were made positive before being added to the
     *     accumulation
     */
    private fun CommutativeAccumulation.Builder.addOperationToProduct(
      expression: MathExpression,
      forceInverse: Boolean,
      invertNegation: Boolean
    ): Int {
      // Note that negation only distributes "leftward" since subsequent right-hand operations would
      // otherwise actually reverse the negation.
      return when {
        expression.binaryOperation.operator == MULTIPLY -> {
          // If the entire operation is inverted, that means each part of the multiplication should
          // be, i.e.: 1/(x*y)=(1/x)*(1/y).
          addOperationToProduct(
            expression.binaryOperation.leftOperand, forceInverse, invertNegation
          ) + addOperationToProduct(
            expression.binaryOperation.rightOperand, forceInverse, invertNegation = false
          )
        }
        expression.binaryOperation.operator == DIVIDE -> {
          // Similar to multiplication, inversion for the whole operation results in distribution
          // except the division inverts for the right-hand operand, i.e.: 1/(x/y)=(1/x)*y.
          addOperationToProduct(
            expression.binaryOperation.leftOperand, forceInverse, invertNegation
          ) + addOperationToProduct(
            expression.binaryOperation.rightOperand, !forceInverse, invertNegation = false
          )
        }
        expression.unaryOperation.operator == NEGATE ->
          addOperationToProduct(expression.unaryOperation.operand, forceInverse, !invertNegation)
        // Positive unary can be treated similarly to groups (inline for nesting).
        expression.unaryOperation.operator == POSITIVE ->
          addOperationToProduct(expression.unaryOperation.operand, forceInverse, invertNegation)
        // Skip groups so that nested operations can be properly combined.
        expression.expressionTypeCase == GROUP ->
          addOperationToProduct(expression.group, forceInverse, invertNegation)
        else -> {
          val operationExpression = expression.toComparableOperation()
          val potentiallyInvertedExpression = if (invertNegation) {
            operationExpression.invertNegation()
          } else operationExpression
          val positiveConvertedOperation = potentiallyInvertedExpression.makePositive()
          if (forceInverse) {
            addCombinedOperations(positiveConvertedOperation.invertInverted())
          } else addCombinedOperations(positiveConvertedOperation)
          if (potentiallyInvertedExpression.isNegated) 1 else 0
        }
      }
    }

    private fun CommutativeAccumulation.Builder.sort() {
      // Replace the list operations with a sorted list of operations. Note that the inner elements
      // are already sorted since this is called during operation creation time (so nested
      // operations would have already been sorted).
      val operationsList = combinedOperationsList.toMutableList()
      clearCombinedOperations()
      addAllCombinedOperations(operationsList.sortedWith(COMPARABLE_OPERATION_COMPARATOR))
    }

    private fun MathExpression.toNonCommutativeOperation(
      setOperation: NonCommutativeOperation.Builder.(
        NonCommutativeOperation.BinaryOperation
      ) -> NonCommutativeOperation.Builder
    ): ComparableOperation {
      return ComparableOperation.newBuilder().apply {
        nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
          setOperation(
            NonCommutativeOperation.BinaryOperation.newBuilder().apply {
              leftOperand = binaryOperation.leftOperand.toComparableOperation()
              rightOperand = binaryOperation.rightOperand.toComparableOperation()
            }.build()
          )
        }.build()
      }.build()
    }

    private fun ComparableOperation.makePositive(): ComparableOperation =
      toBuilder().apply { isNegated = false }.build()

    private fun ComparableOperation.invertNegation(): ComparableOperation =
      toBuilder().apply { isNegated = !isNegated }.build()

    private fun ComparableOperation.invertInverted(): ComparableOperation =
      toBuilder().apply { isInverted = !isInverted }.build()
  }
}
