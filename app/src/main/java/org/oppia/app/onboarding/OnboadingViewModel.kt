package org.oppia.app.onboarding

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for slide in onboarding flow. */
class OnboardingViewModel(val context: Context, val index: Int) : ObservableViewModel() {

  // TODO (Rajat): Update this after disucssion with Chantel.
  val slideImage = ObservableField<Int>(R.drawable.lesson_thumbnail_graphic_child_with_cupcakes)
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
        title.set(context.resources.getString(R.string.slide_0_title))
        description.set(context.resources.getString(R.string.slide_0_description))
      }
      1 -> {
        title.set(context.resources.getString(R.string.slide_1_title))
        description.set(context.resources.getString(R.string.slide_1_description))
      }
      2 -> {
        title.set(context.resources.getString(R.string.slide_2_title))
        description.set(context.resources.getString(R.string.slide_2_description))
      }
      3 -> {
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