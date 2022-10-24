package org.oppia.android.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** View model in [HelpFragment]. */
class HelpListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) : HelpViewModel() {
  private val arrayList = ArrayList<HelpItemViewModel>()

  val helpItemList: List<HelpItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<HelpItemViewModel> {
    for (item in HelpItems.values()) {
      val category = when (item) {
        HelpItems.FAQ -> resourceHandler.getStringInLocale(R.string.help_activity_frequently_asked_questions_FAQ_text)
        HelpItems.THIRD_PARTY -> resourceHandler.getStringInLocale(
          R.string.third_party_dependency_list_activity_title
        )
        HelpItems.PRIVACY_POLICY -> resourceHandler.getStringInLocale(
          R.string.help_activity_privacy_policy_title
        )
        HelpItems.TERMS_OF_SERVICE -> resourceHandler.getStringInLocale(
          R.string.help_activity_terms_of_service_title
        )
      }
      arrayList += HelpItemViewModel(
        activity,
        category,
        isMultipane.get() ?: false,
        resourceHandler
      )
    }
    return arrayList
  }
}
