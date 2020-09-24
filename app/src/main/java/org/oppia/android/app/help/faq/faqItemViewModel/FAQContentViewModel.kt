package org.oppia.app.help.faq.faqItemViewModel

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.app.help.faq.RouteToFAQSingleListener

/** Content view model for the recycler view in [FAQFragment]. */
class FAQContentViewModel(
  private val activity: AppCompatActivity,
  val question: String,
  val answer: String
) : FAQItemViewModel() {

  /** Used to control visibility of divider. */
  val showDivider = ObservableField(true)

  fun clickOnFAQQuestion() {
    val routeToFAQSingleListener = activity as RouteToFAQSingleListener
    routeToFAQSingleListener.onRouteToFAQSingle(question, answer)
  }
}
