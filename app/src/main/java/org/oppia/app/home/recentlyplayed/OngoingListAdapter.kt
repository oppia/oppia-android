package org.oppia.app.home.recentlyplayed

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.ui.R
import org.oppia.app.databinding.databinding.OngoingStoryCardBinding
import org.oppia.app.databinding.databinding.SectionTitleBinding

private const val VIEW_TYPE_SECTION_TITLE_TEXT = 1
private const val VIEW_TYPE_SECTION_STORY_ITEM = 2

/** Adapter to inflate different items/views inside [RecyclerView] for Ongoing Story List. */
class OngoingListAdapter(
  private val activity: AppCompatActivity,
  private val itemList: MutableList<RecentlyPlayedItemViewModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val orientation = Resources.getSystem().configuration.orientation
  private var titleIndex: Int = 0
  private var storyGridPosition: Int = 0
  private var spanCount = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_SECTION_TITLE_TEXT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          SectionTitleBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        SectionTitleViewHolder(binding)
      }
      VIEW_TYPE_SECTION_STORY_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          OngoingStoryCardBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        OngoingStoryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (holder.itemViewType) {
      VIEW_TYPE_SECTION_TITLE_TEXT -> {
        titleIndex = position
        (holder as SectionTitleViewHolder).bind(itemList[position] as SectionTitleViewModel)
      }
      VIEW_TYPE_SECTION_STORY_ITEM -> {
        storyGridPosition = position - titleIndex
        (holder as OngoingStoryViewHolder).bind(itemList[position] as OngoingStoryViewModel)
        val marginMin =
          (activity as Context).resources.getDimensionPixelSize(R.dimen.recently_played_margin_min)
        val marginMax =
          (activity as Context).resources.getDimensionPixelSize(R.dimen.recently_played_margin_max)
        val params =
          holder.binding.ongoingStoryCardView.layoutParams as (ViewGroup.MarginLayoutParams)
        val marginTop = if (activity.resources.getBoolean(R.bool.isTablet)) {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
        } else {
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (storyGridPosition > 2) {
              (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_16)
            } else {
              (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
            }
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
          }
        }
        val marginBottom = 0
        when (spanCount) {
          2 -> {
            when {
              storyGridPosition % spanCount == 0 -> params.setMargins(
                marginMin,
                marginTop,
                marginMax,
                marginBottom
              )
              else -> params.setMargins(
                marginMax,
                marginTop,
                marginMin,
                marginBottom
              )
            }
          }
          3 -> {
            when {
              storyGridPosition % spanCount == 1 -> params.setMargins(
                marginMax,
                marginTop,
                /* right= */ 0,
                marginBottom
              )
              storyGridPosition % spanCount == 2 -> params.setMargins(
                marginMin,
                marginTop,
                marginMin,
                marginBottom
              )
              storyGridPosition % spanCount == 0 -> params.setMargins(
                /* left= */ 0,
                marginTop,
                marginMax,
                marginBottom
              )
            }
          }
          4 -> {
            when {
              (storyGridPosition) % spanCount == 1 -> params.setMargins(
                marginMax,
                marginTop,
                /* right= */ 0,
                marginBottom
              )
              (storyGridPosition) % spanCount == 2 -> params.setMargins(
                marginMin,
                marginTop,
                marginMin / 2,
                marginBottom
              )
              (storyGridPosition) % spanCount == 3 -> params.setMargins(
                marginMin / 2,
                marginTop,
                marginMin,
                marginBottom
              )
              (storyGridPosition) % spanCount == 0 -> params.setMargins(
                /* left= */ 0,
                marginTop,
                marginMax,
                marginBottom
              )
            }
          }
        }
        holder.binding.ongoingStoryCardView.layoutParams = params
        holder.binding.ongoingStoryCardView.requestLayout()
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is SectionTitleViewModel -> {
        VIEW_TYPE_SECTION_TITLE_TEXT
      }
      is OngoingStoryViewModel -> {
        VIEW_TYPE_SECTION_STORY_ITEM
      }
      else -> throw IllegalArgumentException(
        "Invalid type of data $position with item ${itemList[position]}"
      )
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  fun setSpanCount(spanCount: Int) {
    this.spanCount = spanCount
  }

  private class SectionTitleViewHolder(
    val binding: SectionTitleBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(sectionTitleViewModel: SectionTitleViewModel) {
      binding.viewModel = sectionTitleViewModel
    }
  }

  private class OngoingStoryViewHolder(
    val binding: OngoingStoryCardBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(ongoingStoryViewModel: OngoingStoryViewModel) {
      binding.viewModel = ongoingStoryViewModel
    }
  }
}
