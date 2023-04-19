package org.oppia.android.app.survey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.SurveyFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

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
  private lateinit var surveyToolbar: Toolbar
  private lateinit var surveyProgressText: TextView
  private lateinit var surveyProgressBar: ProgressBar
  private lateinit var questionTextView: TextView
  private lateinit var questionRecycleView: RecyclerView

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
    binding.lifecycleOwner = fragment
    questionRecycleView = binding.root.findViewById(R.id.survey_question_recycler_view)
    questionTextView = binding.root.findViewById(R.id.survey_question_text)
    surveyToolbar = binding.surveyToolbar
    surveyProgressText = binding.surveyProgressText
    surveyProgressBar = binding.surveyProgressBar
    activity.setSupportActionBar(surveyToolbar)

    binding.surveyToolbar.setNavigationOnClickListener {
      // Implementation of this will show a confirm exit dialog.
      activity.onBackPressed()
    }
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
