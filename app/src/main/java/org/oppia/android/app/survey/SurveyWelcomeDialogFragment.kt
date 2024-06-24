package org.oppia.android.app.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveyWelcomeDialogFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that displays a fullscreen dialog for survey on-boarding. */
class SurveyWelcomeDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var surveyWelcomeDialogFragmentPresenter: SurveyWelcomeDialogFragmentPresenter

  companion object {
    /** Arguments key for SurveyWelcomeDialogFragment. */
    const val SURVEY_WELCOME_DIALOG_FRAGMENT_ARGUMENTS_KEY = "SurveyWelcomeDialogFragment.arguments"

    /**
     * Creates a new instance of a DialogFragment to display the survey on-boarding message.
     *
     * @param profileId the ID of the profile viewing the survey prompt
     * @return [SurveyWelcomeDialogFragment]: DialogFragment
     */
    fun newInstance(
      profileId: ProfileId,
      topicId: String,
      explorationId: String,
      mandatoryQuestionNames: List<SurveyQuestionName>
    ): SurveyWelcomeDialogFragment {
      val args = SurveyWelcomeDialogFragmentArguments.newBuilder().apply {
        this.topicId = topicId
        this.explorationId = explorationId
        this.addAllQuestions(
          extractQuestions(mandatoryQuestionNames)
        )
      }.build()
      return SurveyWelcomeDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(SURVEY_WELCOME_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }

    private fun extractQuestions(questionNames: List<SurveyQuestionName>): List<Int> {
      return questionNames.map { questionName -> questionName.number }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.SurveyOnboardingDialogStyle)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val arguments =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to SurveyWelcomeDialogFragment" }

    val args = arguments.getProto(
      SURVEY_WELCOME_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      SurveyWelcomeDialogFragmentArguments.getDefaultInstance()
    )

    val profileId = arguments.extractCurrentUserProfileId()
    val topicId = args.topicId!!
    val explorationId = args.explorationId!!
    val surveyQuestions = getQuestions(args.questionsList)

    return surveyWelcomeDialogFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId,
      explorationId,
      surveyQuestions
    )
  }

  private fun getQuestions(questionNumberList: List<Int>): List<SurveyQuestionName> {
    return questionNumberList.map { number -> SurveyQuestionName.forNumber(number) }
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.SurveyOnboardingDialogStyle)
  }
}
