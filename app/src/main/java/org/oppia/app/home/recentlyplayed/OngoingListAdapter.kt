package org.oppia.app.home.recentlyplayed

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.OngoingStoryCardBinding
import org.oppia.app.databinding.SectionTitleBinding

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
  private var recyclerView: RecyclerView? = null

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

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.recyclerView = recyclerView
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (holder.itemViewType) {
      VIEW_TYPE_SECTION_TITLE_TEXT -> {
        titleIndex = position + 1
        (holder as SectionTitleViewHolder).bind(itemList[position] as SectionTitleViewModel)
      }
      VIEW_TYPE_SECTION_STORY_ITEM -> {
        val spanCount = (recyclerView!!.layoutManager as GridLayoutManager).spanCount
        storyGridPosition = position - titleIndex
        val x: Int = storyGridPosition % spanCount
        val column = x + 1
        (holder as OngoingStoryViewHolder).bind(itemList[position] as OngoingStoryViewModel)
        val marginStart = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          if (column == 1) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_8)
          }
        } else {
          if (column == 1) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_72)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_16)
          }
        }
        val marginEnd = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          if (storyGridPosition % 2 == 1) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_8)
          }
        } else {
          if (column == 3) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_72)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_0)
          }
        }
        val marginTop = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          if (storyGridPosition > 1) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_16)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
          }
        } else {
          if (storyGridPosition <= 2) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
          } else {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_16)
          }
        }
        val params =
          holder.binding.ongoingStoryCardView!!.layoutParams as (ViewGroup.MarginLayoutParams)
        val marginBottom = 0
        params.setMargins(marginStart, marginTop, marginEnd, marginBottom)
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
      else -> throw IllegalArgumentException("Invalid type of data $position with item ${itemList[position]}")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
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
