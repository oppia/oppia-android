package org.oppia.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for recent stories. */
class ContinuePlayingActivity : InjectableAppCompatActivity() {
  @Inject lateinit var continuePlayingActivityPresenter: ContinuePlayingActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    continuePlayingActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ContinuePlayingActivity]. */
    fun createContinuePlayingActivityIntent(context: Context): Intent {
      return Intent(context, ContinuePlayingActivity::class.java)
    }
  }
}
