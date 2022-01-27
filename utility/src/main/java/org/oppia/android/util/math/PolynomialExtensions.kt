package org.oppia.android.util.math

import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real

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
