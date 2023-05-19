package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.databinding.SurveyWelcomeDialogFragmentBinding
import javax.inject.Inject

const val TAG_SURVEY_WELCOME_DIALOG = "SURVEY_WELCOME_DIALOG"

/** Presenter for [SurveyWelcomeDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class SurveyWelcomeDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    topicId: String
  ): View {
    val binding =
      SurveyWelcomeDialogFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    binding.beginSurveyButton.setOnClickListener {
      val intent =
        SurveyActivity.createSurveyActivityIntent(fragment.activity!!, profileId, topicId)
      fragment.startActivity(intent)
    }

    binding.maybeLaterButton.setOnClickListener {
      val intent =
        TopicActivity.createTopicActivityIntent(fragment.activity!!, profileId.internalId, topicId)
      fragment.startActivity(intent)
    }

    return binding.root
  }
}
