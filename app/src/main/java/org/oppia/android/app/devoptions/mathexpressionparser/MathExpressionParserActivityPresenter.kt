package org.oppia.android.app.devoptions.mathexpressionparser

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [MathExpressionParserActivity]. */
@ActivityScope
class MathExpressionParserActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Called when [MathExpressionParserActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.math_expression_parser_activity)

    if (getMathExpressionParserFragment() == null) {
      val forceNetworkTypeFragment = MathExpressionParserFragment.createNewInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.math_expression_parser_container,
        forceNetworkTypeFragment
      ).commitNow()
    }
  }

  private fun getMathExpressionParserFragment(): MathExpressionParserFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.force_network_type_container) as? MathExpressionParserFragment
  }
}
