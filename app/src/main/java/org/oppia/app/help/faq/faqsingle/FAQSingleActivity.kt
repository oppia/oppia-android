package org.oppia.app.help.faq.faqsingle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The FAQ page activity for placement of single FAQ. */
class FAQSingleActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var faqSingleActivityPresenter: FAQSingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val question = intent.getStringExtra(FAQ_SINGLE_ACTIVITY_QUESTION)
    val answer = intent.getStringExtra(FAQ_SINGLE_ACTIVITY_ANSWER)
    faqSingleActivityPresenter.handleOnCreate(question, answer)
  }

  companion object {
    internal const val FAQ_SINGLE_ACTIVITY_QUESTION = "FAQSingleActivity.question"
    internal const val FAQ_SINGLE_ACTIVITY_ANSWER = "FAQSingleActivity.answer"

    fun createFAQSingleActivityIntent(context: Context, question: String, answer: String): Intent {
      val intent = Intent(context, FAQSingleActivity::class.java)
      intent.putExtra(FAQ_SINGLE_ACTIVITY_QUESTION, question)
      intent.putExtra(FAQ_SINGLE_ACTIVITY_ANSWER, answer)
      return intent
    }
  }
}
