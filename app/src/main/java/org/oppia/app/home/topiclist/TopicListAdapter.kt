package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.library.baseAdapters.BR
import org.oppia.app.R
import org.oppia.app.databinding.PromotedStoryCardBinding

private const val VIEW_TYPE_PROMOTED_STORY = 1

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class TopicListAdapter(
  private val itemList: MutableList<Any>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  lateinit var promotedStoryViewModel: PromotedStoryViewModel

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#249): Generalize this binding to make adding future items easier.
      VIEW_TYPE_PROMOTED_STORY -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<PromotedStoryCardBinding>(
            inflater,
            R.layout.promoted_story_card,
            parent,
            /* attachToParent= */false
          )
        PromotedStoryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type") as Throwable
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_PROMOTED_STORY -> {
        (holder as PromotedStoryViewHolder).bind(itemList[position] as PromotedStoryViewModel)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is PromotedStoryViewModel -> {
        promotedStoryViewModel = itemList[position] as PromotedStoryViewModel
        VIEW_TYPE_PROMOTED_STORY
      }
      else -> throw IllegalArgumentException("Invalid type of data $position")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class PromotedStoryViewHolder(
    val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(promotedStoryViewModel: PromotedStoryViewModel) {
      binding.setVariable(BR.viewModel, promotedStoryViewModel)
    }
  }
}
