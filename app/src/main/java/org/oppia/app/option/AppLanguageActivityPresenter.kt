package org.oppia.app.option

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
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(activity, R.layout.app_language_activity)

    languageSelectionAdapter = LanguageSelectionAdapter()
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }
    binding.toolbar.setNavigationOnClickListener {
      (activity as AppLanguageActivity).finish()
    }
    createAdapter()
  }
  private fun createAdapter() {
    val languageList = ArrayList<String>()//Creating an empty arraylist
    languageList.add("English")//Adding object in arraylist
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setlanguageList(languageList)

  }
}
