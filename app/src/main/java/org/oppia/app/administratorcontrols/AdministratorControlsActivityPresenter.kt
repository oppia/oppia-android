package org.oppia.app.administratorcontrols

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.app.databinding.AdministratorControlsActivityBinding
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.app.settings.profile.ProfileListFragment
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private var isMultipane = false
  private lateinit var lastLoadedFragment: String
  private lateinit var binding: AdministratorControlsActivityBinding

  fun handleOnCreate(extraControlsTitle: String?, lastLoadedFragment: String) {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.administrator_controls_activity
    )
    setUpNavigationDrawer()
    this.lastLoadedFragment = lastLoadedFragment
    binding.extraControlsTitle?.apply {
      text = extraControlsTitle
    }
    isMultipane = binding.administratorControlsFragmentMultipanePlaceholder != null
    val previousFragment = getAdministratorControlsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_placeholder,
      AdministratorControlsFragment.newInstance(isMultipane)
    ).commitNow()
    if (isMultipane) {
      when (lastLoadedFragment) {
        PROFILE_LIST_FRAGMENT -> (activity as AdministratorControlsActivity).loadProfileList()
        APP_VERSION_FRAGMENT -> (activity as AdministratorControlsActivity).loadAppVersion()
      }
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar = binding.administratorControlsActivityToolbar as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      binding.administratorControlsActivityDrawerLayout,
      toolbar, /* menuItemId= */ 0
    )
  }

  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_fragment_placeholder
      ) as AdministratorControlsFragment?
  }

  fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileListFragment.newInstance(isMultipane)
    ).commitNow()
  }

  fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      AppVersionFragment()
    ).commitNow()
  }

  fun setExtraControlsTitle(title: String) {
    binding.extraControlsTitle?.text = title
  }

  fun handleOnSaveInstanceState(outState: Bundle) {
    val titleTextView = binding.extraControlsTitle
    if (titleTextView != null) {
      outState.putString(SELECTED_CONTROLS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(
      AdministratorControlsActivity.LAST_LOADED_FRAGMENT_SAVED_KEY, lastLoadedFragment
    )
  }
}
