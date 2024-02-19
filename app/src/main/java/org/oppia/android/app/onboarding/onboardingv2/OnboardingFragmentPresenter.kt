package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.OnboardingAppLanguageSelectionFragmentBinding
import javax.inject.Inject

/** The presenter for [OnboardingFragment] V2. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: OnboardingAppLanguageSelectionFragmentBinding

  /** Handle creation and binding of the [OnboardingFragment] V2 layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingAppLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.let {
      it.lifecycleOwner = fragment
    }

    binding.onboardingLanguageTitle.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.onboarding_language_activity_title,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingLanguageLetsGoButton.setOnClickListener {
      val intent = OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(activity)
      fragment.startActivity(intent)
    }

    return binding.root
  }
}
