package org.oppia.app.drawer

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import org.oppia.app.R
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.help.HelpActivity
import org.oppia.app.help.HelpFragmentPresenter
import org.oppia.app.home.HomeActivity
import javax.inject.Inject

/** [NavigationDrawerFragment] to show navigation drawer. */
class NavigationDrawerFragment : InjectableFragment() {


  @Inject
  lateinit var navigationDrawerFragmentPresenter: NavigationDrawerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return navigationDrawerFragmentPresenter.handleCreateView(inflater, container)
  }

}
