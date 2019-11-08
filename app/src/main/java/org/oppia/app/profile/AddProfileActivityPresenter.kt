package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AddProfileActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

const val GALLERY_INTENT_RESULT_CODE = 1

/** The presenter for [AddProfileActivity]. */
@ActivityScope
class AddProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AddProfileViewModel>
) {
  lateinit var uploadImageView: ImageView
  private val addViewModel by lazy {
    getAddProfileViewModel()
  }
  private var selectedImage: Uri? = null
  var allowDownloadAccess = false
  var inputtedPin = false
  var inputtedConfirmPin = false

  @ExperimentalCoroutinesApi
  fun handleOnCreate() {
    activity.title = "Add Profile"
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AddProfileActivityBinding>(activity, R.layout.add_profile_activity)

    binding.apply {
      viewModel = addViewModel
    }

    binding.allowDownloadSwitch.setOnCheckedChangeListener { _, isChecked ->
      allowDownloadAccess = isChecked
    }

    binding.infoIcon.setOnClickListener {
      showInfoDialog()
    }

    addTextChangeListeners(binding)

    binding.uploadImageButton.setOnClickListener {
      val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
    }
    uploadImageView = binding.uploadImageButton

    binding.createButton.setOnClickListener {
      addViewModel.clearAllErrorMessages()
      val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
      imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
      val name = binding.inputName.getInput()
      val pin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()
      var failed = false
      if (name.isEmpty()) {
        addViewModel.nameErrorMsg.set(activity.resources.getString(R.string.add_profile_error_name_empty))
        failed = true
      }
      if (pin.isNotEmpty() && pin.length < 3) {
        addViewModel.pinErrorMsg.set(activity.resources.getString(R.string.add_profile_error_pin_length))
        failed = true
      }
      if (pin != confirmPin) {
        addViewModel.confirmPinErrorMsg.set(activity.resources.getString(R.string.add_profile_error_pin_confirm_wrong))
        failed = true
      }
      if (failed)  {
        binding.scroll.smoothScrollTo(0,0)
        return@setOnClickListener
      }
      profileManagementController.addProfile(name, pin, selectedImage, allowDownloadAccess, isAdmin = false).observe(activity, Observer {
        if (it.isSuccess()) {
          val intent = Intent(activity, ProfileActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
          activity.startActivity(intent)
        } else if (it.isFailure()) {
          when (it.getErrorOrNull()) {
            is ProfileManagementController.ProfileNameNotUniqueException -> addViewModel.nameErrorMsg.set(activity.resources.getString(R.string.add_profile_error_name_not_unique))
            is ProfileManagementController.ProfileNameOnlyLettersException -> addViewModel.nameErrorMsg.set(activity.resources.getString(R.string.add_profile_error_name_only_letters))
          }
          binding.scroll.smoothScrollTo(0,0)
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

  private fun addTextChangeListeners(binding: AddProfileActivityBinding) {
    fun setValidPin() {
      if (inputtedPin && inputtedConfirmPin) {
        addViewModel.validPin.set(true)
      } else {
        binding.allowDownloadSwitch.isChecked = false
        addViewModel.validPin.set(false)
      }
    }

    binding.inputPin.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(pin: CharSequence?, start: Int, before: Int, count: Int) {
        pin?.let {
          addViewModel.pinErrorMsg.set("")
          inputtedPin = it.isNotEmpty()
          setValidPin()
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.inputConfirmPin.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          addViewModel.confirmPinErrorMsg.set("")
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin()
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.inputName.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          addViewModel.nameErrorMsg.set("")
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })
  }

  private fun showInfoDialog() {
    AlertDialog.Builder(activity as Context, R.style.AlertDialogTheme)
      .setMessage(R.string.add_profile_pin_info)
      .setPositiveButton(R.string.add_profile_close) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun getAddProfileViewModel(): AddProfileViewModel {
    return viewModelProvider.getForActivity(activity, AddProfileViewModel::class.java)
  }
}
