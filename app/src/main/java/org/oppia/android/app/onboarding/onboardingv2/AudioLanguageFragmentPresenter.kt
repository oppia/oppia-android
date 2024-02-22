package org.oppia.android.app.onboarding.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment] V2. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding
  private val orientation = Resources.getSystem().configuration.orientation

  /**
   * Returns a newly inflated view to render the fragment with the specified [audioLanguage] as the
   * initial selected language.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    audioLanguage: AudioLanguage
  ): View {

    activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
    }

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingNavigationContinue.setOnClickListener {
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingStepsCount?.visibility =
      if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE

    return binding.root
  }
}
