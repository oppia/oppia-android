package org.oppia.app.hintsandsolution

import android.animation.ValueAnimator
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.HintsSummaryBinding
import org.oppia.app.databinding.SolutionSummaryBinding
import org.oppia.util.parser.HtmlParser

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val TAG_REVEAL_SOLUTION_DIALOG = "REVEAL_SOLUTION_DIALOG"
private const val VIEW_TYPE_HINT_ITEM = 1
private const val VIEW_TYPE_SOLUTION_ITEM = 2

/** Adapter to bind Hints to [RecyclerView] inside [HintsAndSolutionDialogFragment]. */
class HintsAndSolutionAdapter(
  private val fragment: Fragment,
  private val itemList: List<HintsAndSolutionItemViewModel>,
  private val expandedHintListIndexListener: ExpandedHintListIndexListener,
  private var currentExpandedHintListIndex: Int?,
  private val explorationId: String?,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceBucketName: String,
  private val entityType: String
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var lastAvailableHintIndex = -1

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

      if (hintsViewModel.isHintRevealed.get()!!) {
        binding.root.visibility = View.VISIBLE
      } else {
        binding.root.visibility = View.GONE
      }

      if (position == 0) {
        binding.topDivider?.visibility = View.GONE
      }

      if (position == lastAvailableHintIndex) {
        if (isHintListVisible) {
          binding.hintListContainer.visibility = View.INVISIBLE
          Handler().postDelayed({
            binding.hintListContainer.alpha = 0f
            binding.hintListContainer.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 200
            valueAnimator.addUpdateListener {
              binding.hintListContainer.alpha = it.animatedValue as Float
            }
            valueAnimator.start()
          },100)
          binding.hintListContainer
            .measure(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
            )
          val height = binding.hintListContainer.measuredHeight
          val dividerAnimator = ValueAnimator.ofInt(-(height), 0)
          dividerAnimator.duration = 300
          dividerAnimator.addUpdateListener {
            binding.bottomDivider?.translationY = (it.animatedValue as Int).toFloat()
          }
          dividerAnimator.start()
        } else {
          if (hintsViewModel.isHintRevealed.get()!!) {
            val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
            valueAnimator.duration = 100
            valueAnimator.addUpdateListener {
              binding.hintListContainer.alpha = it.animatedValue as Float
            }
            valueAnimator.start()
            binding.hintListContainer
              .measure(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
              )
            val height = binding.hintListContainer.measuredHeight
            val dividerAnimator = ValueAnimator.ofInt(0, -(height))
            dividerAnimator.duration = 300
            dividerAnimator.addUpdateListener {
              binding.bottomDivider?.translationY = (it.animatedValue as Int).toFloat()
            }
            dividerAnimator.addListener {
              binding.hintListContainer.visibility = View.GONE
            }
            dividerAnimator.start()
          }
        }
      } else {
        if (isHintListVisible) {
          binding.hintListContainer.visibility = View.INVISIBLE
          Handler().postDelayed({
            binding.hintListContainer.alpha = 0f
            binding.hintListContainer.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 200
            valueAnimator.addUpdateListener {
              binding.hintListContainer.alpha = it.animatedValue as Float
            }
            valueAnimator.start()
          },100)
        } else {
          binding.hintListContainer.visibility = View.GONE
        }
      }

      binding.hintTitle.text = hintsViewModel.title.get()!!.replace("_", " ").capitalize()
      binding.hintsAndSolutionSummary.text =
        htmlParserFactory.create(
          resourceBucketName, entityType, explorationId!!, /* imageCenterAlign= */ true
        ).parseOppiaHtml(
          hintsViewModel.hintsAndSolutionSummary.get()!!, binding.hintsAndSolutionSummary
        )

      if (hintsViewModel.hintCanBeRevealed.get()!!) {
        binding.bottomDivider?.visibility = View.VISIBLE
        binding.root.visibility = View.VISIBLE
        binding.revealHintButton.setOnClickListener {
          hintsViewModel.isHintRevealed.set(true)
          (fragment.requireActivity() as? RevealHintListener)?.revealHint(true, position)
          currentExpandedHintListIndex =
            if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
              null
            } else {
              position
            }
          expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
        }
      } else {
        binding.bottomDivider?.visibility = View.GONE
      }

      if (lastAvailableHintIndex == itemList.size - 1) {
        binding.bottomDivider?.visibility = View.GONE
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

      if (solutionViewModel.isSolutionRevealed.get()!!) {
        binding.root.visibility = View.VISIBLE
      } else {
        binding.root.visibility = View.GONE
      }

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

      if (isHintListVisible) {
        binding.solutionContainer.visibility = View.INVISIBLE
        Handler().postDelayed({
          binding.solutionContainer.alpha = 0f
          binding.solutionContainer.visibility = View.VISIBLE
          val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
          valueAnimator.duration = 200
          valueAnimator.addUpdateListener {
            binding.solutionContainer.alpha = it.animatedValue as Float
          }
          valueAnimator.start()
        },100)
        binding.solutionContainer
          .measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
          )
        val height = binding.solutionContainer.measuredHeight
        val dividerAnimator = ValueAnimator.ofInt(-(height), 0)
        dividerAnimator.duration = 300
        dividerAnimator.addUpdateListener {
          binding.bottomDivider?.translationY = (it.animatedValue as Int).toFloat()
        }
        dividerAnimator.start()
      } else {
        if (solutionViewModel.isSolutionRevealed.get()!!) {
          val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
          valueAnimator.duration = 100
          valueAnimator.addUpdateListener {
            binding.solutionContainer.alpha = it.animatedValue as Float
          }
          valueAnimator.start()
          binding.solutionContainer
            .measure(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
            )
          val height = binding.solutionContainer.measuredHeight
          val dividerAnimator = ValueAnimator.ofInt(0, -(height))
          dividerAnimator.duration = 300
          dividerAnimator.addUpdateListener {
            binding.bottomDivider?.translationY = (it.animatedValue as Int).toFloat()
          }
          dividerAnimator.addListener {
            binding.solutionContainer.visibility = View.GONE
          }
          dividerAnimator.start()
        }
      }

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
    val previousFragment =
      fragment.childFragmentManager.findFragmentByTag(TAG_REVEAL_SOLUTION_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = RevealSolutionDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_REVEAL_SOLUTION_DIALOG)
  }

  fun setRevealSolution(saveUserChoice: Boolean) {
    if (itemList[itemList.size - 1] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 1] as SolutionViewModel
      val position = itemList.size - 1
      if (saveUserChoice) {
        solutionViewModel.isSolutionRevealed.set(saveUserChoice)
        (fragment.requireActivity() as? RevealSolutionInterface)?.revealSolution(saveUserChoice)
        currentExpandedHintListIndex =
        if (currentExpandedHintListIndex != null && currentExpandedHintListIndex == position) {
          null
        } else {
          position
        }
        expandedHintListIndexListener.onExpandListIconClicked(currentExpandedHintListIndex)
      }
      notifyItemChanged(itemList.size - 1)
    }
  }

  fun setNewHintIsAvailable(hintIndex: Int) {
    if (itemList[hintIndex] is HintsViewModel) {
      val hintsViewModel = itemList[hintIndex] as HintsViewModel
      hintsViewModel.hintCanBeRevealed.set(true)
      lastAvailableHintIndex = hintIndex
      notifyItemChanged(hintIndex)
    }
  }

  fun setSolutionCanBeRevealed(allHintsExhausted: Boolean) {
    if (itemList[itemList.size - 1] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 1] as SolutionViewModel
      solutionViewModel.solutionCanBeRevealed.set(allHintsExhausted)
      lastAvailableHintIndex = itemList.size - 1
      notifyItemChanged(itemList.size - 1)
    }
  }
}
