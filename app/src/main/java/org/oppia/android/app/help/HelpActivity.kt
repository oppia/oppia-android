package org.oppia.android.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.RouteToFAQSingleListener
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.model.PoliciesArguments
import org.oppia.android.app.model.PoliciesArguments.PolicyPage
import org.oppia.android.app.policies.PoliciesActivity
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

const val HELP_OPTIONS_TITLE_SAVED_KEY = "HelpActivity.help_options_title"
const val SELECTED_FRAGMENT_SAVED_KEY = "HelpActivity.selected_fragment"
const val THIRD_PARTY_DEPENDENCY_INDEX_SAVED_KEY =
  "HelpActivity.third_party_dependency_index"
const val LICENSE_INDEX_SAVED_KEY = "HelpActivity.license_index"
const val FAQ_LIST_FRAGMENT_TAG = "FAQListFragment.tag"
const val POLICIES_ARGUMENT_PROTO = "PoliciesActivity.policy_page"
const val POLICIES_FRAGMENT_TAG = "PoliciesFragment.tag"
const val THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG = "ThirdPartyDependencyListFragment.tag"
const val LICENSE_LIST_FRAGMENT_TAG = "LicenseListFragment.tag"
const val LICENSE_TEXT_FRAGMENT_TAG = "LicenseTextFragment.tag"

/** The help page activity for FAQs, third-party dependencies and policies page. */
class HelpActivity :
  InjectableAppCompatActivity(),
  RouteToFAQListListener,
  RouteToFAQSingleListener,
  RouteToPoliciesListener,
  RouteToThirdPartyDependencyListListener,
  LoadFaqListFragmentListener,
  LoadPoliciesFragmentListener,
  LoadThirdPartyDependencyListFragmentListener,
  LoadLicenseListFragmentListener,
  LoadLicenseTextViewerFragmentListener {

  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private lateinit var selectedFragment: String
  private lateinit var selectedHelpOptionsTitle: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    selectedFragment =
      savedInstanceState?.getStringFromBundle(SELECTED_FRAGMENT_SAVED_KEY) ?: FAQ_LIST_FRAGMENT_TAG
    val selectedDependencyIndex =
      savedInstanceState?.getInt(THIRD_PARTY_DEPENDENCY_INDEX_SAVED_KEY) ?: 0
    val selectedLicenseIndex = savedInstanceState?.getInt(LICENSE_INDEX_SAVED_KEY) ?: 0
    selectedHelpOptionsTitle = savedInstanceState?.getStringFromBundle(HELP_OPTIONS_TITLE_SAVED_KEY)
      ?: resourceHandler.getStringInLocale(R.string.faq_activity_title)
    val policiesArguments = savedInstanceState?.getProto(
      POLICIES_ARGUMENT_PROTO,
      PoliciesArguments.getDefaultInstance()
    )
    helpActivityPresenter.handleOnCreate(
      selectedHelpOptionsTitle,
      isFromNavigationDrawer,
      selectedFragment,
      selectedDependencyIndex,
      selectedLicenseIndex,
      policiesArguments
    )
    title = resourceHandler.getStringInLocale(R.string.menu_help)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "HelpActivity.bool_is_from_navigation_drawer"

    fun createHelpActivityIntent(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, HelpActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      return intent
    }
  }

  override fun onRouteToFAQList() {
    val intent = FAQListActivity.createFAQListActivityIntent(this)
    startActivity(intent)
  }

  override fun onRouteToThirdPartyDependencyList() {
    val intent = ThirdPartyDependencyListActivity.createThirdPartyDependencyListActivityIntent(this)
    startActivity(intent)
  }

  override fun loadFaqListFragment() {
    helpActivityPresenter.handleLoadFAQListFragment()
  }

  override fun loadThirdPartyDependencyListFragment() {
    helpActivityPresenter.handleLoadThirdPartyDependencyListFragment()
  }

  override fun loadLicenseListFragment(dependencyIndex: Int) {
    helpActivityPresenter.handleLoadLicenseListFragment(dependencyIndex)
  }

  override fun loadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int) {
    helpActivityPresenter.handleLoadLicenseTextViewerFragment(dependencyIndex, licenseIndex)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    helpActivityPresenter.handleOnSavedInstanceState(outState)
  }

  // TODO(#3681): Add support to display Single FAQ in split mode on tablet devices.
  override fun onRouteToFAQSingle(question: String, answer: String) {
    startActivity(FAQSingleActivity.createFAQSingleActivityIntent(this, question, answer))
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    startActivity(PoliciesActivity.createPoliciesActivityIntent(this, policyPage))
  }

  override fun loadPoliciesFragment(policyPage: PolicyPage) {
    helpActivityPresenter.handleLoadPoliciesFragment(policyPage)
  }
}
