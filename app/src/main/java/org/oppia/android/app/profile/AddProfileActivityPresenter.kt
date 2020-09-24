package org.oppia.app.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AddProfileActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

const val GALLERY_INTENT_RESULT_CODE = 1

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
  private var checkboxStateClicked = false
  private var inputtedConfirmPin = false
  private lateinit var alertDialog: AlertDialog

  fun handleOnCreate() {

    val binding = DataBindingUtil.setContentView<AddProfileActivityBinding>(
      activity,
      R.layout.add_profile_activity
    )

    binding.apply {
      lifecycleOwner = activity
      viewModel = profileViewModel
    }
    binding.addProfileActivityAllowDownloadSwitch.setOnCheckedChangeListener { _, isChecked ->
      allowDownloadAccess = isChecked
    }
    binding.addProfileActivityPinCheckBox.setOnCheckedChangeListener { _, isChecked ->
      profileViewModel.createPin.set(isChecked)
      checkboxStateClicked = isChecked
    }

    binding.addProfileActivityInfoImageView.setOnClickListener {
      showInfoDialog()
    }
    val toolbar = activity.findViewById<View>(R.id.add_profile_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    uploadImageView = binding.addProfileActivityUserImageView
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
            ResourcesCompat.getColor(activity.resources, R.color.avatar_background_11, null),
            PorterDuff.Mode.DST_OVER
          )
          return false
        }
      })
      .into(uploadImageView)

    addButtonListeners(binding)

    binding.addProfileActivityUserNameProfileInputView.post {
      addTextChangedListener(binding.addProfileActivityUserNameProfileInputView) { name ->
        name?.let {
          profileViewModel.isButtonActive.set(it.isNotEmpty())
          profileViewModel.nameErrorMsg.set("")
          profileViewModel.inputName.set(it.toString())
        }
      }
    }
    binding.addProfileActivityPinProfileInputView.post {
      addTextChangedListener(binding.addProfileActivityPinProfileInputView) { pin ->
        pin?.let {
          profileViewModel.inputPin.set(it.toString())
          profileViewModel.pinErrorMsg.set("")
          inputtedPin = pin.isNotEmpty()
          setValidPin(binding)
        }
      }
    }
    binding.addProfileActivityConfirmPinProfileInputView.post {
      addTextChangedListener(binding.addProfileActivityConfirmPinProfileInputView) { confirmPin ->
        confirmPin?.let {
          profileViewModel.inputConfirmPin.set(it.toString())
          profileViewModel.confirmPinErrorMsg.set("")
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin(binding)
        }
      }
    }

    binding.addProfileActivityUserNameProfileInputView.setInput(
      profileViewModel.inputName.get().toString()
    )
    binding.addProfileActivityPinProfileInputView.setInput(
      profileViewModel.inputPin.get().toString()
    )
    binding.addProfileActivityConfirmPinProfileInputView.setInput(
      profileViewModel.inputConfirmPin.get().toString()
    )
    if (profileViewModel.showInfoAlertPopup.get()!!) {
      showInfoDialog()
    }
  }

  private fun setValidPin(binding: AddProfileActivityBinding) {
    if (inputtedPin && inputtedConfirmPin) {
      profileViewModel.validPin.set(true)
    } else {
      binding.addProfileActivityAllowDownloadSwitch.isChecked = false
      profileViewModel.validPin.set(false)
    }
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
    uploadImageView.setOnClickListener {
      openGalleryIntent()
    }
    binding.addProfileActivityEditUserImageView.setOnClickListener {
      openGalleryIntent()
    }

    binding.addProfileActivityCreateButton.setOnClickListener {
      profileViewModel.clearAllErrorMessages()

      val imm = activity.getSystemService(
        Context.INPUT_METHOD_SERVICE
      ) as? InputMethodManager
      imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)

      val name = binding.addProfileActivityUserNameProfileInputView.getInput()
      var pin = ""
      var confirmPin = ""
      if (checkboxStateClicked) {
        pin = binding.addProfileActivityPinProfileInputView.getInput()
        confirmPin = binding.addProfileActivityConfirmPinProfileInputView.getInput()
      }

      if (checkInputsAreValid(name, pin, confirmPin)) {
        binding.addProfileActivityScrollView.smoothScrollTo(0, 0)
        return@setOnClickListener
      }

      profileManagementController
        .addProfile(
          name = name,
          pin = pin,
          avatarImagePath = selectedImage,
          allowDownloadAccess = allowDownloadAccess,
          colorRgb = activity.intent.getIntExtra(KEY_ADD_PROFILE_COLOR_RGB, -10710042),
          isAdmin = false
        ).toLiveData()
        .observe(
          activity,
          Observer {
            handleAddProfileResult(it, binding)
          }
        )
    }
  }

  private fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }

  private fun checkInputsAreValid(name: String, pin: String, confirmPin: String): Boolean {
    var failed = false
    if (name.isEmpty()) {
      profileViewModel.nameErrorMsg.set(
        activity.resources.getString(
          R.string.add_profile_error_name_empty
        )
      )
      failed = true
    }
    if (pin.isNotEmpty() && pin.length < 3) {
      profileViewModel.pinErrorMsg.set(
        activity.resources.getString(
          R.string.add_profile_error_pin_length
        )
      )
      failed = true
    }
    if (pin != confirmPin) {
      profileViewModel.confirmPinErrorMsg.set(
        activity.resources.getString(
          R.string.add_profile_error_pin_confirm_wrong
        )
      )
      failed = true
    }
    return failed
  }

  private fun handleAddProfileResult(
    result: AsyncResult<Any?>,
    binding: AddProfileActivityBinding
  ) {
    if (result.isSuccess()) {
      val intent = Intent(activity, ProfileChooserActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      activity.startActivity(intent)
    } else if (result.isFailure()) {
      when (result.getErrorOrNull()) {
        is ProfileManagementController.ProfileNameNotUniqueException ->
          profileViewModel.nameErrorMsg.set(
            activity.resources.getString(
              R.string.add_profile_error_name_not_unique
            )
          )
        is ProfileManagementController.ProfileNameOnlyLettersException ->
          profileViewModel.nameErrorMsg.set(
            activity.resources.getString(
              R.string.add_profile_error_name_only_letters
            )
          )
      }
      binding.addProfileActivityScrollView.smoothScrollTo(0, 0)
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
    profileViewModel.showInfoAlertPopup.set(true)
    alertDialog = AlertDialog.Builder(activity as Context, R.style.AlertDialogTheme)
      .setMessage(R.string.add_profile_pin_info)
      .setPositiveButton(R.string.add_profile_close) { dialog, _ ->
        profileViewModel.showInfoAlertPopup.set(false)
        dialog.dismiss()
      }
      .setCancelable(false)
      .create()
    alertDialog.show()
  }

  fun dismissAlertDialog() {
    if (::alertDialog.isInitialized && alertDialog.isShowing) {
      alertDialog.dismiss()
    }
  }

  private fun getAddProfileViewModel(): AddProfileViewModel {
    return viewModelProvider.getForActivity(activity, AddProfileViewModel::class.java)
  }
}
