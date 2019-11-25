package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.PromotedStoryCardBinding

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class PromotedStoryListAdapter(
  private val itemList: MutableList<PromotedStoryViewModel>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding =
      PromotedStoryCardBinding.inflate(
        inflater,
        parent,
        /* attachToParent= */ false
      )
    return PromotedStoryListViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as PromotedStoryListViewHolder).bind(itemList[position])
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class PromotedStoryListViewHolder(
    val binding: PromotedStoryCardBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(promotedStoryViewModel: PromotedStoryViewModel) {
      binding.viewModel = promotedStoryViewModel
    }
  }
}
