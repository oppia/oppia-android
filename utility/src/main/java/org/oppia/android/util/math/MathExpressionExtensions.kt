package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import kotlin.math.pow

// TODO: split up this extensions file into multiple, clean it up, reorganize, and add tests.

// XXX: Polynomials can actually take lots of forms, so this conversion is intentionally limited
// based on intended use cases and can be extended in the future, as needed. For example, both
// x^(2^3) and ((x^2+2x+1)/(x+1)) won't be considered polynomials, but it can be seen that they are
// indeed after being simplified. This implementation doesn't yet support recognizing binomials
// (e.g. (x+1)^2) but needs to be updated to. The representation for Polynomial doesn't quite
// support much outside of general form.

// Consider expressions like x/2 (should be treated like (1/2)x).
// Consider: (1+2)*x or (1+2)x

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
  return reduceToPolynomial()
}

fun Polynomial.isUnivariate(): Boolean = getUniqueVariableCount() == 1

fun Polynomial.isMultivariate(): Boolean = getUniqueVariableCount() > 1

private fun Polynomial.getUniqueVariableCount(): Int {
  return termList.flatMap(Term::getVariableList).map(Variable::getName).toSet().size
}

fun Polynomial.toAnswerString(): String {
  return termList.joinToString(separator = " + ", transform = Term::toAnswerString)
}

private fun Term.toAnswerString(): String {
  val productValues = mutableListOf<String>()

  // Include the coefficient if there is one (coefficients of 1 are ignored only if there are
  // variables present).
  if (!coefficient.isApproximatelyEqualTo(1.0) || variableList.isEmpty()) {
    productValues += coefficient.toAnswerString()
  }

  // Include any present variables.
  productValues += variableList.map(Variable::toAnswerString)

  // Take the product of all relevant values of the term.
  return productValues.joinToString(separator = "*")
}

private fun Variable.toAnswerString(): String {
  return if (power > 1) "$name^$power" else name
}

private fun Real.toAnswerString(): String {
  // Note that the rational part is first converted to an improper fraction since mixed fractions
  // can't be expressed as a single coefficient in typical polynomial syntax).
  return if (hasRational()) rational.toImproperForm().toAnswerString() else irrational.toString()
}

private fun MathExpression.reduceToPolynomial(): Polynomial? {
  return when (expressionTypeCase) {
    CONSTANT -> createPolynomialFromConstant(constant)
    VARIABLE -> createSingleTermPolynomial(variable)
    UNARY_OPERATION -> unaryOperation.reduceToPolynomial()
    BINARY_OPERATION -> binaryOperation.reduceToPolynomial()
    else -> null
  }
}

private fun MathUnaryOperation.reduceToPolynomial(): Polynomial? {
  return when (operator) {
    MathUnaryOperation.Operator.NEGATION -> -(operand.reduceToPolynomial() ?: return null)
    else -> null
  }
}

private fun MathBinaryOperation.reduceToPolynomial(): Polynomial? {
  val leftPolynomial = leftOperand.reduceToPolynomial() ?: return null
  val rightPolynomial = rightOperand.reduceToPolynomial() ?: return null
  return when (operator) {
    MathBinaryOperation.Operator.ADD -> leftPolynomial + rightPolynomial
    MathBinaryOperation.Operator.SUBTRACT -> leftPolynomial - rightPolynomial
    MathBinaryOperation.Operator.MULTIPLY -> leftPolynomial * rightPolynomial
    MathBinaryOperation.Operator.DIVIDE -> leftPolynomial / rightPolynomial
    MathBinaryOperation.Operator.EXPONENTIATE -> leftPolynomial.pow(rightPolynomial)
    else -> null
  }
}

/** Returns whether this polynomial is a constant-only polynomial (contains no variables). */
private fun Polynomial.isConstant(): Boolean {
  return termCount == 1 && getTerm(0).variableCount == 0
}

/**
 * Returns the first term coefficient from this polynomial. This corresponds to the whole value of
 * the polynomial iff isConstant() returns true, otherwise this value isn't useful.
 *
 * Note that this function can throw if the polynomial is empty (so isConstant() should always be
 * checked first).
 */
