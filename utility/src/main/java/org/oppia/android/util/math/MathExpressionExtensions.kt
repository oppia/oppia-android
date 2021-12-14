package org.oppia.android.util.math

import java.text.NumberFormat
import java.util.Locale
import java.util.SortedSet
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.ComparableOperationList.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperationList.CommutativeAccumulation.AccumulationType
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase.COMMUTATIVE_ACCUMULATION
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase.COMPARISONTYPE_NOT_SET
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase.CONSTANT_TERM
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase.NON_COMMUTATIVE_OPERATION
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase.VARIABLE_TERM
import org.oppia.android.app.model.ComparableOperationList.NonCommutativeOperation
import org.oppia.android.app.model.ComparableOperationList.NonCommutativeOperation.OperationTypeCase
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED

// TODO: split up this extensions file into multiple, clean it up, reorganize, and add tests.

// XXX: Polynomials can actually take lots of forms, so this conversion is intentionally limited
// based on intended use cases and can be extended in the future, as needed. For example, both
// x^(2^3) and ((x^2+2x+1)/(x+1)) won't be considered polynomials, but it can be seen that they are
// indeed after being simplified. This implementation doesn't yet support recognizing binomials
// (e.g. (x+1)^2) but needs to be updated to. The representation for Polynomial doesn't quite
// support much outside of general form (except negative exponents).

// TODO: Consider expressions like x/2 (should be treated like (1/2)x).
// TODO: Consider: (1+2)*x or (1+2)x
// TODO: Make sure that all 'when' cases here do not use 'else' branches to ensure structural
//  changes require changing logic.

private val COMPARABLE_OPERATION_COMPARATOR: Comparator<ComparableOperation> by lazy {
  // Some of the comparators must be deferred since they indirectly reference this comparator (which
  // isn't valid until it's fully assembled).
  Comparator.comparing(ComparableOperation::getComparisonTypeCase)
    .thenComparing(ComparableOperation::getIsNegated)
    .thenComparing(ComparableOperation::getIsInverted)
    .thenSelectAmong(
      ComparableOperation::getComparisonTypeCase,
      COMMUTATIVE_ACCUMULATION to comparingDeferred(
        ComparableOperation::getCommutativeAccumulation
      ) { COMMUTATIVE_ACCUMULATION_COMPARATOR },
      NON_COMMUTATIVE_OPERATION to comparingDeferred(
        ComparableOperation::getNonCommutativeOperation
      ) { NON_COMMUTATIVE_OPERATION_COMPARATOR },
      CONSTANT_TERM to Comparator.comparing(ComparableOperation::getConstantTerm, REAL_COMPARATOR),
      VARIABLE_TERM to Comparator.comparing(ComparableOperation::getVariableTerm)
    )
}

