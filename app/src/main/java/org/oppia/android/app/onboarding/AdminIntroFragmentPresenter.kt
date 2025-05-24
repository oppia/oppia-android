package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.AdminIntroFragmentBinding
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [ProfileChooserActivity] intent parameters. */
const val PROFILE_CHOOSER_PARAMS_KEY = "ProfileChooserActivity.params"

/** The presenter for [AdminIntroFragment]. */
class AdminIntroFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController
) {

  private lateinit var binding: AdminIntroFragmentBinding

  /** Creates and returns the view for the [AdminIntroFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    profileType: ProfileType
  ): View? {
    binding = AdminIntroFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    createComposeView(profileId, profileType)

    profileManagementController.markProfileOnboardingStarted(profileId)

    return binding.root
  }

  private fun createComposeView(profileId: ProfileId, profileType: ProfileType) {
    binding.adminIntroComposeView.setViewCompositionStrategy(
      ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
    )
    binding.adminIntroComposeView.setContent {
      MaterialTheme {
        AdminIntroScreen(profileId, profileType)
      }
    }
  }

  @Composable
  private fun AdminIntroScreen(profileId: ProfileId, profileType: ProfileType) {
    val backgroundColor = colorResource(R.color.component_color_onboarding_intro_background_color)
    val tealColor = colorResource(R.color.component_color_onboarding_shared_green_color)

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
    ) {
      WavyBackground(backgroundColorResId = R.color.component_color_onboarding_shared_white_color)

      Box(
        modifier = Modifier
          .wrapContentWidth()
          .padding(horizontal = dimensionResource(R.dimen.onboarding_admin_intro_card_padding))
          .align(Alignment.Center)
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
          shape = RoundedCornerShape(4.dp),
          backgroundColor = tealColor,
          elevation = 8.dp
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = resourceHandler.getStringInLocaleWithWrapping(
                R.string.admin_intro_activity_header
              ),
              color = colorResource(R.color.component_color_onboarding_shared_white_color),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
            )

            Row(
              verticalAlignment = Alignment.Top,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              Text(
                text = "✓",
                color = colorResource(R.color.component_color_onboarding_shared_white_color),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
              )
              Text(
                text = resourceHandler.getStringInLocaleWithWrapping(
                  R.string.admin_intro_activity_settings_text
                ),
                color = colorResource(R.color.component_color_onboarding_shared_white_color),
                fontSize = 16.sp
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              verticalAlignment = Alignment.Top,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              Text(
                text = "✓",
                color = colorResource(R.color.component_color_onboarding_shared_white_color),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
              )
              Text(
                text = resourceHandler.getStringInLocaleWithWrapping(
                  R.string.admin_intro_activity_learners_text
                ),
                color = colorResource(R.color.component_color_onboarding_shared_white_color),
                fontSize = 16.sp
              )
            }
          }
        }

        Image(
          painter = painterResource(id = R.drawable.otter),
          contentDescription = resourceHandler.getStringInLocaleWithWrapping(
            R.string.onboarding_otter_content_description
          ),
          modifier = Modifier
            .size(120.dp)
            .align(Alignment.TopCenter)
            .offset(y = (-32).dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = stringResource(R.string.onboarding_step_count_three),
          color = tealColor,
          fontSize = 16.sp,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TextButton(
            onClick = { activity.finish() }
          ) {
            Text(
              text = stringResource(R.string.onboarding_navigation_back),
              color = tealColor,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = {
              navigateToProfileChooserActivity(profileId, profileType)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = tealColor),
            modifier = Modifier
              .height(48.dp)
              .width(160.dp)
              .padding(top = 12.dp)
          ) {
            Text(
              text = stringResource(R.string.onboarding_navigation_continue),
              color = colorResource(R.color.component_color_onboarding_shared_white_color),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }

  // TODO(#4938): Refactor to create profile nickname screen, the next onboarding step.
  private fun navigateToProfileChooserActivity(profileId: ProfileId, profileType: ProfileType) {
    val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
    intent.apply {
      decorateWithUserProfileId(profileId)
      putProtoExtra(
        PROFILE_CHOOSER_PARAMS_KEY,
        ProfileChooserActivityParams.newBuilder()
          .setProfileType(profileType)
          .build()
      )
    }
    fragment.startActivity(intent)
    // Finish this activity as well as all activities immediately below it in the current
    // task so that the user cannot navigate back to the onboarding flow by pressing the
    // back button once onboarding is complete.
    fragment.activity?.finishAffinity()
  }
}