private fun Polynomial.getConstant(): Real {
  return getTerm(0).coefficient
}

private operator fun Polynomial.unaryMinus(): Polynomial {
  // Negating a polynomial just requires flipping the signs on all coefficients.
  return toBuilder()
      .clearTerm()
      .addAllTerm(termList.map { it.toBuilder().setCoefficient(-it.coefficient).build() })
      .build()
}

private operator fun Polynomial.plus(rhs: Polynomial): Polynomial {
  // Adding two polynomials just requires combining their terms lists.
  return Polynomial.newBuilder().addAllTerm(termList).addAllTerm(rhs.termList).build()
}

private operator fun Polynomial.minus(rhs: Polynomial): Polynomial {
  // a - b = a + -b
  return this + -rhs
}

private operator fun Polynomial.times(rhs: Polynomial): Polynomial {
  // Polynomial multiplication is simply multiplying each term in one by each term in the other.
  // TODO: ensure this properly computes trivial cases like (x^2 becoming x-squared) or whether
  //  those cases need to be special cased.
  return Polynomial.newBuilder()
      .addAllTerm(termList.flatMap { leftTerm ->
        rhs.termList.map { rightTerm -> leftTerm * rightTerm }
      }).build()
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
    // TODO: test (x+2)/0
    return null // Dividing by zero is invalid and thus cannot yield a polynomial.
  }

  var quotient = createPolynomialFromConstant(createCoefficientValueOf(value = 0))
  var remainder = this
  val divisorDegree = rhs.getDegree()
  val leadingDivisorTerm = rhs.getLeadingTerm()
  while (!remainder.isApproximatelyZero() && remainder.getDegree() >= divisorDegree) {
    // Attempt to divide the leading terms (this may fail).
    val newTerm = remainder.getLeadingTerm() / leadingDivisorTerm ?: return null
    quotient += newTerm.toPolynomial()
    remainder -= newTerm.toPolynomial() * rhs
  }
  if (!remainder.isApproximatelyZero()) {
    // A non-zero remainder indicates the division was not "pure" which means the result is a
    // non-polynomial.
    return null
  }
  return quotient
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

private fun List<Variable>.toPowerMap(): Map<String, Int> {
  return associateBy({ it.name }, { it.power })
}

private fun Map<String, Int>.toVariableList(): List<Variable> {
  return map { (name, power) -> Variable.newBuilder().setName(name).setPower(power).build() }
}

private fun Polynomial.getLeadingTerm(): Term {
  // Return the leading term. Reference: https://undergroundmathematics.org/glossary/leading-term.
  return termList.reduce { maxTerm, term ->
    val maxTermDegree = maxTerm.highestDegree()
    val termDegree = term.highestDegree()
    return@reduce if (termDegree > maxTermDegree) term else maxTerm
  }
}

// Return the highest power to represent the degree of the polynomial. Reference:
// https://www.varsitytutors.com/algebra_1-help/how-to-find-the-degree-of-a-polynomial.
private fun Polynomial.getDegree(): Int = getLeadingTerm().highestDegree()

private fun Term.highestDegree(): Int {
  return variableList.map(Variable::getPower).max() ?: 0
}

private fun Polynomial.isApproximatelyZero(): Boolean {
  return isConstant() && getConstant().isApproximatelyZero()
}

private fun Polynomial.pow(exp: Int): Polynomial {
  // Anything raised to the power of 0 is 1.
  if (exp == 0) return createPolynomialFromConstant(createCoefficientValueOfOne())
  if (exp == 1) return this
  var newValue = this
  for (i in 1 until exp) newValue *= this
  return newValue
}

private fun Polynomial.pow(exp: Real): Polynomial? {
  // Polynomials can only be raised to positive integers (or zero).
  return if (exp.hasRational() && exp.rational.isOnlyWholeNumber() && !exp.rational.isNegative) {
    pow(exp.rational.wholeNumber)
  } else null
}

