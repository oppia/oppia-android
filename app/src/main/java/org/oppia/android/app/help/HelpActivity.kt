package org.oppia.android.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.faq.RouteToFAQSingleListener
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.LicenseTextViewerActivity
import org.oppia.android.app.help.thirdparty.RouteToLicenseListListener
import org.oppia.android.app.help.thirdparty.RouteToLicenseTextListener
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import javax.inject.Inject

private const val HELP_OPTIONS_TITLE_SAVED_KEY = "HelpActivity.help_options_title"
private const val SELECTED_FRAGMENT_SAVED_KEY = "HelpActivity.selected_fragment"
const val FAQ_LIST_FRAGMENT_TAG = "FAQListFragment.tag"
const val THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG = "ThirdPartyDependencyListFragment.tag"

/** The help page activity for FAQs and third-party dependencies. */
class HelpActivity :
  InjectableAppCompatActivity(),
  RouteToFAQListListener,
  RouteToFAQSingleListener,
  RouteToThirdPartyDependencyListListener,
  RouteToLicenseTextListener,
  RouteToLicenseListListener,
  LoadFAQListFragmentListener,
  LoadThirdPartyDependencyListFragmentListener {

  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  private lateinit var selectedFragment: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    selectedFragment =
      savedInstanceState?.getString(SELECTED_FRAGMENT_SAVED_KEY) ?: FAQ_LIST_FRAGMENT_TAG
    val extraHelpOptionsTitle = savedInstanceState?.getString(HELP_OPTIONS_TITLE_SAVED_KEY)
    helpActivityPresenter.handleOnCreate(
      extraHelpOptionsTitle,
      isFromNavigationDrawer,
      selectedFragment
    )
    title = getString(R.string.menu_help)
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

  override fun loadFAQListFragment() {
    selectedFragment = FAQ_LIST_FRAGMENT_TAG
    helpActivityPresenter.handleLoadFAQListFragment()
  }

  override fun loadThirdPartyDependencyListFragment() {
    selectedFragment = THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_TAG
    helpActivityPresenter.handleLoadThirdPartyDependencyListFragment()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val titleTextView = findViewById<TextView>(R.id.help_multipane_options_title_textview)
    if (titleTextView != null) {
      outState.putString(HELP_OPTIONS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(SELECTED_FRAGMENT_SAVED_KEY, selectedFragment)
  }

  override fun onRouteToLicenseText(dependencyIndex: Int, licenseIndex: Int) {
    startActivity(
      LicenseTextViewerActivity.createLicenseTextViewerActivityIntent(
        this,
        dependencyIndex,
        licenseIndex
      )
    )
  }

  override fun onRouteToLicenseList(dependencyIndex: Int) {
    startActivity(
      LicenseListActivity
        .createLicenseListActivityIntent(
          context = this,
          dependencyIndex = dependencyIndex
        )
    )
  }

  override fun onRouteToFAQSingle(question: String, answer: String) {
    startActivity(FAQSingleActivity.createFAQSingleActivityIntent(this, question, answer))
  }
}
