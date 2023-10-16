package org.oppia.android.app.drawer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** [NavigationDrawerFragment] to show navigation drawer. */
class NavigationDrawerFragment :
  InjectableFragment(),
  RouteToProfileProgressListener,
  ExitProfileDialogInterface {

  @Inject
  lateinit var navigationDrawerFragmentPresenter: NavigationDrawerFragmentPresenter

  private lateinit var drawerLayout: DrawerLayout
  private lateinit var toolbar: Toolbar
  private var menuItemId: Int = 0

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val navigationDrawerFragmentView =
      navigationDrawerFragmentPresenter.handleCreateView(inflater, container)
    navigationDrawerFragmentPresenter.setUpDrawer(drawerLayout, toolbar, menuItemId)
    return navigationDrawerFragmentView
  }

  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    this.drawerLayout = drawerLayout
    this.toolbar = toolbar
    this.menuItemId = menuItemId
  }

  override fun routeToProfileProgress(profileId: Int) {
    navigationDrawerFragmentPresenter.openProfileProgress(profileId)
  }

  override fun highlightLastCheckedMenuItem() {
    navigationDrawerFragmentPresenter.highlightLastCheckedMenuItem()
  }

  override fun highlightAdministratorControlsItem() {
    navigationDrawerFragmentPresenter.highlightAdministratorControlsItem()
  }

  override fun highlightDeveloperOptionsItem() {
    navigationDrawerFragmentPresenter.highlightDeveloperOptionsItem()
  }

  override fun unhighlightSwitchProfileMenuItem() {
    navigationDrawerFragmentPresenter.unhighlightSwitchProfileMenuItem()
  }
}
