package org.oppia.android.app.survey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.databinding.SurveyFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [SurveyFragment]. */
class SurveyFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val surveyController: SurveyController,
  private val surveyProgressController: SurveyProgressController,
  private val surveyViewModel: SurveyViewModel
) {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var binding: SurveyFragmentBinding
  private lateinit var surveyToolbar: Toolbar

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.topicId = topicId

    binding = SurveyFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = surveyViewModel
    }

    surveyToolbar = binding.surveyToolbar
    activity.setSupportActionBar(surveyToolbar)
    surveyToolbar.setNavigationOnClickListener {
      val dialogFragment = ExitSurveyConfirmationDialogFragment.newInstance(profileId)
      dialogFragment.showNow(fragment.childFragmentManager, TAG_EXIT_SURVEY_CONFIRMATION_DIALOG)
    }

    startSurveySession() // todo maybe move this to the activity

    return binding.root
  }

  private fun startSurveySession() {
    val startDataProvider = surveyController.startSurveySession()
    startDataProvider.toLiveData().observe(
      activity,
      {
        when (it) {
          is AsyncResult.Pending ->
            oppiaLogger.d("SurveyFragment", "Starting survey session")
          is AsyncResult.Failure -> {
            oppiaLogger.e("SurveyFragment", "Failed to start survey session", it.error)
            activity.finish() // Can't recover from the session failing to start.
          }
          is AsyncResult.Success -> {
            oppiaLogger.d("SurveyFragment", "Successfully started survey session")
            subscribeToCurrentQuestion()
          }
        }
      }
    )
  }

  private fun subscribeToCurrentQuestion() {
    surveyProgressController.getCurrentQuestion().toLiveData().observe(
      fragment,
      {
        processEphemeralQuestionResult(it)
      }
    )
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralSurveyQuestion>) {
    when (result) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "SurveyFragment", "Failed to retrieve ephemeral question", result.error
        )
      }
      is AsyncResult.Pending -> {} // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralQuestion(result.value)
    }
  }

  private fun processEphemeralQuestion(ephemeralQuestion: EphemeralSurveyQuestion) {
    // updateProgress(ephemeralQuestion.currentQuestionIndex, ephemeralQuestion.totalQuestionCount)
    updateProgress(1, 4)
    updateQuestionText(ephemeralQuestion.question.questionName)
  }

  private fun updateProgress(currentQuestionIndex: Int, questionCount: Int) {
    surveyViewModel.updateQuestionProgress(
      progressPercentage = (((currentQuestionIndex + 1) / questionCount.toDouble()) * 100).toInt()
    )
  }

  private fun updateQuestionText(questionName: SurveyQuestionName) {
    surveyViewModel.updateQuestionText(questionName)
  }

  fun handleKeyboardAction() {
    hideKeyboard()
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.view!!.windowToken,
      InputMethodManager.SHOW_FORCED
    )
  }
}
