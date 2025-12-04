package org.oppia.android.app.help.faq.faqsingle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.FAQSingleActivityParams
import org.oppia.android.app.model.ScreenName.FAQ_SINGLE_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The FAQ page activity for placement of single FAQ. */
class FAQSingleActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var faqSingleActivityPresenter: FAQSingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      FAQ_SINGLE_ACTIVITY_PARAMS_KEY,
      FAQSingleActivityParams.getDefaultInstance()
    )

    val question = checkNotNull(args?.question) {
      "Expected $FAQ_SINGLE_ACTIVITY_QUESTION to be in intent extras."
    }
    val answer = checkNotNull(args?.answer) {
      "Expected $FAQ_SINGLE_ACTIVITY_ANSWER to be in intent extras."
    }
    faqSingleActivityPresenter.handleOnCreate(question, answer)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val FAQ_SINGLE_ACTIVITY_QUESTION = "FAQSingleActivity.question"
    const val FAQ_SINGLE_ACTIVITY_ANSWER = "FAQSingleActivity.answer"

    /** Params key for FAQSingleActivity. */
    const val FAQ_SINGLE_ACTIVITY_PARAMS_KEY = "FAQSingleActivity.params"

    fun createFAQSingleActivityIntent(context: Context, question: String, answer: String): Intent {

      val intent = Intent(context, FAQSingleActivity::class.java).apply {
        val args = FAQSingleActivityParams.newBuilder().apply {
          this.question = question
          this.answer = answer
        }.build()
        putProtoExtra(FAQ_SINGLE_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(FAQ_SINGLE_ACTIVITY)
      }
      return intent
    }
  }
}
