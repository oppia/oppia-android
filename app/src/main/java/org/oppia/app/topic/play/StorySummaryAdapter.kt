package org.oppia.app.topic.play

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.TopicPlayStorySummaryBinding
import org.oppia.app.databinding.TopicPlayTitleBinding
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val VIEW_TYPE_TITLE_TEXT = 1
private const val VIEW_TYPE_STORY_ITEM = 2
private const val ANIMATION_DURATION: Long = 400

/** Adapter to bind StorySummary to [RecyclerView] inside [TopicPlayFragment]. */
class StorySummaryAdapter(
  private val context: Context,
  private val itemList: MutableList<TopicPlayItemViewModel>,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val expandedChapterListIndexListener: ExpandedChapterListIndexListener,
  private var currentExpandedChapterListIndex: Int?
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_TITLE_TEXT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicPlayTitleBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        TopicPlayTitleViewHolder(binding)
      }
      VIEW_TYPE_STORY_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicPlayStorySummaryBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        StorySummaryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_TITLE_TEXT -> {
        (holder as TopicPlayTitleViewHolder).bind()
      }
      VIEW_TYPE_STORY_ITEM -> {
        (holder as StorySummaryViewHolder).bind(itemList[i] as StorySummaryViewModel, i)
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is TopicPlayTitleViewModel -> {
        VIEW_TYPE_TITLE_TEXT
      }
      is StorySummaryViewModel -> {
        VIEW_TYPE_STORY_ITEM
      }
      else -> throw IllegalArgumentException("Invalid type of data $position with item ${itemList[position]}")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class TopicPlayTitleViewHolder(
    binding: TopicPlayTitleBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {}
  }

  inner class StorySummaryViewHolder(private val binding: TopicPlayStorySummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(storySummaryViewModel: StorySummaryViewModel, position: Int) {
      binding.viewModel = storySummaryViewModel

      val chapterSummaries = storySummaryViewModel.storySummary.chapterList
      val completedChapterCount =
        chapterSummaries.map(ChapterSummary::getChapterPlayState)
          .filter {
            it == ChapterPlayState.COMPLETED
          }
          .size
      val storyPercentage: Int = (completedChapterCount * 100) / storySummaryViewModel.storySummary.chapterCount
      binding.storyPercentage = storyPercentage
      binding.storyProgressView.setStoryChapterDetails(
        storySummaryViewModel.storySummary.chapterCount,
        completedChapterCount
      )

      val chapterList = storySummaryViewModel.storySummary.chapterList
      binding.chapterRecyclerView.adapter = ChapterSummaryAdapter(chapterList, chapterSummarySelector)

      if (currentExpandedChapterListIndex != null && currentExpandedChapterListIndex == position) {
        val aniRotate = AnimationUtils.loadAnimation(context, R.anim.rotation_clockwise_180)
        binding.chapterListDropDownIcon.startAnimation(aniRotate)
        expand(binding.chapterListContainer)
      } else {
        val aniRotate = AnimationUtils.loadAnimation(context, R.anim.rotation_anti_clockwise_180)
        binding.chapterListDropDownIcon.startAnimation(aniRotate)
        collapse(binding.chapterListContainer)
      }

      binding.root.setOnClickListener {
        val previousItem = currentExpandedChapterListIndex


        if (binding.chapterListContainer.isVisible) {
          currentExpandedChapterListIndex = null
        } else {
          currentExpandedChapterListIndex = position
        }
        expandedChapterListIndexListener.onExpandListIconClicked(currentExpandedChapterListIndex)
        if (previousItem != null && previousItem != position) {
          notifyItemChanged(previousItem)
        }
        notifyItemChanged(position)
      }
    }

    private fun expand(chapterListContainer: View) {
      Log.d("TAG", "expand")
      chapterListContainer.clearAnimation()
      val matchParentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec((chapterListContainer.parent as View).width, View.MeasureSpec.EXACTLY)
      val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
      chapterListContainer.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
      val targetHeight = chapterListContainer.measuredHeight

      // Older versions of android (pre API 21) cancel animations for views with a height of 0.
      chapterListContainer.layoutParams.height = 1
      chapterListContainer.visibility = View.VISIBLE
      val expandAnimation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
          if (interpolatedTime == 1f) {
            chapterListContainer.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
          } else {
            chapterListContainer.layoutParams.height = (targetHeight * interpolatedTime).toInt()
          }
          chapterListContainer.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
          return true
        }
      }
      expandAnimation.duration = ANIMATION_DURATION
      chapterListContainer.startAnimation(expandAnimation)
    }

    private fun collapse(chapterListContainer: View) {
      Log.d("TAG", "collapse")
      chapterListContainer.clearAnimation()

      val initialHeight = chapterListContainer.measuredHeight

      val collapseAnimation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
          if (interpolatedTime == 1f) {
            chapterListContainer.visibility = View.GONE
          } else {
            chapterListContainer.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
          }
          chapterListContainer.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
          return true
        }
      }
      collapseAnimation.duration = ANIMATION_DURATION
      chapterListContainer.startAnimation(collapseAnimation)
    }
  }
}
