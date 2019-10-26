package org.oppia.app.player.state

import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_selection_interaction_items.view.item_selection_contents_text_view
import kotlinx.android.synthetic.main.item_selection_interaction_items.view.checkbox_container
import kotlinx.android.synthetic.main.item_selection_interaction_items.view.item_selection_checkbox
import kotlinx.android.synthetic.main.multiple_choice_interaction_items.view.multiple_choice_content_text_view
import kotlinx.android.synthetic.main.multiple_choice_interaction_items.view.multiple_choice_radio_button
import kotlinx.android.synthetic.main.multiple_choice_interaction_items.view.radio_container
import org.oppia.app.R
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.util.parser.HtmlParser

private const val VIEW_TYPE_RADIO_BUTTONS = 1
private const val VIEW_TYPE_CHECKBOXES = 2
private const val INTERACTION_ADAPTER_TAG = "Interaction Adapter"

/**
 * Adapter to bind the interactions to the [RecyclerView]. It handles MultipleChoiceInput
 * and ItemSelectionInput interaction views.
 * */
class SelectionInteractionAdapter(
  private val htmlParserFactory: HtmlParser.Factory,
  private val entityType: String,
  private val explorationId: String,
  private val itemList: List<SelectionInteractionContentViewModel>,
  private val selectionInteractionViewModel: SelectionInteractionViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_RADIO_BUTTONS -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<MultipleChoiceInteractionItemsBinding>(
            inflater,
            R.layout.multiple_choice_interaction_items,
            parent,
            /* attachToParent= */ false
          )
        MultipleChoiceViewHolder(binding)
      }
      VIEW_TYPE_CHECKBOXES -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<ItemSelectionInteractionItemsBinding>(
            inflater,
            R.layout.item_selection_interaction_items,
            parent,
            /* attachToParent= */ false
          )
        ItemSelectionViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_RADIO_BUTTONS -> (holder as MultipleChoiceViewHolder).bind(itemList[position])
      VIEW_TYPE_CHECKBOXES -> (holder as ItemSelectionViewHolder).bind(itemList[position])
    }
  }

  // Determines the appropriate ViewType according to the interaction type.
  override fun getItemViewType(position: Int): Int {
    return if (selectionInteractionViewModel.interactionId == "ItemSelectionInput") {
      if (selectionInteractionViewModel.maxAllowableSelectionCount > 1) {
        VIEW_TYPE_CHECKBOXES
      } else {
        VIEW_TYPE_RADIO_BUTTONS
      }
    } else {
      VIEW_TYPE_RADIO_BUTTONS
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private inner class ItemSelectionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(selectionInteractionContentViewModel: SelectionInteractionContentViewModel) {
      binding.setVariable(BR.htmlContent, selectionInteractionContentViewModel.htmlContent)
      binding.executePendingBindings()
      val htmlResult: Spannable = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
        selectionInteractionContentViewModel.htmlContent,
        binding.root.item_selection_contents_text_view
      )
      binding.root.item_selection_contents_text_view.text = htmlResult
      binding.root.item_selection_checkbox.isChecked = selectionInteractionContentViewModel.isAnswerSelected
      binding.root.checkbox_container.setOnClickListener {
        if (binding.root.item_selection_checkbox.isChecked) {
          itemList[adapterPosition].isAnswerSelected = false
          selectionInteractionViewModel.selectedItems -= adapterPosition
        } else {
          val selectedItemCount = selectionInteractionViewModel.selectedItems.size
          if (selectedItemCount != selectionInteractionViewModel.maxAllowableSelectionCount) {
            itemList[adapterPosition].isAnswerSelected = true
            selectionInteractionViewModel.selectedItems += adapterPosition
          } else {
            Log.d(
              INTERACTION_ADAPTER_TAG,
              "You cannot select more than ${selectionInteractionViewModel.maxAllowableSelectionCount} options"
            )
          }
        }
        notifyDataSetChanged()
      }
    }
  }

  private inner class MultipleChoiceViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(selectionInteractionContentViewModel: SelectionInteractionContentViewModel) {
      binding.setVariable(BR.htmlContent, selectionInteractionContentViewModel.htmlContent)
      binding.executePendingBindings()
      val htmlResult: Spannable = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
        selectionInteractionContentViewModel.htmlContent,
        binding.root.multiple_choice_content_text_view
      )
      binding.root.multiple_choice_content_text_view.text = htmlResult
      binding.root.multiple_choice_radio_button.isChecked = selectionInteractionContentViewModel.isAnswerSelected
      binding.root.radio_container.setOnClickListener {
        // TODO(BenHenning): Trigger this flow via data-binding (it should be possible for changes in the UI to
        //  automatically pipe up to the state adapter with an up-to-date model modified via data binding).
        // Ensure only the current item is selected per the data model.
        itemList.forEach { item -> item.isAnswerSelected = false }
        selectionInteractionViewModel.selectedItems.clear()
        itemList[adapterPosition].isAnswerSelected = true
        selectionInteractionViewModel.selectedItems += adapterPosition
        notifyDataSetChanged()
      }
    }
  }
}