private val COMMUTATIVE_ACCUMULATION_COMPARATOR: Comparator<CommutativeAccumulation> by lazy {
  Comparator.comparing(CommutativeAccumulation::getAccumulationType)
    .thenComparing({ accumulation ->
      accumulation.combinedOperationsList.toSortedSet(COMPARABLE_OPERATION_COMPARATOR)
    }, COMPARABLE_OPERATION_COMPARATOR.toSetComparator())
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

private val REAL_COMPARATOR: Comparator<Real> by lazy { Comparator.comparing(Real::toDouble) }

private val POLYNOMIAL_VARIABLE_COMPARATOR: Comparator<Variable> by lazy {
  // Note that power is reversed because larger powers should actually be sorted ahead of smaller
  // powers for the same variable name (but variable name still takes precedence). This ensures
  // cases like x^2y+y^2x are sorted in that order.
  Comparator.comparing(Variable::getName).thenComparingReversed(Variable::getPower)
}

private val POLYNOMIAL_TERM_COMPARATOR: Comparator<Term> by lazy {
  // First, sort by all variable names to ensure xy is placed ahead of xz. Then, sort by variable
  // powers in order of the variables (such that x^2y is ranked higher thank xy). Finally, sort by
  // the coefficient to ensure equality through the comparator works correctly (though in practice
  // like terms should always be combined). Note the specific reversing happening here. It's done in
  // this way so that sorted set bigger/smaller list is reversed (which matches expectations since
  // larger terms should appear earlier in the results). This is implementing an ordering similar to
  // https://en.wikipedia.org/wiki/Polynomial#Definition, except for multi-variable functions (where
  // variables of higher degree are preferred over lower degree by lexicographical order of variable
  // names).
  Comparator.comparing<Term, SortedSet<Variable>>(
    { term -> term.variableList.toSortedSet(POLYNOMIAL_VARIABLE_COMPARATOR) },
    POLYNOMIAL_VARIABLE_COMPARATOR.reversed().toSetComparator()
  ).reversed().thenComparing(Term::getCoefficient, REAL_COMPARATOR)
}

private fun <T, U> comparingDeferred(
  keySelector: (T) -> U, comparatorSelector: () -> Comparator<U>
): Comparator<T> {
  // Store as captured val for memoization.
  val comparator by lazy { comparatorSelector() }
  return Comparator.comparing(keySelector) { o1, o2 ->
    comparator.compare(o1, o2)
  }
}

private fun <T, U: Comparable<U>> Comparator<T>.thenComparingReversed(
  keySelector: (T) -> U
): Comparator<T> = thenComparing(Comparator.comparing(keySelector).reversed())

private fun <T, E: Enum<E>> Comparator<T>.thenSelectAmong(
  enumSelector: (T) -> E, vararg comparators: Pair<E, Comparator<T>>
): Comparator<T> {
  val comparatorMap = comparators.toMap()
  return thenComparing(
    Comparator { o1, o2 ->
      val enum1 = enumSelector(o1)
      val enum2 = enumSelector(o2)
      check(enum1 == enum2) {
        "Expected objects to have the same enum values: $o1 ($enum1), $o2 ($enum2)"
      }
      val comparator =
        checkNotNull(comparatorMap[enum1]) { "No comparator for matched enum: $enum1" }
      return@Comparator comparator.compare(o1, o2)
    }
  )
}

private fun <T> Comparator<T>.toSetComparator(): Comparator<SortedSet<T>> {
  val itemComparator = this
  return Comparator { first, second ->
    // Reference: https://stackoverflow.com/a/30107086.
    val firstIter = first.iterator()
    val secondIter = second.iterator()
    while (firstIter.hasNext() && secondIter.hasNext()) {
      val comparison = itemComparator.compare(firstIter.next(), secondIter.next())
      if (comparison != 0) return@Comparator comparison // Found a different item.
    }

    // Everything is equal up to here, see if the lists are different length.
    return@Comparator when {
      firstIter.hasNext() -> 1 // The first list is longer, therefore "greater."
      secondIter.hasNext() -> -1 // Ditto, but for the second list.
      else -> 0 // Otherwise, they're the same length with all equal items (and are thus equal).
    }
  }
}

fun MathExpression.toComparableOperationList(): ComparableOperationList {
  return ComparableOperationList.newBuilder().apply {
    rootOperation = stripGroups().toComparableOperation().stabilizeNegation().sort()
  }.build()
}

private fun ComparableOperation.stabilizeNegation(): ComparableOperation {
  return when (comparisonTypeCase) {
    COMMUTATIVE_ACCUMULATION -> {
      val stabilizedOperations =
        commutativeAccumulation.combinedOperationsList.map { it.stabilizeNegation() }
      when (commutativeAccumulation.accumulationType) {
        AccumulationType.SUMMATION -> toBuilder().apply {
          commutativeAccumulation = commutativeAccumulation.toBuilder().apply {
            clearCombinedOperations()
            addAllCombinedOperations(stabilizedOperations)
          }.build()
        }.build()
        AccumulationType.PRODUCT -> {
          // Negations can be combined for all constituent operations & brought up to the top-level
          // operation.
          val negativeCount = stabilizedOperations.count { it.isNegated } + if (isNegated) 1 else 0
          val positiveOperations = stabilizedOperations.map { it.makePositive() }
          toBuilder().apply {
            isNegated = (negativeCount % 2) == 1
            commutativeAccumulation = commutativeAccumulation.toBuilder().apply {
              clearCombinedOperations()
              addAllCombinedOperations(positiveOperations)
            }.build()
          }.build()
        }
        AccumulationType.ACCUMULATION_TYPE_UNSPECIFIED, AccumulationType.UNRECOGNIZED, null -> this
      }
    }
    NON_COMMUTATIVE_OPERATION -> toBuilder().apply {
      // Negation can't be extracted from commutative operations.
      nonCommutativeOperation = when (nonCommutativeOperation.operationTypeCase) {
        OperationTypeCase.EXPONENTIATION -> nonCommutativeOperation.toBuilder().apply {
          exponentiation = nonCommutativeOperation.exponentiation.toBuilder().apply {
            leftOperand = nonCommutativeOperation.exponentiation.leftOperand.stabilizeNegation()
            rightOperand = nonCommutativeOperation.exponentiation.rightOperand.stabilizeNegation()
          }.build()
        }.build()
        OperationTypeCase.SQUARE_ROOT -> nonCommutativeOperation.toBuilder().apply {
          squareRoot = nonCommutativeOperation.squareRoot.stabilizeNegation()
        }.build()
        OperationTypeCase.OPERATIONTYPE_NOT_SET, null -> nonCommutativeOperation
      }
    }.build()
    CONSTANT_TERM -> this
    VARIABLE_TERM -> this
    COMPARISONTYPE_NOT_SET, null -> this
  }
}

private fun ComparableOperation.sort(): ComparableOperation {
  return when (comparisonTypeCase) {
    COMMUTATIVE_ACCUMULATION -> toBuilder().apply {
      commutativeAccumulation = commutativeAccumulation.toBuilder().apply {
        clearCombinedOperations()
        // Sort the operations themselves before sorting them relative to each other.
        val innerSortedList = commutativeAccumulation.combinedOperationsList.map { it.sort() }
        addAllCombinedOperations(innerSortedList.sortedWith(COMPARABLE_OPERATION_COMPARATOR))
      }.build()
    }.build()
    NON_COMMUTATIVE_OPERATION -> toBuilder().apply {
      nonCommutativeOperation = when (nonCommutativeOperation.operationTypeCase) {
        OperationTypeCase.EXPONENTIATION -> nonCommutativeOperation.toBuilder().apply {
          exponentiation = nonCommutativeOperation.exponentiation.toBuilder().apply {
            leftOperand = nonCommutativeOperation.exponentiation.leftOperand.sort()
            rightOperand = nonCommutativeOperation.exponentiation.rightOperand.sort()
          }.build()
        }.build()
        OperationTypeCase.SQUARE_ROOT -> nonCommutativeOperation.toBuilder().apply {
          squareRoot = nonCommutativeOperation.squareRoot.sort()
        }.build()
        OperationTypeCase.OPERATIONTYPE_NOT_SET, null -> nonCommutativeOperation
      }
    }.build()
    CONSTANT_TERM, VARIABLE_TERM, COMPARISONTYPE_NOT_SET, null -> this
  }
}

private fun MathExpression.stripGroups(): MathExpression {
  return when (expressionTypeCase) {
    BINARY_OPERATION -> toBuilder().apply {
      binaryOperation = binaryOperation.toBuilder().apply {
        leftOperand = binaryOperation.leftOperand.stripGroups()
        rightOperand = binaryOperation.rightOperand.stripGroups()
      }.build()
    }.build()
    UNARY_OPERATION -> toBuilder().apply {
      unaryOperation = unaryOperation.toBuilder().apply {
        operand = unaryOperation.operand.stripGroups()
      }.build()
    }.build()
    FUNCTION_CALL -> toBuilder().apply {
      functionCall = functionCall.toBuilder().apply {
        argument = functionCall.argument.stripGroups()
      }.build()
    }.build()
    GROUP -> group.stripGroups()
    CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
  }
}

private val ONE_HALF by lazy {
  Real.newBuilder().apply {
    rational = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()
  }.build()
}

private fun MathExpression.replaceSquareRoots(): MathExpression {
  return when (expressionTypeCase) {
    BINARY_OPERATION -> toBuilder().apply {
      binaryOperation = binaryOperation.toBuilder().apply {
        leftOperand = binaryOperation.leftOperand.replaceSquareRoots()
        rightOperand = binaryOperation.rightOperand.replaceSquareRoots()
      }.build()
    }.build()
    UNARY_OPERATION -> toBuilder().apply {
      unaryOperation = unaryOperation.toBuilder().apply {
        operand = unaryOperation.operand.replaceSquareRoots()
      }.build()
    }.build()
    FUNCTION_CALL -> when (functionCall.functionType) {
      FunctionType.SQUARE_ROOT -> toBuilder().apply {
        // Replace the square root function call with the equivalent exponentiation. That is,
        // sqrt(x)=x^(1/2).
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          operator = BinaryOperator.EXPONENTIATE
          leftOperand = functionCall.argument.replaceSquareRoots()
          rightOperand = MathExpression.newBuilder().apply {
            constant = ONE_HALF
          }.build()
        }.build()
      }.build()
      FunctionType.FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> this
    }
    GROUP -> group.replaceSquareRoots()
    CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
  }
}

