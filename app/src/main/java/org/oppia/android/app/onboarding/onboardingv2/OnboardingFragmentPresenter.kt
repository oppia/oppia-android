package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.onboarding.OnboardingViewModel
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.OnboardingAppLanguageSelectionFragmentBinding

/** The presenter for [OnboardingFragment] V2. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>
) {
  private lateinit var binding: OnboardingAppLanguageSelectionFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingAppLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }

    binding.onboardingLanguageDropdown.adapter = ArrayAdapter(
      fragment.requireContext(),
      R.layout.onboarding_language_dropdown_item,
      R.id.onboarding_language_text_view,
      arrayOf("English")
    )

    binding.onboardingLanguageLetsGoButton.setOnClickListener {
      val intent = OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(activity)
      fragment.startActivity(intent)
    }

    return binding.root
  }

  private fun getOnboardingViewModel(): OnboardingViewModel {
    return viewModelProvider.getForFragment(fragment, OnboardingViewModel::class.java)
  }
}
