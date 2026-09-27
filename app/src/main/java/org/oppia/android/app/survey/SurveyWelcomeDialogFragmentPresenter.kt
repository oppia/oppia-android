package org.oppia.android.app.survey

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.SurveyWelcomeDialogFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.survey.SurveyController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.toProfileIdPreservingZero
import javax.inject.Inject

const val TAG_SURVEY_WELCOME_DIALOG = "SURVEY_WELCOME_DIALOG"

/** Presenter for [SurveyWelcomeDialogFragment], sets up bindings. */
@FragmentScope
class SurveyWelcomeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val surveyController: SurveyController,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val profileManagementController: ProfileManagementController,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var explorationId: String
  private lateinit var rootView: View
  private val dismissSurveyListener = activity as DismissSurveyListener

  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: LegacyProfileId,
    topicId: String,
    explorationId: String,
    questionNames: List<SurveyQuestionName>,
  ): View {
    this.explorationId = explorationId
    val binding =
      SurveyWelcomeDialogFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    rootView = binding.root

    binding.lifecycleOwner = fragment

    binding.beginSurveyButton.setOnClickListener {
      startSurveySession(profileId, topicId, questionNames)
    }

    binding.maybeLaterButton.setOnClickListener {
      dismissSurveyListener.dismissSurvey()
    }

    profileManagementController.updateSurveyLastShownTimestamp(
      profileId.toProfileIdPreservingZero()
    )

    logSurveyPopUpShownEvent(explorationId, topicId, profileId)

    return binding.root
  }

  /**
   * Applies edge-to-edge window insets to the survey welcome [dialog].
   *
   * The dialog is shown in its own window, so the insets applied to the host activity don't reach
   * it and its content would otherwise be drawn underneath the status bar.
   */
  fun applyEdgeToEdgeInsets(dialog: Dialog) {
    if (!enableEdgeToEdge.value) return
    EdgeToEdgeHelper.applyToDialogRootView(
      dialog,
      rootView,
      R.color.component_color_shared_dialogs_secondary_color
    )
  }

  private fun startSurveySession(
    profileId: LegacyProfileId,
    topicId: String,
    questions: List<SurveyQuestionName>
  ) {
    val startDataProvider = surveyController.startSurveySession(questions, profileId = profileId)
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
              SurveyActivity.createSurveyActivityIntent(activity, profileId, topicId, explorationId)
            fragment.startActivity(intent)
            activity.finish()
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.remove(fragment).commitNow()
          }
        }
      }
    )
  }

  private fun logSurveyPopUpShownEvent(
    explorationId: String,
    topicId: String,
    profileId: LegacyProfileId
  ) {
    analyticsController.logImportantEvent(
      oppiaLogger.createShowSurveyPopupContext(
        explorationId,
        topicId
      ),
      profileId = profileId
    )
  }
}
