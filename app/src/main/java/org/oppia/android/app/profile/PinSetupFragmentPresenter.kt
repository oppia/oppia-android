package org.oppia.android.app.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.PinSetupFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** The presenter for [PinSetupFragment]. */
@FragmentScope
class PinSetupFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var binding: PinSetupFragmentBinding

  /** Creates and returns the view for the [PinSetupFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View? {
    binding = PinSetupFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    createComposeView(profileId)
    return binding.root
  }

  private fun createComposeView(profileId: ProfileId) {
    binding.pinSetupComposeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          PinSetupScreen(profileId)
        }
      }
    }
  }

  @Composable
  fun PinSetupScreen(profileId: ProfileId) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val maxPinLength = 5

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_header
        ),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = colorResource(R.color.component_color_shared_primary_text_color),
        modifier = Modifier.padding(bottom = 16.dp)
      )

      Text(
        text = resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_message
        ),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = colorResource(R.color.component_color_shared_primary_text_color),
        modifier = Modifier.padding(bottom = 24.dp)
      )

      Text(
        text = resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_enter_pin_label
        ),
        fontSize = 14.sp,
        color = colorResource(R.color.component_color_shared_primary_text_color),
        modifier = Modifier
          .align(Alignment.Start)
          .padding(bottom = 8.dp)
      )

      OutlinedTextField(
        value = pin,
        onValueChange = { if (it.length <= maxPinLength) pin = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true
      )

      Text(
        text = resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_confirm_pin_label
        ),
        fontSize = 14.sp,
        color = colorResource(R.color.component_color_shared_primary_text_color),
        modifier = Modifier
          .align(Alignment.Start)
          .padding(bottom = 8.dp)
      )

      OutlinedTextField(
        value = confirmPin,
        onValueChange = { if (it.length <= maxPinLength) confirmPin = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      PinErrorText(
        showError = showError,
        errorMessage = errorMessage
      )

      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = resourceHandler.getStringInLocaleWithWrapping(R.string.onboarding_step_count_five),
        fontSize = 14.sp,
        color = colorResource(R.color.component_color_onboarding_shared_green_color),
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
            color = colorResource(R.color.component_color_onboarding_shared_green_color),
            fontWeight = FontWeight.Bold
          )
        }

        Button(
          onClick = {
            val (hasError, validationMessage) = showPinValidationError(pin, confirmPin)
            showError = hasError
            errorMessage = validationMessage ?: ""

            if (!showError) {
              updatePin(profileId, pin)
            }
          },
          modifier = Modifier.height(48.dp)
            .width(160.dp)
            .padding(top = 12.dp),
          colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.component_color_onboarding_shared_green_color)
          )
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

  @Composable
  private fun PinErrorText(showError: Boolean, errorMessage: String) {
    if (showError) {
      Text(
        text = errorMessage,
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontSize = 14.sp,
          color = colorResource(id = R.color.component_color_shared_error_color)
        )
      )
    }
  }

  private fun showPinValidationError(pin: String, confirmPin: String): Pair<Boolean, String?> {
    val isValidPin = pin.length == 5 && pin.all(Char::isDigit)
    val isValidConfirmPin = confirmPin.length == 5 && confirmPin.all(Char::isDigit)

    return when {
      !isValidPin || !isValidConfirmPin -> true to resourceHandler.getStringInLocaleWithWrapping(
        R.string.pin_setup_activity_length_error
      )

      pin != confirmPin -> true to resourceHandler.getStringInLocaleWithWrapping(
        R.string.pin_setup_activity_mismatch_error
      )

      else -> false to null
    }
  }

  private fun updatePin(profileId: ProfileId, pin: String) {
    profileManagementController.updatePin(profileId, pin).toLiveData().observe(fragment) {
      if (it is AsyncResult.Success) {
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
        intent.decorateWithUserProfileId(profileId)
        fragment.startActivity(intent)
        // We don't want the user to be able to go back to the onboarding screens.
        fragment.activity?.finishAffinity()
      }
    }
  }
}
