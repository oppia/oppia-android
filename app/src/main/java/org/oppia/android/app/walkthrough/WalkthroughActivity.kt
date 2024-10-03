package org.oppia.android.app.walkthrough

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.WALKTHROUGH_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Activity that contains the walkthrough flow for users. */
class WalkthroughActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  WalkthroughFragmentChangeListener {
  @Inject
  lateinit var walkthroughActivityPresenter: WalkthroughActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    walkthroughActivityPresenter.handleOnCreate()

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          walkthroughActivityPresenter.handleSystemBack()
        }
      }
    )
  }

  override fun currentPage(walkthroughPage: Int) {
    walkthroughActivityPresenter.changePage(walkthroughPage)
  }

  override fun pageWithTopicId(walkthroughPage: Int, topicId: String) {
    walkthroughActivityPresenter.setTopicId(topicId)
    walkthroughActivityPresenter.changePage(walkthroughPage)
  }

  companion object {

    fun createWalkthroughActivityIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      return Intent(context, WalkthroughActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(WALKTHROUGH_ACTIVITY)
      }
    }
  }
}
