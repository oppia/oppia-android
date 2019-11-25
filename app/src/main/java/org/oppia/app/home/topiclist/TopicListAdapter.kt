package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.TopicSummaryViewBinding

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class TopicListAdapter(
  private val itemList: MutableList<TopicSummaryViewModel>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding =
      TopicSummaryViewBinding.inflate(
        inflater,
        parent,
        /* attachToParent= */ false
      )
    return TopicListViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as TopicListViewHolder).bind(itemList[position])
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class TopicListViewHolder(
    val binding: TopicSummaryViewBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(topicSummaryViewModel: TopicSummaryViewModel) {
      binding.viewModel = topicSummaryViewModel
    }
  }
}
