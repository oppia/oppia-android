package org.oppia.android.app.survey

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.SurveyActivityBinding
import javax.inject.Inject

private const val TAG_SURVEY_FRAGMENT = "TAG_SURVEY_FRAGMENT"

const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
const val TOPIC_ID_ARGUMENT_KEY = "topic_id"
const val EXPLORATION_ID_ARGUMENT_KEY = "exploration_id"

/** The Presenter for [SurveyActivity]. */
@ActivityScope
class SurveyActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var binding: SurveyActivityBinding

  /** Handle creation and binding of the SurveyActivity layout. */
  fun handleOnCreate(
    profileId: ProfileId,
    topicId: String,
    explorationId: String
  ) {
    binding = DataBindingUtil.setContentView(activity, R.layout.survey_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    if (getSurveyFragment() == null) {
      val surveyFragment = SurveyFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, profileId.internalId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      args.putString(EXPLORATION_ID_ARGUMENT_KEY, explorationId)

      surveyFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.survey_fragment_placeholder,
        surveyFragment, TAG_SURVEY_FRAGMENT
      ).commitNow()
    }
  }

  private fun getSurveyFragment(): SurveyFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_SURVEY_FRAGMENT
    ) as? SurveyFragment
  }
}
