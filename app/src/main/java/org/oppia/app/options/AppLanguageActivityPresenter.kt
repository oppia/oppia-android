package org.oppia.app.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AppLanguageActivityBinding
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  private var prefSummaryValue: String? = null

  fun handleOnCreate(prefKey: String, prefSummaryValue: String) {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(activity, R.layout.app_language_activity)

    this.prefSummaryValue = prefSummaryValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey)
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    binding.appLanguageToolbar.setNavigationOnClickListener {
      val message = prefSummaryValue
      val intent = Intent()
      intent.putExtra(MESSAGE_APP_LANGUAGE, message)
      (activity as AppLanguageActivity).setResult(2, intent)
      activity.finish()
    }
    createAdapter()
  }

  private fun createAdapter() {
    val languageList = ArrayList<String>()
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)

    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }
}
