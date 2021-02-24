package org.oppia.android.app.help.faq

/** Listener for when a selection should result to [FAQSingleActivity]. */
interface RouteToFAQSingleListener {
  fun onRouteToFAQSingle(question: String, answer: String)
}
