package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import java.util.SortedSet

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

/** Returns whether this polynomial is a constant-only polynomial (contains no variables). */
fun Polynomial.isConstant(): Boolean = termCount == 1 && getTerm(0).variableCount == 0

fun Polynomial.isSingleTerm(): Boolean = termList.size == 1

fun Polynomial.isApproximatelyZero(): Boolean =
  termList.all { it.coefficient.isApproximatelyZero() } // Zero polynomials only have 0 coefs.

/**
 * Returns the first term coefficient from this polynomial. This corresponds to the whole value of
 * the polynomial iff isConstant() returns true, otherwise this value isn't useful.
 *
 * Note that this function can throw if the polynomial is empty (so isConstant() should always be
 * checked first).
 */
fun Polynomial.getConstant(): Real = getTerm(0).coefficient

// Return the highest power to represent the degree of the polynomial. Reference:
// https://www.varsitytutors.com/algebra_1-help/how-to-find-the-degree-of-a-polynomial.
fun Polynomial.getDegree(): Int = getLeadingTerm().highestDegree()

/**
 * Returns a human-readable, plaintext representation of this [Polynomial].
 *
 * The returned string is guaranteed to be a syntactically correct algebraic expression representing
 * the polynomial, e.g. "1+x-7x^2").
 */
