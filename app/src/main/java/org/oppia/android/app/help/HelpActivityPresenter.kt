package org.oppia.android.app.help

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
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
import org.oppia.android.app.model.PoliciesArguments
import org.oppia.android.app.model.PoliciesArguments.PolicyPage
import org.oppia.android.app.policies.PoliciesFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The presenter for [HelpActivity]. */
@ActivityScope
class HelpActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var toolbar: Toolbar

  private lateinit var selectedFragmentTag: String
  private lateinit var selectedHelpOptionTitle: String
  private var selectedDependencyIndex: Int? = null
  private var selectedLicenseIndex: Int? = null
  private var internalPolicyPage: PolicyPage = PolicyPage.POLICY_PAGE_UNSPECIFIED

  fun handleOnCreate(
    helpOptionsTitle: String,
    isFromNavigationDrawer: Boolean,
    selectedFragment: String,
    dependencyIndex: Int,
    licenseIndex: Int,
    policiesArguments: PoliciesArguments?
  ) {
    selectedFragmentTag = selectedFragment
    selectedDependencyIndex = dependencyIndex
    selectedLicenseIndex = licenseIndex
    selectedHelpOptionTitle = helpOptionsTitle
    if (policiesArguments != null) {
      internalPolicyPage = policiesArguments.policyPage
    }

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
      setMultipaneContainerTitle(helpOptionsTitle)
    }
    val isMultipane = activity.findViewById<FrameLayout>(R.id.multipane_options_container) != null
    if (isMultipane) {
      loadMultipaneFragment(selectedFragment, dependencyIndex, licenseIndex)
      setBackButtonClickListener()
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
    selectThirdPartyDependencyListFragment()
    val previousFragment = getMultipaneOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commit()
    }
    val thirdPartyDependencyListFragment = ThirdPartyDependencyListFragment.newInstance(true)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      thirdPartyDependencyListFragment
    ).commitNow()
  }

  /** Loads [FAQListFragment] in tablet devices. */
  fun handleLoadFAQListFragment() {
    selectFAQListFragment()
    val previousFragment = getMultipaneOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commit()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      FAQListFragment()
    ).commitNow()
  }

  /** Loads [LicenseListFragment] in tablet devices. */
  fun handleLoadLicenseListFragment(dependencyIndex: Int) {
    selectLicenseListFragment(dependencyIndex)
    val previousFragment = getMultipaneOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commit()
    }
    val licenseListFragment = LicenseListFragment.newInstance(dependencyIndex, true)
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.multipane_options_container, licenseListFragment)
      .commitNow()
  }

  /** Loads [LicenseTextViewerFragment] in tablet devices. */
  fun handleLoadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int) {
    selectLicenseTextViewerFragment(dependencyIndex, licenseIndex)
    val previousFragment = getMultipaneOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commit()
    }
    val licenseTextViewerFragment = LicenseTextViewerFragment.newInstance(
      dependencyIndex,
      licenseIndex
    )
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.multipane_options_container, licenseTextViewerFragment)
      .commitNow()
  }

  /** Handles onSavedInstanceState() method for [HelpActivity]. */
  fun handleOnSavedInstanceState(outState: Bundle) {
    val titleTextView = activity.findViewById<TextView>(R.id.help_multipane_options_title_textview)
    if (titleTextView != null) {
      outState.putString(HELP_OPTIONS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(SELECTED_FRAGMENT_SAVED_KEY, selectedFragmentTag)
    selectedDependencyIndex?.let { outState.putInt(THIRD_PARTY_DEPENDENCY_INDEX_SAVED_KEY, it) }
    selectedLicenseIndex?.let { outState.putInt(LICENSE_INDEX_SAVED_KEY, it) }
    val policiesArguments =
      PoliciesArguments
        .newBuilder()
        .setPolicyPage(internalPolicyPage)
        .build()
    outState.putProto(POLICIES_ARGUMENT_PROTO, policiesArguments)
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.help_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun setBackButtonClickListener() {
    val helpOptionsBackButton =
      activity.findViewById<ImageButton>(R.id.help_multipane_options_back_button)
    helpOptionsBackButton.setOnClickListener {
      val currentFragment = getMultipaneOptionsFragment()
      if (currentFragment != null) {
        when (currentFragment) {
          is LicenseTextViewerFragment -> {
            handleLoadLicenseListFragment(
              checkNotNull(selectedDependencyIndex) {
                "Expected dependency index to be selected & defined"
              }
            )
          }
          is LicenseListFragment -> handleLoadThirdPartyDependencyListFragment()
        }
      }
    }
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
      FAQ_LIST_FRAGMENT_TAG -> handleLoadFAQListFragment()
      POLICIES_FRAGMENT_TAG -> handleLoadPoliciesFragment(internalPolicyPage)
      THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG -> handleLoadThirdPartyDependencyListFragment()
      LICENSE_LIST_FRAGMENT_TAG -> handleLoadLicenseListFragment(dependencyIndex)
      LICENSE_TEXT_FRAGMENT_TAG -> handleLoadLicenseTextViewerFragment(
        dependencyIndex,
        licenseIndex
      )
    }
  }

  private fun selectFAQListFragment() {
    setMultipaneContainerTitle(resourceHandler.getStringInLocale(R.string.faq_activity_title))
    setMultipaneBackButtonVisibility(View.GONE)
    selectedFragmentTag = FAQ_LIST_FRAGMENT_TAG
    selectedHelpOptionTitle = getMultipaneContainerTitle()
  }

  private fun selectThirdPartyDependencyListFragment() {
    setMultipaneContainerTitle(
      resourceHandler.getStringInLocale(R.string.third_party_dependency_list_activity_title)
    )
    setMultipaneBackButtonVisibility(View.GONE)
    selectedFragmentTag = THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG
    selectedHelpOptionTitle = getMultipaneContainerTitle()
  }

  private fun selectLicenseListFragment(dependencyIndex: Int) {
    setMultipaneContainerTitle(
      resourceHandler.getStringInLocale(R.string.license_list_activity_title)
    )
    setMultipaneBackButtonVisibility(View.VISIBLE)
    setHelpBackButtonContentDescription(LICENSE_LIST_FRAGMENT_TAG)
    selectedFragmentTag = LICENSE_LIST_FRAGMENT_TAG
    selectedDependencyIndex = dependencyIndex
    selectedHelpOptionTitle = getMultipaneContainerTitle()
  }

  private fun selectLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int) {
    setMultipaneContainerTitle(retrieveLicenseName(dependencyIndex, licenseIndex))
    setMultipaneBackButtonVisibility(View.VISIBLE)
    setHelpBackButtonContentDescription(LICENSE_TEXT_FRAGMENT_TAG)
    selectedFragmentTag = LICENSE_TEXT_FRAGMENT_TAG
    selectedDependencyIndex = dependencyIndex
    selectedLicenseIndex = licenseIndex
    selectedHelpOptionTitle = getMultipaneContainerTitle()
  }

  private fun retrieveLicenseName(dependencyIndex: Int, licenseIndex: Int): String {
    val thirdPartyDependencyLicenseNamesArray = activity.resources.obtainTypedArray(
      R.array.third_party_dependency_license_names_array
    )
    val licenseNamesArrayId = thirdPartyDependencyLicenseNamesArray.getResourceId(
      dependencyIndex,
      /* defValue= */ 0
    )
    val licenseNamesArray = resourceHandler.getStringArrayInLocale(licenseNamesArrayId)
    thirdPartyDependencyLicenseNamesArray.recycle()
    return licenseNamesArray[licenseIndex]
  }

  private fun getMultipaneContainerTitle(): String {
    return activity.findViewById<TextView>(
      R.id.help_multipane_options_title_textview
    ).text.toString()
  }

  private fun setHelpBackButtonContentDescription(fragmentTag: String) {
    when (fragmentTag) {
      LICENSE_LIST_FRAGMENT_TAG -> {
        val thirdPartyDependenciesList = resourceHandler.getStringInLocale(
          R.string.help_activity_third_party_dependencies_list
        )
        activity.findViewById<ImageButton>(R.id.help_multipane_options_back_button)
          .contentDescription = resourceHandler.getStringInLocaleWithoutWrapping(
          R.string.help_activity_back_arrow_description,
          thirdPartyDependenciesList
        )
      }
      LICENSE_TEXT_FRAGMENT_TAG -> {
        val copyrightLicensesList = resourceHandler.getStringInLocale(
          R.string.help_activity_copyright_licenses_list
        )
        activity.findViewById<ImageButton>(R.id.help_multipane_options_back_button)
          .contentDescription = resourceHandler.getStringInLocaleWithoutWrapping(
          R.string.help_activity_back_arrow_description,
          copyrightLicensesList
        )
      }
    }
  }

  private fun setMultipaneContainerTitle(title: String) {
    activity.findViewById<TextView>(R.id.help_multipane_options_title_textview).text = title
  }

  private fun setMultipaneBackButtonVisibility(visibility: Int) {
    activity.findViewById<ImageButton>(R.id.help_multipane_options_back_button).visibility =
      visibility
  }

  private fun getMultipaneOptionsFragment(): Fragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
  }

  fun handleLoadPoliciesFragment(policyPage: PolicyPage) {
    internalPolicyPage = policyPage
    selectPoliciesFragment(policyPage)

    val policiesArguments =
      PoliciesArguments
        .newBuilder()
        .setPolicyPage(policyPage)
        .build()
    val previousFragment = getMultipaneOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.multipane_options_container,
      PoliciesFragment.newInstance(policiesArguments)
    ).commitNow()
  }

  private fun selectPoliciesFragment(policyPage: PolicyPage) {
    when (policyPage) {
      PolicyPage.PRIVACY_POLICY -> setMultipaneContainerTitle(
        resourceHandler.getStringInLocale(R.string.privacy_policy_title)
      )
      PolicyPage.TERMS_OF_SERVICE -> setMultipaneContainerTitle(
        resourceHandler.getStringInLocale(R.string.terms_of_service_title)
      )
      else -> {}
    }
    setMultipaneBackButtonVisibility(View.GONE)
    selectedFragmentTag = POLICIES_FRAGMENT_TAG
    selectedHelpOptionTitle = getMultipaneContainerTitle()
  }
}
