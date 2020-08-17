package org.oppia.app.options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.LanguageItemsBinding

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind languages to [RecyclerView] inside [AppLanguageActivityPresenter] and [DefaultAudioActivityPresenter]. */
class LanguageSelectionAdapter(
  private val prefKey: String,
  private val onLanguageClicked: (language: String) -> Unit = {}
) :
  RecyclerView.Adapter<LanguageSelectionAdapter.LanguageViewHolder>() {

  private var prefSummaryValue: String? = null
  private var languageList: List<String> = ArrayList()
  private lateinit var selectedLanguage: String
  private var selectedPosition: Int = -1
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
    val languageListItemBinding = DataBindingUtil.inflate<LanguageItemsBinding>(
      LayoutInflater.from(parent.context),
      R.layout.language_items, parent,
      /* attachToRoot= */ false
    )
    return LanguageViewHolder(languageListItemBinding)
  }

  override fun onBindViewHolder(languageViewHolder: LanguageViewHolder, i: Int) {
    languageViewHolder.bind(languageList[i], i)
  }

  override fun getItemCount(): Int {
    return languageList.size
  }

  fun setLanguageList(languageList: List<String>) {
    this.languageList = languageList
    notifyDataSetChanged()
  }

  fun setDefaultLanguageSelected(prefSummaryValue: String?) {
    selectedPosition = languageList.indexOf(prefSummaryValue)
    this.prefSummaryValue = prefSummaryValue
  }

  fun getSelectedLanguage(): String {
    selectedLanguage = languageList[selectedPosition]
    return selectedLanguage
  }

  inner class LanguageViewHolder(val binding: LanguageItemsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(language: String, position: Int) {
      binding.setVariable(BR.languageString, language)
      binding.languageRadioButton.isChecked = position == selectedPosition
      binding.radioContainer.setOnClickListener {
        if (prefKey == APP_LANGUAGE) {
          selectedPosition = adapterPosition
          notifyDataSetChanged()
          onLanguageClicked.invoke(getSelectedLanguage())
        } else {
          selectedPosition = adapterPosition
          notifyDataSetChanged()
        }
      }
    }
  }
}
