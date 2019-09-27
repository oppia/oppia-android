package org.oppia.app.player.content

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_card_items.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ContentCardItemsBinding
import org.oppia.app.databinding.LearnersCardItemBinding
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.util.data.UrlImageParser

/** Adapter to bind the contents to the [RecyclerView]. It handles rich-text content. */
class ContentCardAdapter(
  private val context: Context,
  val contentList: MutableList<GaeSubtitledHtml>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val VIEW_TYPE_CONTENT = 1
  private val VIEW_TYPE_LEARNER = 2

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

    return when (viewType) {
      VIEW_TYPE_CONTENT -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<ContentCardItemsBinding>(inflater, R.layout.content_card_items, parent, false)
        ContentViewHolder(binding)
      }
      VIEW_TYPE_LEARNER -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<LearnersCardItemBinding>(inflater, R.layout.learners_card_item, parent, false)
        LearnersViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> (holder as ContentViewHolder).bind(contentList!!.get(position).html)
      VIEW_TYPE_LEARNER -> (holder as LearnersViewHolder).bind(contentList!!.get(position).html)
    }
  }

  // Determines the appropriate ViewType according to the sender of the message.
  override fun getItemViewType(position: Int): Int {

    return if (!contentList!!.get(position).contentId!!.contains("content") &&
      !contentList!!.get(position).contentId!!.contains(
        "Feedback"
      )
    ) {
      VIEW_TYPE_LEARNER
    } else {
      VIEW_TYPE_CONTENT
    }
  }

  override fun getItemCount(): Int {
    return contentList!!.size
  }

  inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(rawString: String?) {

      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();

      val html: Spannable = parseHtml(rawString, binding.root.tvContents)

      binding.root.tvContents.text = html
    }
  }

  private fun parseHtml(rawString: String?, tvContents: TextView): Spannable {
    val html: Spannable
    var htmlContent = rawString

    var CUSTOM_TAG = "oppia-noninteractive-image"
    var HTML_TAG = "img"
    var CUSTOM_ATTRIBUTE = "filepath-with-value"
    var HTML_ATTRIBUTE = "src"

    if (htmlContent!!.contains(CUSTOM_TAG)) {

      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    var imageGetter = UrlImageParser(tvContents, context)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }
    return html
  }

  inner class LearnersViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(rawString: String?) {

      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();

      val html: Spannable = parseHtml(rawString, binding.root.tvContents)

      binding.root.tvContents.text = html
    }
  }
}
