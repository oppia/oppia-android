package org.oppia.android.app.onboarding

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/** Adapter to control the slide details in onboarding flow. */
class OnboardingPagerAdapter(
  val context: Context,
  val onboardingSlideFinalViewModel: OnboardingSlideFinalViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    TODO("Not yet implemented")
  }

  override fun getItemCount(): Int {
    return TOTAL_NUMBER_OF_SLIDES
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    TODO("Not yet implemented")
  }
}
