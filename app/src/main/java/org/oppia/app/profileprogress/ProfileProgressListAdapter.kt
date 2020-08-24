package org.oppia.app.profileprogress

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ProfileProgressHeaderBinding
import org.oppia.app.databinding.ProfileProgressRecentlyPlayedStoryCardBinding

private const val VIEW_TYPE_HEADER = 1
private const val VIEW_TYPE_RECENTLY_PLAYED_STORY = 2

class ProfileProgressListAdapter(
  private val activity: AppCompatActivity,
  private val itemList: MutableList<ProfileProgressItemViewModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val orientation = Resources.getSystem().configuration.orientation
  private var spanCount = 0
  private var titleIndex = 0
  private var storyGridPosition: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_HEADER -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          ProfileProgressHeaderBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        ProfileProgressHeaderViewHolder(binding)
      }
      VIEW_TYPE_RECENTLY_PLAYED_STORY -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          ProfileProgressRecentlyPlayedStoryCardBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        ProfileProgressRecentlyPlayedStoryCardViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> {
        titleIndex = position
        (holder as ProfileProgressHeaderViewHolder).bind(
          itemList[position] as ProfileProgressHeaderViewModel
        )
      }
      VIEW_TYPE_RECENTLY_PLAYED_STORY -> {
        storyGridPosition = position - titleIndex
        (holder as ProfileProgressRecentlyPlayedStoryCardViewHolder)
          .bind(itemList[position] as RecentlyPlayedStorySummaryViewModel)

        val params =
          holder.binding.profileProgressRecentlyPlayedStoryCard
            .layoutParams as (ViewGroup.MarginLayoutParams)

        val marginMin =
          (activity as Context).resources.getDimensionPixelSize(R.dimen.recently_played_margin_min)
        val marginMax =
          (activity as Context).resources.getDimensionPixelSize(R.dimen.recently_played_margin_max)

        val marginTop = if (activity.resources.getBoolean(R.bool.isTablet)) {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
        } else {
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_20)
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
        }
        holder.binding.profileProgressRecentlyPlayedStoryCard.layoutParams = params
        holder.binding.profileProgressRecentlyPlayedStoryCard.requestLayout()
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is ProfileProgressHeaderViewModel -> {
        VIEW_TYPE_HEADER
      }
      is RecentlyPlayedStorySummaryViewModel -> {
        VIEW_TYPE_RECENTLY_PLAYED_STORY
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

  private class ProfileProgressHeaderViewHolder(
    val binding: ProfileProgressHeaderBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(profileProgressHeaderViewModel: ProfileProgressHeaderViewModel) {
      binding.viewModel = profileProgressHeaderViewModel
    }
  }

  private class ProfileProgressRecentlyPlayedStoryCardViewHolder(
    val binding: ProfileProgressRecentlyPlayedStoryCardBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(recentlyPlayedStorySummaryViewModel: RecentlyPlayedStorySummaryViewModel) {
      binding.viewModel = recentlyPlayedStorySummaryViewModel
    }
  }
}