private fun MathExpression.toComparableOperation(): ComparableOperation {
  return when (expressionTypeCase) {
    CONSTANT -> ComparableOperation.newBuilder().apply {
      constantTerm = constant
    }.build()
    VARIABLE -> ComparableOperation.newBuilder().apply {
      variableTerm = variable
    }.build()
    BINARY_OPERATION -> when (binaryOperation.operator) {
      BinaryOperator.ADD -> toSummation(isRhsNegative = false)
      BinaryOperator.SUBTRACT -> toSummation(isRhsNegative = true)
      BinaryOperator.MULTIPLY -> toProduct(isRhsInverted = false)
      BinaryOperator.DIVIDE -> toProduct(isRhsInverted = true)
      BinaryOperator.EXPONENTIATE ->
        toNonCommutativeOperation(NonCommutativeOperation.Builder::setExponentiation)
      BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null ->
        ComparableOperation.getDefaultInstance()
    }
    UNARY_OPERATION -> when (unaryOperation.operator) {
      UnaryOperator.NEGATE -> unaryOperation.operand.toComparableOperation().makeNegative()
      UnaryOperator.POSITIVE -> unaryOperation.operand.toComparableOperation()
      UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null ->
        ComparableOperation.getDefaultInstance()
    }
    FUNCTION_CALL -> when (functionCall.functionType) {
      FunctionType.SQUARE_ROOT -> ComparableOperation.newBuilder().apply {
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
      accumulationType = AccumulationType.SUMMATION
      addOperationToSum(binaryOperation.leftOperand, forceNegative = false)
      addOperationToSum(binaryOperation.rightOperand, forceNegative = isRhsNegative)
    }.build()
  }.build()
}

private fun MathExpression.toProduct(isRhsInverted: Boolean): ComparableOperation {
  return ComparableOperation.newBuilder().apply {
    commutativeAccumulation = CommutativeAccumulation.newBuilder().apply {
      accumulationType = AccumulationType.PRODUCT
      addOperationToProduct(binaryOperation.leftOperand, forceInverse = false)
      addOperationToProduct(binaryOperation.rightOperand, forceInverse = isRhsInverted)
    }.build()
  }.build()
}

private fun CommutativeAccumulation.Builder.addOperationToSum(
  expression: MathExpression, forceNegative: Boolean
) {
  when (expression.binaryOperation.operator) {
    BinaryOperator.ADD -> {
      // If the whole operation is negative, carry it to the left-hand side of the operation.
      addOperationToSum(expression.binaryOperation.leftOperand, forceNegative)
      addOperationToSum(expression.binaryOperation.rightOperand, forceNegative = false)
    }
    BinaryOperator.SUBTRACT -> {
      addOperationToSum(expression.binaryOperation.leftOperand, forceNegative)
      addOperationToSum(expression.binaryOperation.rightOperand, forceNegative = true)
    }
    else -> if (forceNegative) {
      addCombinedOperations(expression.toComparableOperation().makeNegative())
    } else addCombinedOperations(expression.toComparableOperation())
  }
}

private fun CommutativeAccumulation.Builder.addOperationToProduct(
  expression: MathExpression, forceInverse: Boolean
) {
  when (expression.binaryOperation.operator) {
    BinaryOperator.MULTIPLY -> {
      // If the whole operation is inverted, carry it to the left-hand side of the operation.
      addOperationToProduct(expression.binaryOperation.leftOperand, forceInverse)
      addOperationToProduct(expression.binaryOperation.rightOperand, forceInverse = false)
    }
    BinaryOperator.DIVIDE -> {
      addOperationToProduct(expression.binaryOperation.leftOperand, forceInverse)
      addOperationToProduct(expression.binaryOperation.rightOperand, forceInverse = true)
    }
    else -> if (forceInverse) {
      addCombinedOperations(expression.toComparableOperation().makeInverted())
    } else addCombinedOperations(expression.toComparableOperation())
  }
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

private fun ComparableOperation.makeNegative(): ComparableOperation =
  toBuilder().apply { isNegated = true }.build()

private fun ComparableOperation.makeInverted(): ComparableOperation =
  toBuilder().apply { isInverted = true }.build()

// TODO: move these to the UI layer & have them utilize non-translatable strings.
private val numberFormat by lazy { NumberFormat.getNumberInstance(Locale.US) }
private val singularOrdinalNames = mapOf(
  1 to "oneth",
  2 to "half",
  3 to "third",
  4 to "fourth",
  5 to "fifth",
  6 to "sixth",
  7 to "seventh",
  8 to "eighth",
  9 to "ninth",
  10 to "tenth",
)
private val pluralOrdinalNames = mapOf(
  1 to "oneths",
  2 to "halves",
  3 to "thirds",
  4 to "fourths",
  5 to "fifths",
  6 to "sixths",
  7 to "sevenths",
  8 to "eighths",
  9 to "ninths",
  10 to "tenths",
)

fun MathEquation.toHumanReadableString(language: OppiaLanguage, divAsFraction: Boolean): String? {
  return when (language) {
    ENGLISH -> toHumanReadableEnglishString(divAsFraction)
    ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, LANGUAGE_UNSPECIFIED, UNRECOGNIZED ->
      null
  }
}

fun MathExpression.toHumanReadableString(language: OppiaLanguage, divAsFraction: Boolean): String? {
  return when (language) {
    ENGLISH -> toHumanReadableEnglishString(divAsFraction)
    ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, LANGUAGE_UNSPECIFIED, UNRECOGNIZED ->
      null
  }
}

private fun MathEquation.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
  val lhsStr = leftSide.toHumanReadableEnglishString(divAsFraction)
  val rhsStr = rightSide.toHumanReadableEnglishString(divAsFraction)
  return if (lhsStr != null && rhsStr != null) "$lhsStr equals $rhsStr" else null
}

private fun MathExpression.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
  // Reference:
  // https://docs.google.com/document/d/1P-dldXQ08O-02ZRG978paiWOSz0dsvcKpDgiV_rKH_Y/view.
  return when (expressionTypeCase) {
    CONSTANT -> if (constant.realTypeCase == INTEGER) {
      numberFormat.format(constant.integer.toLong())
    } else constant.toPlainString()
    VARIABLE -> when (variable) {
      "z" -> "zed"
      "Z" -> "Zed"
      else -> variable
    }
    BINARY_OPERATION -> {
      val lhs = binaryOperation.leftOperand
      val rhs = binaryOperation.rightOperand
      val lhsStr = lhs.toHumanReadableEnglishString(divAsFraction)
      val rhsStr = rhs.toHumanReadableEnglishString(divAsFraction)
      if (lhsStr == null || rhsStr == null) return null
      when (binaryOperation.operator) {
        BinaryOperator.ADD -> "$lhsStr plus $rhsStr"
        BinaryOperator.SUBTRACT -> "$lhsStr minus $rhsStr"
        BinaryOperator.MULTIPLY -> {
          if (binaryOperation.canBeReadAsImplicitMultiplication()) {
            "$lhsStr $rhsStr"
          } else "$lhsStr times $rhsStr"
        }
        BinaryOperator.DIVIDE -> {
          if (divAsFraction && lhs.isConstantInteger() && rhs.isConstantInteger()) {
            val numerator = lhs.constant.integer
            val denominator = rhs.constant.integer
            if (numerator in 0..10 && denominator in 1..10 && denominator >= numerator) {
              val ordinalName =
                if (numerator == 1) {
                  singularOrdinalNames.getValue(denominator)
                } else pluralOrdinalNames.getValue(denominator)
              "$numerator $ordinalName"
            } else "$lhsStr over $rhsStr"
          } else if (divAsFraction) {
            "the fraction with numerator $lhsStr and denominator $rhsStr"
          } else "$lhsStr divided by $rhsStr"
        }
        BinaryOperator.EXPONENTIATE -> "$lhsStr raised to the power of $rhsStr"
        BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
      }
    }
    UNARY_OPERATION -> {
      val operandStr = unaryOperation.operand.toHumanReadableEnglishString(divAsFraction)
      when (unaryOperation.operator) {
        UnaryOperator.NEGATE -> operandStr?.let { "negative $it" }
        UnaryOperator.POSITIVE -> operandStr?.let { "positive $it" }
        UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
      }
    }
    FUNCTION_CALL -> {
      val argStr = functionCall.argument.toHumanReadableEnglishString(divAsFraction)
      when (functionCall.functionType) {
        FunctionType.SQUARE_ROOT -> argStr?.let {
          if (functionCall.argument.isSingleTerm()) {
            "square root of $it"
          } else "start square root $it end square root"
        }
        FunctionType.FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> null
      }
    }
    GROUP -> group.toHumanReadableEnglishString(divAsFraction)?.let {
      if (isSingleTerm()) it else "open parenthesis $it close parenthesis"
    }
    EXPRESSIONTYPE_NOT_SET, null -> null
  }
}

private fun MathBinaryOperation.canBeReadAsImplicitMultiplication(): Boolean {
  // Note that exponentiation is specialized since it's higher precedence than multiplication which
  // means the graph won't look like "constant * variable" for polynomial terms like 2x^4 (which are
  // cases the system should read using implicit multiplication, e.g. "two x raised to the power of
  // 4").
  if (!isImplicit || !leftOperand.isConstant()) return false
  return rightOperand.isVariable() || rightOperand.isExponentiation()
}

private fun MathExpression.isConstantInteger(): Boolean =
  expressionTypeCase == CONSTANT && constant.realTypeCase == INTEGER

private fun MathExpression.isConstant(): Boolean = expressionTypeCase == CONSTANT

private fun MathExpression.isVariable(): Boolean = expressionTypeCase == VARIABLE

private fun MathExpression.isExponentiation(): Boolean =
  expressionTypeCase == BINARY_OPERATION && binaryOperation.operator == BinaryOperator.EXPONENTIATE

private fun MathExpression.isSingleTerm(): Boolean = when (expressionTypeCase) {
  CONSTANT, VARIABLE, FUNCTION_CALL -> true
  BINARY_OPERATION, UNARY_OPERATION -> false
  GROUP -> group.isSingleTerm()
  EXPRESSIONTYPE_NOT_SET, null -> false
}

