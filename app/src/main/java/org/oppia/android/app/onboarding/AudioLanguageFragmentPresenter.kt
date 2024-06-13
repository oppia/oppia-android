package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import javax.inject.Inject
import org.oppia.android.app.home.HomeActivity

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding

  /**
   * Returns a newly inflated view to render the fragment with an evaluated [audioLanguage] as the
   * initial selected language, based on current locale.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
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

    binding.onboardingNavigationContinue.setOnClickListener {
      val intent =
        HomeActivity.createHomeActivity(fragment.requireContext(), internalProfileId)
      fragment.startActivity(intent)
      fragment.activity?.finish()
    }

    return binding.root
  }
}
