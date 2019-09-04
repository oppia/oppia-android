package org.oppia.app.home

import android.os.Bundle
import org.oppia.app.ParentActivity
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.FragmentTransactions
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity :  ParentActivity(), FragmentTransactions  {
  @Inject lateinit var homeActivityController: HomeActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityController.handleOnCreate()
    init(resources.getString(R.string.menu_home))
  }
}
