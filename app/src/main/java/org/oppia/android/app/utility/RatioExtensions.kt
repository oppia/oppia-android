package org.oppia.app.utility

import android.content.Context
import org.oppia.app.R
import org.oppia.app.model.RatioExpression

/**
 * Returns an accessibly readable string representation of this [RatioExpression].
 * E.g. [1, 2, 3] will yield to 1 to 2 to 3
 */
fun RatioExpression.toAccessibleAnswerString(context: Context): String {
  return ratioComponentList.joinToString(
    context.getString(R.string.ratio_content_description_separator)
  )
}
