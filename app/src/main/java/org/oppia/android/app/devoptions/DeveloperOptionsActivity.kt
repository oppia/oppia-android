package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ForceNetworkTypeActivityParams
import org.oppia.android.app.model.MarkChaptersCompletedActivityParams
import org.oppia.android.app.model.MarkStoriesCompletedActivityParams
import org.oppia.android.app.model.MarkTopicsCompletedActivityParams
import org.oppia.android.app.model.MathExpressionParserActivityParams
import org.oppia.android.app.model.ScreenName.DEVELOPER_OPTIONS_ACTIVITY
import org.oppia.android.app.model.ViewEventLogsActivityParams
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Developer Options. */
class DeveloperOptionsActivity :
  InjectableAppCompatActivity(),
  ForceCrashButtonClickListener,
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener,
  RouteToMarkTopicsCompletedListener,
  RouteToViewEventLogsListener,
  RouteToForceNetworkTypeListener,
  RouteToMathExpressionParserTestListener {

  @Inject lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter
  @Inject lateinit var resourceHandler: AppLanguageResourceHandler
  @Inject lateinit var activityRouter: ActivityRouter

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    internalProfileId = intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    developerOptionsActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.developer_options_activity_title)
  }

  override fun routeToMarkChaptersCompleted() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        markChaptersCompletedActivityParams =
          MarkChaptersCompletedActivityParams.newBuilder().apply {
            this.internalProfileId = internalProfileId
            this.showConfirmationNotice = false
          }.build()
      }.build()
    )
  }

  override fun routeToMarkStoriesCompleted() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        markStoriesCompletedActivityParams = MarkStoriesCompletedActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
  }

  override fun routeToMarkTopicsCompleted() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        markTopicsCompletedActivityParams = MarkTopicsCompletedActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
  }

  override fun routeToViewEventLogs() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        viewEventLogsActivityParams = ViewEventLogsActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun routeToForceNetworkType() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        forceNetworkTypeActivityParams = ForceNetworkTypeActivityParams.getDefaultInstance()
      }.build()
    )
  }

  override fun routeToMathExpressionParserTest() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        mathExpressionParserActivityParams = MathExpressionParserActivityParams.getDefaultInstance()
      }.build()
    )
  }

  companion object {
    /** Function to create intent for DeveloperOptionsActivity */
    fun createIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, DeveloperOptionsActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
        decorateWithScreenName(DEVELOPER_OPTIONS_ACTIVITY)
      }
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }

  interface Injector {
    fun inject(activity: DeveloperOptionsActivity)
  }
}
