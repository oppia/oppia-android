package org.oppia.android.app.help.faq.faqsingle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The FAQ page activity for placement of single FAQ. */
class FAQSingleActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var faqSingleActivityPresenter: FAQSingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val question = checkNotNull(intent.getStringExtra(FAQ_SINGLE_ACTIVITY_QUESTION)) {
      "Expected FAQ_SINGLE_ACTIVITY_QUESTION to be in intent extras."
    }
    val answer = checkNotNull(intent.getStringExtra(FAQ_SINGLE_ACTIVITY_ANSWER)) {
      "Expected FAQ_SINGLE_ACTIVITY_ANSWER to be in intent extras."
    }
    faqSingleActivityPresenter.handleOnCreate(question, answer)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val FAQ_SINGLE_ACTIVITY_QUESTION = "FAQSingleActivity.question"
    const val FAQ_SINGLE_ACTIVITY_ANSWER = "FAQSingleActivity.answer"

    fun createFAQSingleActivityIntent(context: Context, question: String, answer: String): Intent {
      val intent = Intent(context, FAQSingleActivity::class.java)
      intent.putExtra(FAQ_SINGLE_ACTIVITY_QUESTION, question)
      intent.putExtra(FAQ_SINGLE_ACTIVITY_ANSWER, answer)
      return intent
    }
  }
}
