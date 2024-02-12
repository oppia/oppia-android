package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.oppia.android.R
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import javax.inject.Inject

class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding

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

    binding.audioLanguageDropdown.adapter = ArrayAdapter(
      fragment.requireContext(),
      R.layout.onboarding_language_dropdown_item,
      R.id.onboarding_language_text_view,
      arrayOf("English")
    )

    binding.onboardingNavigationContinue.setOnClickListener {
      val intent = HomeActivity.createHomeActivity(activity, profileId = 1)
      fragment.startActivity(intent)
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    return binding.root
  }
}
