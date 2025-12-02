package org.oppia.android.app.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.delay
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.databinding.databinding.ProfileLoginFragmentBinding
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/**
 * Test tag for the pin input field's container.
 *
 * It is generally a bad practice to customize something that affects the real production behavior
 * of a component for test-specific reasons, but in this case, TEST_TAG has been chosen as a last
 * resort because this Composable does not contain any other useful modifiers that we can use to
 * identify it in a test run.
 */
const val PIN_INPUT_TEST_TAG = "TEST_TAG.input"

/**
 * Test tag for each of the PIN boxes.
 *
 * Similar to [PIN_INPUT_TEST_TAG], we are adding a TEST_TAG modifier to the PIN boxes which are
 * dynamically generated and do not contain text when created that can be used to match them in
 * tests.
 */
const val PIN_BOX_TEST_TAG = "TEST_TAG.pin_box_"

/** The presenter for [ProfileLoginFragment]. */
class ProfileLoginFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val appStartupStateController: AppStartupStateController,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: ProfileLoginFragmentBinding
  private lateinit var profileLiveData: LiveData<Profile>
  private lateinit var adminProfileLiveData: LiveData<Profile>
  private var loginFlow: ProfileLoginActivity.Companion.LoginFlow =
    ProfileLoginActivity.Companion.LoginFlow.OPEN_EXISTING_PROFILE

  /** Creates and returns the view for the [ProfileLoginFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View? {
    // Determine how this screen was opened to route appropriately on successful login.
    loginFlow = ProfileLoginActivity.extractLoginFlowFromIntent(activity.intent)
    binding = ProfileLoginFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    profileLiveData =
      getProfileResult(profileManagementController.getProfile(profileId).toLiveData())

    getAdminPin()

    createComposeView()

    return binding.root
  }

  private fun getAdminPin() {
    val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()

    adminProfileLiveData =
      getProfileResult(profileManagementController.getProfile(adminProfileId).toLiveData())
  }

  private fun createComposeView() {
    binding.profileLoginComposeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          PinEntryScreen()
        }
      }
    }
  }

  @OptIn(ExperimentalComposeUiApi::class)
  @Composable
  private fun PinEntryScreen() {
    var showError by remember { mutableStateOf(false) }
    val profile: Profile by profileLiveData.observeAsState(initial = Profile.getDefaultInstance())
    val profileName = profile.name
    val profileId = profile.id
    val profileType = profile.profileType
    var pinValue by remember { mutableStateOf("") }
    var shakeOffset by remember { mutableStateOf(0f) }
    val pinLength = if (profileType == ProfileType.SUPERVISOR) 5 else 3
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(colorResource(R.color.component_color_profile_login_background_color))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        WelcomeHeader(profileName)

        Spacer(modifier = Modifier.height(24.dp))

        PinInputField(
          pinValue = pinValue,
          onPinChanged = { newValue ->
            pinValue = newValue
            if (newValue.length == pinLength) {
              showError = maybeShowValidationError(newValue, profile.pin, profileId)
            }
          },
          pinLength = pinLength,
          modifier = Modifier
            .offset(x = shakeOffset.dp)
            .focusRequester(focusRequester)
            .focusable()
        )

        LaunchedEffect(Unit) {
          focusRequester.requestFocus()
          keyboardController?.show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        PinErrorText(
          showError = showError,
          errorMessage = resourceHandler.getStringInLocale(
            R.string.profile_login_activity_pin_error
          ),
          setShakeOffset = { shakeOffset = it },
          onErrorDismissed = { showError = false },
          resetPin = { pinValue = "" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ForgotPinButton(profileType, profileId, profileName)
      }
    }
  }

  private fun maybeShowValidationError(
    enteredPin: String,
    profilePin: String,
    profileId: ProfileId
  ): Boolean {
    val showError: Boolean
    if (enteredPin == profilePin) {
      loginToProfile(profileId)
      showError = false
    } else {
      showError = true
    }
    return showError
  }

  private fun loginToProfile(profileId: ProfileId) {
    profileManagementController.loginToProfile(profileId).toLiveData()
      .observe(fragment) {
        if (it is AsyncResult.Success) {
          when (loginFlow) {
            ProfileLoginActivity.Companion.LoginFlow.ADD_NEW_LEARNER -> {
              val profile = profileLiveData.value
              // For add-new flow, require a supervisor login to proceed to creating a profile.
              if (profile?.profileType == ProfileType.SUPERVISOR) {
                val intent = org.oppia.android.app.onboarding.CreateProfileActivity
                  .createProfileActivityIntent(
                    activity,
                    profile.id,
                    ProfileType.ADDITIONAL_LEARNER
                  )
                activity.startActivity(intent)
              } else {
                // Non-supervisors shouldn't be in this flow; default to home/classroom.
                activity.startActivity(
                  if (enableMultipleClassrooms.value) {
                    ClassroomListActivity.createClassroomListActivity(activity, profileId)
                  } else {
                    HomeActivity.createHomeActivity(activity, profileId)
                  }
                )
                activity.finish()
              }
            }
            ProfileLoginActivity.Companion.LoginFlow.OPEN_EXISTING_PROFILE -> {
              activity.startActivity(
                if (enableMultipleClassrooms.value) {
                  ClassroomListActivity.createClassroomListActivity(activity, profileId)
                } else {
                  HomeActivity.createHomeActivity(activity, profileId)
                }
              )
              activity.finish()
            }
          }
        }
      }
  }

  @Composable
  private fun WelcomeHeader(profileName: String) {
    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.profile_login_activity_greeting_text, profileName
      ),
      style = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(id = R.color.component_color_profile_login_primary_text_color)
      )
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = resourceHandler.getStringInLocaleWithWrapping(
        R.string.profile_login_activity_enter_pin_prompt
      ),
      textAlign = TextAlign.Center,
      style = TextStyle(
        fontSize = 16.sp,
        color = colorResource(id = R.color.component_color_profile_login_primary_text_color)
      )
    )
  }

  @Composable
  private fun PinInputField(
    pinValue: String,
    onPinChanged: (String) -> Unit,
    pinLength: Int,
    modifier: Modifier = Modifier
  ) {
    BasicTextField(
      modifier = modifier.testTag(PIN_INPUT_TEST_TAG),
      value = pinValue,
      onValueChange = { newValue ->
        if (newValue.length <= pinLength && newValue.all { it.isDigit() }) {
          onPinChanged(newValue)
        }
      },
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
      decorationBox = {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = modifier
        ) {
          repeat(pinLength) { index ->
            PinInputBox(index, pinValue)
          }
        }
      }
    )
  }

  @Composable
  private fun PinInputBox(index: Int, pinValue: String) {
    val transformedPin = PasswordVisualTransformation().filter(AnnotatedString(pinValue))
    val pinChar = pinValue.getOrNull(index)?.toString() ?: ""
    val maskedChar = transformedPin.text.getOrNull(index)?.toString() ?: ""
    val isFocused = pinValue.length == index

    Box(
      modifier = Modifier
        .size(48.dp)
        .aspectRatio(0.7F)
        .border(
          width = if (isFocused) 2.dp else 1.dp,
          color = if (isFocused) {
            colorResource(id = R.color.component_color_profile_login_shared_primary_color)
          } else {
            colorResource(
              id = R.color.component_color_profile_login_unfocused_outline_color
            )
          },
          shape = RoundedCornerShape(4.dp)
        )
        .background(
          colorResource(id = R.color.component_color_profile_login_shared_white_color)
        )
        .testTag(PIN_BOX_TEST_TAG + index)
        .semantics { contentDescription = pinChar },
      contentAlignment = Alignment.Center
    ) {
      Text(text = maskedChar, style = MaterialTheme.typography.h6)
    }
  }

  @Composable
  private fun PinErrorText(
    showError: Boolean,
    errorMessage: String,
    setShakeOffset: (Float) -> Unit,
    resetPin: () -> Unit,
    onErrorDismissed: () -> Unit
  ) {
    if (showError) {
      Text(
        text = errorMessage,
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontSize = 14.sp,
          color = colorResource(id = R.color.component_color_shared_error_color)
        )
      )

      LaunchedEffect(true) {
        // Shake animation using offset.
        repeat(3) {
          setShakeOffset(-10f)
          delay(50)
          setShakeOffset(10f)
          delay(50)
        }

        setShakeOffset(0f)

        // Clear the incorrect PIN and hide the error message after a 2s delay.
        delay(2000)
        resetPin()
        onErrorDismissed()
      }
    }
  }

  @Composable
  private fun ForgotPinButton(profileType: ProfileType, profileId: ProfileId, profileName: String) {
    val adminProfile:
      Profile by adminProfileLiveData.observeAsState(initial = Profile.getDefaultInstance())
    val adminPin = adminProfile.pin
    val openForgotPinDialog = remember { mutableStateOf(false) }
    val openConfirmationDialog = remember { mutableStateOf(false) }

    TextButton(
      onClick = { openForgotPinDialog.value = true }
    ) {
      Text(
        text = resourceHandler.getStringInLocaleWithWrapping
        (R.string.profile_login_activity_forgot_pin_text),
        style = TextStyle(
          fontSize = 16.sp,
          color = colorResource(id = R.color.component_color_profile_login_shared_primary_color)
        )
      )
    }

    if (openForgotPinDialog.value) {
      if (profileType == ProfileType.SUPERVISOR) {
        AdminPinRecoveryDialog(
          onDismissRequest = { openForgotPinDialog.value = false },
          onConfirmation = {
            openForgotPinDialog.value = false
            openConfirmationDialog.value = true
          }
        )
      } else {
        showResetNonAdminPinFlow(adminPin, openForgotPinDialog, profileId, profileName)
      }
    }

    if (openConfirmationDialog.value) {
      DataResetConfirmationDialog(
        onDismissRequest = { openConfirmationDialog.value = false },
        deleteAppData = { deleteAppData() }
      )
    }
  }

  private fun showResetNonAdminPinFlow(
    correctAdminPin: String,
    openForgotPinDialog: MutableState<Boolean>,
    profileId: ProfileId,
    profileName: String
  ) {
    openForgotPinDialog.value = false
    AdminSettingsDialogFragment
      .newInstance(correctAdminPin, profileId, profileName)
      .showNow(activity.supportFragmentManager, TAG_VALIDATE_ADMIN_PIN_DIALOG)
  }

  /**
   * Deletes all app data by removing all profiles, resetting onboarding state, and closing the app.
   *
   * Once completed, the user will be forced to go through onboarding again to re-create their
   * profile.
   */
  private fun deleteAppData() {
    profileManagementController.deleteAllProfiles().toLiveData().observe(fragment) {
      // Reset onboarding so the app starts fresh.
      appStartupStateController.resetOnboardingState()
      // Close the app to ensure a clean restart regardless of reset outcome.
      activity.finishAffinity()
    }
  }

  private fun getProfileResult(profileResult: LiveData<AsyncResult<Profile>>): LiveData<Profile> {
    return Transformations.map(profileResult, ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    val profile = when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("ProfileLoginActivity", "Failed to retrieve profile", profileResult.error)
        Profile.getDefaultInstance()
      }

      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
    return profile
  }
}
