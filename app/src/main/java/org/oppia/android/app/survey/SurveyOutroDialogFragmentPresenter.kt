package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.SurveyOutroDialogFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.survey.SurveyController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

const val TAG_SURVEY_OUTRO_DIALOG = "SURVEY_OUTRO_DIALOG"

/** Presenter for [SurveyWelcomeDialogFragment], sets up bindings. */
@FragmentScope
class SurveyOutroDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler,
  private val surveyController: SurveyController,
  private val oppiaLogger: OppiaLogger
) {
  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
  ): View {
    val binding =
      SurveyOutroDialogFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    binding.surveyOnboardingText.text = resourceHandler.getStringInLocaleWithWrapping(
      R.string.survey_thank_you_message_text, appName
    )

    binding.finishSurveyButton.setOnClickListener {
      endSurveyWithCallback { closeSurveyDialogAndActivity() }
    }

    return binding.root
  }

  private fun closeSurveyDialogAndActivity() {
    activity.finish()
    activity.supportFragmentManager.beginTransaction().remove(fragment).commitNow()
  }

  private fun endSurveyWithCallback(callback: () -> Unit) {
    surveyController.stopSurveySession(surveyCompleted = true).toLiveData().observe(
      activity,
      { result ->
        when (result) {
          is AsyncResult.Pending -> oppiaLogger.d("SurveyActivity", "Stopping survey session")
          is AsyncResult.Failure -> {
            oppiaLogger.d("SurveyActivity", "Failed to stop the survey session", result.error)
            activity.finish() // Can't recover from the session failing to stop.
          }
          is AsyncResult.Success -> {
            oppiaLogger.d("SurveyActivity", "Stopped the survey session")
            callback()
          }
        }
      }
    )
  }
}
