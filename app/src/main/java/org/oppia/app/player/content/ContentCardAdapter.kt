package org.oppia.app.player.content

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_card_items.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ContentCardItemsBinding
import org.oppia.app.databinding.RightInteractionCardItemBinding
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.util.data.UrlImageParser
import org.xml.sax.Attributes

/** Adapter to bind the HTML contents to the [RecyclerView]. */
class ContentCardAdapter(
  internal var context: Context,
  contentList: MutableList<GaeSubtitledHtml>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val VIEW_TYPE_CONTENT = 1
  private val VIEW_TYPE_RIGHT_INTERACTION = 2

  internal var contentList: MutableList<GaeSubtitledHtml>?

  init {
    this.contentList = contentList
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    if (viewType == VIEW_TYPE_CONTENT) {
      val inflater = LayoutInflater.from(parent.getContext())
      val binding =
        DataBindingUtil.inflate<ContentCardItemsBinding>(inflater, R.layout.content_card_items, parent, false)
      return ContentViewHolder(binding)
    } else {
      val inflater = LayoutInflater.from(parent.getContext())
      val binding = DataBindingUtil.inflate<RightInteractionCardItemBinding>(
        inflater,
        R.layout.right_interaction_card_item,
        parent,
        false
      )
      return RightInteractionViewHolder(binding)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> (holder as ContentViewHolder).bind(contentList!!.get(position).html)
      VIEW_TYPE_RIGHT_INTERACTION -> (holder as RightInteractionViewHolder).bind(contentList!!.get(position).html)
    }
  }

  inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(str: String?) {

      var htmlContent = str

      binding.setVariable(BR.htmlContent, htmlContent)
      binding.executePendingBindings();

      var CUSTOM_TAG = "oppia-noninteractive-image"
      var HTML_TAG = "img"
      var CUSTOM_ATTRIBUTE = "filepath-with-value"
      var HTML_ATTRIBUTE = "src"

      if (htmlContent!!.contains(CUSTOM_TAG)) {

        htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
        htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
        htmlContent = htmlContent.replace("&amp;quot;", "")
      }

      var imageGetter = UrlImageParser(binding.root.tvContents, context)
      val html: Spannable
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
      } else {
        html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
      }
      binding.root.tvContents.text = html
    }
  }

  inner class RightInteractionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(htmlContent: String?) {
      binding.setVariable(BR.htmlContent, htmlContent)
      binding.executePendingBindings();

      var result: Spanned
      result = HtmlParser.buildSpannedText(binding.root.tvContents,context,htmlContent,
        HtmlParser.TagHandler()
        { b: Boolean, s: String, editable: Editable?, attributes: Attributes? ->
          if (b && s.equals("oppia-noninteractive-image")) {
            var value: String? = HtmlParser.getValue(attributes, "filepath-with-value");

            // unescapeEntities method to remove html quotes
            var strictMode: Boolean = true;
            var unescapedString: String = org.jsoup.parser.Parser.unescapeEntities(value, strictMode);
            Log.d("value", "*****" + value)
            Log.d("unescapedString", "*****" + unescapedString)
          }
          false;

        })
      binding.root.tvContents.text = result
//
//      var imageGetter = UrlImageParser(binding.root.tvContents, context)
//      val html: Spannable
//      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//        html = Html.fromHtml(result.toString(), Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
//      } else {
//        html = Html.fromHtml(result.toString(), imageGetter,null) as Spannable
//      }
//
//      binding.root.tvContents.text = html
    }
  }

  // Determines the appropriate ViewType according to the sender of the message.
  override fun getItemViewType(position: Int): Int {

    if (!contentList!!.get(position).contentId!!.contains("content") && !contentList!!.get(position).contentId!!.contains(
        "Feedback"
      )
    ) {
      return VIEW_TYPE_RIGHT_INTERACTION
    } else {
      return VIEW_TYPE_CONTENT
    }
  }

  override fun getItemCount(): Int {
    return contentList!!.size
  }
}
