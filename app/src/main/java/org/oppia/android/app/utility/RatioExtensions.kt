package org.oppia.android.app.utility

import org.oppia.android.R
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.translation.AppLanguageResourceHandler

/**
 * Returns an accessibly readable string representation of this [RatioExpression].
 * E.g. [1, 2, 3] will yield to 1 to 2 to 3
 */
fun RatioExpression.toAccessibleAnswerString(resourceHandler: AppLanguageResourceHandler): String {
  return ratioComponentList.joinToString(
    resourceHandler.getStringInLocale(R.string.ratio_content_description_separator)
  )
}
