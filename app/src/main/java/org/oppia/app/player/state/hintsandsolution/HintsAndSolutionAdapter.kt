package org.oppia.app.player.state.hintsandsolution

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.HintsAndSolutionSummaryBinding
import org.oppia.app.databinding.SolutionSummaryBinding

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val VIEW_TYPE_HINT_ITEM = 1
private const val VIEW_TYPE_SOLUTION_ITEM = 2

/** Adapter to bind StorySummary to [RecyclerView] inside [HintsAndSolutionFragment]. */
class HintsAndSolutionAdapter(
  private val itemList: MutableList<HintsAndSolutionItemViewModel>,
  private val expandedHintListIndexListener: ExpandedHintListIndexListener,
  private var currentExpandedHintListIndex: Int?
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_HINT_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          HintsAndSolutionSummaryBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        HintsAndSolutionSummaryViewHolder(binding)
      }
      VIEW_TYPE_SOLUTION_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          SolutionSummaryBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        SolutionSummaryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_HINT_ITEM -> {
        (holder as HintsAndSolutionSummaryViewHolder).bind(itemList[i] as HintsAndSolutionViewModel, i)
      }
      VIEW_TYPE_SOLUTION_ITEM -> {
        (holder as SolutionSummaryViewHolder).bind(itemList[i] as SolutionViewModel, i)
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is HintsAndSolutionViewModel -> {
        VIEW_TYPE_HINT_ITEM
      }
      is SolutionViewModel -> {
        VIEW_TYPE_SOLUTION_ITEM
      }
      else -> throw IllegalArgumentException("Invalid type of data $position with item ${itemList[position]}")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }


  inner class HintsAndSolutionSummaryViewHolder(private val binding: HintsAndSolutionSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(hintsAndSolutionViewModel: HintsAndSolutionViewModel, position: Int) {
      var isChapterListVisible = false
      if (currentExpandedHintListIndex != null) {
        isChapterListVisible = currentExpandedHintListIndex!! == position
      }
      binding.isListExpanded = isChapterListVisible
      binding.viewModel = hintsAndSolutionViewModel

      binding.root.setOnClickListener {
        val previousIndex: Int? = currentExpandedHintListIndex
        currentExpandedHintListIndex =
          if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
            null
          } else {
            position
          }
        expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
        if (previousIndex != null && currentExpandedHintListIndex != null && previousIndex == currentExpandedHintListIndex) {
          notifyItemChanged(currentExpandedHintListIndex!!)
        } else {
          if (previousIndex != null) {
            notifyItemChanged(previousIndex)
          }
          if (currentExpandedHintListIndex != null) {
            notifyItemChanged(currentExpandedHintListIndex!!)
          }
        }
      }
    }
  }

  inner class SolutionSummaryViewHolder(private val binding: SolutionSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(solutionViewModel: SolutionViewModel, position: Int) {
      var isChapterListVisible = false
      if (currentExpandedHintListIndex != null) {
        isChapterListVisible = currentExpandedHintListIndex!! == position
      }
      binding.isListExpanded = isChapterListVisible
      binding.viewModel = solutionViewModel

      binding.root.setOnClickListener {
        val previousIndex: Int? = currentExpandedHintListIndex
        currentExpandedHintListIndex =
          if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
            null
          } else {
            position
          }
        expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
        if (previousIndex != null && currentExpandedHintListIndex != null && previousIndex == currentExpandedHintListIndex) {
          notifyItemChanged(currentExpandedHintListIndex!!)
        } else {
          if (previousIndex != null) {
            notifyItemChanged(previousIndex)
          }
          if (currentExpandedHintListIndex != null) {
            notifyItemChanged(currentExpandedHintListIndex!!)
          }
        }
      }
    }
  }
}
