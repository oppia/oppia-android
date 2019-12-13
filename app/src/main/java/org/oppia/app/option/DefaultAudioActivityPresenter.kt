package org.oppia.app.option

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.DefaultAudioActivityBinding
import javax.inject.Inject

/** The presenter for [DefaultAudioActivity]. */
@ActivityScope
class DefaultAudioActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<DefaultAudioActivityBinding>(activity, R.layout.default_audio_activity)

    languageSelectionAdapter = LanguageSelectionAdapter()
    binding.audioLanguageRecyclerView.apply {
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
