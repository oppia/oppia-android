package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.DEVELOPER_OPTIONS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for Developer Options. */
class DeveloperOptionsActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ForceCrashButtonClickListener,
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener,
  RouteToMarkTopicsCompletedListener,
  RouteToViewEventLogsListener,
  RouteToForceNetworkTypeListener,
  RouteToMathExpressionParserTestListener {

  @Inject
  lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    developerOptionsActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.developer_options_activity_title)
  }

  override fun routeToMarkChaptersCompleted() {
    if (!profileId.loggedOut) {
      startActivity(
        MarkChaptersCompletedActivity.createMarkChaptersCompletedIntent(
          context = this, profileId.loggedInInternalProfileId, showConfirmationNotice = false
        )
      )
    }
  }

  override fun routeToMarkStoriesCompleted() {
    startActivity(
      MarkStoriesCompletedActivity
        .createMarkStoriesCompletedIntent(this, profileId.loggedInInternalProfileId)
    )
  }

  override fun routeToMarkTopicsCompleted() {
    startActivity(
      MarkTopicsCompletedActivity
        .createMarkTopicsCompletedIntent(this, profileId.loggedInInternalProfileId)
    )
  }

  override fun routeToViewEventLogs() {
    startActivity(ViewEventLogsActivity.createViewEventLogsActivityIntent(this))
  }

  override fun routeToForceNetworkType() {
    startActivity(ForceNetworkTypeActivity.createForceNetworkTypeActivityIntent(this))
  }

  override fun routeToMathExpressionParserTest() {
    startActivity(MathExpressionParserActivity.createIntent(this))
  }

  companion object {

    /** Function to create intent for DeveloperOptionsActivity. */
    fun createDeveloperOptionsActivityIntent(context: Context, profileId: ProfileId): Intent {

      return Intent(context, DeveloperOptionsActivity::class.java).apply {
        decorateWithScreenName(DEVELOPER_OPTIONS_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }
}
