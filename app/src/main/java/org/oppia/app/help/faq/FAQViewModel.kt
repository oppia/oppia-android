package org.oppia.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQItemViewModel
import javax.inject.Inject

/** View model in [FAQFragment]. */
class FAQViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ViewModel() {
  private val arrayList = ArrayList<FAQItemViewModel>()

  val faqItemList: List<FAQItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<FAQItemViewModel> {
    val faqHeaderViewModel = FAQHeaderViewModel()
    arrayList.add(faqHeaderViewModel)

    val questions: Array<String> = activity.resources.getStringArray(R.array.faq_questions)
    for (question in questions) {
      val faqContentViewModel = FAQContentViewModel(question)
      arrayList.add(faqContentViewModel)
    }
    return arrayList
  }
}
