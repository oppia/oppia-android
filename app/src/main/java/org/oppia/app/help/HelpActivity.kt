package org.oppia.app.help

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class HelpActivity : InjectableAppCompatActivity() {
  @Inject lateinit var helpActivityController: HelpActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)

    helpActivityController.handleOnCreate()
  }
}
