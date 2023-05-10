package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.SurveyExitConfirmationDialogBinding
import javax.inject.Inject

const val TAG_EXIT_SURVEY_CONFIRMATION_DIALOG = "EXIT_SURVEY_CONFIRMATION_DIALOG"

/** Presenter for [ExitSurveyConfirmationDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class ExitSurveyConfirmationDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
) {
  private lateinit var profileId: ProfileId

  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View {
    this.profileId = profileId

    val binding =
      SurveyExitConfirmationDialogBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    binding.continueSurveyButton.setOnClickListener {
      // TODO: dismiss dialog
    }

    binding.exitSurveyButton.setOnClickListener {
      // TODO: submit survey if applicable and return to topic
    }

    return binding.root
  }
}
