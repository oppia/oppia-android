package org.oppia.android.app.help.faq.faqItemViewModel

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.help.faq.RouteToFAQSingleListener

/** Content view model for the recycler view in [FAQFragment]. */
class FAQContentViewModel(
  private val activity: AppCompatActivity,
  val question: String,
  val answer: String,
  val showDivider: Boolean
) : FAQItemViewModel() {

  fun clickOnFAQQuestion() {
    val routeToFAQSingleListener = activity as RouteToFAQSingleListener
    routeToFAQSingleListener.onRouteToFAQSingle(question, answer)
  }
}
