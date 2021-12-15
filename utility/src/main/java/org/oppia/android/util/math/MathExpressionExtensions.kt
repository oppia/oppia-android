package org.oppia.android.util.math

import java.text.NumberFormat
import java.util.Locale
import java.util.SortedSet
import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathUnaryOperation
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
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET
import org.oppia.android.util.math.ExpressionToComparableOperationListConverter.Companion.toComparable
import org.oppia.android.util.math.ExpressionToLatexConverter.Companion.convertToLatex
import org.oppia.android.util.math.NumericExpressionEvaluator.Companion.evaluate

// TODO: split up this extensions file into multiple, clean it up, reorganize, and add tests.

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

fun MathExpression.toComparableOperationList(): ComparableOperationList =
  stripGroups().toComparable()

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

fun MathEquation.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

fun MathExpression.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

fun MathExpression.evaluateAsNumericExpression(): Real? = evaluate()

fun MathExpression.toPolynomial(): Polynomial? {
  // Polynomials are created by converting subsequent parts of a math expression to polynomials and
  // then combining them using math operations. In some cases (such as exponentiation and division),
  // this can fail (which indicates a non-polynomial expression). In other cases (such as square
  // roots), additional processing is needed. This method guarantees that there are no zero terms
  // returned, and that the returned polynomial's terms are deterministically arranged such that two
  // similar expressions only different by commutativity/term reordering will result in exactly the
  // same polynomial (i.e. Polynomial.isEqualTo method will return true when comparing the two).
  return stripGroups().replaceSquareRoots()
    .reduceToPolynomial()
    ?.removeUnnecessaryVariables()
    ?.sort()
}

private fun Polynomial.sort() = Polynomial.newBuilder().apply {
  addAllTerm(this@sort.termList.sortedWith(POLYNOMIAL_TERM_COMPARATOR))
}.build()

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

private operator fun Polynomial.unaryMinus(): Polynomial {
  // Negating a polynomial just requires flipping the signs on all coefficients.
  return toBuilder()
    .clearTerm()
    .addAllTerm(termList.map { it.toBuilder().setCoefficient(-it.coefficient).build() })
    .build()
}

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
    addAllTerm(
      this@removeUnnecessaryVariables.termList.filter { term ->
        !term.coefficient.isApproximatelyZero()
      }
    )
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

private fun createSingleVariablePolynomial(variableName: String): Polynomial {
  return createSingleTermPolynomial(
    Term.newBuilder().apply {
      coefficient = createCoefficientValueOfOne()
      addVariable(
        Variable.newBuilder().apply {
          name = variableName
          power = 1
        }.build()
      )
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

private fun Real.isApproximatelyZero(): Boolean = isApproximatelyEqualTo(0.0)

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
