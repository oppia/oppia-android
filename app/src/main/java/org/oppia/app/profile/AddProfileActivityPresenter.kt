package org.oppia.app.profile

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AddProfileActivityBinding
import org.oppia.app.home.HomeActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.logging.Logger
import javax.inject.Inject

const val GALLERY_INTENT_RESULT_CODE = 1

/** The presenter for [AddProfileActivity]. */
@ActivityScope
class AddProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AddProfileViewModel>
  ) {
  var allowDownloadAccess = false
  var selectedImage: Uri? = null
  lateinit var uploadImageView: ImageView

  @ExperimentalCoroutinesApi
  fun handleOnCreate() {
    activity.title = "Add Profile"
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AddProfileActivityBinding>(activity, R.layout.add_profile_activity)
    val addViewModel = getAddProfileViewModel()

    binding.apply {
      viewModel = addViewModel
    }

    binding.allowDownloadSwitch.setOnCheckedChangeListener { _, isChecked ->
      allowDownloadAccess = isChecked
    }

    binding.confirmPinInput.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          if (it.length == 3 && it.toString() == binding.pinInput.text.toString()) {
            addViewModel.validPin.set(true)
          } else {
            binding.allowDownloadSwitch.isChecked = false
            addViewModel.validPin.set(false)
          }
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.uploadImageButton.setOnClickListener {
      val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
    }
    uploadImageView = binding.uploadImageButton

    binding.createButton.setOnClickListener {
      val name = binding.inputName.text.toString()
      val pin = binding.pinInput.text.toString()
      val confirmPin = binding.confirmPinInput.text.toString()
//      if (pin.length < 3) {
//        Toast.makeText(activity, activity.resources.getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show()
//        return@setOnClickListener
//      }
      if (pin != confirmPin) {
        Toast.makeText(activity, activity.resources.getString(R.string.confirm_pin_wrong), Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      profileManagementController.addProfile(name, pin, selectedImage, allowDownloadAccess, isAdmin = false).observe(activity, Observer {
        if (it.isSuccess()) {
          val intent = Intent(activity, ProfileActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
          activity.startActivity(intent)
        } else if (it.isFailure()) {
          val error = it.getErrorOrNull()
          if (error is ProfileManagementController.ProfileNameNotUniqueException) {
            Toast.makeText(
              activity,
              activity.resources.getString(R.string.name_not_unique),
              Toast.LENGTH_SHORT
            ).show()
          } else if (error is ProfileManagementController.FailedToStoreImageException) {
            Toast.makeText(
              activity,
              activity.resources.getString(R.string.failed_image_store),
              Toast.LENGTH_SHORT
            ).show()
          } else {
            Toast.makeText(
              activity,
              activity.resources.getString(R.string.name_only_letters),
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      })
    }
  }

  fun handleOnActivityResult(data: Intent?) {
    data?.let {
      selectedImage = data.data
      Glide.with(activity)
        .load(selectedImage)
        .centerCrop()
        .apply(RequestOptions.circleCropTransform())
        .into(uploadImageView)
    }
  }


  private fun getAddProfileViewModel(): AddProfileViewModel {
    return viewModelProvider.getForActivity(activity, AddProfileViewModel::class.java)
  }
}
