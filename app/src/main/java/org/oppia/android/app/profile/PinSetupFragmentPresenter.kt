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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.PinSetupFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.onboarding.PROFILE_CHOOSER_PARAMS_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.putProtoExtra
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
    val focusManager = LocalFocusManager.current
    var uiState by remember { mutableStateOf(PinSetupUiState()) }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(1f))

      PinSetupHeader()

      PinSetupMessage()

      PinSetupInstructionText()

      PinInputField(
        value = uiState.pin,
        onValueChange = { newValue ->
          // Filter to only allow digits and limit to ADMIN_PIN_LENGTH
          if (newValue.all { it.isDigit() } && newValue.length <= ADMIN_PIN_LENGTH) {
            uiState = uiState.copy(
              pin = newValue,
              pinError = validatePinInput(newValue),
              // Clear general error when user starts typing
              showError = if (newValue.isNotEmpty()) false else uiState.showError
            )
          }
        },
        label = resourceHandler.getStringInLocaleWithWrapping(R.string.pin_setup_activity_enter_pin_label),
        error = uiState.pinError,
        isError = uiState.pinError.isNotEmpty(),
        focusManager = focusManager,
        imeAction = ImeAction.Next
      )

      PinInputField(
        value = uiState.confirmPin,
        onValueChange = { newValue ->
          // Filter to only allow digits and limit to ADMIN_PIN_LENGTH
          if (newValue.all { it.isDigit() } && newValue.length <= ADMIN_PIN_LENGTH) {
            uiState = uiState.copy(
              confirmPin = newValue,
              confirmPinError = if (newValue.isNotEmpty() && uiState.pin.isNotEmpty()) 
                validateConfirmPinInput(uiState.pin, newValue) else "",
              // Clear general error when user starts typing
              showError = if (newValue.isNotEmpty()) false else uiState.showError
            )
          }
        },
        label = resourceHandler.getStringInLocaleWithWrapping(R.string.pin_setup_activity_confirm_pin_label),
        error = uiState.confirmPinError,
        isError = uiState.confirmPinError.isNotEmpty(),
        focusManager = focusManager,
        imeAction = ImeAction.Done,
        onDone = {
          val validationResult = validatePins(uiState.pin, uiState.confirmPin)
          if (validationResult.isValid) {
            updatePin(profileId, uiState.pin)
          } else {
            uiState = uiState.copy(
              showError = true,
              errorMessage = validationResult.errorMessage
            )
          }
        }
      )

      PinErrorText(
        showError = uiState.showError,
        errorMessage = uiState.errorMessage
      )

      Spacer(modifier = Modifier.weight(1f))

      StepCounter()

      NavigationButtons(
        onBackClick = { activity.finish() },
        onContinueClick = {
          val validationResult = validatePins(uiState.pin, uiState.confirmPin)
          if (validationResult.isValid) {
            uiState = uiState.copy(showError = false)
            updatePin(profileId, uiState.pin)
          } else {
            uiState = uiState.copy(
              showError = true,
              errorMessage = validationResult.errorMessage
            )
          }
        }
      )
    }
  }

  @Composable
  private fun PinSetupHeader() {
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
  }

  @Composable
  private fun PinSetupMessage() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.pin_setup_activity_message
      ),
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      color = colorResource(R.color.component_color_shared_primary_text_color),
      modifier = Modifier.padding(bottom = 24.dp)
    )
  }

  @Composable
  private fun PinSetupInstructionText() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.pin_setup_activity_enter_pin_label
      ),
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      color = colorResource(R.color.component_color_shared_primary_text_color),
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }

  @Composable
  private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    isError: Boolean,
    focusManager: FocusManager,
    imeAction: ImeAction,
    onDone: (() -> Unit)? = null
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = if (error.isEmpty()) 16.dp else 4.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
          keyboardType = KeyboardType.Number,
          imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
          onNext = if (imeAction == ImeAction.Next) {
            { focusManager.moveFocus(FocusDirection.Down) }
          } else null,
          onDone = if (imeAction == ImeAction.Done) {
            {
              focusManager.clearFocus()
              onDone?.invoke()
            }
          } else null
        ),
        visualTransformation = PasswordVisualTransformation(),
        isError = isError,
        textStyle = LocalTextStyle.current.copy(
          fontSize = 16.sp
        ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
          textColor = colorResource(R.color.component_color_shared_primary_text_color),
          backgroundColor = colorResource(R.color.component_color_shared_activity_background_color),
          focusedBorderColor = colorResource(R.color.component_color_shared_dark_text_color),
          errorBorderColor = colorResource(R.color.component_color_shared_error_color)
        )
      )
      if (error.isNotEmpty()) {
        Text(
          text = error,
          color = colorResource(R.color.component_color_shared_error_color),
          fontSize = 12.sp,
          modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )
      }
    }
  }

  @Composable
  private fun StepCounter() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(R.string.onboarding_step_count_five),
      fontSize = 14.sp,
      color = colorResource(R.color.component_color_onboarding_shared_green_color),
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }

  @Composable
  private fun NavigationButtons(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      TextButton(
        onClick = onBackClick
      ) {
        Text(
          text = resourceHandler.getStringInLocaleWithWrapping(R.string.onboarding_navigation_back),
          color = colorResource(R.color.component_color_onboarding_shared_green_color),
          fontWeight = FontWeight.Bold
        )
      }

      Button(
        onClick = onContinueClick,
        modifier = Modifier.height(48.dp)
          .width(160.dp)
          .padding(top = 12.dp),
        colors = ButtonDefaults.buttonColors(
          backgroundColor = colorResource(R.color.component_color_onboarding_shared_green_color)
        )
      ) {
        Text(
          text = resourceHandler.getStringInLocaleWithWrapping(R.string.onboarding_navigation_continue),
          color = colorResource(R.color.component_color_onboarding_shared_white_color),
          fontWeight = FontWeight.Bold
        )
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

  private fun validatePinInput(pin: String): String {
    return when {
      pin.isNotEmpty() && pin.length < ADMIN_PIN_LENGTH -> {
        // Use existing length error string for real-time feedback
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_length_error
        )
      }
      else -> ""
    }
  }

  private fun validateConfirmPinInput(pin: String, confirmPin: String): String {
    return when {
      confirmPin.isNotEmpty() && confirmPin != pin -> {
        // Use existing mismatch error string for real-time feedback  
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.pin_setup_activity_mismatch_error
        )
      }
      else -> ""
    }
  }

  private fun validatePins(pin: String, confirmPin: String): PinValidationResult {
    val isValidPin = pin.length == ADMIN_PIN_LENGTH && pin.all(Char::isDigit)
    val isValidConfirmPin = confirmPin.length == ADMIN_PIN_LENGTH && confirmPin.all(Char::isDigit)

    return when {
      !isValidPin || !isValidConfirmPin -> {
        PinValidationResult(
          isValid = false,
          errorMessage = resourceHandler.getStringInLocaleWithWrapping(
            R.string.pin_setup_activity_length_error
          )
        )
      }
      pin != confirmPin -> {
        PinValidationResult(
          isValid = false,
          errorMessage = resourceHandler.getStringInLocaleWithWrapping(
            R.string.pin_setup_activity_mismatch_error
          )
        )
      }
      else -> {
        PinValidationResult(isValid = true)
      }
    }
  }

  private fun updatePin(profileId: ProfileId, pin: String) {
    profileManagementController.updatePin(profileId, pin).toLiveData().observe(fragment) {
      if (it is AsyncResult.Success) {
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity).also { intent ->
          intent.decorateWithUserProfileId(profileId)
          intent.putProtoExtra(
            PROFILE_CHOOSER_PARAMS_KEY,
            ProfileChooserActivityParams.newBuilder()
              .setParentScreen(ProfileChooserActivityParams.ParentScreen.CREATE_PIN_SCREEN)
              .build()
          )
        }
        fragment.startActivity(intent)
        // We don't want the user to be able to revisit the onboarding screens after this last step.
        fragment.activity?.finishAffinity()
      }
    }
  }

  companion object {
    /** The required length for admin PINs. */
    private const val ADMIN_PIN_LENGTH = 5
  }
}

/** Data class to encapsulate the PIN setup UI state. */
data class PinSetupUiState(
  val pin: String = "",
  val confirmPin: String = "",
  val showError: Boolean = false,
  val errorMessage: String = "",
  val pinError: String = "",
  val confirmPinError: String = ""
)

/** Data class for PIN validation result. */
data class PinValidationResult(
  val isValid: Boolean,
  val errorMessage: String = ""
)
