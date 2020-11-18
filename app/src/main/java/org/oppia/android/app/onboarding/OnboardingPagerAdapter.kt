/**
Fix the naming, format and if there is any tweak. Not working properly on landscape mode
 */

package org.oppia.android.app.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.databinding.OnboardingSlideBinding
import org.oppia.android.databinding.OnboardingSlideFinalBinding

private const val VIEW_TYPE_TITLE_TEXT = 1
private const val VIEW_TYPE_STORY_ITEM = 2

class OnboardingPagerAdapter(
  val context: Context
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_TITLE_TEXT -> {
        val binding = OnboardingSlideBinding.inflate(
          LayoutInflater.from(context),
          parent,
          false
        )
        OnboardinSlideViewHolder(binding)
      }
      VIEW_TYPE_STORY_ITEM -> {
        val binding =
          OnboardingSlideFinalBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
          )
        OnboardinSlideFinalViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == TOTAL_NUMBER_OF_SLIDES - 1) {
      VIEW_TYPE_STORY_ITEM
    } else {
      VIEW_TYPE_TITLE_TEXT
    }
  }

  override fun getItemCount(): Int {
    return TOTAL_NUMBER_OF_SLIDES
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_TITLE_TEXT -> {
        val onboardingSlideViewModel =
          OnboardingSlideViewModel(context, ViewPagerSlide.getSlideForPosition(position))
        (holder as OnboardinSlideViewHolder).bind(onboardingSlideViewModel)
      }
      VIEW_TYPE_STORY_ITEM -> {
        (holder as OnboardinSlideFinalViewHolder).bind()
      }
    }
  }

  private class OnboardinSlideViewHolder(
    private val binding: OnboardingSlideBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(onboardingSlideViewModel: OnboardingSlideViewModel) {
      binding.viewModel = onboardingSlideViewModel
    }
  }

  private class OnboardinSlideFinalViewHolder(
    binding: OnboardingSlideFinalBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {}
  }
}