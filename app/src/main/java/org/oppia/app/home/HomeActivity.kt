package org.oppia.app.home

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.utility.Logger
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity() {
  @Inject lateinit var homeActivityController: HomeActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    Logger(this).e("Log","Sample testing")
    homeActivityController.handleOnCreate()
  }
}
