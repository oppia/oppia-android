package org.oppia.android.app.devoptions.viewlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import javax.inject.Inject

/** Activity that hosts the developer Logcat screen. */
class ViewLogcatActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject lateinit var presenter: ViewLogcatActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    presenter.handleOnCreate()
  }

  companion object {
    /** Creates an intent for [ViewLogcatActivity]. */
    fun createIntent(context: Context): Intent = Intent(context, ViewLogcatActivity::class.java)
  }
}
