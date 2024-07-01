package org.oppia.android.app.onboarding

import android.content.Intent
import android.graphics.PorterDuff
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.CreateProfileFragmentBinding
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageViewTarget
import javax.inject.Inject

/** Presenter for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val createProfileViewModel: CreateProfileViewModel,
  private val imageLoader: ImageLoader
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView
  private lateinit var selectedImage: String

  /** Launcher for picking an image from device gallery. */
  lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

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

      createProfileViewModel.hasErrorMessage.set(nickname.isBlank())

      if (createProfileViewModel.hasErrorMessage.get() != true) {
        val intent = IntroActivity.createIntroActivity(activity, nickname)
        fragment.startActivity(intent)
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

  /** Receive the result of image upload and load it into the image view. */
  fun handleOnActivityResult(intent: Intent?) {
    intent?.let {
      binding.createProfilePicturePrompt.visibility = View.GONE
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
      activityResultLauncher.launch(
        galleryIntent
      )
    }
    binding.createProfilePicturePrompt.setOnClickListener {
      activityResultLauncher.launch(
        galleryIntent
      )
    }
    binding.createProfileUserImageView.setOnClickListener {
      activityResultLauncher.launch(
        galleryIntent
      )
    }
  }
}
