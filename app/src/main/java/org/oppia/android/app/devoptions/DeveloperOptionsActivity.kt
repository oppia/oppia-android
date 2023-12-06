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
import org.oppia.android.app.model.DeveloperOptionsActivityArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.DEVELOPER_OPTIONS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
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

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      DEVELOPER_OPTIONS_ACTIVITY_AEGUMENTS_KEY,
      DeveloperOptionsActivityArguments.getDefaultInstance()
    )
    internalProfileId = args?.internalProfileId ?: -1
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

  override fun routeToMathExpressionParserTest() {
    startActivity(MathExpressionParserActivity.createIntent(this))
  }

  companion object {
    /** Arguments key for DeveloperOptionsActivity */
    const val DEVELOPER_OPTIONS_ACTIVITY_AEGUMENTS_KEY = "DeveloperOptionsActivity.arguments"

    /** Function to create intent for DeveloperOptionsActivity. */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val args =
        DeveloperOptionsActivityArguments.newBuilder().setInternalProfileId(internalProfileId)
          .build()
      val profileid = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      return Intent(context, DeveloperOptionsActivity::class.java).apply {
        putProtoExtra(DEVELOPER_OPTIONS_ACTIVITY_AEGUMENTS_KEY, args)
        decorateWithScreenName(DEVELOPER_OPTIONS_ACTIVITY)
//        decorateWithUserProfileId(profileid)
      }
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }
}
