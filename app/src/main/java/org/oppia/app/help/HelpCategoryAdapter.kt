package org.oppia.app.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.HelpItemBinding

/** The adapter to set up the recycler view in the [HelpFragment] */
class HelpCategoryAdapter(
  private val arrayList: ArrayList<HelpViewModel>
) :
  RecyclerView.Adapter<HelpCategoryAdapter.HelpItemView>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): HelpCategoryAdapter.HelpItemView {
    val layoutInflater = LayoutInflater.from(parent.context)
    val helpItemBinding = HelpItemBinding.inflate(
      layoutInflater,
      parent,
      /* attachToParent= */ false
    )
    return HelpItemView(helpItemBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: HelpCategoryAdapter.HelpItemView, position: Int) {
    holder.bind(arrayList[position])
  }

  class HelpItemView(private val helpItemBinding: HelpItemBinding) :
    RecyclerView.ViewHolder(helpItemBinding.root) {
    fun bind(helpViewModel: HelpViewModel) {
      this.helpItemBinding.viewmodel = helpViewModel
      helpItemBinding.executePendingBindings()
    }
  }
}
