package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.AdminIntroFragmentBinding
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

/** The presenter for [AdminIntroFragment]. */
class AdminIntroFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler
) {

  private lateinit var binding: AdminIntroFragmentBinding

  /** Creates and returns the view for the [AdminIntroFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View? {
    binding = AdminIntroFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    createComposeView()
    return binding.root
  }

  private fun createComposeView() {
    binding.adminIntroComposeView.setViewCompositionStrategy(
      ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
    )
    binding.adminIntroComposeView.setContent {
      MaterialTheme {
        // TODO: Add composable
      }
    }
  }
}
