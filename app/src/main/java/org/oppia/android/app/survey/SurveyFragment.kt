package org.oppia.android.app.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

/** Fragment that represents the current state of a survey. */
class SurveyFragment : InjectableFragment() {

  companion object {
    /**
     * Creates a new instance of a SurveyFragment.
     * @param internalProfileId used by SurveyFragment to record the survey action taken by user.
     * @param topicId used by SurveyFragment for logging purposes.
     * @return a new instance of [SurveyFragment].
     */
    fun newInstance(
      internalProfileId: Int,
      topicId: String
    ): SurveyFragment {
      val surveyFragment = SurveyFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      surveyFragment.arguments = args
      return surveyFragment
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
    val internalProfileId = arguments!!.getInt(PROFILE_ID_ARGUMENT_KEY, -1)
    val topicId = arguments!!.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)!!

    return surveyFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }

  fun handleKeyboardAction() = surveyFragmentPresenter.handleKeyboardAction()

  // override fun onNextButtonClicked() = surveyFragmentPresenter.onNextButtonClicked()

  // override fun onPreviousButtonClicked() = surveyFragmentPresenter.onPreviousButtonClicked()
}
