package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.databinding.SurveyWelcomeDialogFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.survey.SurveyController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

const val TAG_SURVEY_WELCOME_DIALOG = "SURVEY_WELCOME_DIALOG"

/** Presenter for [SurveyWelcomeDialogFragment], sets up bindings. */
@FragmentScope
class SurveyWelcomeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val surveyController: SurveyController,
  private val oppiaLogger: OppiaLogger
) {
  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    topicId: String,
    questionNames: List<SurveyQuestionName>,
  ): View {
    val binding =
      SurveyWelcomeDialogFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    binding.beginSurveyButton.setOnClickListener {
      startSurveySession(profileId, topicId, questionNames)
    }

    binding.maybeLaterButton.setOnClickListener {
      activity.supportFragmentManager.beginTransaction()
        .remove(fragment)
        .commitNow()
    }

    return binding.root
  }

  private fun startSurveySession(
    profileId: ProfileId,
    topicId: String,
    questions: List<SurveyQuestionName>
  ) {
    val startDataProvider = surveyController.startSurveySession(questions)
    startDataProvider.toLiveData().observe(
      activity,
      {
        when (it) {
          is AsyncResult.Pending ->
            oppiaLogger.d("SurveyWelcomeDialogFragment", "Starting a survey session")
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "SurveyWelcomeDialogFragment",
              "Failed to start a survey session",
              it.error
            )
            activity.finish() // Can't recover from the session failing to start.
          }
          is AsyncResult.Success -> {
            oppiaLogger.d("SurveyWelcomeDialogFragment", "Successfully started a survey session")
            val intent =
              SurveyActivity.createSurveyActivityIntent(activity, profileId, topicId)
            fragment.startActivity(intent)
            activity.finish()
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.remove(fragment).commitAllowingStateLoss()
          }
        }
      }
    )
  }
}