fun MathEquation.toRawLatex(divAsFraction: Boolean): String {
  return "${leftSide.toRawLatex(divAsFraction)} = ${rightSide.toRawLatex(divAsFraction)}"
}

fun MathExpression.toRawLatex(divAsFraction: Boolean): String {
  return when (expressionTypeCase) {
    CONSTANT -> constant.toPlainString()
    VARIABLE -> variable
    BINARY_OPERATION -> {
      val lhsLatex = binaryOperation.leftOperand.toRawLatex(divAsFraction)
      val rhsLatex = binaryOperation.rightOperand.toRawLatex(divAsFraction)
      when (binaryOperation.operator) {
        BinaryOperator.ADD -> "$lhsLatex + $rhsLatex"
        BinaryOperator.SUBTRACT -> "$lhsLatex - $rhsLatex"
        BinaryOperator.MULTIPLY -> if (binaryOperation.isImplicit) {
          "$lhsLatex$rhsLatex"
        } else "$lhsLatex \\times $rhsLatex"
        BinaryOperator.DIVIDE -> if (divAsFraction) {
          "\\frac{$lhsLatex}{$rhsLatex}"
        } else "$lhsLatex \\div $rhsLatex"
        BinaryOperator.EXPONENTIATE -> "$lhsLatex ^ {$rhsLatex}"
        BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null ->
          "$lhsLatex $rhsLatex"
      }
    }
    UNARY_OPERATION -> {
      val operandLatex = unaryOperation.operand.toRawLatex(divAsFraction)
      when (unaryOperation.operator) {
        UnaryOperator.NEGATE -> "-$operandLatex"
        UnaryOperator.POSITIVE -> "+$operandLatex"
        UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> operandLatex
      }
    }
    FUNCTION_CALL -> {
      val argumentLatex = functionCall.argument.toRawLatex(divAsFraction)
      when (functionCall.functionType) {
        FunctionType.SQUARE_ROOT -> "\\sqrt{$argumentLatex}"
        FunctionType.FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> argumentLatex
      }
    }
    GROUP -> "(${group.toRawLatex(divAsFraction)})"
    EXPRESSIONTYPE_NOT_SET, null -> ""
  }
}

// TODO: add proper error channels for the return value.
fun MathExpression.evaluateAsNumericExpression(): Real? = evaluate()

fun MathExpression.evaluate(): Real? {
  return when (expressionTypeCase) {
    CONSTANT -> constant
    VARIABLE -> null // Variables not supported in numeric expressions.
    BINARY_OPERATION -> binaryOperation.evaluate()
    UNARY_OPERATION -> unaryOperation.evaluate()
    FUNCTION_CALL -> functionCall.evaluate()
    GROUP -> group.evaluate()
    EXPRESSIONTYPE_NOT_SET, null -> null
  }
}

private fun MathBinaryOperation.evaluate(): Real? {
  return when (operator) {
    BinaryOperator.ADD ->
      rightOperand.evaluate()?.let { leftOperand.evaluate()?.plus(it) }
    BinaryOperator.SUBTRACT ->
      rightOperand.evaluate()?.let { leftOperand.evaluate()?.minus(it) }
    BinaryOperator.MULTIPLY ->
      rightOperand.evaluate()?.let { leftOperand.evaluate()?.times(it) }
    BinaryOperator.DIVIDE ->
      rightOperand.evaluate()?.let { leftOperand.evaluate()?.div(it) }
    BinaryOperator.EXPONENTIATE ->
      rightOperand.evaluate()?.let { leftOperand.evaluate()?.pow(it) }
    BinaryOperator.OPERATOR_UNSPECIFIED,
    BinaryOperator.UNRECOGNIZED,
    null -> null
  }
}

private fun MathUnaryOperation.evaluate(): Real? {
  return when (operator) {
    UnaryOperator.NEGATE -> operand.evaluate()?.let { -it }
    UnaryOperator.POSITIVE -> operand.evaluate() // '+2' is the same as just '2'.
    UnaryOperator.OPERATOR_UNSPECIFIED,
    UnaryOperator.UNRECOGNIZED,
    null -> null
  }
}

private fun MathFunctionCall.evaluate(): Real? {
  return when (functionType) {
    FunctionType.SQUARE_ROOT -> argument.evaluate()?.let { sqrt(it) }
    FunctionType.FUNCTION_UNSPECIFIED,
    FunctionType.UNRECOGNIZED,
    null -> null
  }
}

// Also: consider how to perform expression analysis for non-polynomial trees

fun MathExpression.toPolynomial(): Polynomial? {
  // Constructing a polynomial more or less requires:
  // 1. Collecting all variables (these will either be part of exponent expressions if they have a
  // power, part of a multiplication expression with a constant/evaluable constant directly or once
  // removed, or part of another expression) and evaluating them into terms.
  // 2. Collecting all non-variable values as top-level constant terms.
  // Note that polynomials are always representable via summation, so multiplication, division, and
  // subtraction must be partially evaluated and eliminated (if they can't be eliminated then the
  // expression is not polynomial or requires a more complex algorithm for reduction such as
  // polynomial long division).

  // ----- Algo probably requires additional data structure component since it's changing the tree piece-by-piece.
  // Consider having two versions of the conversion: one for rigid polynomials and another for forcing expressions into a polynomial.

  // First thoughts on a possible algorithm:
  // 1. Find all variable expressions, then go up 1 node (short-circuit: if no parent, it's a polynomial with 1 variable term)
  // 2. For each variable:
  //   a. If the parent is a multiplication expression, try to reduce the other term to a constant (this would be the term of the variable). Remove the multiplication term.
  //   b. If the parent is an exponent, try to reduce the right-hand side. This would become the variable's power. Remove the exponent term.
  //   c. If the parent is a unary operation, in-line that.
  // 3. Repeat (2) until variables become irreducible.
  // 4. Enumerate all exponents, multiplications, and divisions: reduce each at its parent to a constant.
  // 5. Replace remaining subtractions with additions + unary operators, then reduce all unary operations to constants.
  // 6. Check: there should be no remaining exponents, multiplications, divisions, subtractions, or unary operations (only addition should remain). If there are, fail: this isn't a polynomial or it isn't one that we support.
  // 7. Traverse the tree and convert each addition operand into a term and construct the final polynomial.
  // 8. Optional: further reduce the polynomial and/or convert to general form.

  // Consider revising the above to recursively find nested polynomials and "build them up". This
  // will allow us to detect each of the pathological cases that can't be handled by the above, plus
  // trivial cases the are handled by the above:
  // 1. Top-level polynomial / polynomials being added / polynomials being subtracted (should be handled by the above algo)
  // 2. Polynomial being divided by another polynomial (we should just fail here--it's quite complex to solve this)
  // 3. Polynomial raised by a constant positive whole number power (e.g. binomial); any other exponent isn't a polynomial (including a polynomial exp)
  // 4. Polynomials multiplied by each other (requires expanding like #3, probably via matrix multiplication
  // 5. Combinations of 1-4 (which requires recursion to find a complete solution for)

  // Final algorithm (non-simplified):
  // 1. Copy the tree so it can be augmented as nodes(expression | polynomial | constant)
  // 2. Replace all variable expressions with polynomials
  // 3. Depth-first evaluate the entire graph (results are polynomials OR concatenation). If any fails, this is not a polynomial or is unsupported. Specifics:
  //   a. Unary (apply to coefficients of the terms)
  //   b. Exponents (right-hand side must be reducible to constant -> calculate the power); may require expansion (e.g. for binomials)
  //   c. Subtraction (replace with addition & negate the coefficient of the polynomial terms)
  //   d. Division (right-hand side must be reducible to constant -> apply to the term, e.g. for x/4)
  //   e. Multiplication (for one side constant, apply to coefficients otherwise perform polynomial multiplication)
  //   f. Addition (treat constants as constant terms & concatenate term lists to compute new polynomial)
  // 4. Collect the final polynomial as the result. Early exiting indicates a non-polynomial.
  return stripGroups().replaceSquareRoots()
    .reduceToPolynomial()
    ?.removeUnnecessaryVariables()
    ?.sort()
}

