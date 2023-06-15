package org.oppia.android.app.help.faq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.FAQ_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The FAQ page activity for placement of different FAQs. */
class FAQListActivity : InjectableAutoLocalizedAppCompatActivity(), RouteToFAQSingleListener {

  @Inject
  lateinit var faqListActivityPresenter: FAQListActivityPresenter
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    faqListActivityPresenter.handleOnCreate()
  }

  companion object {
    fun createFAQListActivityIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, FAQListActivity::class.java).apply {
        decorateWithScreenName(FAQ_LIST_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun onRouteToFAQSingle(question: String, answer: String) {
    startActivity(
      FAQSingleActivity.createFAQSingleActivityIntent
      (this, question, answer, profileId)
    )
  }
}
