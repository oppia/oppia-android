package org.oppia.app.options

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AppLanguageActivityBinding
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  private lateinit var prefSummaryValue: String
  private lateinit var profileId: ProfileId

  fun handleOnCreate(prefKey: String, prefSummaryValue: String, internalProfileId: Int) {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(activity, R.layout.app_language_activity)

    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    Log.d("profileId","==="+internalProfileId)
    this.prefSummaryValue = prefSummaryValue

    Log.d("prefSummaryValue","==="+prefSummaryValue)
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey)
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    binding.appLanguageToolbar.setNavigationOnClickListener {
      updateAppLanguage(languageSelectionAdapter.getSelectedLanguage())
      val message = languageSelectionAdapter.getSelectedLanguage()
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_APP_LANGUAGE, message)
      (activity as AppLanguageActivity).setResult(REQUEST_CODE_APP_LANGUAGE, intent)
      activity.finish()
    }
    createAdapter()
  }
  

  fun updateAppLanguage(language: String) {
    when (language) {
      getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.ENGLISH_APP_LANGUAGE
        )
      }
      getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.HINDI_APP_LANGUAGE
        )
      }
      getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.CHINESE_APP_LANGUAGE
        )
      }
      getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.FRENCH_APP_LANGUAGE
        )
      }
    }

  }

  private fun createAdapter() {
    // TODO(#669): Replace dummy list with actual language list from backend.
    val languageList = ArrayList<String>()
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)
    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }

  fun getAppLanguage(appLanguage: AppLanguage): String {
    return when (appLanguage) {
      AppLanguage.ENGLISH_APP_LANGUAGE -> "English"
      AppLanguage.HINDI_APP_LANGUAGE -> "Hindi"
      AppLanguage.FRENCH_APP_LANGUAGE -> "French"
      AppLanguage.CHINESE_APP_LANGUAGE -> "Chinese"
      else -> "English"
    }
  }
  
}
