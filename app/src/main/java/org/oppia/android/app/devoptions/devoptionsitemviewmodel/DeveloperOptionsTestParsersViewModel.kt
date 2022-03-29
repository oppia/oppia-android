package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToMathExpressionParserTestListener

/**
 * [DeveloperOptionsItemViewModel] to provide features to test and debug math expressions and
 * equations.
 */
class DeveloperOptionsTestParsersViewModel(
  private val routeToMathExpressionParserTestListener: RouteToMathExpressionParserTestListener
) : DeveloperOptionsItemViewModel() {
  /** Routes the user to an activity for testing math expressions & equations. */
  fun onMathExpressionsClicked() {
    routeToMathExpressionParserTestListener.routeToMathExpressionParserTest()
  }
}
