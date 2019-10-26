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
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumberWithUnitsInputInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.listener.ButtonInteractionListener
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumberWithUnitsViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.InteractionAnswerRetriever
import org.oppia.util.parser.HtmlParser

private const val VIEW_TYPE_CONTENT = 1
private const val VIEW_TYPE_INTERACTION_READ_ONLY = 2
private const val VIEW_TYPE_STATE_BUTTON = 3
private const val VIEW_TYPE_SELECTION_INTERACTION = 4
private const val VIEW_TYPE_FRACTION_INPUT_INTERACTION = 5
private const val VIEW_TYPE_NUMERIC_INPUT_INTERACTION = 6
private const val VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION = 7
private const val VIEW_TYPE_TEXT_INPUT_INTERACTION = 8

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class StateAdapter(
  private val itemList: MutableList<Any>,
  private val buttonInteractionListener: ButtonInteractionListener,
  private val htmlParserFactory: HtmlParser.Factory,
  private val entityType: String,
  private val explorationId: String
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            /* attachToParent= */false
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
      VIEW_TYPE_FRACTION_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<FractionInteractionItemBinding>(
            inflater,
            R.layout.fraction_interaction_item,
            parent,
            /* attachToParent= */ false
          )
        FractionInteractionViewHolder(binding)
      }
      VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<NumericInputInteractionItemBinding>(
            inflater,
            R.layout.numeric_input_interaction_item,
            parent,
            /* attachToParent= */ false
          )
        NumericInputInteractionViewHolder(binding)
      }
      VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<NumberWithUnitsInputInteractionItemBinding>(
            inflater,
            R.layout.number_with_units_input_interaction_item,
            parent,
            /* attachToParent= */ false
          )
        NumberWithUnitsInputInteractionViewHolder(binding)
      }
      VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<TextInputInteractionItemBinding>(
            inflater,
            R.layout.text_input_interaction_item,
            parent,
            /* attachToParent= */ false
          )
        TextInputInteractionViewHolder(binding)
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
        (holder as SelectionInteractionViewHolder).bind(
          itemList[position] as SelectionInteractionViewModel
        )
      }
      VIEW_TYPE_FRACTION_INPUT_INTERACTION -> {
        (holder as FractionInteractionViewHolder).bind(itemList[position] as FractionInteractionViewModel)
      }
      VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
        (holder as NumericInputInteractionViewHolder).bind(itemList[position] as NumericInputViewModel)
      }
      VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION -> {
        (holder as NumberWithUnitsInputInteractionViewHolder).bind(itemList[position] as NumberWithUnitsViewModel)
      }
      VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
        (holder as TextInputInteractionViewHolder).bind(itemList[position] as TextInputViewModel)
      }
    }
  }

  override fun getItemViewType(position: Int): Int = getTypeForItem(itemList[position])

  private fun getTypeForItem(item: Any): Int {
    return when (item) {
      is StateButtonViewModel -> VIEW_TYPE_STATE_BUTTON
      is ContentViewModel -> VIEW_TYPE_CONTENT
      is SelectionInteractionViewModel -> VIEW_TYPE_SELECTION_INTERACTION
      is FractionInteractionViewModel -> VIEW_TYPE_FRACTION_INPUT_INTERACTION
      is NumericInputViewModel -> VIEW_TYPE_NUMERIC_INPUT_INTERACTION
      is NumberWithUnitsViewModel -> VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION
      is TextInputViewModel -> VIEW_TYPE_TEXT_INPUT_INTERACTION
      else -> throw IllegalArgumentException("Invalid type of data: $item")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  // TODO(BenHenning): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(): InteractionObject {
    // TODO(BenHenning): Find a better way to do this. First, the search is bad. Second, the implication that more than
    // one interaction view can be active is bad.
    val pendingAnswerModel = itemList.findLast(this::isMutableInteractionType)
    return (pendingAnswerModel as InteractionAnswerRetriever).getPendingAnswer()
  }

  // TODO(BenHenning): Find a better way to do this (maybe an enum or sealed class?)
  private fun isMutableInteractionType(item: Any): Boolean {
    return when (getTypeForItem(item)) {
      VIEW_TYPE_SELECTION_INTERACTION,
      VIEW_TYPE_FRACTION_INPUT_INTERACTION,
      VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
      VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION,
      VIEW_TYPE_TEXT_INPUT_INTERACTION -> true
      else -> false
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

  inner class SelectionInteractionViewHolder(
    private val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(customizationArgs: SelectionInteractionViewModel) {
      val choiceInteractionContentList: MutableList<SelectionInteractionContentViewModel> = ArrayList()
      val gaeCustomArgsInString = customizationArgs.choiceItems.toString().replace("[", "").replace("]", "")
      val items = gaeCustomArgsInString.split(",").toTypedArray()
      for (itemIndex in 0 until items.size) {
        val selectionContentViewModel = SelectionInteractionContentViewModel(itemIndex)
        selectionContentViewModel.htmlContent = items[itemIndex]
        selectionContentViewModel.isAnswerSelected = false
        choiceInteractionContentList.add(selectionContentViewModel)
      }
      val interactionAdapter =
        SelectionInteractionAdapter(htmlParserFactory, entityType, explorationId, choiceInteractionContentList, customizationArgs)
      binding.root.selection_interaction_recyclerview.adapter = interactionAdapter
    }
  }

  inner class FractionInteractionViewHolder(
    private val binding: FractionInteractionItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(viewModel: FractionInteractionViewModel) {
      // TODO(BenHenning): Bind custom hint text.
      binding.viewModel = viewModel
    }
  }

  inner class NumericInputInteractionViewHolder(
    private val binding: NumericInputInteractionItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(viewModel: NumericInputViewModel) {
      // TODO(BenHenning): Bind custom hint text.
      binding.viewModel = viewModel
    }
  }

  inner class NumberWithUnitsInputInteractionViewHolder(
    private val binding: NumberWithUnitsInputInteractionItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(viewModel: NumberWithUnitsViewModel) {
      // TODO(BenHenning): Bind custom hint text.
      binding.viewModel = viewModel
    }
  }

  inner class TextInputInteractionViewHolder(
    private val binding: TextInputInteractionItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(viewModel: TextInputViewModel) {
      // TODO(BenHenning): Bind custom hint text.
      binding.viewModel = viewModel
    }
  }
}
