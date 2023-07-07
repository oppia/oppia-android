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
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that displays a fullscreen dialog for survey on-boarding. */
class SurveyWelcomeDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var surveyWelcomeDialogFragmentPresenter: SurveyWelcomeDialogFragmentPresenter

  companion object {
    internal const val PROFILE_ID_KEY = "SurveyWelcomeDialogFragment.profile_id"
    internal const val TOPIC_ID_KEY = "SurveyWelcomeDialogFragment.topic_id"
    internal const val MANDATORY_QUESTION_NAMES_KEY = "SurveyWelcomeDialogFragment.question_names"

    /**
     * Creates a new instance of a DialogFragment to display the survey on-boarding message.
     *
     * @param profileId the ID of the profile viewing the survey prompt
     * @return [SurveyWelcomeDialogFragment]: DialogFragment
     */
    fun newInstance(
      profileId: ProfileId,
      topicId: String,
      mandatoryQuestionNames: List<SurveyQuestionName>,
    ): SurveyWelcomeDialogFragment {
      return SurveyWelcomeDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_ID_KEY, profileId)
          putString(TOPIC_ID_KEY, topicId)
          putQuestions(MANDATORY_QUESTION_NAMES_KEY, extractQuestions(mandatoryQuestionNames))
        }
      }
    }
  }

  private fun Bundle.putQuestions(name: String, nameList: IntArray) {
    putSerializable(name, nameList)
  }

  private fun extractQuestions(questionNames: List<SurveyQuestionName>): IntArray {
    return questionNames.map { questionName -> questionName.number }.toIntArray()
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
    val args =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to SurveyWelcomeDialogFragment" }

    val profileId = args.getProto(PROFILE_ID_KEY, ProfileId.getDefaultInstance())
    val topicId = args.getStringFromBundle(TOPIC_ID_KEY)!!
    val surveyQuestions = args.getQuestions()

    return surveyWelcomeDialogFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId,
      surveyQuestions
    )
  }

  private fun Bundle.getQuestions(): List<SurveyQuestionName> {
    val questionArgs = getIntArray(MANDATORY_QUESTION_NAMES_KEY)
    return questionArgs?.map { number -> SurveyQuestionName.forNumber(number) }
      ?: listOf()
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.SurveyOnboardingDialogStyle)
  }
}
