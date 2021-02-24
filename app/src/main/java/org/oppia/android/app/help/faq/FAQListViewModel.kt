package org.oppia.android.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.android.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [FAQListFragment]. */
class FAQListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {
  private val arrayList = ArrayList<FAQItemViewModel>()

  val faqItemList: List<FAQItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<FAQItemViewModel> {
    val faqHeaderViewModel = FAQHeaderViewModel()
    arrayList.add(faqHeaderViewModel)
    val questions: Array<String> = activity.resources.getStringArray(R.array.faq_questions)
    val answers: Array<String> = activity.resources.getStringArray(R.array.faq_answers)
    questions.forEachIndexed { index, question ->
      val faqContentViewModel = FAQContentViewModel(activity, question, answers[index])
      if (questions[questions.size - 1] == question) {
        faqContentViewModel.showDivider.set(false)
      } else {
        faqContentViewModel.showDivider.set(true)
      }
      arrayList.add(faqContentViewModel)
    }
    return arrayList
  }
}
