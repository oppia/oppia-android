package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.library.baseAdapters.BR
import kotlinx.android.synthetic.main.content_item.view.*
import kotlinx.android.synthetic.main.interation_read_only_item.view.*
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.InterationReadOnlyItemBinding

class StateAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  val VIEW_TYPE_CONTENT = 1
  val VIEW_TYPE_INTERACTION_READ_ONLY = 2
  val VIEW_TYPE_INTERACTION = 3
  val VIEW_TYPE_STATE_BUTTON = 4

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_CONTENT -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<ContentItemBinding>(
            inflater,
            R.layout.content_item,
            parent,
            /* attachToParent= */false
          )
        ContentViewHolder(binding)
      }
      VIEW_TYPE_INTERACTION_READ_ONLY -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<InterationReadOnlyItemBinding>(
            inflater,
            R.layout.interation_read_only_item,
            parent,
            /* attachToParent= */false
          )
        InteractionFeedbackViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> (holder as ContentViewHolder).bind("Sample String")
      VIEW_TYPE_INTERACTION_READ_ONLY -> (holder as InteractionFeedbackViewHolder).bind("Sample Interaction")
    }
  }

  // Determines the appropriate ViewType according to the content_id.
//  override fun getItemViewType(position: Int): Int {
//    val contentId = contentList.get(position).contentId
//    return if (!contentId.contains("content") && !contentId.contains("Feedback")) {
//      VIEW_TYPE_INTERACTION_READ_ONLY
//    } else {
//      VIEW_TYPE_CONTENT
//    }
//  }

  fun addItem() {

  }

  override fun getItemCount(): Int {
    return 0
  }

  private class ContentViewHolder(
    val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      binding.root.content_text_view.text = rawString
    }
  }

  private class InteractionFeedbackViewHolder(
    val binding: ViewDataBinding
  ) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      binding.root.interaction_read_only_text_view.text = rawString
    }
  }
}
