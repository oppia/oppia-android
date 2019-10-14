package org.oppia.app.player.content

import android.content.Context
import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_card_items.view.*
import kotlinx.android.synthetic.main.interation_feedback_card_item.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ContentCardItemsBinding
import org.oppia.app.databinding.InterationFeedbackCardItemBinding
import org.oppia.util.data.HtmlParser

// TODO(#172): Make use of generic data-binding-enabled RecyclerView adapter
/** Adapter to bind the contents to the [RecyclerView]. It handles rich-text content. */
class ContentCardAdapter(
  private val context: Context,
  private val entity_type: String,
  private val entity_id: String,
  val contentList: MutableList<ContentViewModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  val VIEW_TYPE_CONTENT = 1
  val VIEW_TYPE_INTERACTION_FEEDBACK = 2

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_CONTENT -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<ContentCardItemsBinding>(inflater, R.layout.content_card_items, parent, false)
        ContentViewHolder(binding)
      }
      VIEW_TYPE_INTERACTION_FEEDBACK -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<InterationFeedbackCardItemBinding>(
            inflater,
            R.layout.interation_feedback_card_item,
            parent,
            false
          )
        InteractionFeedbackViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> (holder as ContentViewHolder).bind(contentList!!.get(position).html_content.get())
      VIEW_TYPE_INTERACTION_FEEDBACK -> (holder as InteractionFeedbackViewHolder).bind(contentList!!.get(position).html_content.get())
    }
  }

  // Determines the appropriate ViewType according to the content_id.
  override fun getItemViewType(position: Int): Int {
    return if (!contentList!!.get(position).contentId.get()!!.contains("content") &&
      !contentList!!.get(position).contentId.get()!!.contains(
        "Feedback"
      )
    ) {
      VIEW_TYPE_INTERACTION_FEEDBACK
    } else {
      VIEW_TYPE_CONTENT
    }
  }

  override fun getItemCount(): Int {
    return contentList!!.size
  }

  private inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      val html: Spannable = HtmlParser(context, entity_type, entity_id).parseHtml(rawString, binding.root.tv_contents)
      binding.root.tv_contents.text = html
    }
  }

  private inner class InteractionFeedbackViewHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      val html: Spannable =
        HtmlParser(context, entity_type, entity_id).parseHtml(rawString, binding.root.tv_interaction_feedback)
      binding.root.tv_interaction_feedback.text = html
    }
  }
}
