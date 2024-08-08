package org.oppia.android.app.survey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.SurveyActivityParams
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for showing a survey. */
class SurveyActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var surveyActivityPresenter: SurveyActivityPresenter

  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val params = intent.extractParams()
    this.profileId = params.profileId ?: ProfileId.newBuilder().setLoggedInInternalProfileId(-1).build()

    surveyActivityPresenter.handleOnCreate(
      profileId,
      params.topicId,
      params.explorationId
    )
  }

  override fun onBackPressed() {
    val dialogFragment = ExitSurveyConfirmationDialogFragment.newInstance(profileId)
    dialogFragment.showNow(supportFragmentManager, TAG_EXIT_SURVEY_CONFIRMATION_DIALOG)
  }

  companion object {
    private const val PARAMS_KEY = "SurveyActivity.params"

    /**
     * A convenience function for creating a new [SurveyActivity] intent by prefilling common
     * params needed by the activity.
     */
    fun createSurveyActivityIntent(
      context: Context,
      profileId: ProfileId,
      topicId: String,
      explorationId: String
    ): Intent {
      val params = SurveyActivityParams.newBuilder().apply {
        this.profileId = profileId
        this.topicId = topicId
        this.explorationId = explorationId
      }.build()
      return createSurveyActivityIntent(context, params)
    }

    /** Returns a new [Intent] open a [SurveyActivity] with the specified [params]. */
    fun createSurveyActivityIntent(
      context: Context,
      params: SurveyActivityParams
    ): Intent {
      return Intent(context, SurveyActivity::class.java).apply {
        putProtoExtra(PARAMS_KEY, params)
        decorateWithScreenName(ScreenName.SURVEY_ACTIVITY)
      }
    }

    private fun Intent.extractParams() =
      getProtoExtra(PARAMS_KEY, SurveyActivityParams.getDefaultInstance())
  }
}
