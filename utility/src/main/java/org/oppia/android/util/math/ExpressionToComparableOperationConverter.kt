package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.PRODUCT
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation.AccumulationType.SUMMATION
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation.BinaryOperation
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

/**
 * Converter from [MathExpression] to [ComparableOperation].
 *
 * See the separate proto details for context, and [convertToComparableOperation] for the actual conversion
 * function.
 */
class ExpressionToComparableOperationConverter private constructor() {
  companion object {
    private val COMPARABLE_OPERATION_COMPARATOR by lazy { createComparableOperationComparator() }

    /**
     * Returns a new [ComparableOperation] representing this [MathExpression].
     *
     * Comparable operations are representations of math expressions that are deterministically
     * arranged to ensure two expressions that only differ due to associativity or commutativity are
     * still equal. This is done by combining neighboring arithmetic operations into accumulations,
     * and still retaining the structures for non-commutative operations. The order of all
     * operations is well-defined and deterministic. Further, how elements retain inverted or
     * negated properties is also deterministic (and designed to minimize negative values).
     *
     * The tests for this method provide very thorough and broad examples of different cases that
     * this function supports. In particular, the equality tests are useful to see what sorts of
     * expressions can be considered the same per [ComparableOperation].
     */
    fun MathExpression.convertToComparableOperation(): ComparableOperation {
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
          NEGATE -> unaryOperation.operand.convertToComparableOperation().invertNegation()
          POSITIVE -> unaryOperation.operand.convertToComparableOperation()
          UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null ->
            ComparableOperation.getDefaultInstance()
        }
        FUNCTION_CALL -> when (functionCall.functionType) {
          SQUARE_ROOT -> ComparableOperation.newBuilder().apply {
            nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
              squareRoot = functionCall.argument.convertToComparableOperation()
            }.build()
          }.build()
          FunctionType.FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null ->
            ComparableOperation.getDefaultInstance()
        }
        GROUP -> group.convertToComparableOperation()
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
        forceNegative -> addCombinedOperations(expression.convertToComparableOperation().invertNegation())
        else -> addCombinedOperations(expression.convertToComparableOperation())
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
          val operationExpression = expression.convertToComparableOperation()
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
      val operationsList = combinedOperationsList.sortedWith(COMPARABLE_OPERATION_COMPARATOR)
      clearCombinedOperations()
      addAllCombinedOperations(operationsList)
    }

    private fun MathExpression.toNonCommutativeOperation(
      setOperation: NonCommutativeOperation.Builder.(
        BinaryOperation
      ) -> NonCommutativeOperation.Builder
    ): ComparableOperation {
      return ComparableOperation.newBuilder().apply {
        nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
          setOperation(
            BinaryOperation.newBuilder().apply {
              leftOperand = binaryOperation.leftOperand.convertToComparableOperation()
              rightOperand = binaryOperation.rightOperand.convertToComparableOperation()
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

    private fun createComparableOperationComparator(): Comparator<ComparableOperation> {
      // Note that this & constituent comparators is designed to also verify undefined fields (such
      // as all the possibilities of a oneof versus just one) for simpler syntax. Computationally,
      // it shouldn't make a large difference since default protos are generally cached for proto
      // lite, and compareProtos short-circuits for default protos. Further, a comparator is created
      // for each staged of the execution since, unfortunately, there's no easy way to circularly
      // reference cached fields.
      return compareBy(ComparableOperation::getComparisonTypeCase)
        .thenBy(ComparableOperation::getIsNegated)
        .thenBy(ComparableOperation::getIsInverted)
        .thenComparator { a, b ->
          createCommutativeAccumulationComparator()
            .compareProtos(a.commutativeAccumulation, b.commutativeAccumulation)
        }.thenComparator { a, b ->
          createNonCommutativeOperationComparator()
            .compareProtos(a.nonCommutativeOperation, b.nonCommutativeOperation)
        }.thenComparator { a, b ->
          REAL_COMPARATOR.compareProtos(a.constantTerm, b.constantTerm)
        }
        .thenBy(ComparableOperation::getVariableTerm)
    }

    private fun createCommutativeAccumulationComparator(): Comparator<CommutativeAccumulation> {
      return compareBy(CommutativeAccumulation::getAccumulationType)
        .thenComparator { a, b ->
          createComparableOperationComparator().compareIterables(
            a.combinedOperationsList, b.combinedOperationsList
          )
        }
    }

    private fun createNonCommutativeOperationComparator(): Comparator<NonCommutativeOperation> {
      return compareBy(NonCommutativeOperation::getOperationTypeCase)
        .thenComparator { a, b ->
          createBinaryOperationComparator().compareProtos(a.exponentiation, b.exponentiation)
        }.thenComparator { a, b ->
          createComparableOperationComparator().compareProtos(a.squareRoot, b.squareRoot)
        }
    }

    private fun createBinaryOperationComparator(): Comparator<BinaryOperation> {
      // Start with a trivial comparator to start the chain for nicer syntax.
      return compareBy(BinaryOperation::hasLeftOperand)
        .thenComparator { a, b ->
          createComparableOperationComparator().compareProtos(a.leftOperand, b.leftOperand)
        }.thenComparator { a, b ->
          createComparableOperationComparator().compareProtos(a.rightOperand, b.rightOperand)
        }
    }
  }
}
