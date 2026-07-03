package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.featureflags.FeatureFlagsActivity
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivity
import org.oppia.android.app.devoptions.platformparameters.PlatformParametersActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ScreenName.DEVELOPER_OPTIONS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for Developer Options. */
class DeveloperOptionsActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ForceCrashButtonClickListener,
  ForceDownloadRemoteParametersButtonClickListener,
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener,
  RouteToMarkTopicsCompletedListener,
  RouteToViewEventLogsListener,
  RouteToForceNetworkTypeListener,
  RouteToMathExpressionParserTestListener,
  RouteToFeatureFlagsListener,
  RouteToPlatformParametersListener,
  AppRestartListener {

  @Inject
  lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.extractCurrentUserProfileId().internalId
    developerOptionsActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.developer_options_activity_title)
  }

  override fun routeToMarkChaptersCompleted() {
    startActivity(
      MarkChaptersCompletedActivity.createMarkChaptersCompletedIntent(
        context = this, internalProfileId, showConfirmationNotice = false
      )
    )
  }

  override fun routeToMarkStoriesCompleted() {
    startActivity(
      MarkStoriesCompletedActivity
        .createMarkStoriesCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToMarkTopicsCompleted() {
    startActivity(
      MarkTopicsCompletedActivity
        .createMarkTopicsCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToViewEventLogs() {
    startActivity(ViewEventLogsActivity.createViewEventLogsActivityIntent(this))
  }

  override fun routeToForceNetworkType() {
    startActivity(ForceNetworkTypeActivity.createForceNetworkTypeActivityIntent(this))
  }

  override fun routeToFeatureFlags() {
    startActivity(
      FeatureFlagsActivity.createFeatureFlagsActivityIntent(this)
    )
  }

  override fun routeToPlatformParameters() {
    startActivity(
      PlatformParametersActivity.createPlatformParametersActivityIntent(this)
    )
  }

  override fun routeToMathExpressionParserTest() {
    startActivity(MathExpressionParserActivity.createIntent(this))
  }

  companion object {

    /** Function to create intent for DeveloperOptionsActivity. */
    fun createDeveloperOptionsActivityIntent(context: Context, profileId: LegacyProfileId): Intent {

      return Intent(context, DeveloperOptionsActivity::class.java).apply {
        decorateWithScreenName(DEVELOPER_OPTIONS_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }

  override fun forceDownloadRemoteParameters() {
    developerOptionsActivityPresenter.forceDownloadRemoteParameters()
  }

  override fun restartApp() {
    developerOptionsActivityPresenter.restartApp()
  }
}
