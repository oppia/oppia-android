package org.oppia.app.option

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.LanguageItemsBinding

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind languages to [RecyclerView] inside [AppLanguageActivityPresenter] and [DefaultAudioActivityPresenter]. */
class LanguageSelectionAdapter :
  RecyclerView.Adapter<LanguageSelectionAdapter.languageViewHolder>() {

  private var languageList: List<String> = ArrayList()
  private var selectedPosition = -1
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): languageViewHolder {
    val languageListItemBinding = DataBindingUtil.inflate<LanguageItemsBinding>(
      LayoutInflater.from(parent.context),
      R.layout.language_items, parent,
      /* attachToRoot= */ false
    )
    return languageViewHolder(languageListItemBinding)
  }

  override fun onBindViewHolder(languageViewHolder: languageViewHolder, i: Int) {
    languageViewHolder.bind(languageList[i], i)
  }

  override fun getItemCount(): Int {
    return languageList.size
  }

  fun setlanguageList(languageList: List<String>) {
    this.languageList = languageList
    notifyDataSetChanged()
  }

  inner class languageViewHolder(val binding: LanguageItemsBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(language: String, position: Int) {
      binding.setVariable(BR.languageString, language)

        if (selectedPosition == position)
          binding.languageRadioButton.setChecked(true)
        else
          binding.languageRadioButton.setChecked(false)

      binding.radioContainer.setOnClickListener {
        selectedPosition = getAdapterPosition()
        notifyDataSetChanged()
      }
    }
  }
}
