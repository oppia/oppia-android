package org.oppia.app.drawer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.profileprogress.ProfileProgressActivity
import javax.inject.Inject

/** [NavigationDrawerFragment] to show navigation drawer. */
class NavigationDrawerFragment : InjectableFragment(), RouteToProfileProgressListener {

  @Inject lateinit var navigationDrawerFragmentPresenter: NavigationDrawerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return navigationDrawerFragmentPresenter.handleCreateView(inflater, container)
  }

  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    navigationDrawerFragmentPresenter.setUpDrawer(drawerLayout, toolbar, menuItemId)
  }

  override fun routeToProfileProgress(profileId: Int) {
    navigationDrawerFragmentPresenter.openProfileProgress(profileId)
  }
}
