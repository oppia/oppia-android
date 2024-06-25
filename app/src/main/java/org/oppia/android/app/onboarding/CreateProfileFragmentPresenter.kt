package org.oppia.android.app.onboarding

import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.CreateProfileFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageViewTarget
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

private const val GALLERY_INTENT_RESULT_CODE = 1

/** Presenter for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val imageLoader: ImageLoader,
  private val createProfileViewModel: CreateProfileViewModel,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  @EnableDownloadsSupport private val enableDownloadsSupport: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView
  private var selectedImageUri: Uri? = null
  private var allowDownloadAccess = enableDownloadsSupport.value

  /** Initialize layout bindings. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = CreateProfileFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = createProfileViewModel
    }

    uploadImageView = binding.createProfileUserImageView

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
      if (!checkNicknameAndUpdateError(nickname)) {
        createProfile(nickname)
      }
    }

    binding.createProfileNicknameEdittext.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun afterTextChanged(s: Editable?) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        createProfileViewModel.hasErrorMessage.set(false)
      }
    })

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }
    binding.createProfileEditPictureIcon.setOnClickListener { openGalleryIntent() }
    binding.createProfilePicturePrompt.setOnClickListener { openGalleryIntent() }
    binding.createProfileUserImageView.setOnClickListener { openGalleryIntent() }

    return binding.root
  }

  private fun checkNicknameAndUpdateError(nickname: String): Boolean {
    val hasError = nickname.isBlank()
    createProfileViewModel.hasErrorMessage.set(hasError)
    return hasError
  }

  /** Receive the result of image upload and load it into the image view. */
  fun handleOnActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    if (requestCode == GALLERY_INTENT_RESULT_CODE && resultCode == Activity.RESULT_OK) {
      binding.createProfilePicturePrompt.visibility = View.GONE

      intent?.let {
        selectedImageUri = checkNotNull(intent.data) { "Could not find the selected image uri." }

        imageLoader.loadBitmap(
          selectedImageUri.toString(),
          ImageViewTarget(uploadImageView)
        )
      }
    }
  }

  private fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    fragment.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }

  private fun createProfile(nickname: String) {
    val profileColor = activity.intent.getProtoExtra(
      CreateProfileActivity.CREATE_PROFILE_ACTIVITY_PARAMS_KEY,
      CreateProfileActivityParams.getDefaultInstance()
    ).colorRgb

    profileManagementController.addProfile(
      name = nickname,
      pin = "",
      avatarImagePath = selectedImageUri,
      allowDownloadAccess = allowDownloadAccess,
      colorRgb = profileColor,
      isAdmin = true
    ).toLiveData()
      .observe(
        fragment,
        { result ->
          handleAddProfileResult(nickname, result, binding)
        }
      )
  }

  private fun handleAddProfileResult(
    nickname: String,
    result: AsyncResult<Any?>,
    binding: CreateProfileFragmentBinding
  ) {
    when (result) {
      is AsyncResult.Success -> {
        createProfileViewModel.hasErrorMessage.set(false)
        val currentUserProfileId = retrieveNewProfileId()

        val intent =
          IntroActivity.createIntroActivity(activity, nickname, currentUserProfileId.internalId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        fragment.startActivity(intent)
      }
      is AsyncResult.Failure -> {
        when (result.error) {
          is ProfileManagementController.ProfileNameNotUniqueException -> {
            createProfileViewModel.hasErrorMessage.set(true)

            binding.createProfileNicknameError.text =
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_not_unique
              )
          }

          is ProfileManagementController.ProfileNameOnlyLettersException -> {
            createProfileViewModel.hasErrorMessage.set(true)

            binding.createProfileNicknameError.text = resourceHandler.getStringInLocale(
              R.string.add_profile_error_name_only_letters
            )
          }
        }
      }
      is AsyncResult.Pending -> {} // Wait for an actual result.
    }
  }

  private fun retrieveNewProfileId(): ProfileId {
    var profileId: ProfileId = ProfileId.getDefaultInstance()
    profileManagementController.getProfiles().toLiveData().observe(
      fragment,
      { profilesResult ->
        when (profilesResult) {
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "CreateProfileFragmentPresenter",
              "Failed to retrieve the list of profiles",
              profilesResult.error
            )
          }
          is AsyncResult.Pending -> {}
          is AsyncResult.Success -> {
            val sortedProfileList = profilesResult.value.sortedBy { it.id.internalId }
            profileId = sortedProfileList.lastOrNull()?.id ?: ProfileId.getDefaultInstance()
          }
        }
      }
    )
    return profileId
  }
}
