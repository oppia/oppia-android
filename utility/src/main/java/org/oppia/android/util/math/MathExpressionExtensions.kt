package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.convertToComparableOperation
import org.oppia.android.util.math.ExpressionToLatexConverter.Companion.convertToLatex
import org.oppia.android.util.math.NumericExpressionEvaluator.Companion.evaluate

/**
 * Returns the LaTeX conversion of this [MathExpression], with the style configuration determined by
 * [divAsFraction].
 *
 * See [convertToLatex] for specifics.
 */
fun MathExpression.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

/**
 * Returns the LaTeX conversion of this [MathEquation], with the style configuration determined by
 * [divAsFraction].
 *
 * See [convertToLatex] for specifics.
 */
fun MathEquation.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

/**
 * Returns the [Real] evaluation of this [MathExpression].
 *
 * See [evaluate] for specifics.
 */
fun MathExpression.evaluateAsNumericExpression(): Real? = evaluate()

/**
 * Returns the [ComparableOperation] representation of this [MathExpression].
 *
 * See [convertToComparableOperation] for details.
 */
fun MathExpression.toComparableOperation(): ComparableOperation = convertToComparableOperation()
