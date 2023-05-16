package org.oppia.android.app.help.faq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.FaqSingleActivityParams
import org.oppia.android.app.model.ScreenName.FAQ_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The FAQ page activity for placement of different FAQs. */
class FAQListActivity : InjectableAppCompatActivity(), RouteToFAQSingleListener {
  @Inject lateinit var faqListActivityPresenter: FAQListActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    faqListActivityPresenter.handleOnCreate()
  }

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, FAQListActivity::class.java).apply {
        decorateWithScreenName(FAQ_LIST_ACTIVITY)
      }
    }
  }

  override fun onRouteToFAQSingle(question: String, answer: String) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        faqSingleActivityParams = FaqSingleActivityParams.newBuilder().apply {
          this.questionText = question
          this.answerText = answer
        }.build()
      }.build()
    )
  }

  interface Injector {
    fun inject(activity: FAQListActivity)
  }
}
