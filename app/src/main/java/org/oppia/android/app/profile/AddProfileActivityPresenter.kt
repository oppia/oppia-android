package org.oppia.android.app.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
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
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AddProfileActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.PlatformParameterValue

const val GALLERY_INTENT_RESULT_CODE = 1

/** The presenter for [AddProfileActivity]. */
@ActivityScope
class AddProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val resourceHandler: AppLanguageResourceHandler,
  private val profileViewModel: AddProfileViewModel,
  @EnableDownloadsSupport private val enableDownloadsSupport: PlatformParameterValue<Boolean>
) {
  private lateinit var uploadImageView: ImageView
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
    if (!enableDownloadsSupport.value) {
      binding.addProfileActivityAllowDownloadConstraintLayout.setOnClickListener {
        allowDownloadAccess = !allowDownloadAccess
        binding.addProfileActivityAllowDownloadSwitch.isChecked = allowDownloadAccess
      }
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
    activity.supportActionBar?.title = resourceHandler.getStringInLocale(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    activity.supportActionBar?.setHomeActionContentDescription(R.string.admin_auth_close)

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

    binding.addProfileActivityUserNameEditText.onTextChanged { name ->
      name?.let {
        if (
          profileViewModel.nameErrorMsg.get()?.isNotEmpty()!! &&
          profileViewModel.inputName.get() == it
        ) {
          profileViewModel.inputName.set(it)
          profileViewModel.isButtonActive.set(it.isNotEmpty())
        } else {
          profileViewModel.isButtonActive.set(it.isNotEmpty())
          profileViewModel.nameErrorMsg.set("")
          profileViewModel.inputName.set(it)
        }
      }
    }

    binding.addProfileActivityPinEditText.onTextChanged { pin ->
      pin?.let {
        if (profileViewModel.pinErrorMsg.get()?.isNotEmpty()!! &&
          profileViewModel.inputPin.get() == it
        ) {
          profileViewModel.inputPin.set(it)
        } else {
          profileViewModel.inputPin.set(it)
          profileViewModel.pinErrorMsg.set("")
          inputtedPin = pin.isNotEmpty()
          setValidPin(binding)
        }
      }
    }

    binding.addProfileActivityConfirmPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        if (
          profileViewModel.confirmPinErrorMsg.get()?.isNotEmpty()!! &&
          profileViewModel.inputConfirmPin.get() == it
        ) {
          profileViewModel.inputConfirmPin.set(it)
        } else {
          profileViewModel.inputConfirmPin.set(it)
          profileViewModel.confirmPinErrorMsg.set("")
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin(binding)
        }
      }
    }

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

      val name = binding.addProfileActivityUserNameEditText.text.toString()
      var pin = ""
      var confirmPin = ""
      if (checkboxStateClicked) {
        pin = binding.addProfileActivityPinEditText.text.toString()
        confirmPin = binding.addProfileActivityConfirmPinEditText.text.toString()
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
          colorRgb = activity.intent.getIntExtra(ADD_PROFILE_COLOR_RGB_EXTRA_KEY, -10710042),
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
        resourceHandler.getStringInLocale(
          R.string.add_profile_error_name_empty
        )
      )
      failed = true
    }
    if (pin.isNotEmpty() && pin.length < 3) {
      profileViewModel.pinErrorMsg.set(
        resourceHandler.getStringInLocale(
          R.string.add_profile_error_pin_length
        )
      )
      failed = true
    }
    if (pin != confirmPin) {
      profileViewModel.confirmPinErrorMsg.set(
        resourceHandler.getStringInLocale(
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
    when (result) {
      is AsyncResult.Success -> {
        val intent = Intent(activity, ProfileChooserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
      }
      is AsyncResult.Failure -> {
        when (result.error) {
          is ProfileManagementController.ProfileNameNotUniqueException ->
            profileViewModel.nameErrorMsg.set(
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_not_unique
              )
            )
          is ProfileManagementController.ProfileNameOnlyLettersException ->
            profileViewModel.nameErrorMsg.set(
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_only_letters
              )
            )
        }
        binding.addProfileActivityScrollView.smoothScrollTo(0, 0)
      }
      is AsyncResult.Pending -> {} // Wait for an actual result.
    }
  }

  private fun showInfoDialog() {
    profileViewModel.showInfoAlertPopup.set(true)
    alertDialog = AlertDialog.Builder(activity as Context, R.style.OppiaAlertDialogTheme)
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
}
