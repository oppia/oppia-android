package org.oppia.app.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.HintsDividerBinding
import org.oppia.app.databinding.HintsSummaryBinding
import org.oppia.app.databinding.SolutionSummaryBinding
import org.oppia.util.parser.HtmlParser

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

const val TAG_REVEAL_SOLUTION_DIALOG = "REVEAL_SOLUTION_DIALOG"
private const val VIEW_TYPE_HINT_ITEM = 1
private const val VIEW_TYPE_SOLUTION_ITEM = 2
private const val VIEW_TYPE_HINTS_DIVIDER_ITEM = 3

/** Adapter to bind Hints to [RecyclerView] inside [HintsAndSolutionDialogFragment]. */
class HintsAndSolutionAdapter(
  private val fragment: Fragment,
  private val itemList: List<HintsAndSolutionItemViewModel>,
  private val expandedHintListIndexListener: ExpandedHintListIndexListener,
  private var currentExpandedHintListIndex: Int?,
  private val explorationId: String?,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceBucketName: String,
  private val entityType: String,
  private val hintIndex: Int?,
  private val isHintRevealed: Boolean?,
  private val solutionIndex: Int?,
  private val isSolutionRevealed: Boolean?
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
      VIEW_TYPE_HINTS_DIVIDER_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          HintsDividerBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        HintsDividerViewHolder(binding)
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
      VIEW_TYPE_HINTS_DIVIDER_ITEM -> {
        holder as HintsDividerViewHolder
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
      is HintsDividerViewModel -> {
        VIEW_TYPE_HINTS_DIVIDER_ITEM
      }
      else -> throw IllegalArgumentException(
        "Invalid type of data " +
          "$position with item ${itemList[position]}"
      )
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

      hintIndex?.let {
        isHintRevealed?.let {
          if (hintIndex == position && isHintRevealed) {
            hintsViewModel.isHintRevealed.set(true)
          }
        }
      }

      binding.viewModel = hintsViewModel

      binding.hintTitle.text = hintsViewModel.title.get()!!.replace("_", " ").capitalize()
      binding.hintsAndSolutionSummary.text =
        htmlParserFactory.create(
          resourceBucketName, entityType, explorationId!!, /* imageCenterAlign= */ true
        ).parseOppiaHtml(
          hintsViewModel.hintsAndSolutionSummary.get()!!, binding.hintsAndSolutionSummary
        )

      if (hintsViewModel.hintCanBeRevealed.get()!!) {
        binding.root.visibility = View.VISIBLE
        binding.revealHintButton.setOnClickListener {
          hintsViewModel.isHintRevealed.set(true)
          expandedHintListIndexListener.onRevealHintClicked(position, /* isHintRevealed= */ true)
          (fragment.requireActivity() as? RevealHintListener)?.revealHint(
            saveUserChoice = true,
            hintIndex = position / 2
          )
          val previousIndex: Int? = currentExpandedHintListIndex
          currentExpandedHintListIndex =
            if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
              null
            } else {
              position
            }
          expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
          if (previousIndex != null && previousIndex != currentExpandedHintListIndex) {
            notifyItemChanged(previousIndex)
          }
        }
      }

      binding.root.setOnClickListener {
        if (hintsViewModel.isHintRevealed.get()!!) {
          val previousIndex: Int? = currentExpandedHintListIndex
          currentExpandedHintListIndex =
            if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
              null
            } else {
              position
            }
          expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
          if (previousIndex != null &&
            currentExpandedHintListIndex != null &&
            previousIndex == currentExpandedHintListIndex
          ) {
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

      solutionIndex?.let {
        isSolutionRevealed?.let {
          if (solutionIndex == position && isSolutionRevealed) {
            solutionViewModel.isSolutionRevealed.set(true)
          }
        }
      }

      binding.viewModel = solutionViewModel
      binding.solutionTitle.text = solutionViewModel.title.get()!!.capitalize()
      // TODO(#1050): Update to display answers for any answer type.
      if (solutionViewModel.correctAnswer.get().isNullOrEmpty()) {
        binding.solutionCorrectAnswer.text =
          """${solutionViewModel.numerator.get()}/${solutionViewModel.denominator.get()}"""
      } else {
        binding.solutionCorrectAnswer.text = solutionViewModel.correctAnswer.get()
      }
      binding.solutionSummary.text = htmlParserFactory.create(
        resourceBucketName, entityType, explorationId!!, /* imageCenterAlign= */ true
      ).parseOppiaHtml(
        solutionViewModel.solutionSummary.get()!!, binding.solutionSummary
      )

      if (solutionViewModel.solutionCanBeRevealed.get()!!) {
        binding.root.visibility = View.VISIBLE
        binding.revealSolutionButton.setOnClickListener {
          showRevealSolutionDialogFragment()
        }
      }

      binding.root.setOnClickListener {
        if (solutionViewModel.isSolutionRevealed.get()!!) {
          val previousIndex: Int? = currentExpandedHintListIndex
          currentExpandedHintListIndex =
            if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
              null
            } else {
              position
            }
          expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
          if (previousIndex != null &&
            currentExpandedHintListIndex != null &&
            previousIndex == currentExpandedHintListIndex
          ) {
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

  inner class HintsDividerViewHolder(private val binding: HintsDividerBinding) :
    RecyclerView.ViewHolder(binding.root)

  private fun showRevealSolutionDialogFragment() {
    val previousFragment =
      fragment.childFragmentManager.findFragmentByTag(TAG_REVEAL_SOLUTION_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = RevealSolutionDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_REVEAL_SOLUTION_DIALOG)
  }

  fun setRevealSolution(saveUserChoice: Boolean) {
    if (itemList[itemList.size - 2] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 2] as SolutionViewModel
      solutionViewModel.isSolutionRevealed.set(saveUserChoice)
      expandedHintListIndexListener.onRevealSolutionClicked(
        /* solutionIndex= */ itemList.size - 2,
        /* isSolutionRevealed= */ true
      )
      (fragment.requireActivity() as? RevealSolutionInterface)?.revealSolution(saveUserChoice)
      val previousIndex: Int? = currentExpandedHintListIndex
      currentExpandedHintListIndex =
        if (currentExpandedHintListIndex != null &&
          currentExpandedHintListIndex == itemList.size - 2
        ) {
          null
        } else {
          itemList.size - 2
        }
      expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
      if (previousIndex != null && previousIndex != currentExpandedHintListIndex) {
        notifyItemChanged(previousIndex)
      }
    }
  }

  fun setNewHintIsAvailable(hintIndex: Int) {
    if (itemList[hintIndex] is HintsViewModel) {
      val hintsViewModel = itemList[hintIndex] as HintsViewModel
      hintsViewModel.hintCanBeRevealed.set(true)
      notifyItemChanged(hintIndex)
    }
  }

  fun setSolutionCanBeRevealed(allHintsExhausted: Boolean) {
    if (itemList[itemList.size - 2] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 2] as SolutionViewModel
      solutionViewModel.solutionCanBeRevealed.set(allHintsExhausted)
      notifyItemChanged(itemList.size - 2)
    }
  }
}
