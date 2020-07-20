package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.ContinueNavigationButtonItemBinding
import org.oppia.app.databinding.DragDropInteractionItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NextButtonItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.SubmitButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.DragAndDropSortInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NextButtonViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.SubmitButtonViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel

private const val VIEW_TYPE_DRAG_AND_DROP = 0
private const val VIEW_TYPE_SUBMIT_BUTTON = 1
private const val VIEW_TYPE_CONTINUE_BUTTON = 2
private const val VIEW_TYPE_FRACTION_INPUT = 3
private const val VIEW_TYPE_SELECTION = 4
private const val VIEW_TYPE_NUMERIC_INPUT = 5
private const val VIEW_TYPE_TEXT_INPUT = 6
private const val VIEW_TYPE_CONTINUE_NAVIGATION_BUTTON = 7
private const val VIEW_TYPE_NEXT_BUTTON = 8

class RhsInteractionsAdapter(private val itemList: MutableList<StateItemViewModel>) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_DRAG_AND_DROP -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DragDropInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        DragAndDropViewHolder(binding)
      }
      VIEW_TYPE_SUBMIT_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubmitButtonItemBinding.inflate(
          inflater,
          parent,
          false
        )
        SubmitButtonViewHolder(binding)
      }
      VIEW_TYPE_CONTINUE_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContinueInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        ContinueButtonViewHolder(binding)
      }
      VIEW_TYPE_FRACTION_INPUT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FractionInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        FractionInputViewHolder(binding)
      }
      VIEW_TYPE_SELECTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SelectionInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        SelectionViewHolder(binding)
      }
      VIEW_TYPE_NUMERIC_INPUT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NumericInputInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        NumericInputViewHolder(binding)
      }
      VIEW_TYPE_TEXT_INPUT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TextInputInteractionItemBinding.inflate(
          inflater,
          parent,
          false
        )
        TextInputViewHolder(binding)
      }
      VIEW_TYPE_CONTINUE_NAVIGATION_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContinueNavigationButtonItemBinding.inflate(
          inflater,
          parent,
          false
        )
        ContinueNavigationButtonViewHolder(binding)
      }
      VIEW_TYPE_NEXT_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NextButtonItemBinding.inflate(
          inflater,
          parent,
          false
        )
        NextButtonViewHolder(binding)
      }
      else -> {
        throw IllegalArgumentException("Invalid view type: $viewType")
      }
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_DRAG_AND_DROP -> {
        (holder as DragAndDropViewHolder).bind(itemList[position] as DragAndDropSortInteractionViewModel)
      }
      VIEW_TYPE_SUBMIT_BUTTON -> {
        (holder as SubmitButtonViewHolder).bind(itemList[position] as SubmitButtonViewModel)
      }
      VIEW_TYPE_CONTINUE_BUTTON -> {
        (holder as ContinueButtonViewHolder).bind(itemList[position] as ContinueInteractionViewModel)
      }
      VIEW_TYPE_FRACTION_INPUT -> {
        (holder as FractionInputViewHolder).bind(itemList[position] as FractionInteractionViewModel)
      }
      VIEW_TYPE_SELECTION -> {
        (holder as SelectionViewHolder).bind(itemList[position] as SelectionInteractionViewModel)
      }
      VIEW_TYPE_NUMERIC_INPUT -> {
        (holder as NumericInputViewHolder).bind(itemList[position] as NumericInputViewModel)
      }
      VIEW_TYPE_TEXT_INPUT -> {
        (holder as TextInputViewHolder).bind(itemList[position] as TextInputViewModel)
      }
      VIEW_TYPE_CONTINUE_NAVIGATION_BUTTON -> {
        (holder as ContinueNavigationButtonViewHolder)
          .bind(itemList[position] as ContinueNavigationButtonViewModel)
      }
      VIEW_TYPE_NEXT_BUTTON -> {
        (holder as NextButtonViewHolder).bind(itemList[position] as NextButtonViewModel)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is DragAndDropSortInteractionViewModel -> {
        VIEW_TYPE_DRAG_AND_DROP
      }
      is SubmitButtonViewModel -> {
        VIEW_TYPE_SUBMIT_BUTTON
      }
      is ContinueInteractionViewModel -> {
        VIEW_TYPE_CONTINUE_BUTTON
      }
      is FractionInteractionViewModel -> {
        VIEW_TYPE_FRACTION_INPUT
      }
      is SelectionInteractionViewModel -> {
        VIEW_TYPE_SELECTION
      }
      is NumericInputViewModel -> {
        VIEW_TYPE_NUMERIC_INPUT
      }
      is TextInputViewModel -> {
        VIEW_TYPE_TEXT_INPUT
      }
      is ContinueNavigationButtonViewModel -> {
        VIEW_TYPE_CONTINUE_NAVIGATION_BUTTON
      }
      is NextButtonViewModel -> {
        VIEW_TYPE_NEXT_BUTTON
      }
      else -> throw IllegalArgumentException(
        "Invalid type of data $position with item ${itemList[position]}"
      )
    }
  }

  class DragAndDropViewHolder(val binding: DragDropInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel) {
      binding.viewModel = dragAndDropSortInteractionViewModel
    }
  }

  class SubmitButtonViewHolder(val binding: SubmitButtonItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(submitButtonViewModel: SubmitButtonViewModel) {
      binding.buttonViewModel = submitButtonViewModel
    }
  }

  class ContinueButtonViewHolder(val binding: ContinueInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(continueInteractionViewModel: ContinueInteractionViewModel) {
      binding.viewModel = continueInteractionViewModel
    }
  }

  class FractionInputViewHolder(val binding: FractionInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(fractionInteractionViewModel: FractionInteractionViewModel) {
      binding.viewModel = fractionInteractionViewModel
    }
  }

  class SelectionViewHolder(val binding: SelectionInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(selectionInteractionViewModel: SelectionInteractionViewModel) {
      binding.viewModel = selectionInteractionViewModel
    }
  }

  class NumericInputViewHolder(val binding: NumericInputInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(numericInputViewModel: NumericInputViewModel) {
      binding.viewModel = numericInputViewModel
    }
  }

  class TextInputViewHolder(val binding: TextInputInteractionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(textInputViewModel: TextInputViewModel) {
      binding.viewModel = textInputViewModel
    }
  }

  class ContinueNavigationButtonViewHolder(val binding: ContinueNavigationButtonItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(continueNavigationButtonViewModel: ContinueNavigationButtonViewModel) {
      binding.buttonViewModel = continueNavigationButtonViewModel
    }
  }

  class NextButtonViewHolder(val binding: NextButtonItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(nextButtonViewModel: NextButtonViewModel) {
      binding.buttonViewModel = nextButtonViewModel
    }
  }
}
