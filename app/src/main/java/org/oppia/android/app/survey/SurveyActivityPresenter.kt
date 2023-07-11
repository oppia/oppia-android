package org.oppia.android.app.survey

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.SurveyActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

private const val TAG_SURVEY_FRAGMENT = "TAG_SURVEY_FRAGMENT"

const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
const val TOPIC_ID_ARGUMENT_KEY = "topic_id"

/** The Presenter for [SurveyActivity]. */
@ActivityScope
class SurveyActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var context: Context

  private lateinit var binding: SurveyActivityBinding

  fun handleOnCreate(
    context: Context,
    profileId: ProfileId,
    topicId: String
  ) {
    binding = DataBindingUtil.setContentView(activity, R.layout.survey_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    this.profileId = profileId
    this.topicId = topicId
    this.context = context

    if (getSurveyFragment() == null) {
      val surveyFragment = SurveyFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, profileId.internalId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)

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
