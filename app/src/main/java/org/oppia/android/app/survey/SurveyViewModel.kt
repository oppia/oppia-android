package org.oppia.android.app.survey

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import javax.inject.Inject
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.app.viewmodel.ObservableViewModel

class SurveyViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<SurveyViewModel> = ObservableArrayList()
  val itemIndex = ObservableField<Int>()

  val progressPercentage = ObservableField(0)

  val questionProgressText: ObservableField<String> =
    ObservableField("$DEFAULT_QUESTION_PROGRESS%")

  fun updateQuestionProgress(
    progressPercentage: Int
  ) {
    this.progressPercentage.set(progressPercentage)
    questionProgressText.set("$progressPercentage%")
  }

  private companion object {
    private const val DEFAULT_QUESTION_PROGRESS = 25
  }
}