private fun Polynomial.pow(exp: Polynomial): Polynomial? {
  // Polynomial exponentiation is only supported if the right side is a constant polynomial,
  // otherwise the result cannot be a polynomial.
  return if (exp.isConstant()) pow(exp.getConstant()) else null
}

private fun MathExpression.toTreeNode(): ExpressionTreeNode {
  return when (expressionTypeCase) {
    CONSTANT -> ExpressionTreeNode.ConstantNode(constant)
    VARIABLE -> ExpressionTreeNode.PolynomialNode(createSingleTermPolynomial(variable))
    UNARY_OPERATION -> ExpressionTreeNode.ExpressionNode(this, unaryOperation.collectChildren())
    BINARY_OPERATION -> ExpressionTreeNode.ExpressionNode(this, binaryOperation.collectChildren())
    else -> ExpressionTreeNode.ExpressionNode(this, mutableListOf())
  }
}

private fun MathUnaryOperation.collectChildren(): MutableList<ExpressionTreeNode> {
  return mutableListOf(operand.toTreeNode())
}

private fun MathBinaryOperation.collectChildren(): MutableList<ExpressionTreeNode> {
  return mutableListOf(leftOperand.toTreeNode(), rightOperand.toTreeNode())
}

private fun createSingleTermPolynomial(variableName: String): Polynomial {
  return Polynomial.newBuilder()
      .addTerm(
          Term.newBuilder()
              .setCoefficient(createCoefficientValueOfOne())
              .addVariable(Variable.newBuilder().setName(variableName).setPower(1))
      ).build()
}

private fun createPolynomialFromConstant(constant: Real): Polynomial {
  return Polynomial.newBuilder()
      .addTerm(Term.newBuilder().setCoefficient(constant))
      .build()
}

private fun createCoefficientValueOf(value: Int): Real {
  return Real.newBuilder()
      .setRational(Fraction.newBuilder().setWholeNumber(value).setDenominator(1))
      .build()
}

private fun createCoefficientValueOfOne(): Real = createCoefficientValueOf(value = 1)

private sealed class ExpressionTreeNode {
  data class ExpressionNode(
      val mathExpression: MathExpression,
      val children: MutableList<ExpressionTreeNode>
  ): ExpressionTreeNode()

  data class PolynomialNode(val polynomial: Polynomial): ExpressionTreeNode()

  data class ConstantNode(val constant: Real): ExpressionTreeNode()
}

// TODO: add a faster isReducibleToConstant recursive function since this is used a lot.

//private fun MathExpression.reduceToConstant(): MathExpression? {
//  return when (expressionTypeCase) {
//    CONSTANT -> this
//    VARIABLE -> null
//    UNARY_OPERATION -> unaryOperation.reduceToConstant()
//    BINARY_OPERATION -> binaryOperation.reduceToConstant()
//    else -> null
//  }
//}

//private fun MathUnaryOperation.reduceToConstant(): MathExpression? {
//  return when (operator) {
//    MathUnaryOperation.Operator.NEGATION -> operand.reduceToConstant()?.transformConstant { -it }
//    else -> null
//  }
//}

//private fun MathBinaryOperation.reduceToConstant(): MathExpression? {
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
//}

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

private fun Real.toDouble(): Double {
  return if (hasRational()) rational.toDouble() else irrational
}

private fun Real.recompute(transform: (Real.Builder) -> Real.Builder): Real {
  return transform(toBuilder().clearRational().clearIrrational()).build()
}

