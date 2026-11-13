package org.oppia.android.app.devoptions.viewlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import javax.inject.Inject

/** Activity that hosts the developer performance metrics screen. */
class ViewPerformanceMetricsActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject lateinit var presenter: ViewPerformanceMetricsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    presenter.handleOnCreate()
  }

  companion object {
    /** Creates an intent for [ViewPerformanceMetricsActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ViewPerformanceMetricsActivity::class.java)
  }
}
