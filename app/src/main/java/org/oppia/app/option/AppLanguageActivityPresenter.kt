package org.oppia.app.option

import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AppLanguageActivityBinding
import org.oppia.app.databinding.LanguageItemsBinding
import org.oppia.app.recyclerview.BindableAdapter
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity):OptionSelectorListener {


  private lateinit var  languageSelectionAdapter:LanguageSelectionAdapter
  fun handleOnCreate(pref_key: String) {
    val binding = DataBindingUtil.setContentView<AppLanguageActivityBinding>(activity, R.layout.app_language_activity)

    languageSelectionAdapter = LanguageSelectionAdapter(pref_key,this)
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

  override fun storyTextSizeSelected(textSize: String, pref_key: String) {
    Log.d("interface","=="+textSize)
  }

  override fun appLanguageSelected(appLanguage: String, pref_key: String) {
    Log.d("interface","=="+appLanguage)
  }

  override fun audioLanguageSelected(audioLanguage: String, pref_key: String) {
    Log.d("interface","=="+audioLanguage)
  }

}
