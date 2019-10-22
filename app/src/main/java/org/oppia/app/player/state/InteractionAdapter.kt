package org.oppia.app.player.state;

import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_selection_interaction_items.view.*
import kotlinx.android.synthetic.main.multiple_choice_interaction_items.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.player.state.itemviewmodel.SelectionContentViewModel
import org.oppia.util.parser.HtmlParser

const val VIEW_TYPE_MULTIPLE_CHOICE = 1
const val VIEW_TYPE_ITEM_SELECTION = 2

/** Adapter to bind the interactions to the [RecyclerView]. It handles MultipleChoiceInput and ItemSelectionInput interaction views. */
class InteractionAdapter(
  private val htmlParserFactory: HtmlParser.Factory,
  private val entityType: String,
  private val explorationId: String,
  val itemList: Array<String>?,
  private val interactionId: String
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
        itemList!!.get(position) as SelectionContentViewModel,
        position,
        mSelectedItem
      )
      VIEW_TYPE_ITEM_SELECTION -> (holder as ItemSelectionViewHolder).bind(
        itemList!!.get(position) as SelectionContentViewModel,
        position,
        mSelectedItem
      )
    }
  }

  // Determines the appropriate ViewType according to the interaction type.
  override fun getItemViewType(position: Int): Int {
    return if (interactionId.equals("ItemSelectionInput")) {
      VIEW_TYPE_ITEM_SELECTION
    } else {
      VIEW_TYPE_MULTIPLE_CHOICE
    }
  }

  override fun getItemCount(): Int {
    return itemList!!.size
  }

  private inner class ItemSelectionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: SelectionContentViewModel, position: Int, selectedPosition: Int) {
      binding.setVariable(BR.htmlContent, rawString.htmlContent)
      binding.executePendingBindings();
      val htmlResult: Spannable = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
        rawString.htmlContent,
        binding.root.tv_item_selection_contents
      )
//      val html: Spannable = HtmlParser(context,entityType,entityId).parseHtml(rawString, binding.root.tv_item_selection_contents)
      binding.root.tv_item_selection_contents.text = htmlResult

      binding.root.rl_checkbox_container.setOnClickListener {
        if (binding.root.cb_item_selection.isChecked)
          binding.root.cb_item_selection.setChecked(false)
        else
          binding.root.cb_item_selection.setChecked(true)
        notifyDataSetChanged()
      }
    }
  }

  private inner class MultipleChoiceViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: SelectionContentViewModel, position: Int, selectedPosition: Int) {
      binding.setVariable(BR.htmlContent, rawString.htmlContent)
      binding.executePendingBindings();
      val htmlResult: Spannable = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
        rawString.htmlContent,
        binding.root.tv_multiple_choice_contents
      )
      binding.root.tv_item_selection_contents.text = htmlResult

      if (selectedPosition == position)
        binding.root.rb_multiple_choice.setChecked(true)
      else
        binding.root.rb_multiple_choice.setChecked(false)

      binding.root.rl_radio_container.setOnClickListener {
        mSelectedItem = getAdapterPosition()
        notifyDataSetChanged()
      }
    }
  }
}
