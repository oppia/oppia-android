package org.oppia.android.app.survey

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyFragmentArguments
import org.oppia.android.app.survey.SurveyFragment.Companion.SURVEY_FRAGMENT_ARGUMENTS_KEY
import org.oppia.android.databinding.SurveyActivityBinding
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val TAG_SURVEY_FRAGMENT = "TAG_SURVEY_FRAGMENT"

/** The Presenter for [SurveyActivity]. */
@ActivityScope
class SurveyActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var binding: SurveyActivityBinding

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
      val args = SurveyFragmentArguments.newBuilder().apply {
        this.topicId = topicId
        this.explorationId = explorationId
      }.build()
      val bundle = Bundle().apply {
        putProto(SURVEY_FRAGMENT_ARGUMENTS_KEY, args)
        decorateWithUserProfileId(profileId)
      }

      surveyFragment.arguments = bundle

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
