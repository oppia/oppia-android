package org.oppia.android.app.help

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.help.faq.FAQListFragment
import org.oppia.android.app.help.thirdparty.LicenseListFragment
import org.oppia.android.app.help.thirdparty.LicenseTextViewerFragment
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListFragment
import javax.inject.Inject

/** The presenter for [HelpActivity]. */
@ActivityScope
class HelpActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var toolbar: Toolbar

  fun handleOnCreate(
    extraHelpOptionsTitle: String?,
    isFromNavigationDrawer: Boolean,
    selectedFragment: String,
    dependencyIndex: Int,
    licenseIndex: Int
  ) {
    if (isFromNavigationDrawer) {
      activity.setContentView(R.layout.help_activity)
      setUpToolbar()
      setUpNavigationDrawer()
    } else {
      activity.setContentView(R.layout.help_without_drawer_activity)
      setUpToolbar()
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
      toolbar.setNavigationOnClickListener {
        activity.finish()
      }
    }
    val titleTextView =
      activity.findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      setMultipaneContainerTitle(extraHelpOptionsTitle!!)
    }
    val isMultipane = activity.findViewById<FrameLayout>(R.id.multipane_options_container) != null
    if (isMultipane) {
      loadMultipaneFragment(selectedFragment, dependencyIndex, licenseIndex)
    }
    val previousFragment = getHelpFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.help_fragment_placeholder,
      HelpFragment.newInstance(isMultipane)
    ).commitNow()
  }

  /** Loads [ThirdPartyDependencyListFragment] in tablet devices. */
  fun handleLoadThirdPartyDependencyListFragment() {
    setMultipaneContainerTitle(
      activity.getString(R.string.third_party_dependency_list_activity_title)
    )
    getMultipaneOptionsFragment()?.let {
      activity.supportFragmentManager.beginTransaction().remove(
        it
      ).commit()
    }
    val thirdPartyDependencyListFragment = ThirdPartyDependencyListFragment.newInstance(true)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      thirdPartyDependencyListFragment
    ).commitNow()
  }

  /** Loads [FAQListFragment] in tablet devices. */
  fun handleLoadFAQListFragment() {
    setMultipaneContainerTitle(activity.getString(R.string.faq_activity_title))
    getMultipaneOptionsFragment()?.let {
      activity.supportFragmentManager.beginTransaction().remove(it)
        .commit()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      FAQListFragment()
    ).commitNow()
  }

  /** Loads [LicenseListFragment] in tablet devices. */
  fun handleLoadLicenseListFragment(dependencyIndex: Int) {
    setMultipaneContainerTitle(activity.getString(R.string.license_list_activity_title))
    val licenseListFragment = LicenseListFragment.newInstance(dependencyIndex, true)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      licenseListFragment
    ).commitNow()
  }

  /** Loads [LicenseTextViewerFragment] in tablet devices. */
  fun handleLoadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int) {
    val thirdPartyDependencyLicenseNamesArray = activity.resources.obtainTypedArray(
      R.array.third_party_dependency_license_names_array
    )
    val licenseNamesArrayId = thirdPartyDependencyLicenseNamesArray.getResourceId(
      dependencyIndex,
      /* defValue= */ 0
    )
    val licenseNamesArray = activity.resources.getStringArray(licenseNamesArrayId)
    setMultipaneContainerTitle(licenseNamesArray[licenseIndex])
    val licenseTextViewerFragment = LicenseTextViewerFragment.newInstance(
      dependencyIndex,
      licenseIndex
    )
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      licenseTextViewerFragment
    ).commitNow()
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.help_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun setUpNavigationDrawer() {
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.help_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(R.id.help_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_help
    )
  }

  private fun getHelpFragment(): HelpFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.help_fragment_placeholder) as HelpFragment?
  }

  private fun loadMultipaneFragment(
    selectedFragment: String,
    dependencyIndex: Int,
    licenseIndex: Int
  ) {
    when (selectedFragment) {
      FAQ_LIST_FRAGMENT -> handleLoadFAQListFragment()
      THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT -> handleLoadThirdPartyDependencyListFragment()
      LICENSE_LIST_FRAGMENT -> handleLoadLicenseListFragment(dependencyIndex)
      LICENSE_TEXT_FRAGMENT -> handleLoadLicenseTextViewerFragment(dependencyIndex, licenseIndex)
    }
  }

  private fun setMultipaneContainerTitle(title: String) {
    activity.findViewById<TextView>(R.id.help_multipane_options_title_textview).text = title
  }

  private fun getMultipaneOptionsFragment(): Fragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
  }
}
