package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.model.ScreenName.DEVELOPER_OPTIONS_ACTIVITY
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

  @Inject
  lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    developerOptionsActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.developer_options_activity_title)
  }

  override fun routeToMarkChaptersCompleted() {
    startActivity(
      MarkChaptersCompletedActivity
        .createMarkChaptersCompletedIntent(this, internalProfileId)
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

  override fun routeToMathExpressionParserTest() {
    startActivity(MathExpressionParserActivity.createIntent(this))
  }

  companion object {
    /** Function to create intent for DeveloperOptionsActivity */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, DeveloperOptionsActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
        decorateWithScreenName(DEVELOPER_OPTIONS_ACTIVITY)
      }
    }

    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }
}
