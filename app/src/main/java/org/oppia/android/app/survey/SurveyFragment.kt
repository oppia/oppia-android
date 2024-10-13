package org.oppia.android.app.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyFragmentArguments
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that represents the current state of a survey. */
class SurveyFragment :
  InjectableFragment(),
  SelectedAnswerAvailabilityReceiver,
  SelectedAnswerHandler {

  companion object {
    /** Arguments key for SurveyFragment. */
    const val SURVEY_FRAGMENT_ARGUMENTS_KEY = "SurveyFragment.arguments"

    /**
     * Creates a new instance of a SurveyFragment.
     *
     * @param internalProfileId used by SurveyFragment to record the survey action taken by user
     * @param topicId used by SurveyFragment for logging purposes
     * @return a new instance of [SurveyFragment]
     */
    fun newInstance(
      internalProfileId: Int,
      topicId: String
    ): SurveyFragment {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val args = SurveyFragmentArguments.newBuilder().apply {
        this.topicId = topicId
      }.build()
      return SurveyFragment().apply {
        arguments = Bundle().apply {
          putProto(SURVEY_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  @Inject
  lateinit var surveyFragmentPresenter: SurveyFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val args = arguments!!.getProto(
      SURVEY_FRAGMENT_ARGUMENTS_KEY,
      SurveyFragmentArguments.getDefaultInstance()
    )

    val internalProfileId = arguments!!.extractCurrentUserProfileId().loggedInInternalProfileId
    val topicId = args.topicId!!
    val explorationId = args.explorationId!!

    return surveyFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      explorationId,
      topicId,
      this
    )
  }

  override fun onPendingAnswerAvailabilityCheck(inputAnswerAvailable: Boolean) {
    surveyFragmentPresenter.updateNextButton(inputAnswerAvailable)
  }

  override fun getMultipleChoiceAnswer(selectedAnswer: SurveySelectedAnswer) {
    surveyFragmentPresenter.getPendingAnswer(selectedAnswer)
  }

  override fun getFreeFormAnswer(answer: SurveySelectedAnswer) {
    surveyFragmentPresenter.submitFreeFormAnswer(answer)
  }
}
