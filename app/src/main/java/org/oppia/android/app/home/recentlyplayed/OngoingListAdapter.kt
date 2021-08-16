package org.oppia.android.app.home.recentlyplayed

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.databinding.OngoingStoryCardBinding
import org.oppia.android.databinding.SectionTitleBinding

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
      // TODO(#632): Generalize this binding to make adding future items easier.
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
