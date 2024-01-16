package org.oppia.android.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.extensions.containsPlaceholderRegex
import javax.inject.Inject

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

    return questionsOrAnswers.map {
      if (it.containsPlaceholderRegex())
        resourceHandler.formatInLocaleWithWrapping(it, appName)
      else it
    }
  }

  private fun retrieveQuestions(): List<String> =
    retrieveQuestionsOrAnswers(resourceHandler.getStringArrayInLocale(R.array.faq_questions))

  private fun retrieveAnswers(): List<String> =
    retrieveQuestionsOrAnswers(resourceHandler.getStringArrayInLocale(R.array.faq_answers))
}
