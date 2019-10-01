package org.oppia.app.player.state;

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_selection_interaction_items.view.*
import kotlinx.android.synthetic.main.multiple_choice_interaction_items.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.util.data.UrlImageParser

const val CUSTOM_TAG = "oppia-noninteractive-image"
const val HTML_TAG = "img"
const val CUSTOM_ATTRIBUTE = "filepath-with-value"
const val HTML_ATTRIBUTE = "src"

const val VIEW_TYPE_MULTIPLE_CHOICE = 1
const val VIEW_TYPE_ITEM_SELECTION = 2

/** Adapter to bind the interactions to the [RecyclerView]. It handles MultipleChoiceInput and ItemSelectionInput interaction views. */
class InteractionAdapter(
  private val context: Context,
  private val entity_type: String,
  private val entity_id: String,
  val itemList: Array<String>?,
  val interactionInstanceId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var mSelectedItem = -1

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_MULTIPLE_CHOICE -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<MultipleChoiceInteractionItemsBinding>(
            inflater,
            R.layout.multiple_choice_interaction_items,
            parent,
            false
          )
        MultipleChoiceViewHolder(binding)
      }
      VIEW_TYPE_ITEM_SELECTION -> {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding =
          DataBindingUtil.inflate<ItemSelectionInteractionItemsBinding>(
            inflater,
            R.layout.item_selection_interaction_items,
            parent,
            false
          )
        ItemSelectionViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_MULTIPLE_CHOICE -> (holder as MultipleChoiceViewHolder).bind(
        itemList!!.get(position),
        position,
        mSelectedItem
      )
      VIEW_TYPE_ITEM_SELECTION -> (holder as ItemSelectionViewHolder).bind(
        itemList!!.get(position),
        position,
        mSelectedItem
      )
    }
  }

  // Determines the appropriate ViewType according to the interaction type.
  override fun getItemViewType(position: Int): Int {
    return if (interactionInstanceId.equals("ItemSelectionInput")) {
      VIEW_TYPE_ITEM_SELECTION
    } else {
      VIEW_TYPE_MULTIPLE_CHOICE
    }
  }

  override fun getItemCount(): Int {
    return itemList!!.size
  }

  private inner class ItemSelectionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
   internal fun bind(rawString: String?, position: Int, selectedPosition: Int) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      val html: Spannable = parseHtml(rawString, binding.root.tv_item_selection_contents)
      binding.root.tv_item_selection_contents.text = html

      binding.root.rl_checkbox_container.setOnClickListener {
        if (binding.root.cb_item_selection.isChecked)
          binding.root.cb_item_selection.setChecked(false)
        else
          binding.root.cb_item_selection.setChecked(true)
        Toast.makeText(context, "" + binding.root.tv_item_selection_contents.text, Toast.LENGTH_LONG).show()
        notifyDataSetChanged()
      }
    }
  }

  private inner class MultipleChoiceViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
   internal fun bind(rawString: String?, position: Int, selectedPosition: Int) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings();
      val html: Spannable = parseHtml(rawString, binding.root.tv_multiple_choice_contents)
      binding.root.tv_multiple_choice_contents.text = html

      if (selectedPosition == position)
        binding.root.rb_multiple_choice.setChecked(true)
      else
        binding.root.rb_multiple_choice.setChecked(false)

      binding.root.rl_radio_container.setOnClickListener {
        Toast.makeText(context, "" + binding.root.tv_multiple_choice_contents.text, Toast.LENGTH_LONG).show()
        mSelectedItem = getAdapterPosition()
        notifyDataSetChanged()
      }
    }
  }

  private fun parseHtml(rawString: String?, tvContents: TextView): Spannable {
    val html: Spannable
    var htmlContent = rawString
    if (htmlContent!!.contains(CUSTOM_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }
    var imageGetter = UrlImageParser(tvContents, context, entity_type, entity_id)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }
    return html
  }
}
