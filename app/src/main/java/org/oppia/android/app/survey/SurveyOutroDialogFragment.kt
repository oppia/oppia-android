package org.oppia.android.app.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Fragment that displays a fullscreen dialog for survey on-boarding. */
class SurveyOutroDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var surveyOutroDialogFragmentPresenter: SurveyOutroDialogFragmentPresenter

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display the survey thank you message.
     *
     * @return [SurveyOutroDialogFragment]: DialogFragment
     */
    fun newInstance(): SurveyOutroDialogFragment {
      return SurveyOutroDialogFragment()
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
    return surveyOutroDialogFragmentPresenter.handleCreateView(
      inflater,
      container
    )
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.SurveyOnboardingDialogStyle)
  }
}
