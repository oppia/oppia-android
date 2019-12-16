package org.oppia.app.option

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
  private lateinit var  languageSelectionAdapter:LanguageSelectionAdapter
  private var prefSummaryValue: String? = null

  fun handleOnCreate(prefKey: String, prefSummaryValue: String) {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(activity, R.layout.app_language_activity)

    this.prefSummaryValue = prefSummaryValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey)
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }
    binding.toolbar.setNavigationOnClickListener {
      val message = prefSummaryValue
      val intent = Intent()
      intent.putExtra("MESSAGE", message)
      (activity as AppLanguageActivity).setResult(2, intent)
      (activity as AppLanguageActivity).finish()//finishing activity
    }
    createAdapter()
  }
  private fun createAdapter() {
    val languageList = ArrayList<String>()//Creating an empty dummy arraylist
    languageList.add("English")//Adding object in dummy arraylist
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setlanguageList(languageList)
    
    languageSelectionAdapter.setDefaultlanguageSelected(prefSummaryValue = prefSummaryValue)
  }
}
