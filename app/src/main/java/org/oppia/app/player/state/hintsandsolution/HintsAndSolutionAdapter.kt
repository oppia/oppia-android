package org.oppia.app.player.state.hintsandsolution

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.HintsSummaryBinding
import org.oppia.app.databinding.SolutionSummaryBinding
import org.oppia.util.parser.HtmlParser

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val TAG_REVEAL_SOLUTION_DIALOG = "REVEAL_SOLUTION_DIALOG"
private const val VIEW_TYPE_HINT_ITEM = 1
private const val VIEW_TYPE_SOLUTION_ITEM = 2

/** Adapter to bind StorySummary to [RecyclerView] inside [HintsAndSolutionFragment]. */
class HintsAndSolutionAdapter(
  private val fragment: Fragment,
  private val itemList: List<HintsAndSolutionItemViewModel>,
  private val expandedHintListIndexListener: ExpandedHintListIndexListener,
  private var currentExpandedHintListIndex: Int?,
  private var explorationId: String,
  private var htmlParserFactory: HtmlParser.Factory,
  private var entityType: String
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_HINT_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          HintsSummaryBinding.inflate(
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
        (holder as HintsAndSolutionSummaryViewHolder).bind(itemList[i] as HintsViewModel, i)
      }
      VIEW_TYPE_SOLUTION_ITEM -> {
        (holder as SolutionSummaryViewHolder).bind(itemList[i] as SolutionViewModel, i)
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is HintsViewModel -> {
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

  inner class HintsAndSolutionSummaryViewHolder(private val binding: HintsSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(hintsViewModel: HintsViewModel, position: Int) {
      var isHintListVisible = false
      if (currentExpandedHintListIndex != null) {
        isHintListVisible = currentExpandedHintListIndex!! == position
      }
      binding.isListExpanded = isHintListVisible
      binding.viewModel = hintsViewModel

      binding.hintTitle.text = hintsViewModel.title.replace("_", " ").capitalize()
      binding.hintsAndSolutionSummary.text =
        htmlParserFactory.create(entityType, explorationId, /* imageCenterAlign= */ true)
          .parseOppiaHtml(
            hintsViewModel.hintsAndSolutionSummary, binding.hintsAndSolutionSummary
          )

      if(hintsViewModel.hintCanBeRevealed) {
        binding.revealHintButton.setOnClickListener {
          hintsViewModel.isHintRevealed = true
          (fragment.requireActivity() as? RevealHintListener)?.revealHint(true, position)
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

      binding.root.setOnClickListener {
        if(hintsViewModel.isHintRevealed) {
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

  inner class SolutionSummaryViewHolder(private val binding: SolutionSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(solutionViewModel: SolutionViewModel, position: Int) {
      var isHintListVisible = false
      if (currentExpandedHintListIndex != null) {
        isHintListVisible = currentExpandedHintListIndex!! == position
      }
      binding.isListExpanded = isHintListVisible
      binding.viewModel = solutionViewModel

      binding.solutionTitle.text = solutionViewModel.title.capitalize()
      binding.solutionSummary.text = htmlParserFactory.create(entityType, explorationId, /* imageCenterAlign= */ true)
        .parseOppiaHtml(
          solutionViewModel.solutionSummary, binding.solutionSummary
        )

      if(solutionViewModel.solutionCanBeRevealed) {
        binding.revealSolutionButton.setOnClickListener {
          showRevealSolutionDialogFragment()
        }
      }

      binding.root.setOnClickListener {
        if(solutionViewModel.isSolutionRevealed) {
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

  private fun showRevealSolutionDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_REVEAL_SOLUTION_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = RevealSolutionDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_REVEAL_SOLUTION_DIALOG)
  }

  fun setRevealSolution(saveUserChoice: Boolean) {
    if (itemList.get(itemList.size - 1) is SolutionViewModel) {
      val solutionViewModel = itemList.get(itemList.size - 1) as SolutionViewModel
      solutionViewModel.isSolutionRevealed = saveUserChoice
      (fragment.requireActivity() as? RevealSolutionInterface)?.revealSolution(saveUserChoice)
      notifyItemChanged(itemList.size - 1)
    }
  }

  fun setNewHintIsAvailable(hintIndex: Int) {
    if (itemList.get(hintIndex) is HintsViewModel) {
      val hintsViewModel = itemList.get(hintIndex) as HintsViewModel
      hintsViewModel.hintCanBeRevealed = true
      notifyItemChanged(hintIndex)
    }
  }

  fun setSolutionCanBeRevealed(allHintsExhausted: Boolean) {
    if (itemList.get(itemList.size - 1) is SolutionViewModel) {
      val solutionViewModel = itemList.get(itemList.size - 1) as SolutionViewModel
      solutionViewModel.solutionCanBeRevealed = allHintsExhausted
      notifyItemChanged(itemList.size - 1)
    }
  }
}
