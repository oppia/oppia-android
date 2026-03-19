package org.oppia.android.app.profile

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import kotlinx.coroutines.delay
import org.oppia.android.app.databinding.databinding.CreateAdminPinFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.CreateAdminPinUiState
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.onboarding.PROFILE_CHOOSER_PARAMS_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The presenter for [CreateAdminPinFragment]. */
@FragmentScope
class CreateAdminPinFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var binding: CreateAdminPinFragmentBinding

  /** Creates and returns the view for the [CreateAdminPinFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = CreateAdminPinFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    createComposeView(savedInstanceState)
    return binding.root
  }

  private fun createComposeView(savedInstanceState: Bundle?) {
    val initialState = savedInstanceState
      ?.getByteArray(UI_STATE_SAVED_INSTANCE_STATE_KEY)
      ?.let { CreateAdminPinUiState.parseFrom(it) }
      ?: CreateAdminPinUiState.getDefaultInstance()

    binding.createAdminPinComposeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          CreateAdminPinScreen(initialState)
        }
      }
    }
  }

  // Saver that serializes CreateAdminPinUiState to/from its proto byte representation.
  private val createAdminPinUiStateSaver = Saver<CreateAdminPinUiState, ByteArray>(
    save = { it.toByteArray() },
    restore = { CreateAdminPinUiState.parseFrom(it) }
  )

  @OptIn(ExperimentalComposeUiApi::class)
  @Composable
  fun CreateAdminPinScreen(initialState: CreateAdminPinUiState) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val orientation = LocalConfiguration.current.orientation
    val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
    val stepCountIsVisible by remember(orientation) {
      derivedStateOf { isPortrait }
    }

    var uiState by rememberSaveable(stateSaver = createAdminPinUiStateSaver) {
      mutableStateOf(initialState)
    }

    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
      delay(100)
      keyboardController?.show()
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(1f))

      CreateAdminPinHeader()

      CreateAdminPinMessage()

      CreateAdminPinInputField(
        value = uiState.pin,
        onValueChange = { onPinChanged(uiState, it) { newState -> uiState = newState } },
        label = resourceHandler
          .getStringInLocaleWithWrapping(R.string.create_admin_pin_activity_enter_pin_label),
        error = computePinError(uiState.pin),
        focusManager = focusManager,
        imeAction = ImeAction.Next,
        focusRequester = focusRequester
      )

      CreateAdminPinInputField(
        value = uiState.confirmPin,
        onValueChange = {
          onConfirmPinChanged(uiState, it) { newState -> uiState = newState }
        },
        label = resourceHandler
          .getStringInLocaleWithWrapping(R.string.create_admin_pin_activity_confirm_pin_label),
        error = computeConfirmPinError(uiState.pin, uiState.confirmPin),
        focusManager = focusManager,
        imeAction = ImeAction.Done,
        onDone = { onSubmit(uiState) { newState -> uiState = newState } }
      )

      CreateAdminPinErrorText(
        showError = uiState.showError,
        errorMessage = uiState.errorMessage
      )

      Spacer(modifier = Modifier.weight(1f))

      if (stepCountIsVisible) {
        CreateAdminPinStepCountText()
        Spacer(modifier = Modifier.height(8.dp))
      }

      CreateAdminPinNavigationButtons(
        onBackClick = { activity.finish() },
        onContinueClick = { onSubmit(uiState) { newState -> uiState = newState } },
        isContinueEnabled = computePinError(uiState.pin).isEmpty() &&
          computeConfirmPinError(uiState.pin, uiState.confirmPin).isEmpty()
      )
    }
  }

  @Composable
  private fun CreateAdminPinHeader() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.create_admin_pin_activity_header
      ),
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      color = colorResource(R.color.component_color_shared_primary_text_color),
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }

  @Composable
  private fun CreateAdminPinMessage() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.create_admin_pin_activity_message
      ),
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      color = colorResource(R.color.component_color_shared_primary_text_color),
      modifier = Modifier.padding(bottom = 24.dp)
    )
  }

  @Composable
  private fun CreateAdminPinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    focusManager: FocusManager,
    imeAction: ImeAction,
    focusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = if (error.isEmpty()) 16.dp else 4.dp)
          .then(
            if (focusRequester != null) Modifier.focusRequester(focusRequester)
            else Modifier
          ),
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
        isError = error.isNotEmpty(),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
          unfocusedBorderColor = colorResource(R.color.component_color_edittext_stroke_color),
          focusedBorderColor = colorResource(R.color.component_color_shared_pin_focused_color),
          cursorColor = colorResource(R.color.component_color_shared_pin_cursor_color),
          backgroundColor =
            colorResource(R.color.component_color_shared_transparent_background_color),
          textColor = colorResource(R.color.component_color_shared_primary_text_color),
          unfocusedLabelColor = colorResource(R.color.component_color_shared_primary_text_color),
          focusedLabelColor = colorResource(R.color.component_color_shared_primary_text_color)
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
  private fun CreateAdminPinStepCountText() {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_step_count_five
      ),
      color = colorResource(R.color.component_color_onboarding_shared_green_text_color),
      fontSize = 16.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }

  @Composable
  private fun CreateAdminPinNavigationButtons(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    isContinueEnabled: Boolean
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      TextButton(onClick = onBackClick) {
        Text(
          text = resourceHandler.getStringInLocaleWithWrapping(R.string.onboarding_navigation_back),
          color = colorResource(R.color.component_color_onboarding_shared_green_text_color),
          fontWeight = FontWeight.Bold
        )
      }

      Button(
        onClick = onContinueClick,
        enabled = isContinueEnabled,
        modifier = Modifier
          .height(48.dp)
          .widthIn(min = 140.dp, max = 200.dp),
        colors = ButtonDefaults.buttonColors(
          backgroundColor = colorResource(R.color.component_color_onboarding_shared_green_color),
          disabledBackgroundColor =
            colorResource(R.color.component_color_shared_item_selection_interaction_disabled_color)
        )
      ) {
        Text(
          text = resourceHandler
            .getStringInLocaleWithWrapping(R.string.onboarding_navigation_continue),
          color = colorResource(R.color.component_color_onboarding_shared_white_color),
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  @Composable
  private fun CreateAdminPinErrorText(showError: Boolean, errorMessage: String) {
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

  private fun onPinChanged(
    uiState: CreateAdminPinUiState,
    newValue: String,
    updateState: (CreateAdminPinUiState) -> Unit
  ) {
    if (newValue.all { it.isDigit() } && newValue.length <= ADMIN_PIN_LENGTH) {
      updateState(
        uiState.toBuilder()
          .setPin(newValue)
          .setShowError(if (newValue.isNotEmpty()) false else uiState.showError)
          .build()
      )
    }
  }

  private fun onConfirmPinChanged(
    uiState: CreateAdminPinUiState,
    newValue: String,
    updateState: (CreateAdminPinUiState) -> Unit
  ) {
    if (newValue.all { it.isDigit() } && newValue.length <= ADMIN_PIN_LENGTH) {
      updateState(
        uiState.toBuilder()
          .setConfirmPin(newValue)
          .setShowError(if (newValue.isNotEmpty()) false else uiState.showError)
          .build()
      )
    }
  }

  private fun onSubmit(
    uiState: CreateAdminPinUiState,
    updateState: (CreateAdminPinUiState) -> Unit
  ) {
    val validationResult = validatePins(uiState.pin, uiState.confirmPin)
    if (validationResult.isValid) {
      updateState(uiState.toBuilder().setShowError(false).build())
      updatePin(uiState.pin)
    } else {
      updateState(
        uiState.toBuilder()
          .setShowError(true)
          .setErrorMessage(validationResult.errorMessage)
          .build()
      )
    }
  }

  private fun computePinError(pin: String): String {
    return when {
      pin.isNotEmpty() && pin.length < ADMIN_PIN_LENGTH ->
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.create_admin_pin_activity_length_error
        )

      else -> ""
    }
  }

  private fun computeConfirmPinError(pin: String, confirmPin: String): String {
    return when {
      confirmPin.isNotEmpty() && confirmPin != pin ->
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.create_admin_pin_activity_mismatch_error
        )

      else -> ""
    }
  }

  private fun validatePins(pin: String, confirmPin: String): PinValidationResult {
    val isValidPin = pin.length == ADMIN_PIN_LENGTH && pin.all(Char::isDigit)
    val isValidConfirmPin = confirmPin.length == ADMIN_PIN_LENGTH && confirmPin.all(Char::isDigit)

    return when {
      isValidPin && isValidConfirmPin && pin == confirmPin -> PinValidationResult(isValid = true)
      pin.isEmpty() -> PinValidationResult(
        isValid = false,
        errorMessage = resourceHandler.getStringInLocaleWithWrapping(
          R.string.create_admin_pin_activity_blank_error
        )
      )

      !isValidPin || !isValidConfirmPin -> PinValidationResult(
        isValid = false,
        errorMessage = resourceHandler.getStringInLocaleWithWrapping(
          R.string.create_admin_pin_activity_length_error
        )
      )

      else -> PinValidationResult(
        isValid = false,
        errorMessage = resourceHandler.getStringInLocaleWithWrapping(
          R.string.create_admin_pin_activity_mismatch_error
        )
      )
    }
  }

  private fun updatePin(pin: String) {
    val profileId = checkNotNull(fragment.arguments?.extractCurrentUserProfileId()) {
      "Expected profileId to be included in the arguments for CreateAdminPinFragment."
    }

    profileManagementController.updatePin(profileId, pin).toLiveData().observe(fragment) {
      if (it is AsyncResult.Success) {
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity).also { intent ->
          intent.decorateWithUserProfileId(profileId)
          intent.putProtoExtra(
            PROFILE_CHOOSER_PARAMS_KEY,
            ProfileChooserActivityParams.newBuilder()
              .setParentScreen(ProfileChooserActivityParams.ParentScreen.CREATE_ADMIN_PIN_SCREEN)
              .build()
          )
        }
        fragment.startActivity(intent)
        fragment.activity?.finishAffinity()
      }
    }
  }

  companion object {
    private const val ADMIN_PIN_LENGTH = 5
    private const val UI_STATE_SAVED_INSTANCE_STATE_KEY = "CreateAdminPinFragmentPresenter.ui_state"
  }

  /** Data class for PIN validation result. */
  private data class PinValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
  )
}
