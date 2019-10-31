package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumberWithUnitsInputInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.listener.StateNavigationButtonListener
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumberWithUnitsViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.util.parser.HtmlParser

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
//class StateAdapter(
//  private val itemList: MutableList<Any>,
//  private val htmlParserFactory: HtmlParser.Factory,
//  private val entityType: String,
//  private val explorationId: String
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//  private enum class ViewType {
//    VIEW_TYPE_CONTENT,
//    VIEW_TYPE_FEEDBACK,
//    VIEW_TYPE_STATE_NAVIGATION_BUTTON,
//    VIEW_TYPE_CONTINUE_INTERACTION,
//    VIEW_TYPE_SELECTION_INTERACTION,
//    VIEW_TYPE_FRACTION_INPUT_INTERACTION,
//    VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
//    VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION,
//    VIEW_TYPE_TEXT_INPUT_INTERACTION
//  }
//
//  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//    return when (ViewType.values()[viewType]) {
//      // TODO(#249): Generalize this binding to make adding future interactions easier.
////      ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON -> {
////        val inflater = LayoutInflater.from(parent.context)
////        val binding =
////          DataBindingUtil.inflate<StateButtonItemBinding>(
////            inflater,
////            R.layout.state_button_item,
////            parent,
////            /* attachToParent= */false
////          )
////        StateButtonViewHolder(binding)
////      }
//      ViewType.VIEW_TYPE_CONTENT -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<ContentItemBinding>(
//            inflater,
//            R.layout.content_item,
//            parent,
//            /* attachToParent= */false
//          )
//        ContentViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_FEEDBACK -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<FeedbackItemBinding>(
//            inflater,
//            R.layout.feedback_item,
//            parent,
//            /* attachToParent= */false
//          )
//        FeedbackViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_CONTINUE_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<ContinueInteractionItemBinding>(
//            inflater,
//            R.layout.continue_interaction_item,
//            parent,
//            /* attachToParent= */false
//          )
//        ContinueInteractionViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_SELECTION_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<SelectionInteractionItemBinding>(
//            inflater,
//            R.layout.selection_interaction_item,
//            parent,
//            /* attachToParent= */ false
//          )
//        SelectionInteractionViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<FractionInteractionItemBinding>(
//            inflater,
//            R.layout.fraction_interaction_item,
//            parent,
//            /* attachToParent= */ false
//          )
//        FractionInteractionViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<NumericInputInteractionItemBinding>(
//            inflater,
//            R.layout.numeric_input_interaction_item,
//            parent,
//            /* attachToParent= */ false
//          )
//        NumericInputInteractionViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<NumberWithUnitsInputInteractionItemBinding>(
//            inflater,
//            R.layout.number_with_units_input_interaction_item,
//            parent,
//            /* attachToParent= */ false
//          )
//        NumberWithUnitsInputInteractionViewHolder(binding)
//      }
//      ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//          DataBindingUtil.inflate<TextInputInteractionItemBinding>(
//            inflater,
//            R.layout.text_input_interaction_item,
//            parent,
//            /* attachToParent= */ false
//          )
//        TextInputInteractionViewHolder(binding)
//      }
//    }
//  }
//
//  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//    when (ViewType.values()[holder.itemViewType]) {
//      ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON -> {
//        (holder as StateButtonViewHolder).bind(itemList[position] as StateNavigationButtonViewModel)
//      }
//      ViewType.VIEW_TYPE_CONTENT -> {
//        (holder as ContentViewHolder).bind((itemList[position] as ContentViewModel).htmlContent)
//      }
//      ViewType.VIEW_TYPE_FEEDBACK -> {
//        (holder as FeedbackViewHolder).bind((itemList[position] as FeedbackViewModel).htmlContent)
//      }
//      ViewType.VIEW_TYPE_CONTINUE_INTERACTION -> {
//        (holder as ContinueInteractionViewHolder).bind(itemList[position] as ContinueInteractionViewModel)
//      }
//      ViewType.VIEW_TYPE_SELECTION_INTERACTION -> {
//        (holder as SelectionInteractionViewHolder).bind(
//          itemList[position] as SelectionInteractionViewModel
//        )
//      }
//      ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION -> {
//        (holder as FractionInteractionViewHolder).bind(itemList[position] as FractionInteractionViewModel)
//      }
//      ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
//        (holder as NumericInputInteractionViewHolder).bind(itemList[position] as NumericInputViewModel)
//      }
//      ViewType.VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION -> {
//        (holder as NumberWithUnitsInputInteractionViewHolder).bind(itemList[position] as NumberWithUnitsViewModel)
//      }
//      ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
//        (holder as TextInputInteractionViewHolder).bind(itemList[position] as TextInputViewModel)
//      }
//    }
//  }
//
//  override fun getItemViewType(position: Int): Int = getTypeForItem(itemList[position]).ordinal
//
//  private fun getTypeForItem(item: Any): ViewType {
//    return when (item) {
//      is StateNavigationButtonViewModel -> ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON
//      is ContentViewModel -> ViewType.VIEW_TYPE_CONTENT
//      is FeedbackViewModel -> ViewType.VIEW_TYPE_FEEDBACK
//      is ContinueInteractionViewModel -> ViewType.VIEW_TYPE_CONTINUE_INTERACTION
//      is SelectionInteractionViewModel -> ViewType.VIEW_TYPE_SELECTION_INTERACTION
//      is FractionInteractionViewModel -> ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION
//      is NumericInputViewModel -> ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION
//      is NumberWithUnitsViewModel -> ViewType.VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION
//      is TextInputViewModel -> ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION
//      else -> throw IllegalArgumentException("Invalid type of data: $item")
//    }
//  }
//
//  override fun getItemCount(): Int {
//    return itemList.size
//  }
//
//  /**
//   * Returns whether there is currently a pending interaction that requires an additional user action to submit the
//   * answer.
//   */
//  fun doesPendingInteractionRequireExplicitSubmission(): Boolean {
//    return getPendingAnswerHandler()?.isExplicitAnswerSubmissionRequired() ?: true
//  }
//
//  // TODO(BenHenning): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
//  fun getPendingAnswer(): InteractionObject {
//    return getPendingAnswerHandler()?.getPendingAnswer() ?: InteractionObject.getDefaultInstance()
//  }
//
//  private fun getPendingAnswerHandler(): InteractionAnswerHandler? {
//    // TODO(BenHenning): Find a better way to do this. First, the search is bad. Second, the implication that more than
//    // one interaction view can be active is bad.
//    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
//  }
//
//  private class StateButtonViewHolder(
//    val binding: StateButtonItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: StateNavigationButtonViewModel) {
//      binding.buttonViewModel = viewModel
//    }
//  }
//
//  inner class ContentViewHolder(val binding: ContentItemBinding) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(rawString: String) {
//      binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
//        rawString, binding.contentTextView
//      )
//    }
//  }
//
//  inner class FeedbackViewHolder(val binding: FeedbackItemBinding) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(rawString: String) {
//      binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
//        rawString, binding.feedbackTextView
//      )
//    }
//  }
//
//  inner class ContinueInteractionViewHolder(
//    val binding: ContinueInteractionItemBinding
//  ): RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: ContinueInteractionViewModel) {
//      binding.viewModel = viewModel
//    }
//  }
//
//  inner class SelectionInteractionViewHolder(
//    private val binding: SelectionInteractionItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: SelectionInteractionViewModel) {
//      val choiceInteractionContentList: MutableList<SelectionInteractionContentViewModel> = ArrayList()
//      val items = viewModel.choiceItems
//      for (itemIndex in 0 until items.size) {
//        val isAnswerSelected = itemIndex in viewModel.selectedItems
//        val selectionContentViewModel = SelectionInteractionContentViewModel(
//          isAnswerInitiallySelected = isAnswerSelected, isReadOnly = viewModel.isReadOnly
//        )
//        selectionContentViewModel.htmlContent = items[itemIndex]
//        selectionContentViewModel.isAnswerSelected = isAnswerSelected
//        choiceInteractionContentList.add(selectionContentViewModel)
//      }
//      val interactionAdapter = SelectionInteractionAdapter(
//        htmlParserFactory, entityType, explorationId, choiceInteractionContentList, viewModel
//      )
//      binding.selectionInteractionRecyclerview.adapter = interactionAdapter
//    }
//  }
//
//  inner class FractionInteractionViewHolder(
//    private val binding: FractionInteractionItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: FractionInteractionViewModel) {
//      // TODO(BenHenning): Bind custom hint text.
//      binding.viewModel = viewModel
//    }
//  }
//
//  inner class NumericInputInteractionViewHolder(
//    private val binding: NumericInputInteractionItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: NumericInputViewModel) {
//      // TODO(BenHenning): Bind custom hint text.
//      binding.viewModel = viewModel
//    }
//  }
//
//  inner class NumberWithUnitsInputInteractionViewHolder(
//    private val binding: NumberWithUnitsInputInteractionItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: NumberWithUnitsViewModel) {
//      // TODO(BenHenning): Bind custom hint text.
//      binding.viewModel = viewModel
//    }
//  }
//
//  inner class TextInputInteractionViewHolder(
//    private val binding: TextInputInteractionItemBinding
//  ) : RecyclerView.ViewHolder(binding.root) {
//    internal fun bind(viewModel: TextInputViewModel) {
//      // TODO(BenHenning): Bind custom hint text.
//      binding.viewModel = viewModel
//    }
//  }
//}
