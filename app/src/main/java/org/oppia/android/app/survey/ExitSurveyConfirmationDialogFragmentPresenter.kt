package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.SurveyExitConfirmationDialogBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.survey.SurveyController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

const val TAG_EXIT_SURVEY_CONFIRMATION_DIALOG = "EXIT_SURVEY_CONFIRMATION_DIALOG"

/** Presenter for [ExitSurveyConfirmationDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class ExitSurveyConfirmationDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val surveyController: SurveyController,
  private val oppiaLogger: OppiaLogger
) {

  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View {
    val binding =
      SurveyExitConfirmationDialogBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    binding.continueSurveyButton.setOnClickListener {
      fragment.parentFragmentManager.beginTransaction()
        .remove(fragment)
        .commitNow()
    }

    binding.exitSurveyButton.setOnClickListener {
      endSurveyWithCallback { closeSurveyDialogAndActivity() }
    }

    return binding.root
  }

  private fun closeSurveyDialogAndActivity() {
    activity.finish()
    fragment.parentFragmentManager.beginTransaction()
      .remove(fragment)
      .commitNow()
  }

  private fun endSurveyWithCallback(callback: () -> Unit) {
    surveyController.stopSurveySession(isCompletion = false).toLiveData().observe(
      activity,
      {
        when (it) {
          is AsyncResult.Pending -> oppiaLogger.d("SurveyActivity", "Stopping survey session")
          is AsyncResult.Failure -> {
            oppiaLogger.d("SurveyActivity", "Failed to stop the survey session")
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
