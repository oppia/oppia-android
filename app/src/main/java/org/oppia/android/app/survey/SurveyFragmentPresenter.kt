package org.oppia.android.app.survey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.SurveyFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger

const val SURVEY_FRAGMENT_PROFILE_ID_ARGUMENT_KEY =
  "SurveyFragmentPresenter.survey_fragment_profile_id"
const val SURVEY_FRAGMENT_TOPIC_ID_ARGUMENT_KEY = "SurveyFragmentPresenter.survey_fragment_topic_id"

/** The presenter for [SurveyFragment]. */
class SurveyFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val context: Context,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var binding: SurveyFragmentBinding

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
    return binding.root
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