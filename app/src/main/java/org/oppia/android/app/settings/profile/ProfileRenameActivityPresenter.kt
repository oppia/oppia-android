package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileRenameActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ProfileRenameViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private val renameViewModel: ProfileRenameViewModel by lazy {
    getProfileRenameViewModel()
  }

  fun handleOnCreate(profileId: Int) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileRenameActivityBinding>(
        activity,
        R.layout.profile_rename_activity
      )
//    val profileId = activity.intent.getIntExtra(PROFILE_RENAME_PROFILE_ID_EXTRA_KEY, 0)

    binding.apply {
//      viewModel = renameViewModel
      lifecycleOwner = activity
    }

    binding.profileRenameToolbar.setNavigationOnClickListener {
      (activity as ProfileRenameActivity).finish()
    }
    if (getProfileRenameFragment() == null) {
      val profileRenameFragment = ProfileRenameFragment.newInstance(profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_rename_fragment_placeholder, profileRenameFragment).commitNow()
    }
/*
    binding.profileRenameSaveButton.setOnClickListener {
      renameViewModel.nameErrorMsg.set("")
      val imm = activity.getSystemService(
        Context.INPUT_METHOD_SERVICE
      ) as? InputMethodManager
      imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
      val name = binding.profileRenameInputEditText.text.toString()
      if (name.isEmpty()) {
        renameViewModel.nameErrorMsg.set(
          resourceHandler.getStringInLocale(
            R.string.add_profile_error_name_empty
          )
        )
        return@setOnClickListener
      }
      profileManagementController
        .updateName(ProfileId.newBuilder().setInternalId(profileId).build(), name).toLiveData()
        .observe(
          activity,
          Observer {
            handleAddProfileResult(it, profileId)
          }
        )
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.profileRenameInputEditText.onTextChanged { name ->
      name?.let {
        if (
          renameViewModel.nameErrorMsg.get()?.isNotEmpty()!! &&
          renameViewModel.inputName.get() == it
        ) {
          renameViewModel.inputName.set(it)
        } else {
          renameViewModel.nameErrorMsg.set("")
          renameViewModel.inputName.set(it)
        }
      }
    }

    binding.profileRenameInputEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.profileRenameSaveButton.callOnClick()
      }
      false
    }
    */
  }

  /*
    private fun handleAddProfileResult(result: AsyncResult<Any?>, profileId: Int) {
      if (result.isSuccess()) {
        val intent = ProfileEditActivity.createProfileEditActivity(activity, profileId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
      } else if (result.isFailure()) {
        when (result.getErrorOrNull()) {
          is ProfileManagementController.ProfileNameNotUniqueException ->
            renameViewModel.nameErrorMsg.set(
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_not_unique
              )
            )
          is ProfileManagementController.ProfileNameOnlyLettersException ->
            renameViewModel.nameErrorMsg.set(
              resourceHandler.getStringInLocale(
                R.string.add_profile_error_name_only_letters
              )
            )
        }
      }
    }
  */

  private fun getProfileRenameFragment(): ProfileRenameFragment? {
    return activity.supportFragmentManager
      .findFragmentById(
        R.id.profile_rename_fragment_placeholder
      ) as ProfileRenameFragment?
  }

  private fun getProfileRenameViewModel(): ProfileRenameViewModel {
    return viewModelProvider.getForActivity(activity, ProfileRenameViewModel::class.java)
  }
}
