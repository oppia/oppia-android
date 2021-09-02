package org.oppia.android.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import java.util.Locale
import javax.inject.Inject

/** View model in [FAQListFragment]. */
class FAQListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {
  val faqItemList: List<FAQItemViewModel> by lazy {
    computeFaqViewModelList()
  }

  private fun computeFaqViewModelList(): List<FAQItemViewModel> {
    val questions = retrieveQuestions()
    val faqs = questions.zip(retrieveAnswers()).mapIndexed { index, (question, answer) ->
      FAQContentViewModel(activity, question, answer, showDivider = index != questions.lastIndex)
    }
    return listOf(FAQHeaderViewModel()) + faqs
  }

  private fun retrieveQuestionsOrAnswers(questionsOrAnswers: Array<String>): List<String> {
    val appName = activity.resources.getString(R.string.app_name)
    return questionsOrAnswers.mapIndexed { index, questionOrAnswer ->
      if (index == QUESTION_INDEX_WITH_OPPIA_REFERENCE) {
        String.format(Locale.getDefault(), questionOrAnswer, appName)
      } else questionOrAnswer
    }
  }

  private fun retrieveQuestions(): List<String> =
    retrieveQuestionsOrAnswers(activity.resources.getStringArray(R.array.faq_questions))

  private fun retrieveAnswers(): List<String> =
    retrieveQuestionsOrAnswers(activity.resources.getStringArray(R.array.faq_answers))

  private companion object {
    private const val QUESTION_INDEX_WITH_OPPIA_REFERENCE = 3
  }
}
