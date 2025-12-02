package org.oppia.android.app.onboarding

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import org.oppia.android.app.databinding.databinding.CreateProfileFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageViewTarget
import javax.inject.Inject

/** Presenter for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val imageLoader: ImageLoader,
  private val createProfileViewModel: CreateProfileViewModel,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView
  private lateinit var selectedImage: String
  private lateinit var profileId: ProfileId
  private lateinit var profileType: ProfileType
  private var selectedImageUri: Uri? = null

  /** Launcher for picking an image from device gallery. */
  lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

  /** Initialize layout bindings. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    profileType: ProfileType
  ): View {
    binding = CreateProfileFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.profileId = profileId
    this.profileType = profileType

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = createProfileViewModel
    }

    uploadImageView = binding.createProfileUserImageView

    // Only show the PIN creation UI when a supervisor is adding an additional learner.
    createProfileViewModel.showPinUi.set(profileType == ProfileType.ADDITIONAL_LEARNER)

    uploadImageView.apply {
      setColorFilter(
        ResourcesCompat.getColor(
          activity.resources,
          R.color.component_color_avatar_background_25_color,
          null
        ),
        PorterDuff.Mode.DST_OVER
      )

      imageLoader.loadDrawable(
        R.drawable.ic_profile_icon,
        ImageViewTarget(this)
      )
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      val nickname = binding.createProfileNicknameEdittext.text.toString().trim()

      if (checkNicknameAndUpdateError(nickname)) return@setOnClickListener

      when (profileType) {
        ProfileType.SUPERVISOR, ProfileType.SOLE_LEARNER -> {
          // First-profile flow: update the existing admin/sole learner profile details.
          updateProfileDetails(nickname, profileType)
        }
        ProfileType.ADDITIONAL_LEARNER -> {
          // Supervisor adding a new learner profile: create a new profile entry.
          val pin = if (createProfileViewModel.showPinFields.get() == true) {
            createProfileViewModel.inputPin.get().orEmpty()
          } else ""
          val confirm = if (createProfileViewModel.showPinFields.get() == true) {
            createProfileViewModel.inputConfirmPin.get().orEmpty()
          } else ""

          // Mirror AddProfileActivityPresenter validations.
          var failed = false
          if (pin.isNotEmpty() && pin.length < 3) {
            createProfileViewModel.pinErrorMsg.set(
              appLanguageResourceHandler.getStringInLocale(
                R.string.add_profile_error_pin_length
              )
            )
            failed = true
          }
          if (pin != confirm) {
            createProfileViewModel.confirmPinErrorMsg.set(
              appLanguageResourceHandler.getStringInLocale(
                R.string.add_profile_error_pin_confirm_wrong
              )
            )
            failed = true
          }
          createProfileViewModel.hasErrorMessage.set(failed)
          if (failed) return@setOnClickListener

          createLearnerProfile(profileName = nickname, pin = pin)
        }
        else -> {
          // Defensive fallback for unexpected/unspecified profile type.
          createProfileViewModel.hasErrorMessage.set(true)
          createProfileViewModel.errorMessage.set(
            appLanguageResourceHandler.getStringInLocale(
              R.string.add_profile_error_missing_profile_type
            )
          )
        }
      }
    }

    binding.createProfileNicknameEdittext.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun afterTextChanged(s: Editable?) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        createProfileViewModel.hasErrorMessage.set(false)
      }
    })

    addViewOnClickListeners(binding)

    return binding.root
  }

  private fun checkNicknameAndUpdateError(nickname: String): Boolean {
    val hasError = nickname.isBlank()
    createProfileViewModel.hasErrorMessage.set(hasError)
    createProfileViewModel.errorMessage.set(
      appLanguageResourceHandler.getStringInLocale(
        R.string.create_profile_activity_nickname_error
      )
    )
    return hasError
  }

  /** Receive the result of image upload and load it into the image view. */
  fun handleOnActivityResult(intent: Intent?) {
    intent?.let {
      binding.createProfilePicturePrompt.visibility = View.GONE
      selectedImageUri = intent.data
      selectedImage =
        checkNotNull(intent.data.toString()) { "Could not find the selected image." }
      imageLoader.loadBitmap(
        selectedImage,
        ImageViewTarget(uploadImageView)
      )
    }
  }

  private fun addViewOnClickListeners(binding: CreateProfileFragmentBinding) {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }
    binding.createProfileEditPictureIcon.setOnClickListener {
      activityResultLauncher.launch(galleryIntent)
    }
    binding.createProfilePicturePrompt.setOnClickListener {
      activityResultLauncher.launch(galleryIntent)
    }
    binding.createProfileUserImageView.setOnClickListener {
      activityResultLauncher.launch(galleryIntent)
    }
  }

  private fun updateProfileDetails(profileName: String, profileType: ProfileType) {
    profileManagementController.updateNewProfileDetails(
      profileId = profileId,
      profileType = profileType,
      avatarImagePath = selectedImageUri,
      colorRgb = selectUniqueRandomColor(),
      newName = profileName,
      isAdmin = true
    ).toLiveData().observe(
      fragment
    ) { result ->
      when (result) {
        is AsyncResult.Success -> {
          createProfileViewModel.hasErrorMessage.set(false)

          val params = IntroActivityParams.newBuilder()
            .setProfileNickname(profileName)
            .setParentScreen(IntroActivityParams.ParentScreen.CREATE_PROFILE_SCREEN)
            .build()

          val learnerIntroIntent =
            IntroActivity.createIntroActivity(activity, params, profileId)

          val adminIntroIntent = AdminIntroActivity.createAdminIntroActivityIntent(
            activity,
            profileId,
            profileType,
            profileName
          )

          val intent = if (profileType == ProfileType.SUPERVISOR) {
            adminIntroIntent
          } else {
            learnerIntroIntent
          }

          fragment.startActivity(intent)
        }

        is AsyncResult.Failure -> {
          createProfileViewModel.hasErrorMessage.set(true)

          val errorMessage = when (result.error) {
            is ProfileManagementController.ProfileNameOnlyLettersException ->
              appLanguageResourceHandler.getStringInLocale(
                R.string.add_profile_error_name_only_letters
              )

            is ProfileManagementController.UnknownProfileTypeException ->
              appLanguageResourceHandler.getStringInLocale(
                R.string.add_profile_error_missing_profile_type
              )

            else -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.add_profile_default_error_message
              )
            }
          }

          createProfileViewModel.errorMessage.set(errorMessage)

          oppiaLogger.e(
            "CreateProfileFragment",
            "Failed to update profile details.",
            result.error
          )
        }

        is AsyncResult.Pending -> {}
      }
    }
  }

  private fun createLearnerProfile(profileName: String, pin: String) {
    profileManagementController
      .addProfile(
        name = profileName,
        pin = pin,
        avatarImagePath = selectedImageUri,
        allowDownloadAccess = true,
        colorRgb = selectUniqueRandomColor(),
        isAdmin = false
      ).toLiveData()
      .observe(activity) { handleAddProfileResult(it, profileName) }
  }

  private fun handleAddProfileResult(result: AsyncResult<Any?>, profileName: String) {
    when (result) {
      is AsyncResult.Success -> {
        fragment.requireContext().showDialog(profileName) {
          fragment.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
        }
      }

      is AsyncResult.Failure -> {
        createProfileViewModel.hasErrorMessage.set(true)

        val errorMessage = when (result.error) {
          is ProfileManagementController.ProfileNameOnlyLettersException ->
            appLanguageResourceHandler.getStringInLocale(
              R.string.add_profile_error_name_only_letters
            )

          is ProfileManagementController.UnknownProfileTypeException ->
            appLanguageResourceHandler.getStringInLocale(
              R.string.add_profile_error_missing_profile_type
            )

          is ProfileManagementController.ProfileNameNotUniqueException ->
            appLanguageResourceHandler.getStringInLocale(
              R.string.add_profile_error_name_not_unique
            )

          else -> {
            appLanguageResourceHandler.getStringInLocale(
              R.string.add_profile_default_error_message
            )
          }
        }

        createProfileViewModel.errorMessage.set(errorMessage)

        oppiaLogger.e(
          "CreateProfileFragment",
          "Failed to create profile.",
          result.error
        )
      }

      is AsyncResult.Pending -> {} // Wait for an actual result.
    }
  }

  private fun Context.showDialog(
    learnerNickname: String,
    onDismiss: () -> Unit
  ) {
    val dialog = Dialog(this)

    val composeView = ComposeView(this).apply {
      ViewTreeLifecycleOwner.set(this, fragment.viewLifecycleOwner)
      ViewTreeViewModelStoreOwner.set(this, fragment)
      ViewTreeSavedStateRegistryOwner.set(this, fragment)

      setContent {
        HandOverNoticeDialog(
          learnerNickname = learnerNickname,
          onDismiss = {
            dialog.dismiss()
            onDismiss()
          }
        )
      }
    }

    dialog.setContentView(composeView)
    dialog.setCancelable(true)
    dialog.show()
  }

  /** Randomly selects a color for the new profile that is not already in use. */
  private fun selectUniqueRandomColor(): Int {
    val usedColors = createProfileViewModel.usedColors.value ?: emptyList()
    return COLORS_LIST
      .map { ContextCompat.getColor(fragment.requireContext(), it) }
      .minus(usedColors)
      .random()
  }

  private companion object {
    private val COLORS_LIST = listOf(
      R.color.component_color_avatar_background_1_color,
      R.color.component_color_avatar_background_2_color,
      R.color.component_color_avatar_background_3_color,
      R.color.component_color_avatar_background_4_color,
      R.color.component_color_avatar_background_5_color,
      R.color.component_color_avatar_background_6_color,
      R.color.component_color_avatar_background_7_color,
      R.color.component_color_avatar_background_8_color,
      R.color.component_color_avatar_background_9_color,
      R.color.component_color_avatar_background_10_color,
      R.color.component_color_avatar_background_11_color,
      R.color.component_color_avatar_background_12_color,
      R.color.component_color_avatar_background_13_color,
      R.color.component_color_avatar_background_14_color,
      R.color.component_color_avatar_background_15_color,
      R.color.component_color_avatar_background_16_color,
      R.color.component_color_avatar_background_17_color,
      R.color.component_color_avatar_background_18_color,
      R.color.component_color_avatar_background_19_color,
      R.color.component_color_avatar_background_20_color,
      R.color.component_color_avatar_background_21_color,
      R.color.component_color_avatar_background_22_color,
      R.color.component_color_avatar_background_23_color,
      R.color.component_color_avatar_background_24_color,
      R.color.component_color_avatar_background_25_color
    )
  }
}
