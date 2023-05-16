package org.oppia.android.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.help.HelpActivityPresenter.Companion.FAQ_LIST_FRAGMENT_TAG
import org.oppia.android.app.help.HelpActivityPresenter.Companion.HELP_OPTIONS_TITLE_SAVED_KEY
import org.oppia.android.app.help.HelpActivityPresenter.Companion.LICENSE_INDEX_SAVED_KEY
import org.oppia.android.app.help.HelpActivityPresenter.Companion.POLICIES_ARGUMENT_PROTO
import org.oppia.android.app.help.HelpActivityPresenter.Companion.SELECTED_FRAGMENT_SAVED_KEY
import org.oppia.android.app.help.HelpActivityPresenter.Companion.THIRD_PARTY_DEPENDENCY_INDEX_SAVED_KEY
import org.oppia.android.app.help.faq.RouteToFAQSingleListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.FaqListActivityParams
import org.oppia.android.app.model.FaqSingleActivityParams
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ScreenName.HELP_ACTIVITY
import org.oppia.android.app.model.ThirdPartyDependencyListActivityParams
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

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
  @Inject lateinit var helpActivityPresenter: HelpActivityPresenter
  @Inject lateinit var resourceHandler: AppLanguageResourceHandler
  @Inject lateinit var activityRouter: ActivityRouter

  private lateinit var selectedFragment: String
  private lateinit var selectedHelpOptionsTitle: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
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
    val policiesActivityParams = savedInstanceState?.getProto(
      POLICIES_ARGUMENT_PROTO,
      PoliciesActivityParams.getDefaultInstance()
    )
    helpActivityPresenter.handleOnCreate(
      selectedHelpOptionsTitle,
      isFromNavigationDrawer,
      selectedFragment,
      selectedDependencyIndex,
      selectedLicenseIndex,
      policiesActivityParams
    )
    title = resourceHandler.getStringInLocale(R.string.menu_help)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "HelpActivity.bool_is_from_navigation_drawer"

    fun createIntent(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, HelpActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      intent.decorateWithScreenName(HELP_ACTIVITY)
      return intent
    }
  }

  override fun onRouteToFAQList() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        faqListActivityParams = FaqListActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun onRouteToThirdPartyDependencyList() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        thirdPartyDependencyListActivityParams =
          ThirdPartyDependencyListActivityParams.getDefaultInstance()
      }.build()
    )
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
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        faqSingleActivityParams = FaqSingleActivityParams.newBuilder().apply {
          this.questionText = question
          this.answerText = answer
        }.build()
      }.build()
    )
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        policiesActivityParams = PoliciesActivityParams.newBuilder().apply {
          this.policyPage = policyPage
        }.build()
      }.build()
    )
  }

  override fun loadPoliciesFragment(policyPage: PolicyPage) {
    helpActivityPresenter.handleLoadPoliciesFragment(policyPage)
  }

  interface Injector {
    fun inject(activity: HelpActivity)
  }
}
