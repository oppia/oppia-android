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

const val GALLERY_INTENT_RESULT_CODE = 1

/** Presenter for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val createProfileViewModel: CreateProfileViewModel
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView
  private var selectedImage: Uri? = null

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
        createProfileViewModel.hasError.set(false)
        val intent = IntroActivity.createIntroActivity(activity, nickname)
        fragment.startActivity(intent)
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