private fun Polynomial.sort() = Polynomial.newBuilder().apply {
  addAllTerm(this@sort.termList.sortedWith(POLYNOMIAL_TERM_COMPARATOR))
}.build()

fun Polynomial.isUnivariate(): Boolean = getUniqueVariableCount() == 1

fun Polynomial.isMultivariate(): Boolean = getUniqueVariableCount() > 1

private fun Polynomial.getUniqueVariableCount(): Int {
  return termList.flatMap(Term::getVariableList).map(Variable::getName).toSet().size
}

fun Polynomial.toPlainText(): String {
  return termList.map { it.toPlainText() }.reduce { acc, termAnswerStr ->
    if (termAnswerStr.startsWith("-")) {
      "$acc - ${termAnswerStr.drop(1)}"
    } else "$acc + $termAnswerStr"
  }
}

private fun Term.toPlainText(): String {
  val productValues = mutableListOf<String>()

  // Include the coefficient if there is one (coefficients of 1 are ignored only if there are
  // variables present).
  productValues += when {
    variableList.isEmpty() || !abs(coefficient).isApproximatelyEqualTo(1.0) -> when {
      coefficient.isRational() && variableList.isNotEmpty() -> "(${coefficient.toPlainText()})"
      else -> coefficient.toPlainText()
    }
    coefficient.isNegative() -> "-"
    else -> ""
  }

  // Include any present variables.
  productValues += variableList.map(Variable::toPlainText)

  // Take the product of all relevant values of the term.
  return productValues.joinToString(separator = "")
}

private fun Variable.toPlainText(): String {
  return if (power > 1) "$name^$power" else name
}

private fun Real.toPlainText(): String = when (realTypeCase) {
  // Note that the rational part is first converted to an improper fraction since mixed fractions
  // can't be expressed as a single coefficient in typical polynomial syntax).
  RATIONAL -> rational.toImproperForm().toAnswerString()
  IRRATIONAL -> irrational.toPlainString()
  INTEGER -> integer.toString()
  REALTYPE_NOT_SET, null -> ""
}

private fun Real.toPlainString(): String = when (realTypeCase) {
  RATIONAL -> rational.toDouble().toPlainString()
  IRRATIONAL -> irrational.toPlainString()
  INTEGER -> integer.toString()
  REALTYPE_NOT_SET, null -> ""
}

private fun MathExpression.reduceToPolynomial(): Polynomial? {
  return when (expressionTypeCase) {
    CONSTANT -> createConstantPolynomial(constant)
    VARIABLE -> createSingleVariablePolynomial(variable)
    UNARY_OPERATION -> unaryOperation.reduceToPolynomial()
    BINARY_OPERATION -> binaryOperation.reduceToPolynomial()
    // Both functions & groups should be removed ahead of polynomial reduction.
    FUNCTION_CALL, GROUP, EXPRESSIONTYPE_NOT_SET, null -> null
  }
}

private fun MathUnaryOperation.reduceToPolynomial(): Polynomial? {
  return when (operator) {
    UnaryOperator.NEGATE -> -(operand.reduceToPolynomial() ?: return null)
    UnaryOperator.POSITIVE -> operand.reduceToPolynomial() // Positive unary changes nothing.
    UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
  }
}

private fun MathBinaryOperation.reduceToPolynomial(): Polynomial? {
  val leftPolynomial = leftOperand.reduceToPolynomial() ?: return null
  val rightPolynomial = rightOperand.reduceToPolynomial() ?: return null
  return when (operator) {
    BinaryOperator.ADD -> leftPolynomial + rightPolynomial
    BinaryOperator.SUBTRACT -> leftPolynomial - rightPolynomial
    BinaryOperator.MULTIPLY -> leftPolynomial * rightPolynomial
    BinaryOperator.DIVIDE -> leftPolynomial / rightPolynomial
    BinaryOperator.EXPONENTIATE -> leftPolynomial.pow(rightPolynomial)
    BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
  }
}

/** Returns whether this polynomial is a constant-only polynomial (contains no variables). */
fun Polynomial.isConstant(): Boolean = termCount == 1 && getTerm(0).variableCount == 0

/**
 * Returns the first term coefficient from this polynomial. This corresponds to the whole value of
 * the polynomial iff isConstant() returns true, otherwise this value isn't useful.
 *
 * Note that this function can throw if the polynomial is empty (so isConstant() should always be
 * checked first).
 */
fun Polynomial.getConstant(): Real = getTerm(0).coefficient

private operator fun Polynomial.unaryMinus(): Polynomial {
  // Negating a polynomial just requires flipping the signs on all coefficients.
  return toBuilder()
    .clearTerm()
    .addAllTerm(termList.map { it.toBuilder().setCoefficient(-it.coefficient).build() })
    .build()
}

// TODO: extract the filtering done during addition & also do it at the end in case initial polynomials are tried (like x^0, or 0y). Add tests for these cases.
private operator fun Polynomial.plus(rhs: Polynomial): Polynomial {
  // Adding two polynomials just requires combining their terms lists (taking into account combining
  // common terms).
  return Polynomial.newBuilder().apply {
    addAllTerm(this@plus.termList + rhs.termList)
  }.build().combineLikeTerms().removeUnnecessaryVariables()
}

private operator fun Polynomial.minus(rhs: Polynomial): Polynomial {
  // a - b = a + -b
  return this + -rhs
}

private operator fun Polynomial.times(rhs: Polynomial): Polynomial {
  // Polynomial multiplication is simply multiplying each term in one by each term in the other.
  val crossMultipliedTerms = termList.flatMap { leftTerm ->
    rhs.termList.map { rightTerm -> leftTerm * rightTerm }
  }

  // Treat each multiplied term as a unique polynomial, then add them together (so that like terms
  // can be properly combined).
  return crossMultipliedTerms.map { createSingleTermPolynomial(it) }.reduce(Polynomial::plus)
}

private operator fun Term.times(rhs: Term): Term {
  // The coefficients are always multiplied.
  val combinedCoefficient = coefficient * rhs.coefficient

  // Next, create a combined list of new variables.
  val combinedVariables = variableList + rhs.variableList

  // Simplify the variables by combining the exponents of like variables. Start with a map of 0
  // powers, then add in the powers of each variable and collect the final list of unique terms.
  val variableNamesMap = mutableMapOf<String, Int>()
  combinedVariables.forEach {
    variableNamesMap.compute(it.name) { _, power ->
      if (power != null) power + it.power else it.power
    }
  }
  val newVariableList = variableNamesMap.map { (name, power) ->
    Variable.newBuilder().setName(name).setPower(power).build()
  }

  return Term.newBuilder()
    .setCoefficient(combinedCoefficient)
    .addAllVariable(newVariableList)
    .build()
}

private operator fun Polynomial.div(rhs: Polynomial): Polynomial? {
  // TODO: ensure this properly computes distributions for fractions, e.g. ((x+3)/2) should become
  //  (1/2)x + (3/2).
  // See https://en.wikipedia.org/wiki/Polynomial_long_division#Pseudocode for reference.
  if (rhs.isApproximatelyZero()) {
    return null // Dividing by zero is invalid and thus cannot yield a polynomial.
  }

  var quotient = createConstantPolynomial(createCoefficientValueOfZero())
  var remainder = this
  val leadingDivisorTerm = rhs.getLeadingTerm()
  val divisorVariable = leadingDivisorTerm.highestDegreeVariable()
  val divisorVariableName = divisorVariable?.name
  val divisorDegree = leadingDivisorTerm.highestDegree()
  while (!remainder.isApproximatelyZero() && remainder.getDegree() >= divisorDegree) {
    // Attempt to divide the leading terms (this may fail). Note that the leading term should always
    // be based on the divisor variable being used (otherwise subsequent division steps will be
    // inconsistent and potentially fail to resolve).
    val newTerm =
      remainder.getLeadingTerm(matchedVariable = divisorVariableName) / leadingDivisorTerm
        ?: return null
    quotient += newTerm.toPolynomial()
    remainder -= newTerm.toPolynomial() * rhs
  }
  return when {
    remainder.isApproximatelyZero() -> quotient // Exact division (i.e. with no remainder).
    remainder.isConstant() && rhs.isConstant() -> {
      // Remainder is a constant term.
      val remainingTerm = remainder.getConstant() / rhs.getConstant()
      quotient + createConstantPolynomial(remainingTerm)
    }
    else -> null // Remainder is a polynomial, so the division failed.
  }
}

