package org.oppia.android.app.administratorcontrols

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.settings.profile.ProfileEditActivity
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.settings.profile.ProfileListFragment
import org.oppia.android.databinding.AdministratorControlsActivityBinding
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private var isMultipane = false
  private lateinit var lastLoadedFragment: String
  private val ADMINISTRATOR_CONTROLS_BACKSTACK: String? = null//"ADMINISTRATOR_CONTROLS_BACKSTACK"

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
      AdministratorControlsFragment.newInstance(isMultipane),
      ADMINISTRATOR_CONTROLS_BACKSTACK
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
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      ProfileListFragment.newInstance(isMultipane),
      ADMINISTRATOR_CONTROLS_BACKSTACK
    ).commitNow()
  }

  fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    getAdministratorControlsFragment()!!.setSelectedFragment(lastLoadedFragment)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.administrator_controls_fragment_multipane_placeholder,
      AppVersionFragment(),
      ADMINISTRATOR_CONTROLS_BACKSTACK
    ).commitNow()
  }

  fun loadProfileEdit(profileId: Int) {
    if (!isMultipane) {
      activity.startActivity(
        ProfileEditActivity.createProfileEditActivity(
          activity,
          profileId,
          isMultipane
        )
      )
    } else {
      lastLoadedFragment = PROFILE_EDIT_FRAGMENT
      binding.administratorControlsMultipaneOptionsBackButton!!.visibility = View.VISIBLE
      val fragment = ProfileEditFragment.newInstance(profileId, isMultipane)
      activity.supportFragmentManager.beginTransaction().add(
        R.id.administrator_controls_fragment_multipane_placeholder,
        fragment,
        ADMINISTRATOR_CONTROLS_BACKSTACK
      )
        .addToBackStack(null)
        .commit()
    }
  }

  val TAG = "tag"
  fun handleOnBackPressed() {
    if (activity.supportFragmentManager.backStackEntryCount > 0)
      activity.supportFragmentManager.popBackStackImmediate()
  }

  fun handleOnResume() {
    activity.supportFragmentManager.addOnBackStackChangedListener {
      Log.i(
        TAG,
        "handleOnResume:\n **** back stack changed\n *******"
      )
    }
    if (isMultipane) {
      binding.administratorControlsMultipaneOptionsBackButton!!.setOnClickListener {
        handleOnBackPressed()
      }
    }
  }

  fun setExtraControlsTitle(title: String) {
    binding.extraControlsTitle?.text = title
  }

  fun handleOnSaveInstanceState(outState: Bundle) {
    val titleTextView = binding.extraControlsTitle
    if (titleTextView != null) {
      outState.putString(SELECTED_CONTROLS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(LAST_LOADED_FRAGMENT_EXTRA_KEY, lastLoadedFragment)
  }
}
