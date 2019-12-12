package org.oppia.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for slide in onboarding flow. */
class OnboardingViewModel @Inject constructor(
  private val activity: AppCompatActivity
) : ObservableViewModel() {

  // TODO (Rajat): Update this after disucssion with Chantel.
  val slideImage = ObservableField<Int>(R.drawable.lesson_thumbnail_graphic_child_with_cupcakes)
  val title = ObservableField<String>("")
  val description = ObservableField<String>("")
  val slideNumber = ObservableField<Int>(0)

  fun slideChanged(index: Int) {
    slideNumber.set(index)
    when (index) {
      0 -> {
        title.set(activity.resources.getString(R.string.slide_0_title))
        description.set(activity.resources.getString(R.string.slide_0_description))
      }
      1 -> {
        title.set(activity.resources.getString(R.string.slide_1_title))
        description.set(activity.resources.getString(R.string.slide_1_description))
      }
      2 -> {
        title.set(activity.resources.getString(R.string.slide_2_title))
        description.set(activity.resources.getString(R.string.slide_2_description))
      }
      3 -> {
        title.set(activity.resources.getString(R.string.slide_3_title))
        description.set(activity.resources.getString(R.string.slide_3_description))
      }
      else -> {
        title.set(activity.resources.getString(R.string.slide_3_title))
        description.set(activity.resources.getString(R.string.slide_3_description))
        slideNumber.set(3)
      }
    }
  }
}