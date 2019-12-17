package org.oppia.app.option

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for setting user preferences. */
class OptionActivity : InjectableAppCompatActivity() {

  @Inject lateinit var optionActivityPresenter: OptionActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    optionActivityPresenter.handleOnCreate()
  }
}
