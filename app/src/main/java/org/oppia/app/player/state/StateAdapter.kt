package org.oppia.app.player.state

import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.library.baseAdapters.BR
import kotlinx.android.synthetic.main.content_item.view.content_text_view
import kotlinx.android.synthetic.main.selection_interaction_item.view.selection_interaction_recyclerview
import kotlinx.android.synthetic.main.state_button_item.view.*
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.listener.ButtonInteractionListener
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionCustomizationArgsViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.util.parser.HtmlParser

@Suppress("unused")
private const val VIEW_TYPE_CONTENT = 1
@Suppress("unused")
private const val VIEW_TYPE_INTERACTION_READ_ONLY = 2
@Suppress("unused")
private const val VIEW_TYPE_NUMERIC_INPUT_INTERACTION = 3
@Suppress("unused")
private const val VIEW_TYPE_TEXT_INPUT_INTERACTION = 4
private const val VIEW_TYPE_STATE_BUTTON = 5
const val VIEW_TYPE_SELECTION_INTERACTION = 6

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class StateAdapter(
  private val itemList: MutableList<Any>,
  private val buttonInteractionListener: ButtonInteractionListener,
  private val htmlParserFactory: HtmlParser.Factory,
  private val entityType: String,
  private val explorationId: String
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  lateinit var stateButtonViewModel: StateButtonViewModel

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#249): Generalize this binding to make adding future interactions easier.
      VIEW_TYPE_STATE_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<StateButtonItemBinding>(
            inflater,
            R.layout.state_button_item,
            parent,
            /* attachToParent= */false
          )
        StateButtonViewHolder(binding, buttonInteractionListener)
      }
      VIEW_TYPE_CONTENT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<ContentItemBinding>(
            inflater,
            R.layout.content_item,
            parent,
            /* attachToParent= */ false
          )
        ContentViewHolder(binding)
      }
      VIEW_TYPE_SELECTION_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<SelectionInteractionItemBinding>(
            inflater,
            R.layout.selection_interaction_item,
            parent,
            /* attachToParent= */ false
          )
        SelectionInteractionViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_STATE_BUTTON -> {
        (holder as StateButtonViewHolder).bind(itemList[position] as StateButtonViewModel)
      }
      VIEW_TYPE_CONTENT -> {
        (holder as ContentViewHolder).bind((itemList[position] as ContentViewModel).htmlContent)
      }
      VIEW_TYPE_SELECTION_INTERACTION -> {
        (holder as SelectionInteractionViewHolder).bind(itemList[position] as SelectionInteractionCustomizationArgsViewModel)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is ContentViewModel -> VIEW_TYPE_CONTENT
      is SelectionInteractionCustomizationArgsViewModel -> VIEW_TYPE_SELECTION_INTERACTION
      is StateButtonViewModel -> {
        stateButtonViewModel = itemList[position] as StateButtonViewModel
        VIEW_TYPE_STATE_BUTTON
      }
      else -> throw IllegalArgumentException("Invalid type of data $position")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings()
      val htmlResult: Spannable = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
        rawString,
        binding.root.content_text_view
      )
      binding.root.content_text_view.text = htmlResult
    }
  }

  private class StateButtonViewHolder(
    val binding: ViewDataBinding,
    private val buttonInteractionListener: ButtonInteractionListener
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(stateButtonViewModel: StateButtonViewModel) {
      binding.setVariable(BR.buttonViewModel, stateButtonViewModel)
      binding.root.interaction_button.setOnClickListener {
        buttonInteractionListener.onInteractionButtonClicked()
      }
      binding.root.next_state_image_view.setOnClickListener {
        buttonInteractionListener.onNextButtonClicked()
      }
      binding.root.previous_state_image_view.setOnClickListener {
        buttonInteractionListener.onPreviousButtonClicked()
      }
      binding.executePendingBindings()
    }
  }

  inner class SelectionInteractionViewHolder(
   private val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(customizationArgs: SelectionInteractionCustomizationArgsViewModel) {
      val items: Array<String>?
      val choiceInteractionContentList: MutableList<SelectionInteractionContentViewModel> = ArrayList()
      binding.executePendingBindings()
      val gaeCustomArgsInString = customizationArgs.choiceItems.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      for (values in items) {
        val selectionContentViewModel = SelectionInteractionContentViewModel()
        selectionContentViewModel.htmlContent = values
        selectionContentViewModel.isAnswerSelected = false
        choiceInteractionContentList.add(selectionContentViewModel)
      }
      val interactionAdapter =
        InteractionAdapter(htmlParserFactory, entityType, explorationId, choiceInteractionContentList, customizationArgs)
      binding.root.selection_interaction_recyclerview.adapter = interactionAdapter
    }
  }

}