private fun combine(
    lhs: Real,
    rhs: Real,
    leftRationalRightRationalOp: (Fraction, Fraction) -> Fraction,
    leftRationalRightIrrationalOp: (Fraction, Double) -> Double,
    leftIrrationalRightRationalOp: (Double, Fraction) -> Double,
    leftIrrationalRightIrrationalOp: (Double, Double) -> Double): Real {
  return when (lhs.realTypeCase) {
    Real.RealTypeCase.RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        Real.RealTypeCase.RATIONAL ->
          lhs.recompute { it.setRational(leftRationalRightRationalOp(lhs.rational, rhs.rational)) }
        Real.RealTypeCase.IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftRationalRightIrrationalOp(lhs.rational, rhs.irrational))
          }
        else -> throw Exception("Invalid real: $rhs.")
      }
    }
    Real.RealTypeCase.IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        Real.RealTypeCase.RATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightRationalOp(lhs.irrational, rhs.rational))
          }
        Real.RealTypeCase.IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightIrrationalOp(lhs.irrational, rhs.irrational))
          }
        else -> throw Exception("Invalid real: $rhs.")
      }
    }
    else -> throw Exception("Invalid real: $lhs.")
  }
}

private fun Real.pow(rhs: Real): Real {
  // Powers can really only be effectively done via floats or whole-number only fractions.
  return when (realTypeCase) {
    Real.RealTypeCase.RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        Real.RealTypeCase.RATIONAL -> recompute {
          if (rhs.rational.isOnlyWholeNumber()) {
            // The fraction can be retained.
            it.setRational(rational.pow(rhs.rational.wholeNumber))
          } else {
            // The fraction can't realistically be retained since it's being raised to an actual
            // fraction, resulting in an irrational number.
            it.setIrrational(rational.toDouble().pow(rhs.rational.toDouble()))
          }
        }
        Real.RealTypeCase.IRRATIONAL -> recompute { it.setIrrational(rational.pow(rhs.irrational)) }
        else -> throw Exception("Invalid real: $rhs.")
      }
    }
    Real.RealTypeCase.IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        Real.RealTypeCase.RATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.rational)) }
        Real.RealTypeCase.IRRATIONAL ->
          recompute { it.setIrrational(irrational.pow(rhs.irrational)) }
        else -> throw Exception("Invalid real: $rhs.")
      }
    }
    else -> throw Exception("Invalid real: $this.")
  }
}

private operator fun Real.unaryMinus(): Real {
  return when (realTypeCase) {
    Real.RealTypeCase.RATIONAL -> recompute { it.setRational(-rational) }
    Real.RealTypeCase.IRRATIONAL -> recompute { it.setIrrational(-irrational) }
    else -> throw Exception("Invalid real: $this.")
  }
}

private operator fun Real.plus(rhs: Real): Real {
  return combine(this, rhs, Fraction::plus, Fraction::plus, Double::plus, Double::plus)
}

private operator fun Real.minus(rhs: Real): Real {
  return combine(this, rhs, Fraction::minus, Fraction::minus, Double::minus, Double::minus)
}

private operator fun Real.times(rhs: Real): Real {
  return combine(this, rhs, Fraction::times, Fraction::times, Double::times, Double::times)
}

private operator fun Real.div(rhs: Real): Real {
  return combine(this, rhs, Fraction::div, Fraction::div, Double::div, Double::div)
}

private fun Double.pow(rhs: Fraction): Double = this.pow(rhs.toDouble())
private fun Fraction.pow(rhs: Double): Double = toDouble().pow(rhs)
private operator fun Double.plus(rhs: Fraction): Double = this + rhs.toFloat()
private operator fun Fraction.plus(rhs: Double): Double = toFloat() + rhs
private operator fun Double.minus(rhs: Fraction): Double = this - rhs.toFloat()
private operator fun Fraction.minus(rhs: Double): Double = toFloat() - rhs
private operator fun Double.times(rhs: Fraction): Double = this * rhs.toFloat()
private operator fun Fraction.times(rhs: Double): Double = toFloat() * rhs
private operator fun Double.div(rhs: Fraction): Double = this / rhs.toFloat()
private operator fun Fraction.div(rhs: Double): Double = toFloat() / rhs

private fun Fraction.pow(exp: Int): Fraction {
  if (exp == 0) return Fraction.newBuilder().setWholeNumber(1).setDenominator(1).build()
  if (exp == 1) return this
  var newValue = this
  for (i in 1 until exp) newValue *= this
  return newValue
}
