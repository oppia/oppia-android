package org.oppia.app.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.ui.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.databinding.AppLanguageActivityBinding
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  private lateinit var prefSummaryValue: String

  fun handleOnCreate(prefKey: String, prefSummaryValue: String) {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(
      activity,
      R.layout.app_language_activity
    )
    this.prefSummaryValue = prefSummaryValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey)
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    binding.appLanguageToolbar.setNavigationOnClickListener {
      val message = languageSelectionAdapter.getSelectedLanguage()
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_APP_LANGUAGE, message)
      (activity as AppLanguageActivity).setResult(REQUEST_CODE_APP_LANGUAGE, intent)
      activity.finish()
    }
    createAdapter()
  }

  fun getLanguageSelected(): String {
    return languageSelectionAdapter.getSelectedLanguage()
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
}
