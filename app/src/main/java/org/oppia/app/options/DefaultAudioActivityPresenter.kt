package org.oppia.app.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.DefaultAudioActivityBinding
import javax.inject.Inject

/** The presenter for [DefaultAudioActivity]. */
@ActivityScope
class DefaultAudioActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {


  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter

  fun handleOnCreate(prefKey: String, prefSummaryValue: String) {
    val binding = DataBindingUtil.setContentView<DefaultAudioActivityBinding>(activity, R.layout.default_audio_activity)

    this.prefSummaryValue = prefSummaryValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey)
    binding.audioLanguageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    binding.audioLanguageToolbar.setNavigationOnClickListener {
      val message = prefSummaryValue
      val intent = Intent()
      intent.putExtra("MESSAGE", message)
      (activity as DefaultAudioActivity).setResult(3, intent)
      activity.finish()
    }
    createAdapter()
  }

  private fun createAdapter() {
    val languageList = ArrayList<String>()
    languageList.add("No Audio")
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)

    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }
}
