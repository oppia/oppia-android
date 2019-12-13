package org.oppia.app.onboarding

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for slide in onboarding flow. */
class OnboardingViewModel(val context: Context, val index: Int) : ObservableViewModel() {
  val slideImage = ObservableField<Int>(R.drawable.ic_onboarding_1)
  val title = ObservableField<String>(context.resources.getString(R.string.slide_0_title))
  val description = ObservableField<String>(context.resources.getString(R.string.slide_0_description))
  val slideNumber = ObservableField<Int>(0)

  init {
    slideChanged(index)
  }

  private fun slideChanged(index: Int) {
    slideNumber.set(index)
    when (index) {
      0 -> {
        slideImage.set(R.drawable.ic_onboarding_1)
        title.set(context.resources.getString(R.string.slide_0_title))
        description.set(context.resources.getString(R.string.slide_0_description))
      }
      1 -> {
        slideImage.set(R.drawable.ic_onboarding_2)
        title.set(context.resources.getString(R.string.slide_1_title))
        description.set(context.resources.getString(R.string.slide_1_description))
      }
      2 -> {
        slideImage.set(R.drawable.ic_onboarding_3)
        title.set(context.resources.getString(R.string.slide_2_title))
        description.set(context.resources.getString(R.string.slide_2_description))
      }
      3 -> {
        slideImage.set(R.drawable.ic_onboarding_4)
        title.set(context.resources.getString(R.string.slide_3_title))
        description.set(context.resources.getString(R.string.slide_3_description))
      }
      else -> {
        title.set(context.resources.getString(R.string.slide_3_title))
        description.set(context.resources.getString(R.string.slide_3_description))
        slideNumber.set(3)
      }
    }
  }
}