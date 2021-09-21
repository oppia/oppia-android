package org.oppia.android.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** View model in [FAQListFragment]. */
class FAQListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
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

  private fun retrieveQuestionsOrAnswers(questionsOrAnswers: List<String>): List<String> {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    return questionsOrAnswers.mapIndexed { index, questionOrAnswer ->
      if (index == QUESTION_INDEX_WITH_OPPIA_REFERENCE) {
        resourceHandler.formatInLocaleWithWrapping(questionOrAnswer, appName)
      } else questionOrAnswer
    }
  }

  private fun retrieveQuestions(): List<String> =
    retrieveQuestionsOrAnswers(resourceHandler.getStringArrayInLocale(R.array.faq_questions))

  private fun retrieveAnswers(): List<String> =
    retrieveQuestionsOrAnswers(resourceHandler.getStringArrayInLocale(R.array.faq_answers))

  private companion object {
    private const val QUESTION_INDEX_WITH_OPPIA_REFERENCE = 3
  }
}
