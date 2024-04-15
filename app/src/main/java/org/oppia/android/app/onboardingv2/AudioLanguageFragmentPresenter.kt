package org.oppia.android.app.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding

/** The presenter for [AudioLanguageFragment] V2. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding

  /**
   * Returns a newly inflated view to render the fragment with the specified [audioLanguage] as the
   * initial selected language.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View {

    activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }
    return binding.root
  }

  private fun getAudioLanguageList(): List<String> {
    return AudioLanguage.values()
      .filter { it.isValid() }
      .map { audioLanguage ->
        appLanguageResourceHandler.computeLocalizedDisplayName(audioLanguage)
      }
  }

  private fun AudioLanguage.isValid(): Boolean {
    return when (this) {
      AudioLanguage.UNRECOGNIZED, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED,
      AudioLanguage.NO_AUDIO -> false
      else -> true
    }
  }
}
