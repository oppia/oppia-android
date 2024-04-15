package org.oppia.android.app.onboardingv2

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.CreateProfileFragmentBinding
import javax.inject.Inject
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.profile.ADD_PROFILE_COLOR_RGB_EXTRA_KEY
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AddProfileActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.PlatformParameterValue

const val GALLERY_INTENT_RESULT_CODE = 1

/** Presenter for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val createProfileViewModel: CreateProfileViewModel,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  @EnableDownloadsSupport private val enableDownloadsSupport: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView
  private var selectedImage: Uri? = null
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
    Glide.with(activity)
      .load(R.drawable.ic_default_avatar)
      .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
          e: GlideException?,
          model: Any?,
          target: Target<Drawable>?,
          isFirstResource: Boolean
        ): Boolean {
          return false
        }

        override fun onResourceReady(
          resource: Drawable?,
          model: Any?,
          target: Target<Drawable>?,
          dataSource: DataSource?,
          isFirstResource: Boolean
        ): Boolean {
          uploadImageView.setColorFilter(
            ResourcesCompat.getColor(
              activity.resources,
              R.color.component_color_avatar_background_25_color,
              null
            ),
            PorterDuff.Mode.DST_OVER
          )
          return false
        }
      })
      .into(uploadImageView)

    binding.onboardingNavigationContinue.setOnClickListener {
      val nickname = binding.createProfileNicknameEdittext.text.toString().trim()

      if (nickname.isNotBlank()) {
        createProfile(nickname)
      } else {
        createProfileViewModel.hasError.set(true)
      }
    }

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }
    binding.createProfileEditPictureIcon.setOnClickListener { openGalleryIntent() }
    binding.createProfilePicturePrompt.setOnClickListener { openGalleryIntent() }
    binding.createProfileUserImageView.setOnClickListener { openGalleryIntent() }

    return binding.root
  }

  private fun createProfile(nickname: String) {
    profileManagementController.addProfile(
      name = nickname,
      pin = "",
      avatarImagePath = selectedImage,
      allowDownloadAccess = allowDownloadAccess,
      colorRgb = activity.intent.getIntExtra(ADD_PROFILE_COLOR_RGB_EXTRA_KEY, -10710042),
      isAdmin = false
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
        createProfileViewModel.hasError.set(false)

        val currentUserProfileId = retrieveNewProfileId()

        val intent =
          IntroActivity.createIntroActivity(activity, nickname, currentUserProfileId.internalId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        fragment.startActivity(intent)
      }
      is AsyncResult.Failure -> {
        when (result.error) {
          is ProfileManagementController.ProfileNameNotUniqueException -> {
            createProfileViewModel.hasError.set(true)

            binding.createProfileNicknameError.text =
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_not_unique
              )
          }

          is ProfileManagementController.ProfileNameOnlyLettersException -> {
            createProfileViewModel.hasError.set(true)

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
            profileId = sortedProfileList.last().id
          }
        }
      }
    )
    return profileId
  }

  /** Receive the result from selecting an image from the device gallery. **/
  fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == GALLERY_INTENT_RESULT_CODE && resultCode == Activity.RESULT_OK) {
      binding.createProfilePicturePrompt.visibility = View.GONE
      data?.let {
        selectedImage = data.data
        Glide.with(activity)
          .load(selectedImage)
          .centerCrop()
          .apply(RequestOptions.circleCropTransform())
          .into(uploadImageView)
      }
    }
  }

  private fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    fragment.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }
}
