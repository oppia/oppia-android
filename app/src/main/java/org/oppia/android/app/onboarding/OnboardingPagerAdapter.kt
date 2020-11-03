package org.oppia.android.app.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R

const val onBoardingSlide = 0
const val onBoardingFinalSlide = 1

/** Adapter to control the slide details in onboarding flow. */
class OnboardingPagerAdapter(
  val context: Context,
  val onboardingSlideFinalViewModel: OnboardingSlideFinalViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == onBoardingSlide) {
      val onBoardingSlideView = LayoutInflater.from(context)
        .inflate(R.layout.onboarding_slide, parent, false)
      SlideViewHolder(onBoardingSlideView)
    } else {
      val onBoardingFinalSlideView = LayoutInflater.from(context)
        .inflate(R.layout.onboarding_slide_final, parent, false)
      FinalSlideViewHolder(onBoardingFinalSlideView)
    }
  }

  override fun getItemCount(): Int {
    return TOTAL_NUMBER_OF_SLIDES
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == TOTAL_NUMBER_OF_SLIDES - 1) {
      onBoardingFinalSlide
    } else {
      onBoardingSlide
    }
  }

  inner class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

  inner class FinalSlideViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}
