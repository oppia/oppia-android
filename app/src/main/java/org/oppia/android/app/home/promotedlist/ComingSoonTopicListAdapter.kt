package org.oppia.android.app.home.promotedlist

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.databinding.ComingSoonTopicViewBinding

/** Adapter to bind promoted stories to [RecyclerView] inside [HomeFragment] to create carousel. */
class ComingSoonTopicListAdapter(
  private val activity: AppCompatActivity,
  private val itemList: MutableList<ComingSoonTopicsViewModel>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var spanCount = 0

  private val orientation = Resources.getSystem().configuration.orientation

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding =
      ComingSoonTopicViewBinding.inflate(
        inflater,
        parent,
        /* attachToParent= */ false
      )
    return ComingSoonTopicsViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as ComingSoonTopicsViewHolder).bind(itemList[position])
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  fun setSpanCount(spanCount: Int) {
    this.spanCount = spanCount
  }

  inner class ComingSoonTopicsViewHolder(
    val binding: ComingSoonTopicViewBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(comingSoonTopicsListViewModel: ComingSoonTopicsViewModel) {
      binding.viewModel = comingSoonTopicsListViewModel
    }
  }
}