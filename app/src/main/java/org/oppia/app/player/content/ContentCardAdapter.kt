package org.oppia.app.player.content

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.util.data.URLImageParser
import androidx.databinding.DataBindingUtil
import org.oppia.app.databinding.ContentCardItemsBinding

class ContentCardAdapter(
  internal var context: Context,
  contentList: MutableList<GaeSubtitledHtml>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val VIEW_TYPE_CONTENT = 1
  private val VIEW_TYPE_RIGHT_INTERACTION = 2

  var interactionType: String? = null
  internal var contentList: MutableList<GaeSubtitledHtml>?

  init {
    this.contentList = contentList
    this.interactionType = interactionType;

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    var view: View

    if (viewType == VIEW_TYPE_CONTENT) {
//      view = LayoutInflater.from(parent.getContext())
//        .inflate(R.layout.content_card_items, parent, false);

      val binding = ContentCardItemsBinding.inflate(
        LayoutInflater.from(parent.context),parent,
        false
      )
      val viewHolder = ContentViewHolder(binding.root)
      return viewHolder
    } else {
      view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.right_interaction_card_item, parent, false);
      return RightInteractionViewHolder(view);
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> (holder as ContentViewHolder).bind(contentList!!.get(position))
      VIEW_TYPE_RIGHT_INTERACTION -> (holder as RightInteractionViewHolder).bind(contentList!!.get(position))
    }

  }

  inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal var tvContents: TextView

    init {
      tvContents = view.findViewById(R.id.tvContents)

    }

    fun bind(gaeSubtitledHtml: GaeSubtitledHtml) {

      val imageGetter = URLImageParser(tvContents, context)
      val html: Spannable
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        html = Html.fromHtml(gaeSubtitledHtml.html, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
      } else {
        html = Html.fromHtml(gaeSubtitledHtml.html, imageGetter, null) as Spannable
      }
      tvContents.text = html
    }
  }

  inner class RightInteractionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal var tvContents: TextView

    init {
      tvContents = view.findViewById(R.id.tvContents)

    }

    fun bind(gaeSubtitledHtml: GaeSubtitledHtml) {

      val imageGetter = URLImageParser(tvContents, context)
      val html: Spannable
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        html = Html.fromHtml(gaeSubtitledHtml.html, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
      } else {
        html = Html.fromHtml(gaeSubtitledHtml.html, imageGetter, null) as Spannable
      }
      tvContents.text = html
    }
  }

  // Determines the appropriate ViewType according to the sender of the message.
  override fun getItemViewType(position: Int): Int {

    if (contentList!!.get(position).contentId!!.contains("content") || contentList!!.get(position).contentId!!.contains(
        "feedback")) {

      return VIEW_TYPE_CONTENT
    } else {
      // If some other user sent the message
      return VIEW_TYPE_RIGHT_INTERACTION
    }
  }

  override fun getItemCount(): Int {
    return contentList!!.size
  }

}
