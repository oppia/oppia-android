package org.oppia.android.app.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ProfileLoginFragment]. */
class ProfileLoginFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: ProfileLoginFragmentBinding
  private lateinit var profileLiveData: LiveData<Profile>
  private lateinit var adminProfileLiveData: LiveData<Profile>

  /** Creates and returns the view for the [ProfileLoginFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View? {
    binding = ProfileLoginFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    profileLiveData =
      getProfileResult(profileManagementController.getProfile(profileId).toLiveData())

    getAdminPin()

    createComposeView()

    return binding.root
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

  @Composable
  fun PinEntryScreen() {
    var showError by remember { mutableStateOf(false) }
    val profile: Profile by profileLiveData.observeAsState(initial = Profile.getDefaultInstance())
    val profileName = profile.name
    val profileId = profile.id
    val profileType = profile.profileType
    var pinValue by remember { mutableStateOf("") }
    var shakeOffset by remember { mutableStateOf(0f) }
    val pinLength = if (profileType == ProfileType.SUPERVISOR) 5 else 3

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
              if (newValue == profile.pin) {
                profileManagementController.loginToProfile(profileId).toLiveData()
                  .observe(fragment) {
                    if (it is AsyncResult.Success) {
                      activity.startActivity(
                        if (enableMultipleClassrooms.value)
                          ClassroomListActivity.createClassroomListActivity(activity, profileId)
                        else
                          HomeActivity.createHomeActivity(activity, profileId)
                      )
                      activity.finish()
                    }
                  }
              } else {
                showError = true
              }
            }
          },
          pinLength = pinLength,
          modifier = Modifier.offset(x = shakeOffset.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PinErrorText(
          showError = showError,
          errorMessage = resourceHandler.getStringInLocale(
            R.string.profile_login_activity_pin_error
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        ForgotPinButton(profileType, profileId, profileName)

        LaunchedEffect(showError) {
          if (showError) {
            // Shake animation using offset
            repeat(3) {
              shakeOffset = -10f
              delay(50)
              shakeOffset = 10f
              delay(50)
            }
            shakeOffset = 0f

            // Clear PIN and hide the error message after delay.
            delay(1000)
            pinValue = ""
            showError = false
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
      value = pinValue,
      onValueChange = { newValue ->
        if (newValue.length <= pinLength && newValue.all { it.isDigit() }) {
          onPinChanged(newValue)
        }
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
      decorationBox = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
          repeat(pinLength) { index ->
            val char = pinValue.getOrNull(index)?.toString() ?: ""
            val isFocused = pinValue.length == index
            Box(
              modifier = Modifier
                .size(48.dp)
                .aspectRatio(0.7F)
                .border(
                  width = if (isFocused) 2.dp else 1.dp,
                  color = if (isFocused)
                    colorResource(id = R.color.component_color_profile_login_shared_primary_color)
                  else
                    colorResource(
                      id = R.color.component_color_profile_login_unfocused_outline_color
                    ),
                  shape = RoundedCornerShape(4.dp)
                )
                .background(
                  colorResource(id = R.color.component_color_profile_login_shared_white_color)
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(text = char, style = MaterialTheme.typography.h6)
            }
          }
        }
      }
    )
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

  @Composable
  private fun ForgotPinButton(profileType: ProfileType, profileId: ProfileId, profileName: String) {
    val adminProfile:
      Profile by adminProfileLiveData.observeAsState(initial = Profile.getDefaultInstance())
    val adminPin = adminProfile.pin
    val openForgotPinDialog = remember { mutableStateOf(false) }

    TextButton(onClick = { openForgotPinDialog.value = true }) {
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
        ForgotAdminPinDialogFlow(openForgotPinDialog)
      } else {
        showResetNonAdminPinFlow(adminPin, openForgotPinDialog, profileId, profileName)
      }
    }
  }

  @Composable
  private fun ForgotAdminPinDialogFlow(openForgotPinDialog: MutableState<Boolean>) {
    val openConfirmationDialog = remember { mutableStateOf(false) }

    if (openForgotPinDialog.value) {
      ForgotAdminPinDialog(
        onDismissRequest = { openForgotPinDialog.value = false },
        onConfirmation = {
          openConfirmationDialog.value = true
        }
      )
    }

    if (openConfirmationDialog.value) {
      ConfirmDataResetDialog(
        onDismissRequest = { openConfirmationDialog.value = false }
      )
    }
  }

  @Composable
  private fun ForgotAdminPinDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
  ) {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    AlertDialog(
      title = {
        Text(
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_login_forgot_pin_dialog_title
          )
        )
      },
      text = {
        Text(
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.profile_login_forgot_pin_dialog_message, appName
          )
        )
      },
      properties = DialogProperties(
        dismissOnClickOutside = false,
        dismissOnBackPress = false
      ),
      onDismissRequest = {
        onDismissRequest()
      },
      dismissButton = {
        TextButton(
          onClick = {
            onDismissRequest()
          }
        ) {
          Text(
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.profile_login_forgot_pin_dialog_cancel_button
            )
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            onConfirmation()
          }
        ) {
          Text(
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.profile_login_forgot_pin_dialog_reset_button,
              appName
            )
          )
        }
      }
    )
  }

  @Composable
  private fun ConfirmDataResetDialog(
    onDismissRequest: () -> Unit
  ) {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    AlertDialog(
      title = {
        Text(
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.admin_confirm_app_wipe_title, appName
          )
        )
      },
      text = {
        Text(
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.admin_confirm_app_wipe_message, appName
          )
        )
      },
      properties = DialogProperties(
        dismissOnClickOutside = false,
        dismissOnBackPress = false
      ),
      onDismissRequest = {
        onDismissRequest()
      },
      dismissButton = {
        TextButton(
          onClick = {
            onDismissRequest()
          }
        ) {
          Text(
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.admin_confirm_app_wipe_negative_button_text
            )
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = { deleteAppData() }
        ) {
          Text(
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.admin_confirm_app_wipe_positive_button_text,
              appName
            )
          )
        }
      }
    )
  }

  private fun showResetNonAdminPinFlow(
    correctAdminPin: String,
    openForgotPinDialog: MutableState<Boolean>,
    profileId: ProfileId,
    profileName: String
  ) {
    openForgotPinDialog.value = false
    val dialogFragment = AdminSettingsDialogFragment
      .newInstance(correctAdminPin, profileId, profileName)
    dialogFragment.showNow(fragment.parentFragmentManager, TAG_VALIDATE_ADMIN_PIN_DIALOG)
  }

  private fun deleteAppData() {
    profileManagementController.deleteAllProfiles().toLiveData().observe(fragment) {
      activity.finishAffinity()
    }
  } // TODO something weird happens when the default profile is created after this == wrong type maybe?

  private fun getAdminPin() {
    val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()

    adminProfileLiveData =
      getProfileResult(profileManagementController.getProfile(adminProfileId).toLiveData())
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