private fun Term.toPolynomial(): Polynomial {
  return Polynomial.newBuilder().addTerm(this).build()
}

private operator fun Term.div(rhs: Term): Term? {
  val dividendPowerMap = variableList.toPowerMap()
  val divisorPowerMap = rhs.variableList.toPowerMap()

  // If any variables are present in the divisor and not the dividend, this division won't work
  // effectively.
  if (!dividendPowerMap.keys.containsAll(divisorPowerMap.keys)) return null

  // Division is simply subtracting the powers of terms in the divisor from those in the dividend.
  val quotientPowerMap = dividendPowerMap.mapValues { (name, power) ->
    power - divisorPowerMap.getOrDefault(name, defaultValue = 0)
  }

  // If there are any negative powers, the divisor can't effectively divide this value.
  if (quotientPowerMap.values.any { it < 0 }) return null

  // Remove variables with powers of 0 since those have been fully divided. Also, divide the
  // coefficients to finish the division.
  return Term.newBuilder()
    .setCoefficient(coefficient / rhs.coefficient)
    .addAllVariable(quotientPowerMap.filter { (_, power) -> power > 0 }.toVariableList())
    .build()
}

private fun Polynomial.combineLikeTerms(): Polynomial {
  // The following algorithm is expected to grow in space by O(N*M) and in time by O(N*m*log(m))
  // where N is the total number  of terms, M is the total number of variables, and m is the largest
  // single count of variables among all terms (this is assuming constant-time insertion for the
  // underlying hashtable).
  val newTerms = termList.groupBy {
    it.variableList.sortedWith(POLYNOMIAL_VARIABLE_COMPARATOR)
  }.mapValues { (_, coefficientTerms) ->
    coefficientTerms.map { it.coefficient }
  }.mapNotNull { (variables, coefficients) ->
    // Combine like terms by summing their coefficients.
    val newCoefficient = coefficients.reduce(Real::plus)
    return@mapNotNull if (!newCoefficient.isApproximatelyZero()) {
      Term.newBuilder().apply {
        coefficient = newCoefficient

        // Remove variables with zero powers (since they evaluate to '1').
        addAllVariable(variables.filter { variable -> variable.power != 0 })
      }.build()
    } else null // Zero terms should be removed.
  }
  return Polynomial.newBuilder().apply {
    addAllTerm(newTerms)
  }.build().ensureAtLeastConstant()
}

private fun Polynomial.removeUnnecessaryVariables(): Polynomial {
  return Polynomial.newBuilder().apply {
    addAllTerm(this@removeUnnecessaryVariables.termList.filter { term ->
      !term.coefficient.isApproximatelyZero()
    })
  }.build().ensureAtLeastConstant()
}

private fun Polynomial.ensureAtLeastConstant(): Polynomial {
  return if (termCount == 0) {
    Polynomial.newBuilder().apply {
      addTerm(createZeroTerm())
    }.build()
  } else this
}

private fun List<Variable>.toPowerMap(): Map<String, Int> {
  return associateBy({ it.name }, { it.power })
}

private fun Map<String, Int>.toVariableList(): List<Variable> {
  return map { (name, power) -> Variable.newBuilder().setName(name).setPower(power).build() }
}

private fun Polynomial.getLeadingTerm(matchedVariable: String? = null): Term {
  // Return the leading term. Reference: https://undergroundmathematics.org/glossary/leading-term.
  return termList.filter { term ->
    matchedVariable?.let { variableName ->
      term.variableList.any { it.name == variableName }
    } ?: true
  }.reduce { maxTerm, term ->
    val maxTermDegree = maxTerm.highestDegree()
    val termDegree = term.highestDegree()
    return@reduce if (termDegree > maxTermDegree) term else maxTerm
  }
}

// Return the highest power to represent the degree of the polynomial. Reference:
// https://www.varsitytutors.com/algebra_1-help/how-to-find-the-degree-of-a-polynomial.
private fun Polynomial.getDegree(): Int = getLeadingTerm().highestDegree()

private fun Term.highestDegreeVariable(): Variable? = variableList.maxByOrNull(Variable::getPower)

private fun Term.highestDegree(): Int = highestDegreeVariable()?.power ?: 0

private fun Term.pow(rational: Fraction): Term? {
  // Raising an exponent by an exponent just requires multiplying the two together.
  val newVariablePowers = variableList.map { variable ->
    variable.power.toWholeNumberFraction() * rational
  }

  // If any powers are not whole numbers then the rational is likely representing a root and the
  // term in question is not rootable to that degree.
  if (newVariablePowers.any { !it.isOnlyWholeNumber() }) return null

  return Term.newBuilder().apply {
    coefficient = this@pow.coefficient
    addAllVariable(
      this@pow.variableList.zip(newVariablePowers).map { (variable, newPower) ->
        variable.toBuilder().apply {
          power = newPower.toWholeNumber()
        }.build()
      }
    )
  }.build()
}

private fun Polynomial.isApproximatelyZero(): Boolean =
  termList.all { it.coefficient.isApproximatelyZero() } // Zero polynomials only have 0 coefs.

private fun Polynomial.pow(exp: Int): Polynomial {
  // Anything raised to the power of 0 is 1.
  if (exp == 0) return createConstantPolynomial(createCoefficientValueOfOne())
  if (exp == 1) return this
  var newValue = this
  for (i in 1 until exp) newValue *= this
  return newValue
}

private fun Polynomial.pow(rational: Fraction): Polynomial? {
  // Polynomials with addition require factoring.
  return if (isSingleTerm()) {
    termList.first().pow(rational)?.toPolynomial()
  } else null
}

private fun Polynomial.pow(exp: Real): Polynomial? {
  val shouldBeInverted = exp.isNegative()
  val positivePower = if (shouldBeInverted) -exp else exp
  val exponentiation = when {
    // Constant polynomials can be raised by any constant.
    isConstant() -> createConstantPolynomial(getConstant().pow(positivePower))

    // Polynomials can only be raised to positive integers (or zero).
    exp.isWholeNumber() -> exp.asWholeNumber()?.let { pow(it) }

    // Polynomials can potentially be raised by a fractional power.
    exp.isRational() -> pow(exp.rational)

    // All other cases require factoring will definitely not compute to polynomials (such as
    // irrational exponents).
    else -> null
  }
  return if (shouldBeInverted) {
    val onePolynomial = createConstantPolynomial(createCoefficientValueOfOne())
    // Note that this division is guaranteed to fail if the exponentiation result is a polynomial.
    // Future implementations may leverage root-finding algorithms to factor for integer inverse
    // powers (such as square root, cubic root, etc.). Non-integer inverse powers will require
    // sampling.
    exponentiation?.let { onePolynomial / it }
  } else exponentiation
}

private fun Polynomial.pow(exp: Polynomial): Polynomial? {
  // Polynomial exponentiation is only supported if the right side is a constant polynomial,
  // otherwise the result cannot be a polynomial (though could still be compared to another
  // expression by utilizing sampling techniques).
  return if (exp.isConstant()) pow(exp.getConstant()) else null
}

