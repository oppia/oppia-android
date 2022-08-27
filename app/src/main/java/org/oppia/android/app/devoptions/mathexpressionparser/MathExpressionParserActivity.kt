package org.oppia.android.app.devoptions.mathexpressionparser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.MATH_EXPRESSION_PARSER_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity to allow the user to test math expressions/equations. */
class MathExpressionParserActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var mathExpressionParserActivityPresenter: MathExpressionParserActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    mathExpressionParserActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.math_expression_parser_activity_title)
  }

  companion object {
    /** Returns [Intent] for [MathExpressionParserActivity]. */
    fun createIntent(context: Context): Intent {
      return Intent(context, MathExpressionParserActivity::class.java).apply {
        decorateWithScreenName(MATH_EXPRESSION_PARSER_ACTIVITY)
      }
    }
  }
}
