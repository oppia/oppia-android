package org.oppia.app.help

import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The help page activity for users FAQ and feedbacks. */
class HelpActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    helpActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_help)
  }
}
