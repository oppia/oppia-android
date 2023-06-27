package org.oppia.android.app.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.SurveyOutroDialogFragmentBinding
import javax.inject.Inject

const val TAG_SURVEY_OUTRO_DIALOG = "SURVEY_OUTRO_DIALOG"

/** Presenter for [SurveyWelcomeDialogFragment], sets up bindings. */
@FragmentScope
class SurveyOutroDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) {
  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
  ): View {
    val binding =
      SurveyOutroDialogFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.lifecycleOwner = fragment

    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    binding.surveyOnboardingText.text = resourceHandler.getStringInLocaleWithWrapping(
      R.string.survey_thank_you_message_text, appName
    )

    binding.finishSurveyButton.setOnClickListener {
      activity.supportFragmentManager.beginTransaction().remove(fragment).commitNow()
      activity.finish()
    }

    return binding.root
  }
}
