package org.oppia.app.profile

import android.app.Activity
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
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.StoryTextSize
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

const val GALLERY_INTENT_RESULT_CODE = 1
private val DEFAULT_STORY_TEXT_SIZE = StoryTextSize.SMALL_TEXT_SIZE
private val DEFAULT_APP_LANGUAGE = AppLanguage.ENGLISH_APP_LANGUAGE
private val DEFAULT_AUDIO_LANGUAGE = AudioLanguage.NO_AUDIO

/** The presenter for [AddProfileActivity]. */
@ActivityScope
class AddProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AddProfileViewModel>
) {
  private lateinit var uploadImageView: ImageView
  private val profileViewModel by lazy {
    getAddProfileViewModel()
  }
  private var selectedImage: Uri? = null
  private var allowDownloadAccess = false
  private var inputtedPin = false
  private var createPin = false
  private var inputtedConfirmPin = false

  @ExperimentalCoroutinesApi
  fun handleOnCreate() {
    activity.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AddProfileActivityBinding>(
      activity,
      R.layout.add_profile_activity
    )

    binding.apply {
      viewModel = profileViewModel
    }

    binding.allowDownloadSwitch.setOnCheckedChangeListener { _, isChecked ->
      allowDownloadAccess = isChecked
    }
    binding.checkboxPin.setOnCheckedChangeListener { _, isChecked ->
      profileViewModel.createPin.set(isChecked)
    }

    binding.infoIcon.setOnClickListener {
      showInfoDialog()
    }

    uploadImageView = binding.uploadImageButton

    addTextChangedListeners(binding)
    addButtonListeners(binding)
  }

  fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == GALLERY_INTENT_RESULT_CODE && resultCode == Activity.RESULT_OK) {
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

  private fun addButtonListeners(binding: AddProfileActivityBinding) {
    binding.uploadImageButton.setOnClickListener {
     openGalleryIntent()
    }
    binding.editImageFab.setOnClickListener {
      openGalleryIntent()
    }

    binding.createButton.setOnClickListener {
      profileViewModel.clearAllErrorMessages()

      val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
      imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)

      val name = binding.inputName.getInput()
      val pin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()

      if (checkInputsAreValid(name, pin, confirmPin)) {
        binding.scroll.smoothScrollTo(0, 0)
        return@setOnClickListener
      }

      profileManagementController.addProfile(
        name = name,
        pin = pin,
        avatarImagePath = selectedImage,
        allowDownloadAccess = allowDownloadAccess,
        colorRgb = activity.intent.getIntExtra(KEY_ADD_PROFILE_COLOR_RGB, -10710042),
        isAdmin = false,
        storyTextSize = DEFAULT_STORY_TEXT_SIZE,
        appLanguage = DEFAULT_APP_LANGUAGE,
        audioLanguage = DEFAULT_AUDIO_LANGUAGE
      )
        .observe(activity, Observer {
          handleAddProfileResult(it, binding)
        })
    }
  }

  private fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }

  private fun checkInputsAreValid(name: String, pin: String, confirmPin: String): Boolean {
    var failed = false
    if (name.isEmpty()) {
      profileViewModel.nameErrorMsg.set(activity.resources.getString(R.string.add_profile_error_name_empty))
      failed = true
    }
    if (pin.isNotEmpty() && pin.length < 3) {
      profileViewModel.pinErrorMsg.set(activity.resources.getString(R.string.add_profile_error_pin_length))
      failed = true
    }
    if (pin != confirmPin) {
      profileViewModel.confirmPinErrorMsg.set(activity.resources.getString(R.string.add_profile_error_pin_confirm_wrong))
      failed = true
    }
    return failed
  }

  private fun handleAddProfileResult(
    result: AsyncResult<Any?>,
    binding: AddProfileActivityBinding
  ) {
    if (result.isSuccess()) {
      val intent = Intent(activity, ProfileActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      activity.startActivity(intent)
    } else if (result.isFailure()) {
      when (result.getErrorOrNull()) {
        is ProfileManagementController.ProfileNameNotUniqueException -> profileViewModel.nameErrorMsg.set(
          activity.resources.getString(
            R.string.add_profile_error_name_not_unique
          )
        )
        is ProfileManagementController.ProfileNameOnlyLettersException -> profileViewModel.nameErrorMsg.set(
          activity.resources.getString(
            R.string.add_profile_error_name_only_letters
          )
        )
      }
      binding.scroll.smoothScrollTo(0, 0)
    }
  }

  private fun addTextChangedListeners(binding: AddProfileActivityBinding) {
    fun setValidPin() {
      if (inputtedPin && inputtedConfirmPin) {
        profileViewModel.validPin.set(true)
      } else {
        binding.allowDownloadSwitch.isChecked = false
        profileViewModel.validPin.set(false)
      }
    }

    addTextChangedListener(binding.inputPin) { pin ->
      pin?.let {
        profileViewModel.pinErrorMsg.set("")
        inputtedPin = pin.isNotEmpty()
        setValidPin()
      }
    }

    addTextChangedListener(binding.inputConfirmPin) { confirmPin ->
      confirmPin?.let {
        profileViewModel.confirmPinErrorMsg.set("")
        inputtedConfirmPin = confirmPin.isNotEmpty()
        setValidPin()
      }
    }

    addTextChangedListener(binding.inputName) { name ->
      name?.let {
        profileViewModel.nameErrorMsg.set("")
      }
    }
  }

  private fun addTextChangedListener(
    profileInputView: ProfileInputView,
    onTextChanged: (CharSequence?) -> Unit
  ) {
    profileInputView.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        onTextChanged(p0)
      }

      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
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