private fun Polynomial.isSingleTerm(): Boolean = termList.size == 1

//private fun MathExpression.toTreeNode(): ExpressionTreeNode {
//  return when (expressionTypeCase) {
//    CONSTANT -> ExpressionTreeNode.ConstantNode(constant)
//    VARIABLE -> ExpressionTreeNode.PolynomialNode(createSingleTermPolynomial(variable))
//    UNARY_OPERATION -> ExpressionTreeNode.ExpressionNode(this, unaryOperation.collectChildren())
//    BINARY_OPERATION -> ExpressionTreeNode.ExpressionNode(this, binaryOperation.collectChildren())
//    else -> ExpressionTreeNode.ExpressionNode(this, mutableListOf())
//  }
//}
//
//private fun MathUnaryOperation.collectChildren(): MutableList<ExpressionTreeNode> {
//  return mutableListOf(operand.toTreeNode())
//}
//
//private fun MathBinaryOperation.collectChildren(): MutableList<ExpressionTreeNode> {
//  return mutableListOf(leftOperand.toTreeNode(), rightOperand.toTreeNode())
//}

private fun createSingleVariablePolynomial(variableName: String): Polynomial {
  return createSingleTermPolynomial(
    Term.newBuilder().apply {
      coefficient = createCoefficientValueOfOne()
      addVariable(Variable.newBuilder().apply {
        name = variableName
        power = 1
      }.build())
    }.build()
  )
}

private fun createConstantPolynomial(constant: Real) =
  createSingleTermPolynomial(Term.newBuilder().setCoefficient(constant).build())

private fun createSingleTermPolynomial(term: Term) =
  Polynomial.newBuilder().apply { addTerm(term) }.build()

private fun createCoefficientValueOf(value: Int) = Real.newBuilder().apply {
  integer = value
}.build()

private fun createCoefficientValueOfZero(): Real = createCoefficientValueOf(value = 0)

private fun createCoefficientValueOfOne(): Real = createCoefficientValueOf(value = 1)

private fun createZeroTerm() = Term.newBuilder().apply {
  coefficient = createCoefficientValueOfZero()
}.build()

private sealed class ExpressionTreeNode {
  data class ExpressionNode(
    val mathExpression: MathExpression,
    val children: MutableList<ExpressionTreeNode>
  ) : ExpressionTreeNode()

  data class PolynomialNode(val polynomial: Polynomial) : ExpressionTreeNode()

  data class ConstantNode(val constant: Real) : ExpressionTreeNode()
}

// TODO: add a faster isReducibleToConstant recursive function since this is used a lot.

// private fun MathExpression.reduceToConstant(): MathExpression? {
//  return when (expressionTypeCase) {
//    CONSTANT -> this
//    VARIABLE -> null
//    UNARY_OPERATION -> unaryOperation.reduceToConstant()
//    BINARY_OPERATION -> binaryOperation.reduceToConstant()
//    else -> null
//  }
// }

// private fun MathUnaryOperation.reduceToConstant(): MathExpression? {
//  return when (operator) {
//    MathUnaryOperation.Operator.NEGATE -> operand.reduceToConstant()?.transformConstant { -it }
//    else -> null
//  }
// }

// private fun MathBinaryOperation.reduceToConstant(): MathExpression? {
//  val leftConstant = leftOperand.reduceToConstant()?.constant ?: return null
//  val rightConstant = rightOperand.reduceToConstant()?.constant ?: return null
//  return when (operator) {
//    MathBinaryOperation.Operator.ADD -> fromConstant(leftConstant + rightConstant)
//    MathBinaryOperation.Operator.SUBTRACT -> fromConstant(leftConstant - rightConstant)
//    MathBinaryOperation.Operator.MULTIPLY -> fromConstant(leftConstant * rightConstant)
//    MathBinaryOperation.Operator.DIVIDE -> fromConstant(leftConstant / rightConstant)
//    MathBinaryOperation.Operator.EXPONENTIATE -> fromConstant(leftConstant.pow(rightConstant))
//    else -> null
//  }
// }

private fun MathExpression.transformConstant(
  transform: (Real.Builder) -> Real.Builder
): MathExpression {
  return toBuilder().setConstant(transform(constant.toBuilder())).build()
}

private fun fromConstant(real: Real): MathExpression {
  return MathExpression.newBuilder().setConstant(real).build()
}

private fun Real.isApproximatelyEqualTo(value: Double): Boolean {
  return toDouble().approximatelyEquals(value)
}

private fun Real.isApproximatelyZero(): Boolean = isApproximatelyEqualTo(0.0)

fun Real.toDouble(): Double {
  return when (realTypeCase) {
    RATIONAL -> rational.toDouble()
    INTEGER -> integer.toDouble()
    IRRATIONAL -> irrational
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

private fun Real.recompute(transform: (Real.Builder) -> Real.Builder): Real {
  return transform(toBuilder().clearRational().clearIrrational().clearInteger()).build()
}

private fun combine(
  lhs: Real,
  rhs: Real,
  leftRationalRightRationalOp: (Fraction, Fraction) -> Fraction,
  leftRationalRightIrrationalOp: (Fraction, Double) -> Double,
  leftRationalRightIntegerOp: (Fraction, Int) -> Fraction,
  leftIrrationalRightRationalOp: (Double, Fraction) -> Double,
  leftIrrationalRightIrrationalOp: (Double, Double) -> Double,
  leftIrrationalRightIntegerOp: (Double, Int) -> Double,
  leftIntegerRightRationalOp: (Int, Fraction) -> Fraction,
  leftIntegerRightIrrationalOp: (Int, Double) -> Double,
  leftIntegerRightIntegerOp: (Int, Int) -> Real,
): Real {
  return when (lhs.realTypeCase) {
    RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute { it.setRational(leftRationalRightRationalOp(lhs.rational, rhs.rational)) }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftRationalRightIrrationalOp(lhs.rational, rhs.irrational))
          }
        INTEGER ->
          lhs.recompute { it.setRational(leftRationalRightIntegerOp(lhs.rational, rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightRationalOp(lhs.irrational, rhs.rational))
          }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightIrrationalOp(lhs.irrational, rhs.irrational))
          }
        INTEGER ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightIntegerOp(lhs.irrational, rhs.integer))
          }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    INTEGER -> {
      // Left-hand side is an integer.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute { it.setRational(leftIntegerRightRationalOp(lhs.integer, rhs.rational)) }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIntegerRightIrrationalOp(lhs.integer, rhs.irrational))
          }
        INTEGER -> leftIntegerRightIntegerOp(lhs.integer, rhs.integer)
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $lhs.")
  }
}

