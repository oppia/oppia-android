package org.oppia.android.app.onboarding.onboardingv2

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.request.target.Target
import org.oppia.android.databinding.CreateProfileFragmentBinding
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.profile.GALLERY_INTENT_RESULT_CODE

/** Presenter for [NewLearnerProfileFragment]. */
@FragmentScope
class NewLearnerProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val createLearnerProfileViewModel: CreateLearnerProfileViewModel
) {
  private lateinit var binding: CreateProfileFragmentBinding
  private lateinit var uploadImageView: ImageView

  /** Initialize layout bindings. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = CreateProfileFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = createLearnerProfileViewModel
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
      if (!binding.createProfileNicknameEdittext.text.isNullOrBlank()) {
        createLearnerProfileViewModel.hasName.set(true)

        val intent = OnboardingLearnerIntroActivity.createOnboardingLearnerIntroActivity(activity)
        fragment.startActivity(intent)
      } else {
        createLearnerProfileViewModel.hasName.set(false)
      }
    }

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }
    binding.createProfileEditPictureIcon.setOnClickListener { openGalleryIntent() }
    binding.createProfilePicturePrompt.setOnClickListener { openGalleryIntent() }
    binding.createProfileUserImageView.setOnClickListener { openGalleryIntent() }

    return binding.root
  }

  private fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }
}