fun Polynomial.toPlainText(): String {
  return termList.map {
    it.toPlainText()
  }.reduce { ongoingPolynomialStr, termAnswerStr ->
    if (termAnswerStr.startsWith("-")) {
      "$ongoingPolynomialStr - ${termAnswerStr.drop(1)}"
    } else "$ongoingPolynomialStr + $termAnswerStr"
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

fun Polynomial.combineLikeTerms(): Polynomial {
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

fun Polynomial.removeUnnecessaryVariables(): Polynomial {
  return Polynomial.newBuilder().apply {
    addAllTerm(
      this@removeUnnecessaryVariables.termList.filter { term ->
        !term.coefficient.isApproximatelyZero()
      }
    )
  }.build().ensureAtLeastConstant()
}

fun Polynomial.simplifyRationals(): Polynomial {
  return Polynomial.newBuilder().apply {
    addAllTerm(
      this@simplifyRationals.termList.map { term ->
        term.toBuilder().apply {
          coefficient = term.coefficient.maybeSimplifyRationalToInteger()
        }.build()
      }
    )
  }.build()
}

fun Polynomial.sort(): Polynomial = Polynomial.newBuilder().apply {
  addAllTerm(this@sort.termList.sortedWith(POLYNOMIAL_TERM_COMPARATOR))
}.build()

operator fun Polynomial.unaryMinus(): Polynomial {
  // Negating a polynomial just requires flipping the signs on all coefficients.
  return toBuilder()
    .clearTerm()
    .addAllTerm(termList.map { it.toBuilder().setCoefficient(-it.coefficient).build() })
    .build()
}

operator fun Polynomial.plus(rhs: Polynomial): Polynomial {
  // Adding two polynomials just requires combining their terms lists (taking into account combining
  // common terms).
  return Polynomial.newBuilder().apply {
    addAllTerm(this@plus.termList + rhs.termList)
  }.build().combineLikeTerms().removeUnnecessaryVariables()
}

operator fun Polynomial.minus(rhs: Polynomial): Polynomial {
  // a - b = a + -b
  return this + -rhs
}

operator fun Polynomial.times(rhs: Polynomial): Polynomial {
  // Polynomial multiplication is simply multiplying each term in one by each term in the other.
  val crossMultipliedTerms = termList.flatMap { leftTerm ->
    rhs.termList.map { rightTerm -> leftTerm * rightTerm }
  }

  // Treat each multiplied term as a unique polynomial, then add them together (so that like terms
  // can be properly combined).
  return crossMultipliedTerms.map { createSingleTermPolynomial(it) }.reduce(Polynomial::plus)
}

operator fun Polynomial.div(rhs: Polynomial): Polynomial? {
  // See https://en.wikipedia.org/wiki/Polynomial_long_division#Pseudocode for reference.
  if (rhs.isApproximatelyZero()) {
    return null // Dividing by zero is invalid and thus cannot yield a polynomial.
  }

  var quotient = createConstantPolynomial(ZERO)
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

fun Polynomial.pow(exp: Polynomial): Polynomial? {
  // Polynomial exponentiation is only supported if the right side is a constant polynomial,
  // otherwise the result cannot be a polynomial (though could still be compared to another
  // expression by utilizing sampling techniques).
  return if (exp.isConstant()) pow(exp.getConstant()) else null
}

fun createConstantPolynomial(constant: Real): Polynomial =
  createSingleTermPolynomial(Term.newBuilder().setCoefficient(constant).build())

fun createSingleTermPolynomial(term: Term): Polynomial =
  Polynomial.newBuilder().apply { addTerm(term) }.build()

private fun Polynomial.pow(exp: Int): Polynomial {
  // Anything raised to the power of 0 is 1.
  if (exp == 0) return createConstantPolynomial(ONE)
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
    val onePolynomial = createConstantPolynomial(ONE)
    // Note that this division is guaranteed to fail if the exponentiation result is a polynomial.
    // Future implementations may leverage root-finding algorithms to factor for integer inverse
    // powers (such as square root, cubic root, etc.). Non-integer inverse powers will require
    // sampling.
    exponentiation?.let { onePolynomial / it }
  } else exponentiation
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

private fun Term.pow(rational: Fraction): Term? {
  // Raising an exponent by an exponent just requires multiplying the two together.
  val newVariablePowers = variableList.map { variable ->
    variable.power.toWholeNumberFraction() * rational
  }

  // If any powers are not whole numbers then the rational is likely representing a root and the
  // term in question is not rootable to that degree.
  if (newVariablePowers.any { !it.isOnlyWholeNumber() }) return null

  return Term.newBuilder().apply {
    coefficient = this@pow.coefficient.pow(
      Real.newBuilder().apply {
        this.rational = rational
      }.build()
    )
    addAllVariable(
      this@pow.variableList.zip(newVariablePowers).map { (variable, newPower) ->
        variable.toBuilder().apply {
          power = newPower.toWholeNumber()
        }.build()
      }
    )
  }.build()
}

private fun Polynomial.ensureAtLeastConstant(): Polynomial {
  return if (termCount == 0) {
    Polynomial.newBuilder().apply {
      addTerm(
        Term.newBuilder().apply {
          coefficient = ZERO
        }.build()
      )
    }.build()
  } else this
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

private fun Term.highestDegreeVariable(): Variable? = variableList.maxByOrNull(Variable::getPower)

private fun Term.highestDegree(): Int = highestDegreeVariable()?.power ?: 0

private fun Term.toPolynomial(): Polynomial {
  return Polynomial.newBuilder().addTerm(this).build()
}

private fun List<Variable>.toPowerMap(): Map<String, Int> {
  return associateBy({ it.name }, { it.power })
}

private fun Map<String, Int>.toVariableList(): List<Variable> {
  return map { (name, power) -> Variable.newBuilder().setName(name).setPower(power).build() }
}

private fun Real.maybeSimplifyRationalToInteger(): Real = when (realTypeCase) {
  Real.RealTypeCase.RATIONAL -> {
    if (rational.isOnlyWholeNumber()) {
      Real.newBuilder().apply {
        integer = this@maybeSimplifyRationalToInteger.rational.toWholeNumber()
      }.build()
    } else this
  }
  // Nothing to do in these cases.
  Real.RealTypeCase.IRRATIONAL, Real.RealTypeCase.INTEGER, Real.RealTypeCase.REALTYPE_NOT_SET,
  null -> this
}

// TODO: figure out of this can be removed.
private fun <T, U : Comparable<U>> Comparator<T>.thenComparingReversed(
  keySelector: (T) -> U
): Comparator<T> = thenComparing(Comparator.comparing(keySelector).reversed())

// TODO: figure out of this can be removed.
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