private fun Real.pow(rhs: Real): Real {
  // Powers can really only be effectively done via floats or whole-number only fractions.
  return when (realTypeCase) {
    RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        RATIONAL -> recompute {
          if (rhs.rational.isOnlyWholeNumber()) {
            // The fraction can be retained.
            it.setRational(rational.pow(rhs.rational.wholeNumber))
          } else {
            // The fraction can't realistically be retained since it's being raised to an actual
            // fraction, resulting in an irrational number.
            it.setIrrational(rational.toDouble().pow(rhs.rational.toDouble()))
          }
        }
        IRRATIONAL -> recompute { it.setIrrational(rational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setRational(rational.pow(rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        RATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.rational)) }
        IRRATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setIrrational(irrational.pow(rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    INTEGER -> {
      // Left-hand side is an integer.
      when (rhs.realTypeCase) {
        RATIONAL -> {
          if (rhs.rational.isOnlyWholeNumber()) {
            // Whole number-only fractions are effectively just int^int.
            integer.pow(rhs.rational.wholeNumber)
          } else {
            // Otherwise, raising by a fraction will result in an irrational number.
            recompute { it.setIrrational(integer.toDouble().pow(rhs.rational.toDouble())) }
          }
        }
        IRRATIONAL -> recompute { it.setIrrational(integer.toDouble().pow(rhs.irrational)) }
        INTEGER -> integer.pow(rhs.integer)
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

private operator fun Real.unaryMinus(): Real {
  return when (realTypeCase) {
    RATIONAL -> recompute { it.setRational(-rational) }
    IRRATIONAL -> recompute { it.setIrrational(-irrational) }
    INTEGER -> recompute { it.setInteger(-integer) }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

private operator fun Real.plus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::plus, Fraction::plus, Fraction::plus, Double::plus, Double::plus,
    Double::plus, Int::plus, Int::plus, Int::add
  )
}

private operator fun Real.minus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::minus, Fraction::minus, Fraction::minus, Double::minus, Double::minus,
    Double::minus, Int::minus, Int::minus, Int::subtract
  )
}

private operator fun Real.times(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::times, Fraction::times, Fraction::times, Double::times, Double::times,
    Double::times, Int::times, Int::times, Int::multiply
  )
}

private operator fun Real.div(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::div, Fraction::div, Fraction::div, Double::div, Double::div, Double::div,
    Int::div, Int::div, Int::divide
  )
}

private fun sqrt(real: Real): Real {
  return when (real.realTypeCase) {
    RATIONAL -> sqrt(real.rational)
    IRRATIONAL -> real.recompute { it.setIrrational(sqrt(real.irrational)) }
    INTEGER -> sqrt(real.integer)
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $real.")
  }
}

private fun abs(real: Real): Real = if (real.isNegative()) -real else real

private fun Real.isInteger(): Boolean = realTypeCase == INTEGER

private fun Real.isNegative(): Boolean = when (realTypeCase) {
  RATIONAL -> rational.isNegative
  IRRATIONAL -> irrational < 0
  INTEGER -> integer < 0
  REALTYPE_NOT_SET, null ->  throw Exception("Invalid real: $this.")
}

private fun Real.asWholeNumber(): Int? {
  return when (realTypeCase) {
    RATIONAL -> if (rational.isOnlyWholeNumber()) rational.toWholeNumber() else null
    INTEGER -> integer
    IRRATIONAL -> null
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

private fun Real.isWholeNumber(): Boolean {
  return when (realTypeCase) {
    RATIONAL -> rational.isOnlyWholeNumber()
    INTEGER -> true
    IRRATIONAL, REALTYPE_NOT_SET, null -> false
  }
}

private fun Real.isRational(): Boolean = realTypeCase == RATIONAL

private fun Double.pow(rhs: Fraction): Double = this.pow(rhs.toDouble())
private fun Fraction.pow(rhs: Double): Double = toDouble().pow(rhs)
private operator fun Double.plus(rhs: Fraction): Double = this + rhs.toFloat()
private operator fun Fraction.plus(rhs: Double): Double = toFloat() + rhs
private operator fun Fraction.plus(rhs: Int): Fraction = this + rhs.toWholeNumberFraction()
private operator fun Int.plus(rhs: Fraction): Fraction = toWholeNumberFraction() + rhs
private operator fun Double.minus(rhs: Fraction): Double = this - rhs.toFloat()
private operator fun Fraction.minus(rhs: Double): Double = toFloat() - rhs
private operator fun Fraction.minus(rhs: Int): Fraction = this - rhs.toWholeNumberFraction()
private operator fun Int.minus(rhs: Fraction): Fraction = toWholeNumberFraction() - rhs
private operator fun Double.times(rhs: Fraction): Double = this * rhs.toFloat()
private operator fun Fraction.times(rhs: Double): Double = toFloat() * rhs
private operator fun Fraction.times(rhs: Int): Fraction = this * rhs.toWholeNumberFraction()
private operator fun Int.times(rhs: Fraction): Fraction = toWholeNumberFraction() * rhs
private operator fun Double.div(rhs: Fraction): Double = this / rhs.toFloat()
private operator fun Fraction.div(rhs: Double): Double = toFloat() / rhs
private operator fun Fraction.div(rhs: Int): Fraction = this / rhs.toWholeNumberFraction()
private operator fun Int.div(rhs: Fraction): Fraction = toWholeNumberFraction() / rhs

private fun Int.add(rhs: Int): Real = Real.newBuilder().apply { integer = this@add + rhs }.build()
private fun Int.subtract(rhs: Int): Real = Real.newBuilder().apply {
  integer = this@subtract - rhs
}.build()
private fun Int.multiply(rhs: Int): Real = Real.newBuilder().apply {
  integer = this@multiply * rhs
}.build()
private fun Int.divide(rhs: Int): Real = Real.newBuilder().apply {
  // If rhs divides this integer, retain the integer.
  val lhs = this@divide
  if ((lhs % rhs) == 0) {
    integer = lhs / rhs
  } else {
    // Otherwise, keep precision by turning the division into a fraction.
    rational = Fraction.newBuilder().apply {
      isNegative = (lhs < 0) xor (rhs < 0)
      numerator = abs(lhs)
      denominator = abs(rhs)
    }.build()
  }
}.build()

private fun Fraction.pow(exp: Int): Fraction {
  return when {
    exp == 0 -> Fraction.newBuilder().setWholeNumber(1).setDenominator(1).build()
    exp == 1 -> this
    // x^-2 == 1/(x^2).
    exp < 1 -> pow(-exp).toInvertedImproperForm().toProperForm()
    else -> { // i > 1
      var newValue = this
      for (i in 1 until exp) newValue *= this
      return newValue
    }
  }
}

private fun sqrt(fraction: Fraction): Real {
  val improper = fraction.toImproperForm()

  // Attempt to take the root of the fraction's numerator & denominator.
  val numeratorRoot = sqrt(improper.numerator)
  val denominatorRoot = sqrt(improper.denominator)

  // If both values stayed as integers, the original fraction can be retained. Otherwise, the
  // fraction must be evaluated by performing a division.
  return Real.newBuilder().apply {
    if (numeratorRoot.realTypeCase == denominatorRoot.realTypeCase && numeratorRoot.isInteger()) {
      val rootedFraction = Fraction.newBuilder().apply {
        isNegative = improper.isNegative
        numerator = numeratorRoot.integer
        denominator = denominatorRoot.integer
      }.build().toProperForm()
      if (rootedFraction.isOnlyWholeNumber()) {
        // If the fractional form doesn't need to be kept, remove it.
        integer = rootedFraction.toWholeNumber()
      } else {
        rational = rootedFraction
      }
    } else {
      irrational = numeratorRoot.toDouble()
    }
  }.build()
}

private fun Int.pow(exp: Int): Real {
  return when {
    exp == 0 -> Real.newBuilder().apply { integer = 0 }.build()
    exp == 1 -> Real.newBuilder().apply { integer = this@pow }.build()
    exp < 0 -> Real.newBuilder().apply { rational = toWholeNumberFraction().pow(exp) }.build()
    else -> {
      // exp > 1
      var computed = this
      for (i in 0 until exp - 1) computed *= this
      Real.newBuilder().apply { integer = computed }.build()
    }
  }
}

private fun sqrt(int: Int): Real {
  // First, check if the integer is a square. Reference for possible methods:
  // https://www.researchgate.net/post/How-to-decide-if-a-given-number-will-have-integer-square-root-or-not.
  var potentialRoot = 2
  while ((potentialRoot * potentialRoot) < int) {
    potentialRoot++
  }
  if (potentialRoot * potentialRoot == int) {
    // There's an exact integer representation of the root.
    return Real.newBuilder().apply {
      integer = potentialRoot
    }.build()
  }

  // Otherwise, compute the irrational square root.
  return Real.newBuilder().apply {
    irrational = sqrt(int.toDouble())
  }.build()
}

private fun Double.toPlainString(): String = toBigDecimal().toPlainString()
