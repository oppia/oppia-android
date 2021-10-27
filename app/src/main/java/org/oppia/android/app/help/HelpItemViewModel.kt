package org.oppia.android.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.policies.RouteToPrivacyPolicyListener
import org.oppia.android.app.policies.RouteToTermsOfServiceListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the recycler view of HelpActivity. */
class HelpItemViewModel(
  val activity: AppCompatActivity,
  val title: String,
  val isMultipane: Boolean,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  fun onClick(title: String) {
    when (title) {
      resourceHandler.getStringInLocale(R.string.frequently_asked_questions_FAQ) -> {
        if (isMultipane) {
          val loadFaqListFragmentListener = activity as LoadFaqListFragmentListener
          loadFaqListFragmentListener.loadFaqListFragment()
        } else {
          val routeToFAQListener = activity as RouteToFAQListListener
          routeToFAQListener.onRouteToFAQList()
        }
      }
      resourceHandler.getStringInLocale(R.string.third_party_dependency_list_activity_title) -> {
        if (isMultipane) {
          val loadThirdPartyDependencyListFragmentListener = activity as
            LoadThirdPartyDependencyListFragmentListener
          loadThirdPartyDependencyListFragmentListener.loadThirdPartyDependencyListFragment()
        } else {
          val routeToThirdPartyDependencyListListener = activity
            as RouteToThirdPartyDependencyListListener
          routeToThirdPartyDependencyListListener.onRouteToThirdPartyDependencyList()
        }
      }
      resourceHandler.getStringInLocale(R.string.privacy_policy_activity_title) -> {
        if (isMultipane) {
          val loadPrivacyPolicyFragmentListener = activity as
            LoadPrivacyPolicyFragmentListener
          loadPrivacyPolicyFragmentListener.loadPrivacyPolicyFragment()
        } else {
          val routeToPrivacyPolicyListener = activity as RouteToPrivacyPolicyListener
          routeToPrivacyPolicyListener.onRouteToPrivacyPolicy()
        }
      }
      resourceHandler.getStringInLocale(R.string.terms_of_service_activity_title) -> {
        if (isMultipane) {
          val loadTermsOfServiceFragmentFragmentListener = activity as
            LoadTermsOfServiceFragmentListener
          loadTermsOfServiceFragmentFragmentListener.loadTermsOfServiceFragment()
        } else {
          val routeToTermsOfServiceListener = activity as RouteToTermsOfServiceListener
          routeToTermsOfServiceListener.onRouteToTermsOfService()
        }
      }
    }
  }
}
