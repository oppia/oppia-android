package org.oppia.android.app.survey

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.databinding.SurveyActivityBinding

/** The Presenter for [SurveyActivity]. */
@ActivityScope
class SurveyActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var surveyToolbar: Toolbar
  private lateinit var surveyProgressText: TextView
  private lateinit var surveyProgressBar: ProgressBar
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

    surveyToolbar = binding.surveyToolbar
    surveyProgressText = binding.surveyProgressText
    surveyProgressBar = binding.surveyProgressBar
    activity.setSupportActionBar(surveyToolbar)

    binding.surveyToolbar.setNavigationOnClickListener {
      // Implementation of this will show a confirm exit dialog.
      activity.onBackPressed()
    }

    this.profileId = profileId
    this.topicId = topicId
    this.context = context
  }

  // Create SurveyFragment instance here.
}
