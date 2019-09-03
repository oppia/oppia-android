package org.oppia.app

import android.os.Bundle
import org.oppia.app.drawer.FragmentTransactions

/** The central activity for all users entering the app. */
class HomeActivity : ParentActivity(), FragmentTransactions {



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)

    init(resources.getString(R.string.menu_home))
  }




}
